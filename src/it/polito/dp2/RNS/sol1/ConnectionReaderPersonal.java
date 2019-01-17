package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.ConnectionReader;
import it.polito.dp2.RNS.PlaceReader;

/**
 * Copyright by Jacopx on 24/11/2018.
 */
public class ConnectionReaderPersonal implements ConnectionReader {
    private PlaceReader from;
    private PlaceReader to;

    public ConnectionReaderPersonal(PlaceReader from, PlaceReader to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public PlaceReader getFrom() {
        return from;
    }

    @Override
    public PlaceReader getTo() {
        return to;
    }
}
