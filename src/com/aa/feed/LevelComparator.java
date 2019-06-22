package com.aa.feed;

import java.util.Comparator;

public class LevelComparator implements Comparator<BookLevel> {

    @Override
    public int compare(BookLevel o1, BookLevel o2) {
        if( (o1.getBidPrice() > o2.getBidPrice()) && (o1.getOfferPrice() < o2.getOfferPrice()) ) {
            return 1;
        } else if( (o1.getBidPrice() < o2.getBidPrice()) &&(o1.getOfferPrice() > o2.getOfferPrice()) ){
            return -1;
        }
        return 0;
    }
}
