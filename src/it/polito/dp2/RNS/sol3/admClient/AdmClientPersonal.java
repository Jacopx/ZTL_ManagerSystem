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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Copyright by Jacopx on 2019-01-21.
 */
public class AdmClientPersonal implements it.polito.dp2.RNS.lab3.AdmClient {
    private String BASE;
    private RnsReader rnsReader;

    public AdmClientPersonal newAdmClient() {
        if(System.getProperty("it.polito.dp2.RNS.lab3.URL") == null) {
            this.BASE = "http://localhost:8080/RnsSystem/rest";
            System.setProperty("it.polito.dp2.RNS.lab3.URL", BASE);
        } else {
            BASE = System.getProperty("it.polito.dp2.RNS.lab3.URL");
        }

        rnsReader = new RnsReaderPersonal();
        AdmClientPersonal monitor = new AdmClientPersonal();
        return monitor;
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
