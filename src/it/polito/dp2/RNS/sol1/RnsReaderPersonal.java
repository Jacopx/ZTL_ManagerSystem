package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.sol1.jaxb.PlaceItem;
import it.polito.dp2.RNS.sol1.jaxb.Roads;
import it.polito.dp2.RNS.sol1.jaxb.VehicleItem;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Copyright by Jacopx on 23/11/2018.
 */
public class RnsReaderPersonal implements RnsReader {
    private HashSet<PlaceReader> places;
    private HashSet<GateReader> gates;
    private HashSet<ParkingAreaReader> parkings;
    private HashMap<String, Roads.Road> roads;
    private HashSet<RoadSegmentReader> segments;
    private HashSet<ConnectionReader> connections;
    private HashSet<VehicleReader> vehicles;

    public RnsReaderPersonal() {
        places = new HashSet<>();
        gates = new HashSet<>();
        parkings = new HashSet<>();
        roads = new HashMap<>();
        segments = new HashSet<>();
        connections = new HashSet<>();
        vehicles = new HashSet<>();
    }

    @Override
    public Set<PlaceReader> getPlaces(String s) {
        if(s == null || s.isEmpty())
            return places;
        return places.stream().filter(p -> p.getId().contains(s)).collect(Collectors.toSet());
    }

    @Override
    public PlaceReader getPlace(String s) {
        return places.stream().filter(g -> s.equals(g.getId())).findFirst().orElse(null);
    }

    @Override
    public Set<GateReader> getGates(GateType gateType) {
        if(gateType == null)
            return gates;
        return gates.stream().filter(g -> gateType.equals(g.getType())).collect(Collectors.toSet());
    }

    public void addGate(PlaceItem gate) {
        GateReaderPersonal newGate = new GateReaderPersonal(gate.getCapacity().intValue(), gate.getId(), gate.getGate().value());
        places.add(newGate);
        gates.add(newGate);
    }

    @Override
    public Set<RoadSegmentReader> getRoadSegments(String s) {
        if(s == null || s.isEmpty())
            return segments;
        return segments.stream().filter(seg -> s.equals(seg.getRoadName())).collect(Collectors.toSet());
    }

    @Override
    public Set<ParkingAreaReader> getParkingAreas(Set<String> set) {
        if(set == null)
            return parkings;
        return parkings.stream().filter(p -> p.getServices().containsAll(set)).collect(Collectors.toSet());
    }

    public void addParking(PlaceItem parking) {
        ParkingAreaReaderPersonal newPark = new ParkingAreaReaderPersonal(parking.getCapacity().intValue(), parking.getId(), parking.getParkingArea().getServices());
        places.add(newPark);
        parkings.add(newPark);
    }

    @Override
    public Set<ConnectionReader> getConnections() {
        return connections;
    }

    @Override
    public Set<VehicleReader> getVehicles(Calendar calendar, Set<VehicleType> set, VehicleState vehicleState) {
        if (calendar == null && set == null && vehicleState == null)
            return vehicles;

        return vehicles.stream()
                .filter(v -> set == null || set.contains(v.getType()))
                .filter(v -> vehicleState == null || vehicleState.equals(v.getState()))
                .filter(v -> calendar == null || calendar.equals(v.getEntryTime()))
                .collect(Collectors.toSet());
    }

    @Override
    public VehicleReader getVehicle(String s) {
        return vehicles.stream().filter(v -> s.equals(v.getId())).findFirst().orElse(null);
    }

    public void addVehicle(VehicleItem vehicle) {
        vehicles.add(new VehicleReaderPersonal(vehicle.getId(), vehicle.getEntryTime().toGregorianCalendar(), vehicle.getType(), vehicle.getState()));
    }

    public void matchPlace(VehicleItem vehicle) {
        PlaceReader placeFrom = places.stream().filter(p -> vehicle.getComesFrom().equals(p.getId())).findFirst().orElse(null);
        PlaceReader placeTo = places.stream().filter(p -> vehicle.getDirectTo().equals(p.getId())).findFirst().orElse(null);
        PlaceReader position = places.stream().filter(p -> vehicle.getPosition().equals(p.getId())).findFirst().orElse(null);
        VehicleReaderPersonal vehicleSearched = (VehicleReaderPersonal) vehicles.stream().filter(v -> vehicle.getId().equals(v.getId())).findFirst().orElse(null);
        assert vehicleSearched != null;
        vehicleSearched.addPlace(placeTo, placeFrom, position);
    }

    public void addRoad(String id, List<PlaceItem> segmentPlaces) {
        Roads.Road road = new Roads.Road();
        road.setId(id);
        for(PlaceItem placeItem: segmentPlaces) {
            RoadSegmentReaderPersonal newRoSeg = new RoadSegmentReaderPersonal(placeItem.getCapacity().intValue(), placeItem.getId(), placeItem.getRoadSegment().getName(), id);
            places.add(newRoSeg);
            segments.add(newRoSeg);
        }
        roads.putIfAbsent(id, road);
    }

    public void addConnection(String from, String to) {
        PlaceReader placeFrom = places.stream().filter(p -> from.equals(p.getId())).findFirst().orElse(null);
        PlaceReader placeTo = places.stream().filter(p -> to.equals(p.getId())).findFirst().orElse(null);
        assert placeFrom != null;
        placeFrom.getNextPlaces().add(placeTo);
        connections.add(new ConnectionReaderPersonal(placeFrom, placeTo));
    }
}
