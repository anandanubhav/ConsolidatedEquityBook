package main.com.aa.feed;

import java.util.Objects;

public class BookLevel {

    private final double bidPrice, offerPrice;
    private final long bidSize, offerSize;

    public BookLevel(double bidPrice, double offerPrice, long bidSize, long offerSize) {
        this.bidPrice = bidPrice;
        this.offerPrice = offerPrice;
        this.bidSize = bidSize;
        this.offerSize = offerSize;
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

    @Override
    public String toString() {
        return "BookLevel{" +
                "bidPrice=" + bidPrice +
                ", offerPrice=" + offerPrice +
                ", bidSize=" + bidSize +
                ", offerSize=" + offerSize +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookLevel bookLevel = (BookLevel) o;
        return Double.compare(bookLevel.bidPrice, bidPrice) == 0 ||
                Double.compare(bookLevel.offerPrice, offerPrice) == 0;
    }
}
