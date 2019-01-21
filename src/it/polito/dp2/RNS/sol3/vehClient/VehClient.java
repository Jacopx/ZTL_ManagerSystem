package it.polito.dp2.RNS.sol3.vehClient;

import it.polito.dp2.RNS.VehicleState;
import it.polito.dp2.RNS.VehicleType;
import it.polito.dp2.RNS.lab3.EntranceRefusedException;
import it.polito.dp2.RNS.lab3.ServiceException;
import it.polito.dp2.RNS.lab3.UnknownPlaceException;
import it.polito.dp2.RNS.lab3.WrongPlaceException;

import java.util.List;

/**
 * Copyright by Jacopx on 2019-01-21.
 */
public class VehClient implements it.polito.dp2.RNS.lab3.VehClient {
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
