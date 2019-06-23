package main.test;

import main.com.aa.ConsolidatedEquityBook;
import main.com.aa.feed.Book;
import main.com.aa.feed.FeedType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConsolidatedEquityBookTest {

    @Test
    public void testClassExists(){
        ConsolidatedEquityBook ceb = new ConsolidatedEquityBook();
        Assertions.assertNotNull(ceb);
    }

    @Test
    public void testTopBooksOnly(){
        ConsolidatedEquityBook ceb = new ConsolidatedEquityBook();
        //ceb.initiateFeeds();

        Book tb1 = new Book.Builder().feedType(FeedType.TOP_OF_THE_BOOK).symbol("JPM").bidPrice(245).bidSize(1200).offerPrice(219).offerSize(1200).build();
        Book tb2 = new Book.Builder().feedType(FeedType.TOP_OF_THE_BOOK).symbol("JPM").bidPrice(230).bidSize(1200).offerPrice(204).offerSize(1200).build();
        Book tb3 = new Book.Builder().feedType(FeedType.TOP_OF_THE_BOOK).symbol("JPM").bidPrice(200).bidSize(1200).offerPrice(229).offerSize(1200).build();
        Book tb4 = new Book.Builder().feedType(FeedType.TOP_OF_THE_BOOK).symbol("JPM").bidPrice(216).bidSize(1200).offerPrice(221).offerSize(1200).build();
        Book tb5 = new Book.Builder().feedType(FeedType.TOP_OF_THE_BOOK).symbol("JPM").bidPrice(207).bidSize(1200).offerPrice(239).offerSize(1200).build();

        ceb.receiveTopBook(tb1);
        ceb.receiveTopBook(tb2);
        ceb.receiveTopBook(tb3);
        ceb.receiveTopBook(tb4);
        ceb.receiveTopBook(tb5);

        System.out.println(ceb.getTopFiveLevelsBySymbol("JPM"));
    }

}
