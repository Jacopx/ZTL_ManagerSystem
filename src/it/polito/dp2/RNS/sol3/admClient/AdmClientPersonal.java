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
import java.util.stream.Collectors;

/**
 * Copyright by Jacopx on 2019-01-21.
 */
public class AdmClientPersonal implements it.polito.dp2.RNS.lab3.AdmClient {
    private String BASE;

    public AdmClientPersonal newAdmClient() {
        if(System.getProperty("it.polito.dp2.RNS.lab3.URL") == null) {
            this.BASE = "http://localhost:8080/RnsSystem/rest";
            System.setProperty("it.polito.dp2.RNS.lab3.URL", BASE);
        } else {
            BASE = System.getProperty("it.polito.dp2.RNS.lab3.URL");
        }

        AdmClientPersonal monitor = new AdmClientPersonal();
        return monitor;
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
        return null;
    }

    @Override
    public PlaceReader getPlace(String s) {
        return null;
    }

    @Override
    public Set<GateReader> getGates(GateType gateType) {
        return null;
    }

    @Override
    public Set<RoadSegmentReader> getRoadSegments(String s) {
        return null;
    }

    @Override
    public Set<ParkingAreaReader> getParkingAreas(Set<String> set) {
        return null;
    }

    @Override
    public Set<ConnectionReader> getConnections() {
        return null;
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
