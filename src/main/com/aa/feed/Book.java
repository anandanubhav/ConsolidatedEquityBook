package main.com.aa.feed;

public class Book {

    private final FeedType feedType;
    private final String symbol, orderId;
    private final Side side;
    private final OrderType orderType;
    private final double bidPrice, offerPrice;
    private final long bidSize, offerSize;
    private final long oldSize;

    private Book(Builder builder){
        this.feedType = builder.feedType;
        this.symbol = builder.symbol;
        this.orderId = builder.orderId;
        this.side = builder.side;
        this.orderType =builder.orderType;;
        this.bidPrice = builder.bidPrice;
        this.offerPrice = builder.offerPrice;
        this.bidSize = builder.bidSize;
        this.offerSize = builder.offerSize;
        this.oldSize = builder.oldSize;
    }

    public FeedType getFeedType() {
        return feedType;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getOrderId() {
        return orderId;
    }

    public Side getSide() {
        return side;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public double getBidPrice() {
        return bidPrice;
    }

    public double getOfferPrice() {
        return offerPrice;
    }

    public long getBidSize() {
        return bidSize;
    }

    public long getOfferSize() {
        return offerSize;
    }

    public long getOldSize() {
        return oldSize;
    }

    @Override
    public String toString() {
        return "Book{" +
                "feedType=" + feedType +
                ", symbol='" + symbol + '\'' +
                ", orderId='" + orderId + '\'' +
                ", side=" + side +
                ", orderType=" + orderType +
                ", bidPrice=" + bidPrice +
                ", offerPrice=" + offerPrice +
                ", bidSize=" + bidSize +
                ", offerSize=" + offerSize +
                ", oldSize=" + oldSize +
                '}';
    }

    public static class Builder {

        private FeedType feedType;
        private String symbol, orderId;
        private Side side;
        private OrderType orderType;
        private double bidPrice, offerPrice;
        private long bidSize, offerSize;
        private long oldSize;

        public Builder() {}

        public Builder fromBook(Book book){
            this.feedType = book.feedType;
            this.symbol = book.symbol;
            this.orderId = book.orderId;
            this.side = book.side;
            this.orderType = book.orderType;
            this.bidPrice = book.bidPrice;
            this.offerPrice = book.offerPrice;
            this.bidSize = book.bidSize;
            this.offerSize = book.offerSize;
            this.oldSize = book.oldSize;
            return this;
        }

        public Builder feedType(FeedType feedType){
            this.feedType = feedType;
            return this;
        }

        public Builder symbol(String symbol){
            this.symbol = symbol;
            return this;
        }

        public Builder orderId(String orderId){
            this.orderId = orderId;
            return this;
        }

        public Builder side(Side side){
            this.side = side;
            return this;
        }

        public Builder orderType(OrderType orderType){
            this.orderType = orderType;
            return this;
        }

        public Builder bidPrice(double bidPrice){
            this.bidPrice = bidPrice;
            return this;
        }

        public Builder offerPrice(double offerPrice){
            this.offerPrice = offerPrice;
            return this;
        }

        public Builder bidSize(long bidSize){
            this.bidSize = bidSize;
            return this;
        }

        public Builder offerSize(long offerSize){
            this.offerSize = offerSize;
            return this;
        }

        public Builder oldSize(long oldSize){
            this.oldSize = oldSize;
            return this;
        }

        public Book build(){
            return new Book(this);
        }
    }
}
