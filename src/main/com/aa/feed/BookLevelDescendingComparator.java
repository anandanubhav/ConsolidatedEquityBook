package main.com.aa.feed;

import java.util.Comparator;

public class BookLevelDescendingComparator implements Comparator<BookLevel> {

    @Override
    public int compare(BookLevel o1, BookLevel o2) {
        if(o1.getPrice() > o2.getPrice()) {
            return -1;
        } else if(o1.getPrice() < o2.getPrice()){
            return 1;
        }
        return 0;
    }
}
