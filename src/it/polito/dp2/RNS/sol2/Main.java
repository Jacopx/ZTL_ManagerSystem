package it.polito.dp2.RNS.sol2;


import it.polito.dp2.RNS.lab2.PathFinderException;

/**
 * Copyright by Jacopx on 05/12/2018.
 */
public class Main {
    public static void main(String[] args) {
        PathFinderFactory pathFinderFactory = new PathFinderFactory();
        try {
            System.setProperty("it.polito.dp2.RNS.lab2.URL", "http://localhost:7474/db");
            System.setProperty("it.polito.dp2.RNS.RnsReaderFactory", "it.polito.dp2.RNS.Random.RnsReaderFactoryImpl");
            System.setProperty("it.polito.dp2.RNS.Random.seed", "9999999");
            System.setProperty("it.polito.dp2.RNS.Random.testcase", "2");

            pathFinderFactory.newPathFinder();
        } catch (PathFinderException e) {
            e.printStackTrace();
        }
    }
}
