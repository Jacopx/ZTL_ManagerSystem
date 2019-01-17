package it.polito.dp2.RNS.sol2;

import it.polito.dp2.RNS.*;
import it.polito.dp2.RNS.lab2.BadStateException;
import it.polito.dp2.RNS.lab2.ModelException;
import it.polito.dp2.RNS.lab2.ServiceException;
import it.polito.dp2.RNS.lab2.UnknownIdException;
import it.polito.dp2.RNS.sol2.jaxb.*;

import java.math.BigInteger;
import java.util.*;
import javax.ws.rs.client.*;
import javax.ws.rs.core.*;

/**
 * Copyright by Jacopx on 29/11/2018.
 */
public class PathFinderPersonal implements it.polito.dp2.RNS.lab2.PathFinder {
    private Set<List<String>> sp;
    private String BASE;
    private boolean loaded;
    private static RnsReader monitor;
    private HashMap<String, String> nodeURL = new HashMap<>();
    private HashMap<String, String> idMap = new HashMap<>();
    private ArrayList<String> connList = new ArrayList<>();

    public PathFinderPersonal() {
        this.sp = null;
        this.loaded = false;
        this.BASE = System.getProperty("it.polito.dp2.RNS.lab2.URL");
    }

    private void loadReader() throws ModelException {
        try {
            monitor = RnsReaderFactory.newInstance().newRnsReader();
        } catch (RnsReaderException e) {
            e.printStackTrace();
        }

        if(monitor == null) {
            throw new ModelException("MONITOR: Not instantiated");
        }

        // PLACE ITEM
        for (PlaceReader placeReader : monitor.getPlaces(null)) {
            try {
                addNode(placeReader.getId());
            } catch (ServiceException | ModelException e) {
                e.printStackTrace();
            }
        }

        // CONNECTIONS
        for (ConnectionReader connectionReader : monitor.getConnections()) {
            try {
                addConnection(connectionReader.getFrom().getId(), connectionReader.getTo().getId());
            } catch (ServiceException | ModelException e) {
                e.printStackTrace();
            }
        }
    }

    public void addNode(String id) throws ServiceException, ModelException {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(BASE).path("data").path("node");

        if(id.isEmpty()) {
            throw new ModelException("PlaceID is null");
        }

        ObjectFactory of = new ObjectFactory();
        Node node = of.createNode();
        node.setId(id);

        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(node));
        if(response.getStatus() != 201) {
            throw new ServiceException("Unable to create node");
        } else {
            BasicResponse nodeResponse = response.readEntity(new GenericType<BasicResponse>(){});
            nodeURL.put(id, nodeResponse.getSelf());
            idMap.put(nodeResponse.getSelf(), id);
        }
    }

    private void addConnection(String from, String to) throws ServiceException, ModelException {
        if(from.isEmpty() || to.isEmpty()) {
            throw new ModelException("FROM and/or TO for connection are empty");
        }
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(nodeURL.get(from)).path("relationships");

        ObjectFactory of = new ObjectFactory();
        Relation relation = of.createRelation();
        relation.setTo(nodeURL.get(to));
        relation.setType(RelationType.CONNECTED_TO);

        Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(relation));
        if(response.getStatus() != 201) {
            throw new ServiceException("Impossible adding relationship");
        } else {
            BasicResponse relationResponse = response.readEntity(new GenericType<BasicResponse>(){});
            connList.add(relationResponse.getSelf());
        }
    }

    private void clearDB() throws ServiceException{
        Client client = ClientBuilder.newClient();
        WebTarget target;

        // Delete all relationships
        for(String URL:connList) {
            target = client.target(URL);
            Response response = target.request(MediaType.APPLICATION_JSON).delete();
            if(response.getStatus() != 204) {
                throw new ServiceException("REL: Not deleted");
            }
        }

        // Delete all nodes
        for(String URL: nodeURL.values()) {
            target = client.target(URL);
            Response response = target.request(MediaType.APPLICATION_JSON).delete();
            if(response.getStatus() != 204) {
                throw new ServiceException("NODE: Not deleted");
            }
        }
    }

    @Override
    public boolean isModelLoaded() { return loaded; }

    @Override
    public void reloadModel() throws ServiceException, ModelException {
        loaded = false;
        // Clearing local DB
        nodeURL.clear();
        connList.clear();
        idMap.clear();
        // Deleting from Neo4j
        clearDB();
        // Reload RnsReader
        loadReader();
        loaded = true;
    }

    @Override
    public Set<List<String>> findShortestPaths(String source, String destination, int maxlength) throws UnknownIdException, BadStateException, ServiceException {
        String src, dst;

        // Check model
        if(!loaded) {
            throw new BadStateException("Model not yet loaded");
        }

        // Check source and destination
        if(!nodeURL.containsKey(source)) {
            throw new UnknownIdException("Missing sources node!");
        } else if(!nodeURL.containsKey(destination)) {
            throw new UnknownIdException("Missing sources node!");
        } else {
            src = nodeURL.get(source);
            dst = nodeURL.get(destination);
        }

        // Creating structure for the request
        ObjectFactory of = new ObjectFactory();
        ShortRequest request = of.createShortRequest();
        request.setTo(dst);
        request.setMaxDepth(BigInteger.valueOf(maxlength));
        Relationships relationships = new Relationships();
        relationships.setDirection(Direction.OUT);
        relationships.setType(RelationType.CONNECTED_TO);
        request.setRelationships(relationships);
        request.setAlgorithm(Algorithm.SHORTEST_PATH);

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(src).path("paths");
        Response response = target.request().accept(MediaType.APPLICATION_JSON).post(Entity.json(request));

        if(response.getStatus() != 200) {
            throw new ServiceException("Error computing path");
        } else {
            List<ShortestResponse> paths = response.readEntity(new GenericType<List<ShortestResponse>>(){});
            sp = new HashSet<>();

            List<String> nodeList = new LinkedList<>();

            for(ShortestResponse newResponse: paths) {
                for(String n: newResponse.getNodes()) {
                    if(idMap.containsKey(n)) {
                        nodeList.add(idMap.get(n));
                    }
                }
                sp.add(nodeList);
            }
            return sp;
        }
    }
}
