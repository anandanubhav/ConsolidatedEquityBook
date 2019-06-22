package com.aa;

import com.aa.feed.Book;
import com.aa.feed.BookLevel;
import com.aa.feed.LevelComparator;
import com.aa.feed.Side;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConsolidatedEquityBook {

    Logger log = Logger.getLogger("ConsolidatedEquityBook");

    private final ExecutorService es = Executors.newFixedThreadPool(2);

    private final LinkedBlockingQueue<Book> topBookQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Book> orderBookQueue = new LinkedBlockingQueue<>();

    private final Map<String, Book> orderBookMap = new ConcurrentHashMap<>();
    private final Map<String, PriorityBlockingQueue<BookLevel>> consolidatedBookMap = new ConcurrentHashMap<>();

    public void initiateFeeds() {
        processTopFeed();
        processOrderFeed();
    }

    public void receiveTopBook(Book book){
        topBookQueue.add(book);
    }

    public void receiveOrderBook(Book book){
        orderBookQueue.add(book);
    }

    public List<BookLevel> getTopFiveBySymbol(String symbol){
        List<BookLevel> topFive = null;
        PriorityBlockingQueue<BookLevel> queue = consolidatedBookMap.get(symbol);
        if(queue != null && !queue.isEmpty()){
            topFive = new ArrayList<>();
            queue.drainTo(topFive,5);
        }
        return topFive;
    }


    private void processTopFeed(){
        Runnable runnable = () -> {
            while(true){
                try {
                    Book book = topBookQueue.take();
                    consolidate(book);
                } catch (InterruptedException e) {
                    log.log(Level.SEVERE, e.getMessage());
                }
            }
        };
        es.submit(runnable);
    }

    private void processOrderFeed(){
        Runnable runnable = () -> {
            while(true){
                try {
                    Book currentBook = orderBookQueue.take();
                    switch (currentBook.getOrderType()){
                       case NEW:
                            orderBookMap.putIfAbsent(currentBook.getOrderId(), currentBook);
                            break;
                        case CANCEL:
                            orderBookMap.remove(currentBook.getOrderId());
                            break;
                        case MODIFY:
                            Book oldBook = orderBookMap.get(currentBook.getOrderId());
                            switch (oldBook.getSide()){
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
                } catch (InterruptedException e) {
                    log.log(Level.SEVERE, e.getMessage());
                }
            }
        };

        es.submit(runnable);
    }

    private synchronized void consolidate(Book book){
        PriorityBlockingQueue<BookLevel> currentQueue = consolidatedBookMap.get(book.getSymbol());
        switch (book.getFeedType()){
            case TOP_OF_THE_BOOK:
                if(currentQueue != null){

                } else {
                    firstTimeTopConsolidator(book);
                }
                break;
            case ORDER_BOOK:
                if(currentQueue != null){

                } else {
                    firstTimeOrderConsolidator(book);
                }
                break;
        }
    }

    private void firstTimeTopConsolidator(Book book) {
        PriorityBlockingQueue<BookLevel> queue = new PriorityBlockingQueue<>(5,new LevelComparator());
        BookLevel bl = new BookLevel(book.getBidPrice(),book.getOfferPrice(),book.getBidSize(),book.getOfferSize());
        queue.offer(bl);
        consolidatedBookMap.putIfAbsent(book.getSymbol(),queue);
    }

    private void firstTimeOrderConsolidator(Book book) {
        PriorityBlockingQueue<BookLevel> queue = new PriorityBlockingQueue<>(5,new LevelComparator());
        BookLevel bl = null;
        if(book.getSide() == Side.BUY) {
            bl = new BookLevel(book.getBidPrice(), -1, book.getBidSize(), -1);
        } else {
            bl = new BookLevel(-1,book.getOfferPrice(),-1,book.getOfferSize());
        }
        queue.offer(bl);
        consolidatedBookMap.putIfAbsent(book.getSymbol(),queue);
    }
}
