package it.polito.dp2.RNS.sol3.admClient;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.RnsReaderFactory;
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
public class AdmClientPersonal implements it.polito.dp2.RNS.lab3.AdmClient {
    private String BASE;
    private int PORT;
    private String ws;
    private String URL;
    private RnsReader rnsReader;

    public AdmClientPersonal newAdmClient() {
        if(System.getProperty("it.polito.dp2.RNS.lab3.URL") == null) {
            this.BASE = "http://localhost:";
            this.PORT = 8080;
            this.ws = "/RnsSystem/rest";
            this.URL = BASE + PORT + ws;
            System.setProperty("it.polito.dp2.RNS.lab3.URL", URL);
        } else {
            URL = System.getProperty("it.polito.dp2.RNS.lab3.URL");
        }

        try {
            rnsReader = RnsReaderFactory.newInstance().newRnsReader();
        } catch (RnsReaderException e) {
            e.printStackTrace();
        }
        AdmClientPersonal monitor = new AdmClientPersonal();

        return monitor;
    }

    @Override
    public Set<VehicleReader> getUpdatedVehicles(String place) throws ServiceException {
        System.out.println("# Plural #");
        Client client = ClientBuilder.newClient();
        System.out.println("URL: " + URL);
        WebTarget target = client.target(URL).path("vehicles");

        System.out.println("Plural ID: " + place);

        Response response = null;
        if(place != null && !place.isEmpty())
            response = target.queryParam("admin", 1).queryParam("position", place).request(MediaType.APPLICATION_JSON).get();
        else
            response = target.queryParam("admin", 1).request(MediaType.APPLICATION_JSON).get();

        System.out.println("Plural CODE#" + response.getStatus());
        if(response.getStatus() == 200) {
            Set<VehicleReader> vehicleReaderSet = new HashSet<>();
            Vehicles vehicleResponse = response.readEntity(new GenericType<Vehicles>(){});
            for(Vehicle v:vehicleResponse.getVehicle()) {
               vehicleReaderSet.add(new VehicleReaderPersonal(v.getId(), v.getEntryTime().toGregorianCalendar(), v.getType(), v.getState()));
            }
            return vehicleReaderSet;
        } else if(response.getStatus() >= 500) {
            return new HashSet<>();
        } else if(response.getStatus() >= 500) {
            throw new ServiceException();
        }
        return null;
    }

    @Override
    public VehicleReader getUpdatedVehicle(String id) throws ServiceException {
        System.out.println("# Singular #");
        Client client = ClientBuilder.newClient();
        System.out.println("URL: " + URL);
        WebTarget target = client.target(URL).path("vehicles");

        System.out.println("Single ID: " + id);

        Response response = null;
        if(id != null && !id.isEmpty())
            response = target.queryParam("admin", 1).queryParam("plateID", id).request(MediaType.APPLICATION_JSON).get();
        else
            return null;

        System.out.println("Single CODE#" + response.getStatus());
        if(response.getStatus() == 200) {
            Vehicles vehicleResponse = response.readEntity(new GenericType<Vehicles>(){});
            for(Vehicle v:vehicleResponse.getVehicle()) {
                return new VehicleReaderPersonal(v.getId(), v.getEntryTime().toGregorianCalendar(), v.getType(), v.getState());
            }
        } else if(response.getStatus() == 400) {
            return null;
        } else if(response.getStatus() >= 500) {
            throw new ServiceException();
        }
        return null;
    }

    @Override
    public Set<PlaceReader> getPlaces(String s) {
        return rnsReader.getPlaces(s);
    }

    @Override
    public PlaceReader getPlace(String s) {
        return rnsReader.getPlace(s);
    }

    @Override
    public Set<GateReader> getGates(GateType gateType) {
        return rnsReader.getGates(gateType);
    }

    @Override
    public Set<RoadSegmentReader> getRoadSegments(String s) {
        return rnsReader.getRoadSegments(s);
    }

    @Override
    public Set<ParkingAreaReader> getParkingAreas(Set<String> set) {
        return rnsReader.getParkingAreas(set);
    }

    @Override
    public Set<ConnectionReader> getConnections() {
        return rnsReader.getConnections();
    }

    @Override
    public Set<VehicleReader> getVehicles(Calendar calendar, Set<VehicleType> set, VehicleState vehicleState) {
        return null;
    }

    @Override
    public VehicleReader getVehicle(String s) {
        return null;
    }
}
