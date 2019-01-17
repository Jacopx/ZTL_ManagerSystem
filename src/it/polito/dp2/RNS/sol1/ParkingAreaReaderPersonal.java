package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.ParkingAreaReader;
import it.polito.dp2.RNS.PlaceReader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Copyright by Jacopx on 24/11/2018.
 */
public class ParkingAreaReaderPersonal implements ParkingAreaReader {
    private int capacity;
    private String id;
    private Set<PlaceReader> nextPlaces;
    private Set<String> services;


    public ParkingAreaReaderPersonal(int capacity, String id, List<String> servicesList) {
        this.capacity = capacity;
        this.id = id;
        this.nextPlaces = new HashSet<>();
        this.services = new HashSet<>(servicesList);
    }

    @Override
    public Set<String> getServices() {
        return services;
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
