package it.polito.dp2.RNS.sol3.service.db;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.lab2.*;
import it.polito.dp2.RNS.lab2.PathFinderFactory;
import it.polito.dp2.RNS.sol1.RnsReaderFactory;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.*;
import it.polito.dp2.RNS.sol3.service.service.SearchPlaces;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright by Jacopx on 15/01/2019.
 */
public class rnsDB {
    private static rnsDB rnsDB = new rnsDB();
    private static long lastId=0;
    private static long lastConn=0;
    private static String URL;

    private ConcurrentHashMap<Long, PlaceExt> placeExtByNode;
    private ConcurrentHashMap<String, Long> placeExtById;
    private ConcurrentHashMap<Long, Connection> connectionById;
    private ConcurrentHashMap<Long, PlaceExt> parkings;
    private ConcurrentHashMap<Long, PlaceExt> segments;
    private ConcurrentHashMap<Long, PlaceExt> gates;

    public static rnsDB getRnsDB() {
        return rnsDB;
    }

    public static synchronized long getNextId() {
        return ++lastId;
    }

    public static synchronized long getNextConn() {
        return ++lastConn;
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

            if(System.getProperty("URL") == null) {
                URL = "http://localhost:8080/RnsSystem/rest";
                System.setProperty("URL", URL);
            }

            URL = System.getProperty("URL");

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
        parkings = new ConcurrentHashMap<>();
        segments = new ConcurrentHashMap<>();
        gates = new ConcurrentHashMap<>();

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
            newGate.setSelf(URL + "/places/" + id);

            createPlace(id, newGate);
            gates.putIfAbsent(id, placeExtByNode.get(id));
        }

        // PLACE PARKING AREA
        for (ParkingAreaReader parkingAreaReader : monitor.getParkingAreas(null)) {

            ParkingItem park = new ParkingItem();
            park.getServices().addAll(parkingAreaReader.getServices());

            Place newPark = new Place();
            newPark.setId(parkingAreaReader.getId());
            newPark.setParking(park);
            long id = getNextId();
            newPark.setSelf(URL + "/places/" + id);

            createPlace(id, newPark);
            parkings.putIfAbsent(id, placeExtByNode.get(id));
        }

        // ROAD SEGMENT
        for (RoadSegmentReader roadSegmentReader : monitor.getRoadSegments(null)) {

            SegmentItem seg = new SegmentItem();
            seg.setName(roadSegmentReader.getName());

            Place newRoadSeg = new Place();
            newRoadSeg.setId(roadSegmentReader.getId());
            newRoadSeg.setSegment(seg);
            long id = getNextId();
            newRoadSeg.setSelf(URL + "/places/" + id);

            createPlace(id, newRoadSeg);
            segments.putIfAbsent(id, placeExtByNode.get(id));
        }

        // CONNECTIONS
        for(ConnectionReader connectionReader:monitor.getConnections()) {
            Connection newConnection = new Connection();
            long id = getNextConn();

            newConnection.setSelf(URL + "/connections/" + id);
            long FROM = placeExtById.get(connectionReader.getFrom().getId());
            PlaceExt placeFrom = placeExtByNode.get(FROM);
            newConnection.setFrom(placeFrom.getPlace().getSelf());
            placeFrom.addConnections(id, newConnection);

            long TO = placeExtById.get(connectionReader.getTo().getId());
            PlaceExt placeTo = placeExtByNode.get(TO);
            newConnection.setTo(placeTo.getPlace().getSelf());
            placeTo.addConnectedBy(id, newConnection);

            connectionById.putIfAbsent(id, newConnection);
        }
    }

    public Places getPlaces(String keyword) {
        Places list = new Places();
        for(PlaceExt place:placeExtByNode.values()) {
            list.getPlace().add(place.getPlace());
//            if(keyword != null) {
//                if(place.getPlace().getId().contains(keyword))
//                    list.getPlace().add(place.getPlace());
//            }
        }
        return list;
    }

    public Places getSegments(String keyword) {
        Places list = new Places();
        for(PlaceExt place:segments.values()) {
            list.getPlace().add(place.getPlace());
//            if(keyword != null) {
//                if(place.getPlace().getId().contains(keyword))
//                    list.getPlace().add(place.getPlace());
//            }
        }
        return list;
    }

    public Places getParkings(String keyword) {
        Places list = new Places();
        for(PlaceExt place:parkings.values()) {
            list.getPlace().add(place.getPlace());
//            if(keyword != null) {
//                if(place.getPlace().getId().contains(keyword))
//                    list.getPlace().add(place.getPlace());
//            }
        }
        return list;
    }

    public Places getGates(String keyword, String type) {
        Places list = new Places();
        for(PlaceExt place:gates.values()) {
            list.getPlace().add(place.getPlace());
//            if(keyword != null || type != null) {
//                if(place.getPlace().getId().contains(keyword) || place.getPlace().getGate().value().equals(type))
//                    list.getPlace().add(place.getPlace());
//            } else if(keyword != null) {
//                if(place.getPlace().getId().contains(keyword))
//                    list.getPlace().add(place.getPlace());
//            } else {
//                if(place.getPlace().getGate().value().equals(type))
//                    list.getPlace().add(place.getPlace());
//            }
        }
        return list;
    }

    public Place getPlace(long node) {
        Connections conns = new Connections();
        conns.getConnection().addAll(placeExtByNode.get(node).getConnectionsC());
        Connections connsBy = new Connections();
        connsBy.getConnection().addAll(placeExtByNode.get(node).getConnectedByC());
        Place returned = new Place();
        returned.setConnections(conns.toString());
        returned.setConnectedBy(conns.toString());
        return returned;
    }

    public Place createPlace(long node, Place place) {
        PlaceExt itemExt = new PlaceExt(node, place);
        if (placeExtByNode.putIfAbsent(node, itemExt)==null) {
            placeExtById.putIfAbsent(place.getId(), node);
            return place;
        } else
            return null;
    }

    public Connection getConnection(long id) {
        return connectionById.get(id);
    }
}
