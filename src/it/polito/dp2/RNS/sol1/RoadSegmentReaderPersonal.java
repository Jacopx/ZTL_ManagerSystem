package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.PlaceReader;
import it.polito.dp2.RNS.RoadSegmentReader;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright by Jacopx on 24/11/2018.
 */
public class RoadSegmentReaderPersonal implements RoadSegmentReader {
    private int capacity;
    private String id;
    private Set<PlaceReader> nextPlaces;
    private String name;
    private String roadName;

    public RoadSegmentReaderPersonal(int capacity, String id, String name, String roadName) {
        this.capacity = capacity;
        this.id = id;
        this.nextPlaces = new HashSet<>();
        this.name = name;
        this.roadName = roadName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getRoadName() {
        return roadName;
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
