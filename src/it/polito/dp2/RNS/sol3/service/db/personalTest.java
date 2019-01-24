package it.polito.dp2.RNS.sol3.service.db;

import it.polito.dp2.RNS.VehicleReader;
import it.polito.dp2.RNS.VehicleType;
import it.polito.dp2.RNS.lab3.*;

/**
 * Copyright by Jacopx on 2019-01-24.
 */
public class personalTest {
    AdmClient admClient;
    VehClient vehClient;

    {
        try {
            admClient = AdmClientFactory.newInstance().newAdmClient();
            System.out.println("PLACES::");
            admClient.getPlaces(null).forEach(p -> System.out.println(p.getId()));
            System.out.println("CONNECTIONS::");
            admClient.getConnections().forEach(c -> System.out.println(c.getFrom() + "-->" + c.getTo()));
            System.out.println("VEHICLES::");
            admClient.getUpdatedVehicles(null).forEach(v -> System.out.println(v.getId()));

            vehClient = VehClientFactory.newInstance().newVehClient();

            vehClient.enter("JAPO1", VehicleType.CAR, "Gate0", "Gate1");

            VehicleReader vehicleReader = admClient.getUpdatedVehicle("JAPO1");
            System.out.println("VehicleReader: " + vehicleReader.getId() + "/Pos: " + vehicleReader.getPosition().getId());
        } catch (AdmClientException | VehClientException | ServiceException | UnknownPlaceException | WrongPlaceException | EntranceRefusedException e) {
            e.printStackTrace();
        }
    }
}
