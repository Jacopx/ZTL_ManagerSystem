package it.polito.dp2.RNS.sol3.service.service;

import it.polito.dp2.RNS.sol3.rest.service.jaxb.*;
import it.polito.dp2.RNS.sol3.service.db.rnsDB;
import org.joda.time.DateTime;

/**
 * Copyright by Jacopx on 2019-01-16.
 */
public class rnsService {
    rnsDB db = new rnsDB();

    public rnsService() {

    }

    public long getNextId() {
        return db.getNextId();
    }

    public Places getPlaces(SearchPlaces scope, String keyword, String type) {
        return null;
    }

    public Place createPlace(long id, Place place) {
        return db.createPlace(id, place);
    }

    public Place getPlace(String id) {
        return null;
    }

    public Connections getConnections() {
        return null;
    }

    public Connection getConnection(String id) {
        return null;
    }

    public Vehicles getVehicles(SearchVehicles all, String keyword, String type, String state, String entrytime, String position) {
        return null;
    }

    public Vehicle getVehicle(String id) {
        return null;
    }
}
