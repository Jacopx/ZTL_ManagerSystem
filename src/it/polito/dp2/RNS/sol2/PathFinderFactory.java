package it.polito.dp2.RNS.sol2;

import it.polito.dp2.RNS.lab2.*;

/**
 * Copyright by Jacopx on 29/11/2018.
 */
public class PathFinderFactory extends it.polito.dp2.RNS.lab2.PathFinderFactory {

    @Override
    public PathFinder newPathFinder() throws PathFinderException {
        PathFinderPersonal pathFinderPersonal = new PathFinderPersonal();
        return pathFinderPersonal;
    }
}
