package it.polito.dp2.RNS.sol3.service.db;

import it.polito.dp2.RNS.sol3.rest.service.jaxb.Connection;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Place;

import java.util.Collection;
import java.util.HashMap;
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
        connections = new HashMap<>();
        connectedBy = new HashMap<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Place getPlace() {
        for(Connection c:connections.values())
            place.getConnections().add(c.getSelf());

        for(Connection c:connectedBy.values())
            place.getConnectedBy().add(c.getSelf());

        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public Map<Long, Connection> getConnections() {
        return connections;
    }

    public Collection<Connection> getConnectionsC() {
        return connections.values();
    }

    public Connection addConnections(long cid, Connection c) {
        return connections.putIfAbsent(cid, c);
    }

    public Map<Long, Connection> getConnectedBy() {
        return connectedBy;
    }

    public Collection<Connection> getConnectedByC() {
        return connectedBy.values();
    }

    public Connection addConnectedBy(long cid, Connection c) {
        return connectedBy.putIfAbsent(cid, c);
    }
}
