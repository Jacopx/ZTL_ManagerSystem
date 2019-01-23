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
import java.util.stream.Collectors;

/**
 * Copyright by Jacopx on 2019-01-21.
 */
public class VehClientPersonal implements it.polito.dp2.RNS.lab3.VehClient {
    private String BASE;
    private int PORT;
    private String ws;
    private String URL;

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
        Client client = ClientBuilder.newClient();
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

        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(vehicle));

        if(response.getStatus() == 201) {
            // CREATED
            VehicleResponse vehicleResponse = response.readEntity(new GenericType<VehicleResponse>(){});
            return new ArrayList<>(vehicleResponse.getPath());
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
        return null;
    }

    @Override
    public void changeState(VehicleState newState) throws ServiceException {

    }

    @Override
    public void exit(String outGate) throws ServiceException, UnknownPlaceException, WrongPlaceException {

    }
}
