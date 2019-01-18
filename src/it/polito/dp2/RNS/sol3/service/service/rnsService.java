package it.polito.dp2.RNS.sol3.service.service;

import it.polito.dp2.RNS.sol3.rest.service.jaxb.*;
import it.polito.dp2.RNS.sol3.service.db.rnsDB;

/**
 * Copyright by Jacopx on 2019-01-16.
 */
public class rnsService {
    private rnsDB db = rnsDB.getRnsDB();

    public long getNextId() {
        return rnsDB.getNextId();
    }

    public long getNextConn() {
        return rnsDB.getNextConn();
    }

    public long getNextVehicle() {
        return rnsDB.getNextVehicle();
    }

    public Places getPlaces(SearchPlaces scope, String keyword, String type) {
        switch (scope) {
            case ALL: default:
                return db.getPlaces(keyword);
        }
    }

    public Place getPlace(long id) {
        return db.getPlace(id);
    }

    public Connections getConnections() {
        return null;
    }

    public Connection getConnection(long id) {
        return db.getConnection(id);
    }

    public Vehicles getVehicles(SearchVehicles all, String keyword, String type, String state, String entrytime, String position) {
        return null;
    }

    public Vehicle getVehicle(long id) {
        return db.getVehicle(id);
    }

    public Vehicle addVehicle(long id, Vehicle vehicle) {
        return db.addVehicle(id, vehicle);
    }

    public Vehicle updateVehicle(long id, Vehicle vehicle) {
        return db.updateVehicle(id, vehicle);
    }
}
