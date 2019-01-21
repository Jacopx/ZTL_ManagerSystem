package it.polito.dp2.RNS.sol3.service.db;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.lab2.*;
import it.polito.dp2.RNS.lab2.PathFinderFactory;
import it.polito.dp2.RNS.sol1.RnsReaderFactory;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.*;
import it.polito.dp2.RNS.sol3.service.service.SearchPlaces;
import it.polito.dp2.RNS.sol3.service.service.SearchVehicles;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Copyright by Jacopx on 15/01/2019.
 */
public class rnsDB {
    private static rnsDB rnsDB = new rnsDB();
    private static long lastId=0;
    private static long lastConn=0;
    private static long lastVehicle=0;
    private static String URL;

    PathFinder pff = null;
    RnsReader monitor = null;

    private ConcurrentHashMap<Long, PlaceExt> placeExtByNode;
    private ConcurrentHashMap<String, Long> placeExtById;
    private ConcurrentHashMap<Long, Connection> connectionById;
    private ConcurrentHashMap<Long, PlaceExt> parkings;
    private ConcurrentHashMap<Long, PlaceExt> segments;
    private ConcurrentHashMap<Long, PlaceExt> gates;
    private ConcurrentHashMap<Long, VehicleExt> vehicles;

    public static rnsDB getRnsDB() {
        return rnsDB;
    }

    public static synchronized long getNextId() {
        return ++lastId;
    }

    public static synchronized long getNextConn() {
        return ++lastConn;
    }

    public static synchronized long getNextVehicle() {
        return ++lastVehicle;
    }

    private rnsDB() {

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
        vehicles = new ConcurrentHashMap<>();

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
            newConnection.setFrom(placeFrom.getPlace().getId());
            newConnection.setFromNode(placeFrom.getPlace().getSelf());
            placeFrom.addConnections(id, newConnection);

            long TO = placeExtById.get(connectionReader.getTo().getId());
            PlaceExt placeTo = placeExtByNode.get(TO);
            newConnection.setTo(placeTo.getPlace().getId());
            newConnection.setToNode(placeTo.getPlace().getSelf());
            placeTo.addConnectedBy(id, newConnection);

            connectionById.putIfAbsent(id, newConnection);
        }

        // VEHICLE for debug
        for(VehicleReader vehicleReader:monitor.getVehicles(null, null, null)) {
            Vehicle v = new Vehicle();
//            v.setEntryTime();
            v.setId(vehicleReader.getId());
            v.setFrom(vehicleReader.getOrigin().getId());
            v.setFromNode(placeExtByNode.get(placeExtById.get(vehicleReader.getOrigin().getId())).getPlace().getSelf());
            v.setTo(vehicleReader.getDestination().getId());
            v.setToNode(placeExtByNode.get(placeExtById.get(vehicleReader.getOrigin().getId())).getPlace().getSelf());
            v.setPosition(vehicleReader.getPosition().getId());
            v.setState(vehicleReader.getState().value());

            addVehicle(getNextVehicle(), v);
        }
    }

    public Places getPlaces(SearchPlaces scope, String keyword) {
        switch (scope) {
            case SEGMENT: {
                return searchPlaces(segments, keyword);
            } case PARKING: {
                return searchPlaces(parkings, keyword);
            } case GATE: {
                return searchPlaces(gates, keyword);
            } case ALL: default: {
                return searchPlaces(placeExtByNode, keyword);
            }
        }
    }

    private Places searchPlaces(ConcurrentHashMap<Long, PlaceExt> place, String keyword) {
        Places list = new Places();
        for(PlaceExt p:place.values()) {
            if(keyword != null && !keyword.isEmpty()) {
                if(p.getPlace().getId().contains(keyword))
                    list.getPlace().add(p.getPlace());
            } else {
                list.getPlace().add(p.getPlace());
            }
        }
        return list;
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

    public Connections getConnections() {
        Connections list = new Connections();
        list.getConnection().addAll(connectionById.values());
        return list;
    }

    public Connection getConnection(long id) {
        return connectionById.get(id);
    }

    //@TODO: Differentiation by vehicle type
    public Vehicle addVehicle(long id, Vehicle vehicle) {
        VehicleExt vehicleExt = new VehicleExt(id, vehicle);
        vehicle.setSelf(URL + "/vehicles/" + id);
        if (vehicles.putIfAbsent(id, vehicleExt)==null) {
            vehicleExt.setPaths(computePath(vehicle));
            return vehicle;
        } else
            return null;
    }

    //@TODO: Add paths to vehicle nodes
    private Set<List<String>> computePath(Vehicle vehicle) {
        try {
            return pff.findShortestPaths(vehicle.getPosition(), vehicle.getTo(), 999);
        } catch (UnknownIdException | BadStateException | ServiceException e) {
            e.printStackTrace();
        }
        return null;
    }

    //@TODO: To be tested
    public Vehicles getVehicles(SearchVehicles scope, String keyword, String state, String entrytime, String position) {
        switch (scope) {
            case CAR: {
                return searchVehicles(vehicles, keyword, state, entrytime, position);
            }
            case TRUCK: {
                return searchVehicles(vehicles, keyword, state, entrytime, position);
            }
            case CARAVAN: {
                return searchVehicles(vehicles, keyword, state, entrytime, position);
            }
            case SHUTTLE: {
                return searchVehicles(vehicles, keyword, state, entrytime, position);
            }
            case ALL: default: {
                return searchVehicles(vehicles, keyword, state, entrytime, position);
            }
        }
    }

    //@TODO: Resolve with stream
    private Vehicles searchVehicles(ConcurrentHashMap<Long, VehicleExt> vehicles, String keyword, String state, String entrytime, String position) {
        Vehicles list = new Vehicles();
        for(VehicleExt v:vehicles.values()) {
            list.getVehicle().add(v.getVehicle());
        }
//        List newList = vehicles.values().stream()
//                .filter(v -> (keyword == null || keyword.isEmpty()) || v.getVehicle().getId().contains(keyword))
//                .filter(v -> (state == null || keyword.isEmpty()) || v.getVehicle().getState().equals(state))
//                .filter(v -> (entrytime ==  null || keyword.isEmpty()) || v.getVehicle().getEntryTime().equals(entrytime))
//                .filter(v -> (position == null || keyword.isEmpty()) || v.getVehicle().getPosition().equals(position))
//                .collect(Collectors.toList());
//        list.getVehicle().addAll(newList);
        return list;
    }

    public Vehicle getVehicle(long id) {
        return vehicles.get(id).getVehicle();
    }

    public Vehicle updateVehicle(long id, Vehicle vehicle) {
        VehicleExt vehicleExt = vehicles.get(id);
        if(vehicleExt == null)
            return null;
        Vehicle old = vehicleExt.getVehicle();
        vehicle.setSelf(old.getSelf());
        vehicle.setId(old.getId());
        vehicle.setEntryTime(old.getEntryTime());
        vehicleExt.setVehicle(vehicle);

        //@TODO: Missing verify goodness
        if(!vehicle.getPosition().equals(old.getPosition())) {
            vehicleExt.setPaths(computePath(vehicle));
        }

        return vehicle;
    }
}
