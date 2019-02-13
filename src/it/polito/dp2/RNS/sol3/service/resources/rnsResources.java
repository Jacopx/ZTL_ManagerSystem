package it.polito.dp2.RNS.sol3.service.resources;

import io.swagger.annotations.*;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.*;
import it.polito.dp2.RNS.sol3.service.service.SearchVehicles;
import it.polito.dp2.RNS.sol3.service.service.rnsService;
import it.polito.dp2.RNS.sol3.service.service.SearchPlaces;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.math.BigInteger;
import java.net.URI;

/**
 * Copyright by Jacopx on 2019-01-16.
 */
@Path("/")
@Api(value = "/")
public class rnsResources {
    public UriInfo uriInfo;
    private UriBuilder main;

    rnsService service = new rnsService();

    public rnsResources(@Context UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @GET
    @ApiOperation(value = "getRnsSystem", notes = "reads main resource")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
    })
    @Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    public RnsSystem getRnsSystem(@ApiParam(value = "Used for make admin request") @QueryParam("admin") int admin) {
        RnsSystem rns = new RnsSystem();

        UriBuilder root = uriInfo.getAbsolutePathBuilder();
        main = uriInfo.getAbsolutePathBuilder();
        UriBuilder places = root.clone().path("places");
        rns.setSelf(root.toTemplate());
        rns.setPlacesLink(places.toTemplate());
        rns.setVehiclesLink(root.clone().path("vehicles").toTemplate());
        rns.setConnectionsLink(root.clone().path("connections").toTemplate());
        return rns;
    }

    @GET
    @Path("/places")
    @ApiOperation(value = "getPlaces", notes = "searches places"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "Not Authorized: Admin parameters required"),
            @ApiResponse(code = 404, message = "Not Found: No place found"),
    })
    @Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    public Places getPlaces(@ApiParam(value = "Used for make admin request") @QueryParam("admin") int admin,
                            @ApiParam(value = "Parameter for choosing the place type") @QueryParam("type") String type,
                            @ApiParam(value = "String for searching places with a keyword on placeID") @QueryParam("keyword") String keyword,
                            @ApiParam(value = "Searching by placeID") @QueryParam("placeID") String placeID
    ) {
        System.out.println("@GET_PLACESSSSS");
        Places places;
        if(admin == 1) {
            if(type != null && !type.isEmpty()) {
                switch (type.toLowerCase()) {
                    case "gate": {
                        places = service.getPlaces(SearchPlaces.GATE, keyword, placeID);
                        break;
                    }
                    case "segment": {
                        places = service.getPlaces(SearchPlaces.SEGMENT, keyword, placeID);
                        break;
                    }
                    case "parking": {
                        places = service.getPlaces(SearchPlaces.PARKING, keyword, placeID);
                        break;
                    }
                    default: {
                        places = service.getPlaces(SearchPlaces.ALL, keyword, placeID);
                        break;
                    }
                }
            } else {
                places = service.getPlaces(SearchPlaces.ALL, keyword, placeID);
            }
        } else {
            throw new NotAuthorizedException("Admin privilege required!");
        }

        // Translate with correct URI
        UriBuilder root = uriInfo.getAbsolutePathBuilder();
        for (Place p:places.getPlace())
            p.setSelf(root.clone().path(p.getId()).toTemplate());

        if (places==null)
            throw new NotFoundException();
        return places;
    }

    @GET
    @Path("/places/{id}")
    @ApiOperation(value = "getPlace", notes = "read single place"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not Found"),
    })
    @Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    public Place getPlace(@ApiParam(value = "Searching by placeID") @PathParam("id") String id) {
        System.out.println("@GET_PLACE#" + id);
        Place place = service.getPlace(id);
        UriBuilder root = uriInfo.getAbsolutePathBuilder();
        place.setSelf(root.path(place.getId()).toTemplate());
        if (place==null)
            throw new NotFoundException();
        return place;
    }

    @GET
    @Path("/connections")
    @ApiOperation(value = "getConnections", notes = "searches connections"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Not Found"),
    })
    @Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    public Connections getConnections() {
        System.out.println("@GET_CONNECTIONSSSSSS");
        Connections conns = service.getConnections();

        UriBuilder root = uriInfo.getAbsolutePathBuilder();
        UriBuilder temp;
        // Translate with correct URI
        for (Connection c:conns.getConnection()) {
            c.setSelf(root.clone().path(String.valueOf(c.getId())).toTemplate());

            temp = main.clone().path("places");
            c.setFromNode(root.path(c.getFrom()).toTemplate());
            temp = main.clone().path("places");
            c.setToNode(root.path(c.getTo()).toTemplate());
        }

        if(conns == null)
            throw new NotFoundException();
        return conns;
    }

    @GET
    @Path("/connections/{id}")
    @ApiOperation(value = "getConnection", notes = "read single connection"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 404, message = "Not Found"),
    })
    @Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    public Connection getConnection(@PathParam("id") long id) {
        System.out.println("@GET_CONNECTION");
        Connection connection = service.getConnection(id);

        UriBuilder root = uriInfo.getAbsolutePathBuilder();
        connection.setSelf(root.path(String.valueOf(connection.getId())).toTemplate());
        UriBuilder from = main.clone().path("places");
        connection.setFromNode(from.path(connection.getFrom()).toTemplate());
        UriBuilder to = main.clone().path("places");
        connection.setToNode(to.path(connection.getTo()).toTemplate());

        if (connection==null)
            throw new NotFoundException();
        return connection;
    }

    @GET
    @Path("/vehicles")
    @ApiOperation(value = "getVehicles", notes = "searches vehicles"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 404, message = "Not Found"),
    })
    @Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    public Vehicles getVehicles(@ApiParam(value = "Used for make admin request") @QueryParam("admin") int admin,
                                @ApiParam(value = "Parameter for choosing the vehicle type") @QueryParam("type") String type,
                                @ApiParam(value = "String for searching vehicles with a keyword on plate") @QueryParam("keyword") String keyword,
                                @ApiParam(value = "State of the vehicle available in the system") @QueryParam("state") String state,
                                @ApiParam(value = "All the vehicle available entered after this time") @QueryParam("entryTime") String entryTime,
                                @ApiParam(value = "Position of the vehicle") @QueryParam("position") String position,
                                @ApiParam(value = "Searching by plate") @QueryParam("plateID") String plateID
    ) {
        System.out.println("@GET_VEHICLESSSS#"+position);
        Vehicles vs = null;
        if(admin == 1) {
            if(type != null && !type.isEmpty()) {
                switch (type.toLowerCase()) {
                    case "car": {
                        vs = service.getVehicles(SearchVehicles.CAR, keyword, state, entryTime, position);
                        break;
                    }
                    case "truck": {
                        vs = service.getVehicles(SearchVehicles.TRUCK, keyword, state, entryTime, position);
                        break;
                    }
                    case "caravan": {
                        vs = service.getVehicles(SearchVehicles.CARAVAN, keyword, state, entryTime, position);
                        break;
                    }
                    case "shuttle": {
                        vs = service.getVehicles(SearchVehicles.SHUTTLE, keyword, state, entryTime, position);
                        break;
                    }
                    default: {
                        System.out.println("DEFAULT");
                        vs = service.getVehicles(SearchVehicles.ALL, keyword, state, entryTime, position);
                        break;
                    }
                }
            } else {
                vs = service.getVehicles(SearchVehicles.ALL, keyword, state, entryTime, position);
            }
        } else {
            throw new NotAuthorizedException("Admin privilege required!");
        }

        if(vs == null)
            vs = new Vehicles();
        return vs;
    }

    @GET
    @Path("/vehicles/{id}")
    @ApiOperation(value = "getVehicle", notes = "read single vehicle"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 404, message = "Not Found"),
    })
    @Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    public Vehicle getVehicle(@ApiParam(value = "Searching by plate") @PathParam("id") String id) {
        System.out.println("@GET_VEHICLE#" + id);
        Vehicle vehicle = service.getVehicle(id);
        System.out.println(vehicle);
        if (vehicle==null)
            throw new NotFoundException();
        return vehicle;
    }

    @POST
    @Path("/vehicles")
    @ApiOperation(value = "createVehicle", notes = "create a new vehicle"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 403, message = "Forbidden: Not correct gateType"),
            @ApiResponse(code = 409, message = "Conflict: Duplicated vehicle"),
            @ApiResponse(code = 422, message = "Unprocessable Entity: source or destination not available or path computation problem")
    })
    @Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    public Response createVehicle(@ApiParam(value = "Create a Vehicle object to be added") Vehicle vehicle) {
        System.out.println("@CREATE_VEHICLE");
        String id = vehicle.getId();
        UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(id);
        URI self = builder.build();
        vehicle.setSelf(self.toString());

        Vehicle created = service.addVehicle(vehicle);
        switch (created.getState()) {
            case "ERROR":
                return Response.created(self).status(400).build();
            case "WRONG_GATE_TYPE":
                return Response.created(self).status(403).build();
            case "DUPLICATE":
                return Response.created(self).status(409).build();
            case "REFUSED":
                return Response.created(self).status(422).build();
            case "UNKNOWN_PLACE":
                return Response.created(self).status(422).build();
            default:
                VehicleResponse v = new VehicleResponse();
                v.setPlateID(created.getId());
                v.setSelf(created.getSelf());
                v.setFromNode(created.getFromNode());
                v.setToNode(created.getToNode());
                for (ShortPaths sp : created.getShortPaths()) {
                    for (SuggPath sgp : sp.getSuggPath())
                        v.getPath().addAll(sgp.getRelation());
                }
                return Response.created(self).status(201).entity(v).build();
        }
    }

    @PUT
    @Path("/vehicles/{id}")
    @ApiOperation(value = "updateVehicle", notes = "update single vehicle"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 403, message = "Forbidden: New position is not reachable from the current position"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Unprocessable Entity: New position is not a valid place")
    })
    @Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    public Response updateVehicle(@ApiParam(value = "Searching by plate") @PathParam("id") String id,
                                  @ApiParam(value = "State of the vehicle available in the system") @QueryParam("state") String state,
                                  @ApiParam(value = "New position of the move") @QueryParam("move") String move) {
        System.out.println("@UPDATE_VEHICLE");
        Vehicle updated = service.updateVehicle(id, state, move);

        UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(id);
        URI self = builder.build();

        if (updated==null)
            throw new NotFoundException();

        switch (updated.getState()) {
            case "REFUSED":
                return Response.created(self).status(403).build();
            case "WRONGPLACE":
                return Response.created(self).status(422).build();
            default:
                VehicleResponse v = new VehicleResponse();
                v.setPlateID(updated.getId());
                v.setSelf(updated.getSelf());
                v.setPositionNode(updated.getPositionNode());
                v.setFromNode(updated.getFromNode());
                v.setToNode(updated.getToNode());
                for (ShortPaths sp : updated.getShortPaths()) {
                    for (SuggPath sgp : sp.getSuggPath())
                        v.getPath().addAll(sgp.getRelation());
                }
                return Response.created(self).status(200).entity(v).build();
        }
    }

    @DELETE
    @Path("/vehicles/{id}")
    @ApiOperation(value = "deleteVehicle", notes = "delete vehicle"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "No content"),
            @ApiResponse(code = 403, message = "Forbidden: Not exit gate type"),
            @ApiResponse(code = 404, message = "Not Found"),
            @ApiResponse(code = 422, message = "Unknown Place"),
    })
    public Response deleteVehicle(@ApiParam(value = "Searching by plate") @PathParam("id") String id,
                                  @ApiParam(value = "Gate ID for the exiting") @QueryParam("outGate") String outGate) {
        Vehicle vehicle = service.deleteVehicle(id, outGate);

        switch (vehicle.getState()) {
            case "WRONG_GATE_TYPE":
                return Response.status(403).build();
            case "NULL":
                return Response.status(404).build();
            case "UNKNOWN_PLACE":
                return Response.status(422).build();
            default: case "REMOVED":
                return Response.status(204).build();
        }
    }

}
