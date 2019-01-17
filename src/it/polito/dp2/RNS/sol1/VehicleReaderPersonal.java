package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.PlaceReader;
import it.polito.dp2.RNS.VehicleReader;
import it.polito.dp2.RNS.VehicleState;
import it.polito.dp2.RNS.VehicleType;

import java.util.Calendar;

/**
 * Copyright by Jacopx on 26/11/2018.
 */
public class VehicleReaderPersonal implements VehicleReader {
    private String id;
    private Calendar entryTime;
    private PlaceReader dest, origin, position;
    private VehicleType vehicleType;
    private VehicleState vehicleState;

    public VehicleReaderPersonal(String id, Calendar entryTime, String vehicleType, String vehicleState) {
        this.id = id;
        this.entryTime = entryTime;
        this.vehicleType = VehicleType.valueOf(vehicleType);
        this.vehicleState = VehicleState.valueOf(vehicleState);
    }

    public void addPlace(PlaceReader dest, PlaceReader origin, PlaceReader position) {
        this.dest = dest;
        this.origin = origin;
        this.position = position;
    }

    @Override
    public VehicleType getType() {
        return vehicleType;
    }

    @Override
    public Calendar getEntryTime() {
        return entryTime;
    }

    @Override
    public PlaceReader getDestination() {
        return dest;
    }

    @Override
    public PlaceReader getOrigin() {
        return origin;
    }

    @Override
    public PlaceReader getPosition() {
        return position;
    }

    @Override
    public VehicleState getState() {
        return vehicleState;
    }

    @Override
    public String getId() {
        return id;
    }
}
