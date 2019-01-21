package it.polito.dp2.RNS.sol3.admClient;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.lab3.ServiceException;
import it.polito.dp2.RNS.sol1.PlaceReaderPersonal;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Place;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Places;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.PlacesRequest;

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

    public AdmClient() {
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

        PlacesRequest placesRequest = new PlacesRequest();
        placesRequest.setAdmin(1);
        placesRequest.setKeyword(s);

        Response response = target.queryParam("keyword", s).request(MediaType.APPLICATION_JSON).get();
        if(response.getStatus() == 200) {
            Places places = response.readEntity(new GenericType<Places>(){});
            Set<PlaceReader> placesSet = new HashSet<>();

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
