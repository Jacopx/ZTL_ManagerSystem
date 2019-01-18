package it.polito.dp2.RNS.sol3.service.db;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.lab2.*;
import it.polito.dp2.RNS.lab2.PathFinderFactory;
import it.polito.dp2.RNS.sol1.RnsReaderFactory;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.*;
import it.polito.dp2.RNS.sol3.service.service.SearchPlaces;

import javax.ws.rs.ClientErrorException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright by Jacopx on 15/01/2019.
 */
public class rnsDB {
    private static rnsDB rnsDB = new rnsDB();
    private static long lastId=0;

    private ConcurrentHashMap<Long,PlaceExt> placeExtByNode;
    private ConcurrentHashMap<String, Long> placeExtById;
    private ConcurrentHashMap<Long, Connection> connectionById;

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

        placeExtByNode = new ConcurrentHashMap<>();
        placeExtById = new ConcurrentHashMap<>();
        connectionById = new ConcurrentHashMap<>();

        // PLACE GATE
        for (GateReader gateReader : monitor.getGates(null)) {
            Place newGate = new Place();
            if(gateReader.getType().toString().equals(GateItem.IN.value())) {
                newGate.setGate(GateItem.IN);
            } else if(gateReader.getType().toString().equals(GateItem.INOUT.value())) {
                newGate.setGate(GateItem.INOUT);
            } else {
                newGate.setGate(GateItem.OUT);
            }
            newGate.setId(gateReader.getId());
            long id = getNextId();
            newGate.setSelf("http://localhost:8080/RnsSystem/rest" + "/places/" + id);

            createPlace(id, newGate);
        }

        // PLACE PARKING AREA
        for (ParkingAreaReader parkingAreaReader : monitor.getParkingAreas(null)) {

            ParkingItem park = new ParkingItem();
            park.getServices().addAll(parkingAreaReader.getServices());

            Place newPark = new Place();
            newPark.setId(parkingAreaReader.getId());
            newPark.setParking(park);
            long id = getNextId();
            newPark.setSelf("http://localhost:8080/RnsSystem/rest" + "/places/" + id);

            createPlace(id, newPark);
        }

        // ROAD SEGMENT
        for (RoadSegmentReader roadSegmentReader : monitor.getRoadSegments(null)) {

            SegmentItem seg = new SegmentItem();
            seg.setName(roadSegmentReader.getName());

            Place newRoadSeg = new Place();
            newRoadSeg.setId(roadSegmentReader.getId());
            newRoadSeg.setSegment(seg);
            long id = getNextId();
            newRoadSeg.setSelf("http://localhost:8080/RnsSystem/rest" + "/places/" + id);

            createPlace(id, newRoadSeg);
        }

        // CONNECTIONS
        for(ConnectionReader connectionReader:monitor.getConnections()) {
            Connection newConnection = new Connection();
            newConnection.setFrom(connectionReader.getFrom().getId());
            newConnection.setTo(connectionReader.getTo().getId());

//            long id = getNextId();
//            long from = placeExtById.get(connectionReader.getFrom().toString());
//            placeExtByNode.get(from).addConnections(id, newConnection);
//
//            long to = placeExtById.get(connectionReader.getFrom().toString());
//            placeExtByNode.get(to).addConnectedBy(id, newConnection);
//
//            connectionById.putIfAbsent(id, newConnection);
        }
    }

    public Collection<Place> getPlaces(SearchPlaces scope, String keyword, String type) {
        return (Collection<Place>) placeExtById;
    }

    public Place getPlace(long node) {
        return placeExtByNode.get(node).getPlace();
    }

    public Place createPlace(long node, Place place) {
        PlaceExt itemExt = new PlaceExt(node, place);
        if (placeExtByNode.putIfAbsent(node, itemExt)==null) {
            placeExtById.putIfAbsent(place.getId(), node);
            return place;
        } else
            return null;
    }
}
