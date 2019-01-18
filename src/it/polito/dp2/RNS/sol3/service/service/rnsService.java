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

    public Places getPlaces(SearchPlaces scope, String keyword, String type) {
        return null;
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

    public Vehicle getVehicle(String id) {
        return null;
    }
}
