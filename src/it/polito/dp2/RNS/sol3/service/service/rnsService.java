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

    public Places getPlaces(SearchPlaces scope, String keyword) {
        return db.getPlaces(scope, keyword);
    }

    public Place getPlace(long id, String placeID) {
        return db.getPlace(id, placeID);
    }

    public Connections getConnections() {
        return db.getConnections();
    }

    public Connection getConnection(long id) {
        return db.getConnection(id);
    }

    public Vehicles getVehicles(SearchVehicles scope, String keyword, String state, String entrytime, String position) {
        return db.getVehicles(scope, keyword, state, entrytime, position);
    }

    public Vehicle getVehicle(long id, String plateID) {
        return db.getVehicle(id, plateID);
    }

    public Vehicle addVehicle(long id, Vehicle vehicle) {
        return db.addVehicle(id, vehicle);
    }

    public Vehicle updateVehicle(long id, Vehicle vehicle) {
        return db.updateVehicle(id, vehicle);
    }
}
