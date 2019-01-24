package it.polito.dp2.RNS.sol3.service.db;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.lab2.*;
import it.polito.dp2.RNS.lab2.PathFinderFactory;
import it.polito.dp2.RNS.sol1.RnsReaderFactory;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.*;
import it.polito.dp2.RNS.sol3.service.service.SearchPlaces;
import it.polito.dp2.RNS.sol3.service.service.SearchVehicles;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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
            newGate.setCapacity(gateReader.getCapacity());
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
            newPark.setCapacity(parkingAreaReader.getCapacity());
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
            seg.setRoadName(roadSegmentReader.getRoadName());

            Place newRoadSeg = new Place();
            newRoadSeg.setCapacity(roadSegmentReader.getCapacity());
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

//        // VEHICLE for debug
//        for(VehicleReader vehicleReader:monitor.getVehicles(null, null, null)) {
//            Vehicle v = new Vehicle();
//
//            try {
//                XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar) vehicleReader.getEntryTime());
//                v.setEntryTime(cal);
//            } catch (DatatypeConfigurationException e) {
//                e.printStackTrace();
//            }
//
//            v.setId(vehicleReader.getId());
//            v.setType(vehicleReader.getType().value());
//            v.setFrom(vehicleReader.getOrigin().getId());
//            v.setFromNode(placeExtByNode.get(placeExtById.get(vehicleReader.getOrigin().getId())).getPlace().getSelf());
//            v.setTo(vehicleReader.getDestination().getId());
//            v.setToNode(placeExtByNode.get(placeExtById.get(vehicleReader.getDestination().getId())).getPlace().getSelf());
//            v.setPosition(vehicleReader.getPosition().getId());
//            v.setPositionNode(placeExtByNode.get(placeExtById.get(vehicleReader.getPosition().getId())).getPlace().getSelf());
//            v.setState(vehicleReader.getState().value());
//
//            addVehicle(getNextVehicle(), v);
//        }
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

    public Place getPlace(long node, String placeID) {
        if(node >= 0)
            return placeExtByNode.get(node).getPlace();
        if(placeID != null && !placeID.isEmpty())
            return placeExtByNode.get(placeExtById.get(placeID)).getPlace();
        return null;
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

    public Vehicle addVehicle(long id, Vehicle vehicle) {
        String temp;

        // TO CHECK
        if(vehicle.getTo() != null && placeExtById.containsKey(vehicle.getTo())) {
            PlaceExt placeExt = placeExtByNode.get(placeExtById.get(vehicle.getTo()));
            if((temp = placeExt.getPlace().getSelf()) != null) {
                vehicle.setToNode(temp);
            } else {
                return generateErrorVehicle(1);
            }
        } else {
            return generateErrorVehicle(1);
        }

        // FROM CHECK
        if(vehicle.getFrom() != null && placeExtById.containsKey(vehicle.getFrom())) {
            PlaceExt placeExt = placeExtByNode.get(placeExtById.get(vehicle.getFrom()));
            if((temp = placeExt.getPlace().getSelf()) != null) {
                vehicle.setFromNode(temp);

                GateItem type = placeExt.getPlace().getGate();
                if(type != null) {
                    if(type.value().isEmpty() || type.value().equals("OUT")) {
                        return generateErrorVehicle(2);
                    }
                } else {
                    return generateErrorVehicle(2);
                }

            } else {
                return generateErrorVehicle(1);
            }
        } else {
            return generateErrorVehicle(1);
        }

        VehicleExt vehicleExt = new VehicleExt(id, vehicle);

        Set<List<String>> computedPath = computePath(vehicle);

        if (computedPath!= null && vehicles.putIfAbsent(id, vehicleExt)==null) {
//            vehicleExt.setPaths(convert(computedPath));
            vehicleExt.setPaths(computedPath);
            return vehicle;
        } else {
            Vehicle refused = new Vehicle();
            refused.setState("REFUSED");
            return refused;
        }
    }

    private Vehicle generateErrorVehicle(int error) {
        Vehicle refused = new Vehicle();
        if(error == 2)
            refused.setState("WRONG_GATE_TYPE");
        else if(error == 1)
            refused.setState("UNKNOWN_PLACE");
        else
            refused.setState("ERROR");
        return refused;
    }

    private Set<List<String>> convert(Set<List<String>> computePath) {
        Set<List<String>> newPaths = new HashSet<>();
        for(List<String> ps:computePath) {
            List<String> newList = new ArrayList<>();
            for(String node:ps) {
                newList.add(placeExtByNode.get(placeExtById.get(node)).getPlace().getSelf());
            }
            newPaths.add(newList);
        }
        return newPaths;
    }

    private Set<List<String>> computePath(Vehicle vehicle) {
        try {
            Set<List<String>> computed = pff.findShortestPaths(vehicle.getPosition(), vehicle.getTo(), 999);
            if(computed.size() > 0) {
                for (List<String> s:computed) {
                    if(s.size() > 0)
                        return computed;
                }
            } else {
                return null;
            }

        } catch (UnknownIdException | BadStateException | ServiceException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Vehicles getVehicles(SearchVehicles scope, String keyword, String state, String entryTime, String position, String plateID) {
        switch (scope) {
            case CAR: {
                return searchVehicles(vehicles, keyword, state, entryTime, position, plateID);
            }
            case TRUCK: {
                return searchVehicles(vehicles, keyword, state, entryTime, position, plateID);
            }
            case CARAVAN: {
                return searchVehicles(vehicles, keyword, state, entryTime, position, plateID);
            }
            case SHUTTLE: {
                return searchVehicles(vehicles, keyword, state, entryTime, position, plateID);
            }
            case ALL: default: {
                return searchVehicles(vehicles, keyword, state, entryTime, position, plateID);
            }
        }
    }

    private Vehicles searchVehicles(ConcurrentHashMap<Long, VehicleExt> vehicles, String keyword, String state, String entryTime, String position, String plateID) {
        Vehicles list = new Vehicles();
        boolean add; int added=0;
        for(VehicleExt v:vehicles.values()) {
            add = true;
            if(plateID != null && !plateID.isEmpty()) {
                if(v.getVehicle().getId().equals(plateID)) {
                    list.getVehicle().add(v.getVehicle());
                    return list;
                } else {
                    continue;
                }
            }

            if(keyword != null && !keyword.isEmpty()) {
                add = v.getVehicle().getId().contains(keyword);
            }
            if(!add) continue;

            if(state != null && !state.isEmpty()) {
                add = v.getVehicle().getState().equals(state);
            }
            if(!add) continue;

            if(entryTime != null && !entryTime.isEmpty()) {
                Date date = null;
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                    date = sdf.parse(entryTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
                if(date != null) cal.setTime(date);

                add = v.getVehicle().getEntryTime().toGregorianCalendar().compareTo(cal) == 0;
                if(!add) continue;
            }

            if(position != null && !position.isEmpty()) {
                add = v.getVehicle().getPosition().equals(position);
            }
            if(!add) continue;

            list.getVehicle().add(v.getVehicle());
            ++added;
        }
        return list;
    }

    public Vehicle getVehicle(long id, String plate) {
        if(id >= 0)
            if(vehicles.get(id) != null)
                return vehicles.get(id).getVehicle();
            else
                return null;
        if(plate != null && !plate.isEmpty()) {
            for(VehicleExt ve:vehicles.values()) {
                if(ve.getVehicle().getId().equals(plate))
                    return ve.getVehicle();
            }
        }
        return null;
    }

    public Vehicle updateVehicle(long id, String state, String move) {
        boolean good = false;
        VehicleExt vehicleExt = vehicles.get(id);
        if(vehicleExt == null)
            return null;

        if(state != null && !state.isEmpty()) {
            vehicleExt.getVehicle().setState(state);
            return vehicleExt.getVehicle();
        }

        if(move != null && !move.isEmpty()) {
            Vehicle vehicle = vehicleExt.getVehicle();
            Vehicle newVehicle = cloneVehicle(vehicle);

            if(placeExtById.containsKey(move))
                if(isReachable(placeExtByNode.get(placeExtById.get(vehicle.getPosition())).getPlace(), placeExtByNode.get(placeExtById.get(move)).getPlace())) {
                    newVehicle.setPosition(move);
                    newVehicle.setPositionNode(placeExtByNode.get(placeExtById.get(move)).getPlace().getSelf());

                    Set<List<String>> computedPath = computePath(newVehicle);

                    if (computedPath!= null) {
//                        vehicleExt.setPaths(convert(computedPath));
                        vehicleExt.setPaths(computedPath);
                        vehicleExt.setVehicle(newVehicle);
                        return newVehicle;
                    } else {
                        Vehicle refused = new Vehicle();
                        refused.setState("REFUSED");
                        return refused;
                    }
                }
        }

        return null;
    }

    private boolean isReachable(Place from, Place to) {
        return from.getConnections().contains(to.getId());
    }

    private Vehicle cloneVehicle(Vehicle vehicle) {
        Vehicle clone = new Vehicle();
        clone.setId(vehicle.getId());
        clone.setSelf(vehicle.getSelf());
        clone.setType(vehicle.getType());
        clone.setEntryTime(vehicle.getEntryTime());
        clone.setFrom(vehicle.getFrom());
        clone.setFromNode(vehicle.getFromNode());
        clone.setTo(vehicle.getTo());
        clone.setToNode(vehicle.getToNode());
        return clone;
    }

    public Vehicle deleteVehicle(long id, String outGate) {
        VehicleExt vehicle = vehicles.get(id);
        if (vehicle == null) {
            Vehicle refused = new Vehicle();
            refused.setState("NULL");
            return refused;
        }

        if(outGate != null && placeExtById.contains(outGate)) {
            PlaceExt gate = placeExtByNode.get(placeExtById.get(outGate));
            if(gate != null) {
                GateItem gateItem = gate.getPlace().getGate();
                if(!gateItem.value().isEmpty() && (gateItem.value().equals("OUT") || gateItem.value().equals("INOUT"))) {
                    Vehicle refused = new Vehicle();
                    refused.setState("REMOVED");
                    return refused;
                } else {
                    return generateErrorVehicle(2);
                }
            } else {
                return generateErrorVehicle(1);
            }
        } else {
            return generateErrorVehicle(1);
        }
    }
}
