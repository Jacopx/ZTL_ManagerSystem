package it.polito.dp2.RNS.sol3.vehClient;

import it.polito.dp2.RNS.VehicleState;
import it.polito.dp2.RNS.VehicleType;
import it.polito.dp2.RNS.lab3.*;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.ShortPaths;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.SuggPath;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Vehicle;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright by Jacopx on 2019-01-21.
 */
public class VehClientPersonal implements it.polito.dp2.RNS.lab3.VehClient {
    private String URL;

    public VehClientPersonal newVehClient() {
        if(System.getProperty("it.polito.dp2.RNS.lab3.URL") == null) {
            this.URL = "http://localhost:8080/RnsSystem/rest";
            System.setProperty("it.polito.dp2.RNS.lab3.URL", URL);
        }

        VehClientPersonal monitor = new VehClientPersonal();
        return monitor;
    }

    @Override
    public List<String> enter(String plateId, VehicleType type, String inGate, String destination) throws ServiceException, UnknownPlaceException, WrongPlaceException, EntranceRefusedException {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL).path("vehicles");

        Vehicle vehicle = new Vehicle();
        vehicle.setId(plateId);
        vehicle.setType(type.value());
        vehicle.setPosition(inGate);
        vehicle.setFrom(inGate);
        vehicle.setTo(destination);
        vehicle.setState("IN_TRANSIT");

        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(vehicle));
        if(response.getStatus() == 201) {
            Vehicle vehicleResponse = response.readEntity(new GenericType<Vehicle>(){});
            List<String> returnPath = new ArrayList<>();
            returnPath.add(vehicleResponse.getFromNode());
            for(ShortPaths sp:vehicleResponse.getShortPaths()) {
                for(SuggPath sup:sp.getSuggPath()) {
                    returnPath.addAll(sup.getRelation());
                }
            }
            return returnPath;
        } else if(response.getStatus() == 406) {
            throw new UnknownPlaceException();
        } else if(response.getStatus() == 409) {
            throw new WrongPlaceException();
        } else if(response.getStatus() == 410) {
            throw new EntranceRefusedException();
        } else {
            throw new ServiceException("Service not available!");
        }
    }

    @Override
    public List<String> move(String newPlace) throws ServiceException, UnknownPlaceException, WrongPlaceException {
        return null;
    }

    @Override
    public void changeState(VehicleState newState) throws ServiceException {

    }

    @Override
    public void exit(String outGate) throws ServiceException, UnknownPlaceException, WrongPlaceException {

    }
}
