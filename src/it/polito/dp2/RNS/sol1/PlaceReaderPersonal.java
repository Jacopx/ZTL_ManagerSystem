package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.PlaceReader;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright by Jacopx on 23/11/2018.
 */
public class PlaceReaderPersonal implements PlaceReader {
    private int capacity;
    private String id;
    private Set<PlaceReader> nextPlaces;

    public PlaceReaderPersonal(int capacity, String id, Set<PlaceReader> nextPlaces) {
        this.capacity = capacity;
        this.id = id;
        this.nextPlaces = new HashSet<>();
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public Set<PlaceReader> getNextPlaces() {
        return nextPlaces;
    }

    @Override
    public String getId() {
        return id;
    }

}
