package it.polito.dp2.RNS.sol3.admClient;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.RnsReaderFactory;
import it.polito.dp2.RNS.lab3.ServiceException;
import it.polito.dp2.RNS.sol1.*;
import it.polito.dp2.RNS.sol1.jaxb.Roads;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Copyright by Jacopx on 2019-01-21.
 */
public class AdmClientPersonal implements it.polito.dp2.RNS.lab3.AdmClient {
    private String BASE;
    private RnsReader rnsReader;
    private Set<PlaceReader> places;
    private Set<GateReader> gates;
    private Set<ParkingAreaReader> parkings;
    private Set<RoadSegmentReader> segments;
    private Set<ConnectionReader> connections;
    private Set<VehicleReader> vehicles;

    public AdmClientPersonal newAdmClient() {
        if(System.getProperty("it.polito.dp2.RNS.lab3.URL") == null) {
            this.BASE = "http://localhost:8080/RnsSystem/rest";
            System.setProperty("it.polito.dp2.RNS.lab3.URL", BASE);
        } else {
            BASE = System.getProperty("it.polito.dp2.RNS.lab3.URL");
        }

        try {
            rnsReader = RnsReaderFactory.newInstance().newRnsReader();
        } catch (RnsReaderException e) {
            e.printStackTrace();
        }
        AdmClientPersonal monitor = new AdmClientPersonal();

        places = new HashSet<>();
        gates = new HashSet<>();
        parkings = new HashSet<>();
        segments = new HashSet<>();
        connections = new HashSet<>();
        vehicles = new HashSet<>();

        loadDB();

        return monitor;
    }

    private void loadDB() {
        places = rnsReader.getPlaces(null);
        System.out.println("Places#"+places.size());
        gates = rnsReader.getGates(null);
        System.out.println("gates#"+gates.size());
        parkings = rnsReader.getParkingAreas(null);
        System.out.println("parkings#"+parkings.size());
        segments = rnsReader.getRoadSegments(null);
        System.out.println("segments#"+segments.size());
        connections = rnsReader.getConnections();
        System.out.println("connections#"+connections.size());
        vehicles = rnsReader.getVehicles(null, null, null);
        System.out.println("vehicles#"+vehicles.size());
    }

    @Override
    public Set<VehicleReader> getUpdatedVehicles(String place) throws ServiceException {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(BASE).path("vehicles");

        Response response = null;
        if(place != null && !place.isEmpty())
            response = target.queryParam("position", place).request(MediaType.APPLICATION_JSON).get();
        else
            return null;

        if(response.getStatus() == 200) {
            Set<VehicleReader> vehicleReaderSet = new HashSet<>();
            Vehicles vehicleResponse = response.readEntity(new GenericType<Vehicles>(){});
            for(Vehicle v:vehicleResponse.getVehicle()) {
               vehicleReaderSet.add(new VehicleReaderPersonal(v.getId(), v.getEntryTime().toGregorianCalendar(), v.getType(), v.getState()));
            }
            return vehicleReaderSet;
        } else {
            throw new ServiceException();
        }
    }

    @Override
    public VehicleReader getUpdatedVehicle(String id) throws ServiceException {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(BASE).path("vehicles");

        Response response = null;
        if(id != null && !id.isEmpty())
            response = target.queryParam("plateID", id).request(MediaType.APPLICATION_JSON).get();
        else
            return null;

        if(response.getStatus() == 200) {
            Vehicles vehicleResponse = response.readEntity(new GenericType<Vehicles>(){});
            for(Vehicle v:vehicleResponse.getVehicle()) {
                return new VehicleReaderPersonal(v.getId(), v.getEntryTime().toGregorianCalendar(), v.getType(), v.getState());
            }
        } else {
            throw new ServiceException();
        }
        return null;
    }

    @Override
    public Set<PlaceReader> getPlaces(String s) {
        return places.stream().filter(p -> s == null || p.getId().equals(s)).collect(Collectors.toSet());
    }

    @Override
    public PlaceReader getPlace(String s) {
        return places.stream().filter(p -> s == null || p.getId().equals(s)).findFirst().get();
    }

    @Override
    public Set<GateReader> getGates(GateType gateType) {
        return gates.stream().filter(g -> gateType == null || g.getType() == gateType).collect(Collectors.toSet());
    }

    @Override
    public Set<RoadSegmentReader> getRoadSegments(String s) {
        return segments.stream().filter(seg -> s == null || seg.getName().equals(s)).collect(Collectors.toSet());
    }

    @Override
    public Set<ParkingAreaReader> getParkingAreas(Set<String> set) {
        return parkings.stream().filter(park -> set == null || park.getServices().containsAll(set)).collect(Collectors.toSet());
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
}
