package it.polito.dp2.RNS.sol3.admClient;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.lab3.ServiceException;
import it.polito.dp2.RNS.sol1.*;
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

/**
 * Copyright by Jacopx on 2019-01-21.
 */
public class AdmClient implements it.polito.dp2.RNS.lab3.AdmClient {
    String URL;

    public void newAdmClient() {
        if(System.getProperty("URL") == null) {
            URL = "http://localhost:8080/RnsSystem/rest";
            System.setProperty("URL", URL);
        }
    }

    @Override
    public Set<VehicleReader> getUpdatedVehicles(String place) throws ServiceException {
        return null;
    }

    @Override
    public VehicleReader getUpdatedVehicle(String id) throws ServiceException {
        return null;
    }

    @Override
    public Set<PlaceReader> getPlaces(String s) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL).path("places");

        Response response = target.queryParam("keyword", s).request(MediaType.APPLICATION_JSON).get();
        if(response.getStatus() == 200) {
            Places places = response.readEntity(new GenericType<Places>(){});
            Set<PlaceReader> placesSet = new HashSet<>();

            //@TODO: Missing parameters
            for(Place p:places.getPlace()) {
                placesSet.add(new PlaceReaderPersonal(p.getCapacity(), p.getId(), null));
            }

            return placesSet;
        }
        return null;
    }

    @Override
    public PlaceReader getPlace(String s) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL).path("places");

        Response response = target.queryParam("placeID", s).request(MediaType.APPLICATION_JSON).get();
        if(response.getStatus() == 200) {
            Place place = response.readEntity(new GenericType<Place>(){});
            return new PlaceReaderPersonal(place.getCapacity(), place.getId(), null);
        }
        return null;
    }

    @Override
    public Set<GateReader> getGates(GateType gateType) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL).path("places");

        Response response = target.queryParam("admin", 1)
                .queryParam("type", gateType.value())
                .request(MediaType.APPLICATION_JSON).get();

        if(response.getStatus() == 200) {
            Places places = response.readEntity(new GenericType<Places>(){});
            Set<GateReader> placesSet = new HashSet<>();

            for(Place p:places.getPlace()) {
                placesSet.add(new GateReaderPersonal(p.getCapacity(), p.getId(), gateType.value()));
            }

            return placesSet;
        }
        return null;
    }

    @Override
    public Set<RoadSegmentReader> getRoadSegments(String s) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL).path("places");

        Response response = target.queryParam("admin", 1)
                .queryParam("type", "segment")
                .queryParam("keyword", s)
                .request(MediaType.APPLICATION_JSON).get();

        if(response.getStatus() == 200) {
            Places places = response.readEntity(new GenericType<Places>(){});
            Set<RoadSegmentReader> placesSet = new HashSet<>();

            for(Place p:places.getPlace()) {
                placesSet.add(new RoadSegmentReaderPersonal(p.getCapacity(), p.getId(), p.getSegment().getName(), p.getSegment().getRoadName()));
            }

            return placesSet;
        }
        return null;
    }

    @Override
    public Set<ParkingAreaReader> getParkingAreas(Set<String> set) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL).path("places");

        Response response = target.queryParam("admin", 1)
                .queryParam("type", "parking")
                .request(MediaType.APPLICATION_JSON).get();

        if(response.getStatus() == 200) {
            Places places = response.readEntity(new GenericType<Places>(){});
            Set<ParkingAreaReader> placesSet = new HashSet<>();

            for(Place p:places.getPlace()) {
                placesSet.add(new ParkingAreaReaderPersonal(p.getCapacity(), p.getId(), p.getParking().getServices()));
            }

            return placesSet;
        }
        return null;
    }

    @Override
    public Set<ConnectionReader> getConnections() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL).path("connections");

        Response response = target.request(MediaType.APPLICATION_JSON).get();

        if(response.getStatus() == 200) {
            Connections conn = response.readEntity(new GenericType<Connections>(){});
            Set<ConnectionReader> connSet = new HashSet<>();

            for(Connection c: conn.getConnection()) {
                connSet.add(new ConnectionReaderPersonal(getPlace(c.getFrom()), getPlace(c.getTo())));
            }

            return connSet;
        }
        return null;
    }

    @Override
    public Set<VehicleReader> getVehicles(Calendar calendar, Set<VehicleType> set, VehicleState vehicleState) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL).path("vehicles");

        Response response = target.queryParam("admin", 1)
                .queryParam("type", set)
                .queryParam("state", vehicleState)
                .queryParam("entrytime", calendar)
                .request(MediaType.APPLICATION_JSON).get();

        if(response.getStatus() == 200) {
            Vehicles vehicles = response.readEntity(new GenericType<Vehicles>(){});
            Set<VehicleReader> vehiclesSet = new HashSet<>();

            for(Vehicle v:vehicles.getVehicle()) {
                vehiclesSet.add(new VehicleReaderPersonal(v.getId(), v.getEntryTime().toGregorianCalendar(), v.getType(), v.getState()));
            }

            return vehiclesSet;
        }
        return null;
    }

    @Override
    public VehicleReader getVehicle(String s) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL).path("vehicles");

        Response response = target.queryParam("placeID", s).request(MediaType.APPLICATION_JSON).get();
        if(response.getStatus() == 200) {
            Vehicle vehicle = response.readEntity(new GenericType<Vehicle>(){});
            return new VehicleReaderPersonal(vehicle.getId(), vehicle.getEntryTime().toGregorianCalendar(), vehicle.getType(), vehicle.getState());
        }
        return null;
    }
}