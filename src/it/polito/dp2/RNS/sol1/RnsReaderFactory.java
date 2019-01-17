package it.polito.dp2.RNS.sol1;

import it.polito.dp2.RNS.RnsReader;
import it.polito.dp2.RNS.sol1.jaxb.ConnectionItem;
import it.polito.dp2.RNS.sol1.jaxb.EntityItem;
import it.polito.dp2.RNS.sol1.jaxb.Rns;
import it.polito.dp2.RNS.sol1.jaxb.Roads;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * Copyright by Jacopx on 21/11/2018.
 */
public class RnsReaderFactory extends it.polito.dp2.RNS.RnsReaderFactory {

    @Override
    public RnsReader newRnsReader() {
        RnsReaderPersonal rnsReaderPersonal = fill();
        return rnsReaderPersonal;
    }

    public static RnsReaderPersonal fill() {
        Object objectJAXB = null;
        String file = System.getProperty("it.polito.dp2.RNS.sol1.RnsInfo.file");
        System.out.println("File: " + file);
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File("xsd/rnsInfo.xsd"));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new File(file)));
//            validator.validate(new StreamSource(new File("xsd/randomOut.xml")));

        } catch (IOException | org.xml.sax.SAXException e) {
            e.printStackTrace();
        }

        try {

            JAXBContext jContext = JAXBContext.newInstance("it.polito.dp2.RNS.sol1.jaxb");
            Unmarshaller unmarshallerObj = jContext.createUnmarshaller();
            objectJAXB = unmarshallerObj.unmarshal(new FileInputStream(file));
//            objectJAXB = unmarshallerObj.unmarshal(new FileInputStream("xsd/randomOut.xml"));

        } catch(Exception e) {
            e.printStackTrace();
        }

        Rns rns = (Rns) objectJAXB;
        RnsReaderPersonal monitor = new RnsReaderPersonal();

        for(EntityItem entity: Objects.requireNonNull(rns).getItem()) {
            if(entity.getPlace() != null) {
                if (entity.getPlace().getGate() != null)
                    monitor.addGate(entity.getPlace());
                else if (entity.getPlace().getParkingArea() != null)
                    monitor.addParking(entity.getPlace());
            } else {
                monitor.addVehicle(entity.getVehicle());
            }
        }

        for(Roads.Road road: rns.getRoads().getRoad()) {
            monitor.addRoad(road.getId(), road.getPlace());
        }

        for(ConnectionItem conn: rns.getConnection()) {
            monitor.addConnection(conn.getFrom(), conn.getTo());
        }

        for(EntityItem entity: rns.getItem()) {
            if(entity.getPlace() == null) {
                monitor.matchPlace(entity.getVehicle());
            }
        }

        return monitor;
    }
}
