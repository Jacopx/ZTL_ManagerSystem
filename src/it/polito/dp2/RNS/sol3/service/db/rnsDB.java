package it.polito.dp2.RNS.sol3.service.db;

import it.polito.dp2.RNS.RnsReader;
import it.polito.dp2.RNS.RnsReaderException;
import it.polito.dp2.RNS.lab2.*;
import it.polito.dp2.RNS.lab2.PathFinderFactory;
import it.polito.dp2.RNS.sol1.RnsReaderFactory;
import it.polito.dp2.RNS.sol1.RnsReaderPersonal;
import it.polito.dp2.RNS.sol2.*;

/**
 * Copyright by Jacopx on 15/01/2019.
 */
public class rnsDB {
    PathFinder pff;
    RnsReader rnsDB;

    {
        try {
            if(System.getProperty("it.polito.dp2.RNS.lab2.URL") == null) {
                System.setProperty("it.polito.dp2.RNS.lab3.Neo4JURL", "http://localhost:7474/db");
            }

            System.setProperty("it.polito.dp2.RNS.RnsReaderFactory", "it.polito.dp2.RNS.Random.RnsReaderFactoryImpl");
            System.setProperty("it.polito.dp2.RNS.lab2.PathFinderFactory", "it.polito.dp2.RNS.sol2.RnsReaderFactoryFactory");

            pff = PathFinderFactory.newInstance().newPathFinder();
            rnsDB = RnsReaderFactory.newInstance().newRnsReader();

        } catch (PathFinderException | RnsReaderException e) {
            e.printStackTrace();
        }

    }
}
