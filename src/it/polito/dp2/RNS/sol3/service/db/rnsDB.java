package it.polito.dp2.RNS.sol3.service.db;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.lab2.*;
import it.polito.dp2.RNS.lab2.PathFinderFactory;
import it.polito.dp2.RNS.sol1.RnsReaderFactory;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Connection;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Place;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Places;
import it.polito.dp2.RNS.sol3.service.service.SearchPlaces;

import javax.ws.rs.ClientErrorException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright by Jacopx on 15/01/2019.
 */
public class rnsDB {
    private static rnsDB rnsDB = new rnsDB();
    private static long lastId=0;

    private ConcurrentHashMap<Long,PlaceExt> placeExtById;

    public static rnsDB getRnsDB() {
        return rnsDB;
    }

    public static synchronized long getNextId() {
        return ++lastId;
    }

    private rnsDB() {
        PathFinder pff;
        RnsReader monitor = null;

        try {
            if(System.getProperty("it.polito.dp2.RNS.lab3.Neo4JURL") == null) {
                System.setProperty("it.polito.dp2.RNS.lab2.URL", "http://localhost:7474/db");
            } else {
                System.setProperty("it.polito.dp2.RNS.lab2.URL", System.getProperty("it.polito.dp2.RNS.lab3.Neo4JURL"));
            }

            System.setProperty("it.polito.dp2.RNS.lab2.PathFinderFactory", "it.polito.dp2.RNS.sol2.PathFinderFactory");
            System.setProperty("it.polito.dp2.RNS.RnsReaderFactory", "it.polito.dp2.RNS.Random.RnsReaderFactoryImpl");

            // Loading Neo4j
            pff = PathFinderFactory.newInstance().newPathFinder();
            pff.reloadModel();

            // Loading local DB
            monitor = RnsReaderFactory.newInstance().newRnsReader();

        } catch (PathFinderException | RnsReaderException | ServiceException | ModelException e) {
            e.printStackTrace();
        }

        placeExtById = new ConcurrentHashMap<>();

        // PLACE GATE
        for (GateReader gateReader : monitor.getGates(null)) {
            Place newGate = new Place();
            newGate.setId(gateReader.getId());
        }

        // PLACE PARKING AREA
        for (ParkingAreaReader parkingAreaReader : monitor.getParkingAreas(null)) {
            Place newPark = new Place();
            newPark.setId(parkingAreaReader.getId());
            createPlace(getNextId(), newPark);
        }

        // ROAD SEGMENT
        for (RoadSegmentReader roadSegmentReader : monitor.getRoadSegments(null)) {
            Place newRoadSeg = new Place();
            newRoadSeg.setId(roadSegmentReader.getId());
            createPlace(getNextId(), newRoadSeg);
        }

        // CONNECTIONS
        for(ConnectionReader connectionReader:monitor.getConnections()) {
            Connection newConnection = new Connection();
        }
    }

    public Collection<Place> getPlaces(SearchPlaces scope, String keyword, String type) {
        return (Collection<Place>) placeExtById;
    }

    public Place getPlace(long id) {
        return placeExtById.get(id).getPlace();
    }

    public Place createPlace(long id, Place place) {
        PlaceExt itemExt = new PlaceExt(id,place);
        if (placeExtById.putIfAbsent(id, itemExt)==null) {
            return place;
        } else
            return null;
    }

    public Place updatePlace(long  id, Place place) {
        PlaceExt pe = placeExtById.get(id);
        if (pe==null)
            return null;
        Place old = pe.getPlace();
        place.setSelf(old.getSelf());
        place.setConnections(old.getConnections());
        place.setConnectedBy(old.getConnectedBy());
//        removeIndexing(old);
        pe.setPlace(place);
//        addIndexing(place);
        return place;
    }

    public Place deletePlace(long id) {
        PlaceExt pe = placeExtById.get(id);
        if (pe==null)
            return null;
        if (!pe.getConnectedBy().isEmpty())
            throw new ClientErrorException(409); // it is connected by some place, we cannot delete
        pe = placeExtById.remove(id);
        if (pe==null)
            return null;
        Place place = pe.getPlace();
        for (Long tid:pe.getConnectionsC()) {
            PlaceExt target = placeExtById.get(tid);
            target.removeConnectedBy(id);
        }
//        removeIndexing(item);
        return place;
    }

}
