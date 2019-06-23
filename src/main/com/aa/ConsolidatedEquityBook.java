package main.com.aa;

import main.com.aa.feed.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class ConsolidatedEquityBook {

    Logger log = Logger.getLogger("ConsolidatedEquityBook");

    private final ExecutorService es = Executors.newFixedThreadPool(2);

    private final LinkedBlockingQueue<Book> topBookQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Book> orderBookQueue = new LinkedBlockingQueue<>();

    private final Map<String, Book> orderBookMap = new ConcurrentHashMap<>();
    private final Map<String, Map<TradeType, PriorityBlockingQueue<BookLevel>>> consolidatedBookMap = new ConcurrentHashMap<>();

    public void receiveTopBook(Book book){
        topBookQueue.add(book);
        processTopFeed();
    }

    public void receiveOrderBook(Book book){
        orderBookQueue.add(book);
        processOrderFeed();
    }

    public List<ConsolidatedBookLevel> getTopFiveLevelsBySymbol(String symbol){
        List<ConsolidatedBookLevel> topFive = null;

        Map<TradeType, PriorityBlockingQueue<BookLevel>> symbolMap =  consolidatedBookMap.get(symbol);
        if(symbolMap != null){
            topFive = new ArrayList<>();

            PriorityBlockingQueue<BookLevel> pbqBids = symbolMap.get(TradeType.BID);
            BookLevel[] aBids = pbqBids.toArray(BookLevel[]::new);
            List<BookLevel> bids = Arrays.asList(aBids);
            Collections.sort(bids);
            Collections.reverse(bids);
            PriorityBlockingQueue<BookLevel> pbqOffers = symbolMap.get(TradeType.OFFER);
            BookLevel[] offers = pbqOffers.toArray(BookLevel[]::new);

            for (int i = 0; i < 5; i++) {
                double bidPrice = -1, offerPrice = -1;
                long bidSize = -1, offerSize = -1;
                if(bids.size() > i){
                    bidPrice = bids.get(i).getPrice();
                    bidSize = bids.get(i).getSize();
                }
                if(offers.length > i){
                    offerPrice = offers[i].getPrice();
                    offerSize = offers[i].getSize();
                }

                ConsolidatedBookLevel cb = new ConsolidatedBookLevel(bidPrice,offerPrice,bidSize,offerSize);
                topFive.add(cb);
            }
        }

        return topFive;
    }


    private void processTopFeed(){
        Book book = topBookQueue.poll();
        if(book != null) {
            consolidate(book);
        }
    }

    private void processOrderFeed(){
        Book currentBook = orderBookQueue.poll();
        if(currentBook != null) {
            switch (currentBook.getOrderType()) {
                case NEW:
                    orderBookMap.putIfAbsent(currentBook.getOrderId(), currentBook);
                    break;
                case CANCEL:
                    currentBook = orderBookMap.remove(currentBook.getOrderId());
                    break;
                case MODIFY:
                    Book oldBook = orderBookMap.get(currentBook.getOrderId());
                    switch (oldBook.getSide()) {
                        case BUY:
                            Book updatedBidBook = new Book.Builder().fromBook(currentBook).oldSize(oldBook.getBidSize()).bidSize(currentBook.getBidSize()).build();
                            currentBook = updatedBidBook;
                            break;
                        case SELL:
                            Book updatedOfferBook = new Book.Builder().fromBook(currentBook).oldSize(oldBook.getOfferSize()).offerSize(currentBook.getBidSize()).build();
                            currentBook = updatedOfferBook;
                            break;
                    }
                    break;
            }

            consolidate(currentBook);
        }
    }

    private synchronized void consolidate(Book book){
        switch (book.getFeedType()){
            case TOP_OF_THE_BOOK:
                if(consolidatedBookMap.get(book.getSymbol()) == null){
                    firstTimeTopConsolidator(book);
                } else {
                    reorderExistingLevelsForTop(book);
                }
                break;
            case ORDER_BOOK:
                switch (book.getOrderType()){
                    case NEW:
                        if(consolidatedBookMap.get(book.getSymbol()) != null){
                            reorderExistingLevelsForNewOrder(book);
                        } else {
                            firstTimeOrderConsolidator(book);
                        }
                        break;
                    case MODIFY:
                        //go and amend quantity
                        break;
                    case CANCEL:
                        //go and remove quantity and bids
                        break;
                }
                break;
        }
    }

    private void reorderExistingLevelsForNewOrder(Book book) {
        TradeType tradeType = book.getSide() == Side.BUY ? TradeType.BID : TradeType.OFFER;
        PriorityBlockingQueue<BookLevel> queue = consolidatedBookMap.get(book.getSymbol()).get(tradeType);
        BookLevel bookLevel = getOrderBookLevel(book);
        reorderLevel(book, tradeType, queue, bookLevel);
    }

    private void reorderExistingLevelsForTop(Book book) {
        /*
         * if level bid price or offer price match - then updated sizes
         * else put and remove any extras
         */

        BookLevel bid = new BookLevel(book.getBidPrice(), book.getBidSize());
        BookLevel offer = new BookLevel(book.getOfferPrice(), book.getOfferSize());
        PriorityBlockingQueue<BookLevel> bidQueue = consolidatedBookMap.get(book.getSymbol()).get(TradeType.BID);
        PriorityBlockingQueue<BookLevel> offerQueue = consolidatedBookMap.get(book.getSymbol()).get(TradeType.OFFER);
        reorderLevel(book, TradeType.BID, bidQueue, bid);
        reorderLevel(book, TradeType.OFFER, offerQueue, offer);
    }


    private void reorderLevel(Book book, TradeType tradeType, PriorityBlockingQueue<BookLevel> queue, BookLevel bookLevel) {
        if (queue.contains(bookLevel)) {
            updateSizeForExisitingBook(book, bookLevel, tradeType);
        } else {
            addAndManageSize(bookLevel, queue);
        }
    }

    private void addAndManageSize(BookLevel level, PriorityBlockingQueue<BookLevel> queue) {
        queue.offer(level);
        while (queue.size() > 5) {
            queue.poll();
        }
    }

    private void updateSizeForExisitingBook(Book book, BookLevel bookLevel, TradeType tradeType) {
        Iterator<BookLevel> itr = consolidatedBookMap.get(book.getSymbol()).get(tradeType).iterator();
        while (itr.hasNext()) {
            BookLevel current = itr.next();
            if (current.getPrice() == bookLevel.getPrice()) {
                current.setSize(current.getSize() + bookLevel.getSize());
                break;
            }
        }
    }

    private void firstTimeTopConsolidator(Book book) {
        PriorityBlockingQueue<BookLevel> bidQueue = new PriorityBlockingQueue<>(5,new BookLevelComparator());
        PriorityBlockingQueue<BookLevel> offerQueue = new PriorityBlockingQueue<>(5,new BookLevelComparator());
        BookLevel blBid = new BookLevel(book.getBidPrice(),book.getBidSize());
        BookLevel blOffer = new BookLevel(book.getOfferPrice(),book.getOfferSize());

        bidQueue.offer(blBid);
        offerQueue.offer(blOffer);
        Map<TradeType, PriorityBlockingQueue<BookLevel>> bookLevels = new ConcurrentHashMap<>();
        bookLevels.put(TradeType.BID, bidQueue);
        bookLevels.put(TradeType.OFFER, offerQueue);
        consolidatedBookMap.putIfAbsent(book.getSymbol(),bookLevels);
    }

    private void firstTimeOrderConsolidator(Book book) {
        PriorityBlockingQueue<BookLevel> queue = new PriorityBlockingQueue<>(5,new BookLevelComparator());
        BookLevel bl = getOrderBookLevel(book);
        queue.offer(bl);
        Map<TradeType, PriorityBlockingQueue<BookLevel>> bookLevels = new ConcurrentHashMap<>();
        if(book.getSide() == Side.BUY){
            bookLevels.put(TradeType.BID, queue);
        } else {
            bookLevels.put(TradeType.OFFER, queue);
        }
        consolidatedBookMap.putIfAbsent(book.getSymbol(), bookLevels);
    }

    private BookLevel getOrderBookLevel(Book book) {
        BookLevel bl;
        if(book.getSide() == Side.BUY) {
            bl = new BookLevel(book.getBidPrice(), book.getBidSize());
        } else {
            bl = new BookLevel(book.getOfferPrice(),book.getOfferSize());
        }
        return bl;
    }
}
