package it.polito.dp2.RNS.sol3.service.db;

import it.polito.dp2.RNS.sol3.rest.service.jaxb.ShortPaths;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.SuggPath;
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
    private ShortPaths shortPaths;

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
        ShortPaths newPaths = new ShortPaths();
        for(List<String> ps:paths) {
            SuggPath newPath = new SuggPath();
            newPath.getRelation().addAll(ps);
            newPaths.getSuggPath().add(newPath);
        }
        vehicle.getShortPaths().add(newPaths);
        this.shortPaths = newPaths;
    }
}
