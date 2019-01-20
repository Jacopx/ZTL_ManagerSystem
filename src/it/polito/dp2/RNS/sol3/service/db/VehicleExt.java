package it.polito.dp2.RNS.sol3.service.db;

import it.polito.dp2.RNS.sol3.rest.service.jaxb.Vehicle;

import java.util.List;
import java.util.Set;

/**
 * Copyright by Jacopx on 2019-01-18.
 */
public class VehicleExt {
    private long id;
    private Vehicle vehicle;
    private Set<List<String>> paths;

    public VehicleExt(long id, Vehicle vehicle) {
        this.id = id;
        this.vehicle = vehicle;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Set<List<String>> getPaths() {
        return paths;
    }

    public void setPaths(Set<List<String>> paths) {
        this.paths = paths;
    }
}