package it.polito.dp2.RNS.sol1;


import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.sol1.jaxb.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.GregorianCalendar;

/**
 * Copyright by Jacopx on 20/11/2018.
 */
public class RnsInfoSerializer {
    private static RnsReader monitor;

    public static void main(String[] args) {

        try {
            monitor = RnsReaderFactory.newInstance().newRnsReader();
        } catch (RnsReaderException e) {
            e.printStackTrace();
        }

        int nVehicle = 0, nGate = 0, nParking = 0, nRoad = 0, nSegment = 0, nConn = 0;

        ObjectFactory objectFactory = new ObjectFactory();
        Rns rns = new ObjectFactory().createRns();

        // PLACE GATE
        for (GateReader gateReader : monitor.getGates(null)) {
            EntityItem entityItem = objectFactory.createEntityItem();
            PlaceItem place = objectFactory.createPlaceItem();
            GateItem gate = GateItem.valueOf(gateReader.getType().value());

            place.setId(gateReader.getId());
            place.setCapacity(BigInteger.valueOf(gateReader.getCapacity()));
            place.setGate(gate);

            entityItem.setPlace(place);
            rns.getItem().add(entityItem);
            ++nGate;
        }
        // PLACE PARKING AREA
        for (ParkingAreaReader parkingAreaReader : monitor.getParkingAreas(null)) {
            EntityItem entityItem = objectFactory.createEntityItem();
            PlaceItem place = objectFactory.createPlaceItem();
            PlaceItem.ParkingArea parkingArea = objectFactory.createPlaceItemParkingArea();

            parkingArea.getServices().addAll(parkingAreaReader.getServices());
            place.setParkingArea(parkingArea);
            place.setId(parkingAreaReader.getId());
            place.setCapacity(BigInteger.valueOf(parkingAreaReader.getCapacity()));

            entityItem.setPlace(place);
            rns.getItem().add(entityItem);
            ++nParking;
        }

        // ROADS
        Roads roads = objectFactory.createRoads();
        boolean newRoad = false;
        for (RoadSegmentReader roadSegmentReader : monitor.getRoadSegments(null)) {
            String name = roadSegmentReader.getRoadName();

            Roads.Road road = roads.getRoad().stream()
                    .filter(roadOfList -> name.equals(roadOfList.getId()))
                    .findAny()
                    .orElse(null);

            int i = roads.getRoad().indexOf(road);

            if(i >= 0) {
                road = roads.getRoad().get(i);
            } else {
                road = objectFactory.createRoadsRoad();
                road.setId(roadSegmentReader.getRoadName());
                newRoad = true;
            }

            PlaceItem place = objectFactory.createPlaceItem();
            SegmentItem segment = new SegmentItem();

            segment.setName(roadSegmentReader.getName());
            place.setRoadSegment(segment);
            place.setId(roadSegmentReader.getId());
            place.setCapacity(BigInteger.valueOf(roadSegmentReader.getCapacity()));
            road.getPlace().add(place);

            if(newRoad) {
                roads.getRoad().add(road);
                newRoad = false;
                ++nRoad;
            }
            ++nSegment;
        }
        rns.setRoads(roads);

        // VEHICLE
        for (VehicleReader vehicleReader : monitor.getVehicles(null, null, null)) {
            boolean empty = true;

            // Verify that is possible to add another vehicle in that place
            int total = 0;
            for(EntityItem entityItem : rns.getItem()) {
                if(entityItem.getVehicle() != null) {
                    if(entityItem.getVehicle().getPosition().equals(vehicleReader.getPosition().getId())) {
                        empty = false;
                        ++total;
                    }
                }
            }

            // If possible ADD, otherwise print ERROR
            if(total >= vehicleReader.getPosition().getCapacity() && empty) {
                System.err.println("Place FULL, can't add vehicle [" + vehicleReader.getId() + "]");
            } else {
                EntityItem entityItem = objectFactory.createEntityItem();
                VehicleItem vehicle = objectFactory.createVehicleItem();

                vehicle.setId(vehicleReader.getId());
                vehicle.setType(vehicleReader.getType().value());
                try {
                    XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar) vehicleReader.getEntryTime());
                    vehicle.setEntryTime(cal);
                } catch (DatatypeConfigurationException e) {
                    e.printStackTrace();
                }
                vehicle.setState(vehicleReader.getState().value());
                vehicle.setComesFrom(vehicleReader.getOrigin().getId());
                vehicle.setDirectTo(vehicleReader.getDestination().getId());
                vehicle.setPosition(vehicleReader.getPosition().getId());

                entityItem.setVehicle(vehicle);
                rns.getItem().add(entityItem);
                ++nVehicle;
            }
        }

        // CONNECTIONS
        for(ConnectionReader connectionReader:monitor.getConnections()) {
            ConnectionItem connection = objectFactory.createConnectionItem();
            connection.setFrom(connectionReader.getFrom().getId());
            connection.setTo(connectionReader.getTo().getId());
            rns.getConnection().add(connection);
            ++nConn;
        }

        System.out.println("Vehicle:\t" + nVehicle);
        System.out.println("Gate:\t\t" + nGate);
        System.out.println("Road:\t\t" + nRoad);
        System.out.println("Segment:\t" + nSegment);
        System.out.println("Parking:\t" + nParking);
        System.out.println("Connection:\t" + nConn);

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance("it.polito.dp2.RNS.sol1.jaxb");
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(rns, new FileOutputStream(args[0]));
        } catch (FileNotFoundException | JAXBException e) {
            e.printStackTrace();
        }
    }
}
