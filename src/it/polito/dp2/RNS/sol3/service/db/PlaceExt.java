package it.polito.dp2.RNS.sol3.service.db;

import it.polito.dp2.RNS.sol3.rest.service.jaxb.Connection;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Place;

import java.util.Collection;
import java.util.Map;

/**
 * Copyright by Jacopx on 2019-01-17.
 */
public class PlaceExt {
    private long id;
    private Place place;
    private Map<Long, Connection> connections;
    private Map<Long, Connection> connectedBy;

    public PlaceExt(long id, Place place) {
        this.id = id;
        this.place = place;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public Map<Long, Connection> getConnections() {
        return connections;
    }

    public Collection<Long> getConnectionsC() {
        return connections.keySet();
    }

    public void setConnections(Map<Long, Connection> connections) {
        this.connections = connections;
    }

    public Connection addConnections(long tid, Connection c) {
        return connections.putIfAbsent(tid, c);
    }

    public Map<Long, Connection> getConnectedBy() {
        return connectedBy;
    }

    public void setConnectedBy(Map<Long, Connection> connectedBy) {
        this.connectedBy = connectedBy;
    }

    public Connection addConnectedBy(long tid, Connection c) {
        return connectedBy.putIfAbsent(tid, c);
    }

    public Connection removeConnectedBy(long tid) { return connectedBy.remove(tid);}
}
