package main.com.aa.feed;

import java.util.Objects;

public class BookLevel implements  Comparable<BookLevel>{

    private double price;
    private long size;

    public BookLevel(double price, long size) {
        this.price = price;
        this.size = size;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "BookLevel{" +
                "price=" + price +
                ", size=" + size +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookLevel bookLevel = (BookLevel) o;
        return Double.compare(bookLevel.price, price) == 0;
    }

    @Override
    public int compareTo(BookLevel o) {
        if(this.getPrice() > o.getPrice()) {
            return 1;
        } else if(this.getPrice() < o.getPrice()){
            return -1;
        }
        return 0;
    }
}
