package it.polito.dp2.RNS.sol3.service.service;

import it.polito.dp2.RNS.sol3.rest.service.jaxb.*;
import it.polito.dp2.RNS.sol3.service.db.rnsDB;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Copyright by Jacopx on 2019-01-16.
 */
public class rnsService {
    private rnsDB db = rnsDB.getRnsDB();

    public long getNextConn() {
        return rnsDB.getNextConn();
    }

    public Places getPlaces(SearchPlaces scope, String keyword, String placeID) {
        return db.getPlaces(scope, keyword, placeID);
    }

    public Place getPlace(String placeID) {
        return db.getPlace(placeID);
    }

    public Connections getConnections() {
        return db.getConnections();
    }

    public Connection getConnection(long id) {
        return db.getConnection(id);
    }

    public Vehicles getVehicles(SearchVehicles scope, String keyword, String state, String entryTime, String position) {
        return db.getVehicles(scope, keyword, state, entryTime, position);
    }

    public Vehicle getVehicle(String plateID) {
        return db.getVehicle(plateID);
    }

    public Vehicle addVehicle(Vehicle vehicle) {
        return db.addVehicle(vehicle);
    }

    public Vehicle updateVehicle(String plateID, String state, String move) {
        return db.updateVehicle(plateID, state, move);
    }

    public Vehicle deleteVehicle(String plateID, String outGate) { return db.deleteVehicle(plateID, outGate); }
}
