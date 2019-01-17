package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.GateReader;
import it.polito.dp2.RNS.GateType;
import it.polito.dp2.RNS.PlaceReader;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright by Jacopx on 24/11/2018.
 */
public class GateReaderPersonal implements GateReader {
    private int capacity;
    private String id;
    private Set<PlaceReader> nextPlaces;
    private GateType gateType;

    public GateReaderPersonal(int capacity, String id, String type) {
        this.capacity = capacity;
        this.id = id;
        this.nextPlaces = new HashSet<>();
        this.gateType = GateType.fromValue(type);
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

    @Override
    public GateType getType() { return gateType; }
}
