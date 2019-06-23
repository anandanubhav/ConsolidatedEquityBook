package main.test;

import main.com.aa.ConsolidatedEquityBook;
import main.com.aa.feed.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ConsolidatedEquityBookTest {

    @Test
    public void testClassExists(){
        ConsolidatedEquityBook ceb = new ConsolidatedEquityBook();
        Assertions.assertNotNull(ceb);
    }

    @Test
    public void testTopBooksOnly(){
        ConsolidatedEquityBook ceb = new ConsolidatedEquityBook();

        Book tb1 = new Book.Builder().feedType(FeedType.TOP_OF_THE_BOOK).symbol("JPM").bidPrice(245).bidSize(1200).offerPrice(219).offerSize(1300).build();
        Book tb2 = new Book.Builder().feedType(FeedType.TOP_OF_THE_BOOK).symbol("JPM").bidPrice(230).bidSize(1000).offerPrice(204).offerSize(1600).build();
        Book tb3 = new Book.Builder().feedType(FeedType.TOP_OF_THE_BOOK).symbol("JPM").bidPrice(200).bidSize(1800).offerPrice(229).offerSize(1400).build();
        Book tb4 = new Book.Builder().feedType(FeedType.TOP_OF_THE_BOOK).symbol("JPM").bidPrice(216).bidSize(1900).offerPrice(221).offerSize(2800).build();
        Book tb5 = new Book.Builder().feedType(FeedType.TOP_OF_THE_BOOK).symbol("JPM").bidPrice(207).bidSize(2200).offerPrice(239).offerSize(2400).build();
        Book tb6 = new Book.Builder().feedType(FeedType.TOP_OF_THE_BOOK).symbol("JPM").bidPrice(267).bidSize(1100).offerPrice(249).offerSize(1200).build();

        ceb.receiveTopBook(tb1);
        ceb.receiveTopBook(tb2);
        ceb.receiveTopBook(tb3);
        ceb.receiveTopBook(tb4);
        ceb.receiveTopBook(tb5);
        ceb.receiveTopBook(tb6);

        List<ConsolidatedBookLevel> topFive = ceb.getTopFiveLevelsBySymbol("JPM");
        System.out.println(topFive);

        Assertions.assertEquals(267,topFive.get(0).getBidPrice());
        Assertions.assertEquals(204,topFive.get(0).getOfferPrice());
        Assertions.assertEquals(207,topFive.get(4).getBidPrice());
        Assertions.assertEquals(239,topFive.get(4).getOfferPrice());
    }

    @Test
    public void testOrderBooksOnly(){
        ConsolidatedEquityBook ceb = new ConsolidatedEquityBook();

        Book obBid1 = new Book.Builder().feedType(FeedType.ORDER_BOOK).orderType(OrderType.NEW).orderId("obb1").symbol("JPM").side(Side.BUY).bidPrice(245).bidSize(1200).build();
        Book obBid2 = new Book.Builder().feedType(FeedType.ORDER_BOOK).orderType(OrderType.NEW).orderId("obb2").symbol("JPM").side(Side.BUY).bidPrice(230).bidSize(1000).build();
        Book obBid3 = new Book.Builder().feedType(FeedType.ORDER_BOOK).orderType(OrderType.NEW).orderId("obb3").symbol("JPM").side(Side.BUY).bidPrice(200).bidSize(1800).build();
        Book obBid4 = new Book.Builder().feedType(FeedType.ORDER_BOOK).orderType(OrderType.NEW).orderId("obb4").symbol("JPM").side(Side.BUY).bidPrice(216).bidSize(1900).build();
        Book obBid5 = new Book.Builder().feedType(FeedType.ORDER_BOOK).orderType(OrderType.NEW).orderId("obb5").symbol("JPM").side(Side.BUY).bidPrice(207).bidSize(2200).build();

        Book obOffer1 = new Book.Builder().feedType(FeedType.ORDER_BOOK).orderType(OrderType.NEW).symbol("JPM").orderId("obbf1").side(Side.SELL).offerPrice(219).offerSize(1300).build();
        Book obOffer2 = new Book.Builder().feedType(FeedType.ORDER_BOOK).orderType(OrderType.NEW).symbol("JPM").orderId("obbf2").side(Side.SELL).offerPrice(204).offerSize(1600).build();
        Book obOffer3 = new Book.Builder().feedType(FeedType.ORDER_BOOK).orderType(OrderType.NEW).symbol("JPM").orderId("obbf3").side(Side.SELL).offerPrice(229).offerSize(1400).build();
        Book obOffer4 = new Book.Builder().feedType(FeedType.ORDER_BOOK).orderType(OrderType.NEW).symbol("JPM").orderId("obbf4").side(Side.SELL).offerPrice(221).offerSize(2800).build();
        Book obOffer5 = new Book.Builder().feedType(FeedType.ORDER_BOOK).orderType(OrderType.NEW).symbol("JPM").orderId("obbf5").side(Side.SELL).offerPrice(239).offerSize(2400).build();



        ceb.receiveOrderBook(obBid1);
        ceb.receiveOrderBook(obBid2);
        ceb.receiveOrderBook(obBid3);
        ceb.receiveOrderBook(obBid4);
        ceb.receiveOrderBook(obBid5);

        ceb.receiveOrderBook(obOffer1);
        ceb.receiveOrderBook(obOffer2);
        ceb.receiveOrderBook(obOffer3);
        ceb.receiveOrderBook(obOffer4);
        ceb.receiveOrderBook(obOffer5);

        List<ConsolidatedBookLevel> topFive = ceb.getTopFiveLevelsBySymbol("JPM");
        System.out.println(topFive);

        Assertions.assertEquals(245,topFive.get(0).getBidPrice());
        Assertions.assertEquals(204,topFive.get(0).getOfferPrice());
        Assertions.assertEquals(200,topFive.get(4).getBidPrice());
        Assertions.assertEquals(239,topFive.get(4).getOfferPrice());
    }

    @Test
    public void testOrderBooksWithNewAndAmendAndCancels(){
        ConsolidatedEquityBook ceb = new ConsolidatedEquityBook();

        Book obBid1 = new Book.Builder().feedType(FeedType.ORDER_BOOK).orderType(OrderType.NEW).orderId("obb1").symbol("JPM").side(Side.BUY).bidPrice(245).bidSize(1200).build();
        Book obBid2 = new Book.Builder().feedType(FeedType.ORDER_BOOK).orderType(OrderType.NEW).orderId("obb2").symbol("JPM").side(Side.BUY).bidPrice(230).bidSize(1000).build();
        Book obBid3 = new Book.Builder().feedType(FeedType.ORDER_BOOK).orderType(OrderType.NEW).orderId("obb3").symbol("JPM").side(Side.BUY).bidPrice(200).bidSize(1800).build();
        Book obBid4 = new Book.Builder().feedType(FeedType.ORDER_BOOK).orderType(OrderType.NEW).orderId("obb4").symbol("JPM").side(Side.BUY).bidPrice(216).bidSize(1900).build();
        Book obBid5 = new Book.Builder().feedType(FeedType.ORDER_BOOK).orderType(OrderType.NEW).orderId("obb5").symbol("JPM").side(Side.BUY).bidPrice(207).bidSize(2200).build();

        Book obOffer1 = new Book.Builder().feedType(FeedType.ORDER_BOOK).orderType(OrderType.NEW).symbol("JPM").orderId("obbf1").side(Side.SELL).offerPrice(219).offerSize(1300).build();
        Book obOffer2 = new Book.Builder().feedType(FeedType.ORDER_BOOK).orderType(OrderType.NEW).symbol("JPM").orderId("obbf2").side(Side.SELL).offerPrice(204).offerSize(1600).build();
        Book obOffer3 = new Book.Builder().feedType(FeedType.ORDER_BOOK).orderType(OrderType.NEW).symbol("JPM").orderId("obbf3").side(Side.SELL).offerPrice(229).offerSize(1400).build();
        Book obOffer4 = new Book.Builder().feedType(FeedType.ORDER_BOOK).orderType(OrderType.NEW).symbol("JPM").orderId("obbf4").side(Side.SELL).offerPrice(221).offerSize(2800).build();
        Book obOffer5 = new Book.Builder().feedType(FeedType.ORDER_BOOK).orderType(OrderType.NEW).symbol("JPM").orderId("obbf5").side(Side.SELL).offerPrice(239).offerSize(2400).build();

        Book obBidAmend1 = new Book.Builder().feedType(FeedType.ORDER_BOOK).orderType(OrderType.MODIFY).orderId("obb1").bidSize(9999).build();

        Book obBidCancel1 = new Book.Builder().feedType(FeedType.ORDER_BOOK).orderType(OrderType.CANCEL).orderId("obb2").build();


        ceb.receiveOrderBook(obBid1);
        ceb.receiveOrderBook(obBid2);
        ceb.receiveOrderBook(obBid3);
        ceb.receiveOrderBook(obBid4);
        ceb.receiveOrderBook(obBid5);

        ceb.receiveOrderBook(obOffer1);
        ceb.receiveOrderBook(obOffer2);
        ceb.receiveOrderBook(obOffer3);
        ceb.receiveOrderBook(obOffer4);
        ceb.receiveOrderBook(obOffer5);

        ceb.receiveOrderBook(obBidAmend1);

        ceb.receiveOrderBook(obBidCancel1);

        List<ConsolidatedBookLevel> topFive = ceb.getTopFiveLevelsBySymbol("JPM");
        System.out.println(topFive);

        Assertions.assertEquals(245,topFive.get(0).getBidPrice());
        Assertions.assertEquals(204,topFive.get(0).getOfferPrice());
        Assertions.assertEquals(-1,topFive.get(4).getBidPrice());
        Assertions.assertEquals(239,topFive.get(4).getOfferPrice());
    }

}
