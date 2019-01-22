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
    private String BASE;

    public VehClientPersonal newVehClient() {
        if(System.getProperty("it.polito.dp2.RNS.lab3.URL") == null) {
            this.BASE = "http://localhost:8080/RnsSystem/rest";
            System.setProperty("it.polito.dp2.RNS.lab3.URL", BASE);
        } else {
            BASE = System.getProperty("it.polito.dp2.RNS.lab3.URL");
        }

        VehClientPersonal monitor = new VehClientPersonal();
        return monitor;
    }

    @Override
    public List<String> enter(String plateId, VehicleType type, String inGate, String destination) throws ServiceException, UnknownPlaceException, WrongPlaceException, EntranceRefusedException {
        return null;
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
