package com.tts.transitapp.model;

import java.util.Comparator;

public class BusComparator implements Comparator<Bus> {
    @Override
    public int compare(Bus o1, Bus o2) {
        if (o1.distance < o2.distance) return -1; //means negative
        if (o1.distance > o2.distance) return 1;  //means positive
        return 0; //means they're equal
    }
}
