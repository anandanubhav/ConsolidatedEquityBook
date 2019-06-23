package main.com.aa;

import main.com.aa.feed.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class ConsolidatedEquityBook {
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
            BookLevel[] aOffers = pbqOffers.toArray(BookLevel[]::new);
            List<BookLevel> offers = Arrays.asList(aOffers);
            Collections.sort(offers);

            for (int i = 0; i < 5; i++) {
                double bidPrice = -1, offerPrice = -1;
                long bidSize = -1, offerSize = -1;
                if(bids.size() > i){
                    bidPrice = bids.get(i).getPrice();
                    bidSize = bids.get(i).getSize();
                }
                if(offers.size() > i){
                    offerPrice = offers.get(i).getPrice();
                    offerSize = offers.get(i).getSize();
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
                    Book cancelledBook = orderBookMap.remove(currentBook.getOrderId());
                    currentBook = new Book.Builder().fromBook(cancelledBook).orderType(currentBook.getOrderType()).oldSize(cancelledBook.getBidSize()).build();
                    break;
                case MODIFY:
                    Book oldBook = orderBookMap.get(currentBook.getOrderId());
                    switch (oldBook.getSide()) {
                        case BUY:
                            currentBook= new Book.Builder().fromBook(oldBook).orderType(currentBook.getOrderType()).oldSize(oldBook.getBidSize()).bidSize(currentBook.getBidSize()).build();
                            break;
                        case SELL:
                            currentBook = new Book.Builder().fromBook(oldBook).orderType(currentBook.getOrderType()).oldSize(oldBook.getOfferSize()).offerSize(currentBook.getBidSize()).build();
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
                        updateSizeForModifyOrder(book);
                        break;
                    case CANCEL:
                        //go and remove quantity and bids
                        updateSizeForCancelOrder(book);
                        break;
                }
                break;
        }
    }

    private void updateSizeForCancelOrder(Book book) {
        TradeType tradeType = book.getSide() == Side.BUY ? TradeType.BID : TradeType.OFFER;
        PriorityBlockingQueue<BookLevel> queue = consolidatedBookMap.get(book.getSymbol()).get(tradeType);
        BookLevel bookLevel = getOrderBookLevel(book);
        Iterator<BookLevel> itr = queue.iterator();
        while (itr.hasNext()) {
            BookLevel current = itr.next();
            if (current.getPrice() == bookLevel.getPrice() && current.getSize() == bookLevel.getSize()) {
                queue.remove(bookLevel);
                break;
            } else if(current.getPrice() == bookLevel.getPrice() ){
                current.setSize(current.getSize() - bookLevel.getSize());
                break;
            }
        }
    }

    private void updateSizeForModifyOrder(Book book) {
        TradeType tradeType = book.getSide() == Side.BUY ? TradeType.BID : TradeType.OFFER;
        PriorityBlockingQueue<BookLevel> queue = consolidatedBookMap.get(book.getSymbol()).get(tradeType);
        BookLevel bookLevel = getOrderBookLevel(book);
        Iterator<BookLevel> itr = queue.iterator();
        while (itr.hasNext()) {
            BookLevel current = itr.next();
            if (current.getPrice() == bookLevel.getPrice()) {
                current.setSize(current.getSize() + bookLevel.getSize() - book.getOldSize());
                break;
            }
        }
    }

    private void reorderExistingLevelsForNewOrder(Book book) {
        TradeType tradeType = book.getSide() == Side.BUY ? TradeType.BID : TradeType.OFFER;
        BookLevel bookLevel = getOrderBookLevel(book);
        PriorityBlockingQueue<BookLevel> queue = consolidatedBookMap.get(book.getSymbol()).get(tradeType);
        if(queue == null){
            queue = new PriorityBlockingQueue<>(5, book.getSide() == Side.BUY ? new BookLevelAscendingComparator() : new BookLevelDescendingComparator());
            consolidatedBookMap.get(book.getSymbol()).put(tradeType,queue);
            reorderLevel(book,tradeType,queue,bookLevel);
        } else {
            reorderLevel(book, tradeType, queue, bookLevel);
        }
    }

    private void reorderExistingLevelsForTop(Book book) {
        BookLevel bid = new BookLevel(book.getBidPrice(), book.getBidSize());
        BookLevel offer = new BookLevel(book.getOfferPrice(), book.getOfferSize());
        PriorityBlockingQueue<BookLevel> bidQueue = consolidatedBookMap.get(book.getSymbol()).get(TradeType.BID);
        PriorityBlockingQueue<BookLevel> offerQueue = consolidatedBookMap.get(book.getSymbol()).get(TradeType.OFFER);
        reorderLevel(book, TradeType.BID, bidQueue, bid);
        reorderLevel(book, TradeType.OFFER, offerQueue, offer);
    }


    private void reorderLevel(Book book, TradeType tradeType, PriorityBlockingQueue<BookLevel> queue, BookLevel bookLevel) {
        if (queue.contains(bookLevel)) {
            updateSizeForExistingBook(book, bookLevel, tradeType);
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

    private void updateSizeForExistingBook(Book book, BookLevel bookLevel, TradeType tradeType) {
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
        PriorityBlockingQueue<BookLevel> bidQueue = new PriorityBlockingQueue<>(5,new BookLevelAscendingComparator());
        PriorityBlockingQueue<BookLevel> offerQueue = new PriorityBlockingQueue<>(5,new BookLevelDescendingComparator());
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
        PriorityBlockingQueue<BookLevel> queue = new PriorityBlockingQueue<>(5, book.getSide() == Side.BUY ? new BookLevelAscendingComparator() : new BookLevelDescendingComparator());
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
