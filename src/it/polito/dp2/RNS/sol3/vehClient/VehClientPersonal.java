package it.polito.dp2.RNS.sol3.vehClient;

import it.polito.dp2.RNS.VehicleState;
import it.polito.dp2.RNS.VehicleType;
import it.polito.dp2.RNS.lab3.*;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.Vehicle;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.VehicleResponse;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Copyright by Jacopx on 2019-01-21.
 */
public class VehClientPersonal implements it.polito.dp2.RNS.lab3.VehClient {
    private String BASE;
    private int PORT;
    private String ws;
    private String URL;
    private Vehicle myself;
    private ArrayList<String> myselfPath;

    public VehClientPersonal newVehClient() {
        if(System.getProperty("it.polito.dp2.RNS.lab3.URL") == null) {
            this.BASE = "http://localhost:";
            this.PORT = 8080;
            this.ws = "/RnsSystem/rest";
            this.URL = BASE + PORT + ws;
            System.setProperty("it.polito.dp2.RNS.lab3.URL", URL);
        } else {
            URL = System.getProperty("it.polito.dp2.RNS.lab3.URL");
        }

        VehClientPersonal monitor = new VehClientPersonal();
        return monitor;
    }

    @Override
    public List<String> enter(String plateId, VehicleType type, String inGate, String destination) throws ServiceException, UnknownPlaceException, WrongPlaceException, EntranceRefusedException {
        System.out.println("# enter #");
        Client client = ClientBuilder.newClient();
        System.out.println(plateId + " " + type.value() + " " + inGate + " " + destination);
        WebTarget target = client.target(URL).path("vehicles");

        Vehicle vehicle = new Vehicle();
        vehicle.setId(plateId);

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        DatatypeFactory datatypeFactory = null; XMLGregorianCalendar now = null;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
            now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }

        vehicle.setEntryTime(now);
        vehicle.setType(type.value());
        vehicle.setPosition(inGate);
        vehicle.setFrom(inGate);
        vehicle.setTo(destination);
        vehicle.setState("IN_TRANSIT");

        myself = vehicle;

        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(vehicle));

        System.out.println("enter CODE#" + response.getStatus());
        if(response.getStatus() == 201) {
            // CREATED
            VehicleResponse vehicleResponse = response.readEntity(new GenericType<VehicleResponse>(){});
            myself.setToNode(vehicleResponse.getToNode());
            myself.setFromNode(vehicleResponse.getFromNode());
            myselfPath = new ArrayList<>(vehicleResponse.getPath());
            return myselfPath;
        } else if(response.getStatus() == 400) {
            // BAD REQUEST
            return null;
        } else if(response.getStatus() == 406) {
            // WRONG GATE TYPE
            throw new UnknownPlaceException();
        } else if(response.getStatus() == 409) {
            // UNKNOWN PLACE
            throw new WrongPlaceException();
        } else if(response.getStatus() == 410) {
            // ENTRANCE REFUSED
            throw new EntranceRefusedException();
        } else {
            // OTHER REASONS
            throw new ServiceException();
        }
    }

    @Override
    public List<String> move(String newPlace) throws ServiceException, UnknownPlaceException, WrongPlaceException {
        System.out.println("# move #");
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL).path("vehicles").path(myself.getId());

        Response response;
        if(newPlace != null && !newPlace.isEmpty())
            response = target.queryParam("move", newPlace).request(MediaType.APPLICATION_JSON).put(Entity.json(myself));
        else
            return null;

        if(response.getStatus() == 200) {
            VehicleResponse vehicleUpdated = response.readEntity(new GenericType<VehicleResponse>(){});
            // MOVE
            myself.setPosition(newPlace);
            myself.setPositionNode(vehicleUpdated.getPositionNode());
            return new ArrayList<String>(vehicleUpdated.getPath());
        } else if(response.getStatus() == 406) {
            // WRONG GATE TYPE
            throw new UnknownPlaceException();
        } else if(response.getStatus() == 409) {
            // UNKNOWN PLACE
            throw new WrongPlaceException();
        } else {
            // OTHER REASONS
            throw new ServiceException();
        }
    }

    @Override
    public void changeState(VehicleState newState) throws ServiceException {
        System.out.println("# changeState #");
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL).path("vehicles").path(myself.getId());

        Response response;
        if(newState != null && !newState.value().isEmpty())
            response = target.queryParam("state", newState.value()).request(MediaType.APPLICATION_JSON).put(Entity.json(myself));
        else
            return;

        if(response.getStatus() == 200) {
            // CHANGED
            myself.setState(newState.value());
        } else if(response.getStatus() == 404) {
            // NOT FOUND
            throw new ServiceException();
        } else {
            // OTHER REASONS
            throw new ServiceException();
        }
    }

    @Override
    public void exit(String outGate) throws ServiceException, UnknownPlaceException, WrongPlaceException {
        System.out.println("# exit #");
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL).path("vehicles").path(myself.getId());

        Response response = target
                .queryParam("outGate", outGate)
                .request(MediaType.APPLICATION_JSON)
                .delete();

        if(response.getStatus() == 200) {
            // DELETED
            myself = null;
        } else if(response.getStatus() == 406) {
            // WRONG GATE TYPE
            throw new UnknownPlaceException();
        } else if(response.getStatus() == 409) {
            // UNKNOWN PLACE
            throw new WrongPlaceException();
        } else {
            // OTHER REASONS
            throw new ServiceException();
        }
    }
}
