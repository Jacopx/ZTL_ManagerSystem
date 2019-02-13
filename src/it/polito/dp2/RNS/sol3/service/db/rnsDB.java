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
import java.math.BigInteger;
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
    private static long lastConn=0;
    private static String URL;

    PathFinder pff = null;
    RnsReader monitor = null;

    private ConcurrentHashMap<String, PlaceExt> placeExtById;
    private ConcurrentHashMap<Long, Connection> connectionById;
    private ConcurrentHashMap<String, PlaceExt> parkings;
    private ConcurrentHashMap<String, PlaceExt> segments;
    private ConcurrentHashMap<String, PlaceExt> gates;
    private ConcurrentHashMap<String, VehicleExt> vehicles;

    public static rnsDB getRnsDB() {
        return rnsDB;
    }

    public static synchronized long getNextConn() {
        return ++lastConn;
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
            String id = gateReader.getId();
//            newGate.setSelf(URL + "/places/" + id);
            newGate.setSelf(id);

            createPlace(id, newGate);
            gates.putIfAbsent(id, placeExtById.get(id));
        }

        // PLACE PARKING AREA
        for (ParkingAreaReader parkingAreaReader : monitor.getParkingAreas(null)) {

            ParkingItem park = new ParkingItem();
            park.getServices().addAll(parkingAreaReader.getServices());

            Place newPark = new Place();
            newPark.setCapacity(parkingAreaReader.getCapacity());
            newPark.setId(parkingAreaReader.getId());
            newPark.setParking(park);
            String id = parkingAreaReader.getId();
//            newPark.setSelf(URL + "/places/" + id);
            newPark.setSelf(id);

            createPlace(id, newPark);
            parkings.putIfAbsent(id, placeExtById.get(id));
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
            String id = roadSegmentReader.getId();
//            newRoadSeg.setSelf(URL + "/places/" + id);
            newRoadSeg.setSelf(id);

            createPlace(id, newRoadSeg);
            segments.putIfAbsent(id, placeExtById.get(id));
        }

        // CONNECTIONS
        for(ConnectionReader connectionReader:monitor.getConnections()) {
            Connection newConnection = new Connection();
            long id = getNextConn();

//            newConnection.setSelf(URL + "/connections/" + id);
            newConnection.setSelf(String.valueOf(id));
            PlaceExt placeFrom = placeExtById.get(connectionReader.getFrom().getId());
            newConnection.setFrom(placeFrom.getPlace().getId());
            newConnection.setFromNode(placeFrom.getPlace().getSelf());
            placeFrom.addConnections(id, newConnection);

            PlaceExt placeTo = placeExtById.get(connectionReader.getTo().getId());
            newConnection.setTo(placeTo.getPlace().getId());
            newConnection.setToNode(placeTo.getPlace().getSelf());
            placeTo.addConnectedBy(id, newConnection);

            connectionById.putIfAbsent(id, newConnection);
        }
    }

    // Searching places by different place type
    public Places getPlaces(SearchPlaces scope, String keyword, String placeID) {
        switch (scope) {
            case SEGMENT: {
                return searchPlaces(segments, keyword, placeID);
            } case PARKING: {
                return searchPlaces(parkings, keyword, placeID);
            } case GATE: {
                return searchPlaces(gates, keyword, placeID);
            } case ALL: default: {
                return searchPlaces(placeExtById, keyword, placeID);
            }
        }
    }

    // Searching function for places
    private Places searchPlaces(ConcurrentHashMap<String, PlaceExt> place, String keyword, String placeID) {
        Places list = new Places();
        list.setPage(BigInteger.ONE);
        list.setTotalPages(BigInteger.ONE);

        for(PlaceExt p:place.values()) {
            if(placeID != null && !placeID.isEmpty()) {
                if (p.getPlace().getId().equals(placeID)) {
                    list.getPlace().add(p.getPlace());
                    break;
                }
            } else if (keyword != null && !keyword.isEmpty()) {
                if (p.getPlace().getId().contains(keyword))
                    list.getPlace().add(p.getPlace());
            }  else {
                list.getPlace().add(p.getPlace());
            }
        }
        return list;
    }

    // Get back single place
    public Place getPlace(String placeID) {
        return placeExtById.get(placeID).getPlace();
    }

    // Adding a new created place
    public Place createPlace(String placeID, Place place) {
        PlaceExt placeExt = new PlaceExt(placeID, place);
        if (placeExtById.putIfAbsent(place.getId(), placeExt)==null) {
            return place;
        } else
            return null;
    }

    // Get back all the connections
    public Connections getConnections() {
        Connections list = new Connections();
        list.setPage(BigInteger.ONE);
        list.setTotalPages(BigInteger.ONE);
        list.getConnection().addAll(connectionById.values());
        return list;
    }

    // Get single connection
    public Connection getConnection(long id) {
        return connectionById.get(id);
    }

    // Adding a vehicle to the system
    public Vehicle addVehicle(Vehicle vehicle) {

        if(vehicles.contains(vehicle.getId())) {
            System.out.println("DUPLICATE - PRESENT");
            Vehicle refused = new Vehicle();
            refused.setState("DUPLICATE");
            return refused;
        }

        String temp;
        // TO CHECK
        if(vehicle.getTo() != null && placeExtById.containsKey(vehicle.getTo())) {
            PlaceExt placeExt = placeExtById.get(vehicle.getTo());
            if((temp = placeExt.getPlace().getSelf()) != null) {
                vehicle.setToNode(temp);
            } else {
                return generateErrorVehicle(1);
            }
        } else {
            return generateErrorVehicle(1);
        }

        // POSITION CHECK
        if(vehicle.getPosition() != null && placeExtById.containsKey(vehicle.getPosition())) {
            PlaceExt placeExt = placeExtById.get(vehicle.getPosition());
            if((temp = placeExt.getPlace().getSelf()) != null) {
                vehicle.setPositionNode(temp);
            } else {
                return generateErrorVehicle(1);
            }
        } else {
            return generateErrorVehicle(1);
        }

        // FROM CHECK
        if(vehicle.getFrom() != null && placeExtById.containsKey(vehicle.getFrom())) {
            PlaceExt placeExt = placeExtById.get(vehicle.getFrom());
            if((temp = placeExt.getPlace().getSelf()) != null) {
                vehicle.setFromNode(temp);

                GateItem type = placeExt.getPlace().getGate();
                if(type != null) {
                    if(type.value().equals("OUT")) {
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

        Set<List<String>> computedPath = computePath(vehicle);
        if(computedPath != null) {
            if(vehicles.contains(vehicle.getId())) {
                VehicleExt vehicleExt = vehicles.get(vehicle.getId());
                vehicleExt.setPaths(computedPath);
            } else {
                VehicleExt vehicleExt = new VehicleExt(vehicle.getId(), vehicle);
                vehicleExt.setPaths(computedPath);
                vehicles.put(vehicle.getId(), vehicleExt);
            }
            return vehicle;
        } else {
            System.out.println("REFUSED");
            Vehicle refused = new Vehicle();
            refused.setState("REFUSED");
            return refused;
        }
    }

    // Generating an error for different case
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

    // Calling the service to computing the path
    private Set<List<String>> computePath(Vehicle vehicle) {
        try {
            Set<List<String>> computed = pff.findShortestPaths(vehicle.getFrom(), vehicle.getTo(), 999);
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

    // Calling the searching functions by vehicles type discrimination
    public Vehicles getVehicles(SearchVehicles scope, String keyword, String state, String entryTime, String position) {
        if(vehicles.isEmpty()) return null;
        switch (scope) {
            case CAR: {
                return searchVehicles(vehicles, keyword, state, entryTime, position);
            }
            case TRUCK: {
                return searchVehicles(vehicles, keyword, state, entryTime, position);
            }
            case CARAVAN: {
                return searchVehicles(vehicles, keyword, state, entryTime, position);
            }
            case SHUTTLE: {
                return searchVehicles(vehicles, keyword, state, entryTime, position);
            }
            case ALL: default: {
                return searchVehicles(vehicles, keyword, state, entryTime, position);
            }
        }
    }

    // Proper function for searching vehicles
    private Vehicles searchVehicles(ConcurrentHashMap<String, VehicleExt> vehicles, String keyword, String state, String entryTime, String position) {
        Vehicles list = new Vehicles();
        list.setPage(BigInteger.ONE);
        list.setTotalPages(BigInteger.ONE);
        boolean add; int added=0;
        for(VehicleExt v:vehicles.values()) {
            add = true;

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

    // Get single vehicle
    public Vehicle getVehicle(String plate) {
        if(plate != null && !plate.isEmpty()) {
            for(VehicleExt ve:vehicles.values()) {
                if(ve.getVehicle().getId().equals(plate))
                    return ve.getVehicle();
            }
        }
        return null;
    }

    // Update an existent vehicle by changing the state or making a move
    public Vehicle updateVehicle(String plateID, String state, String move) {
        System.out.println("# update Vehicle #");

        VehicleExt vehicleExt = vehicles.get(plateID);
        if(vehicleExt == null)
            return null;

        if(state != null && !state.isEmpty()) {
            vehicleExt.getVehicle().setState(state);
            return vehicleExt.getVehicle();
        }

        if(move != null && !move.isEmpty()) {
            if(placeExtById.get(move) != null) {
                Vehicle vehicle = vehicleExt.getVehicle();
                vehicle.setPosition(move);

                if(!vehicle.getShortPaths().contains(move)) {
                    if(isReachable(placeExtById.get(vehicle.getPosition()).getPlace(), placeExtById.get(move).getPlace())) {
                        vehicle.setPositionNode(placeExtById.get(move).getPlace().getSelf());

                        Set<List<String>> computedPath = computePath(vehicle);

                        if (computedPath!= null) {
                            vehicleExt.setPaths(computedPath);
                            return vehicle;
                        } else {
                            Vehicle refused = new Vehicle();
                            refused.setState("REFUSED");
                            return refused;
                        }
                    }
                }
                vehicle.getShortPaths().clear();
                return vehicle;
            } else {
                Vehicle refused = new Vehicle();
                refused.setState("WRONGPLACE");
                return refused;
            }
        }
        return null;
    }

    private boolean isReachable(Place from, Place to) {
        return from.getConnections().contains(to.getId());
    }

    // Removing a vehicle from the system
    public Vehicle deleteVehicle(String plateID, String outGate) {
        System.out.println("# DELETE #");
        VehicleExt vehicle = vehicles.get(plateID);
        if (vehicle == null) {
            Vehicle refused = new Vehicle();
            refused.setState("NULL");
            return refused;
        }

        if(outGate != null) {
            PlaceExt gate = placeExtById.get(outGate);
            if(gate != null) {
                GateItem gateItem = gate.getPlace().getGate();
                if(!gateItem.value().isEmpty() && !gateItem.value().equals("IN")) {
                    vehicles.remove(plateID);
                    Vehicle removed = new Vehicle();
                    removed.setState("REMOVED");
                    System.out.println("REMOVED");
                    return removed;
                } else {
                    System.out.println("NOT DELETE 1");
                    return generateErrorVehicle(2);
                }
            } else {
                System.out.println("NOT DELETE 2");
                return generateErrorVehicle(1);
            }
        } else {
            System.out.println("NOT DELETE 3");
            return generateErrorVehicle(1);
        }
    }
}