package it.polito.dp2.RNS.sol3.service.resources;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.polito.dp2.RNS.sol3.rest.service.jaxb.*;
import it.polito.dp2.RNS.sol3.service.service.SearchVehicles;
import it.polito.dp2.RNS.sol3.service.service.rnsService;
import it.polito.dp2.RNS.sol3.service.service.SearchPlaces;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;

/**
 * Copyright by Jacopx on 2019-01-16.
 */
@Path("/")
@Api(value = "/")
public class rnsResources {
    public UriInfo uriInfo;

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
    public RnsSystem getRnsSystem(@QueryParam("admin") int admin) {
        RnsSystem rns = new RnsSystem();
        UriBuilder root = uriInfo.getAbsolutePathBuilder();
        UriBuilder places = root.clone().path("places");
        rns.setSelf(root.toTemplate());
        rns.setPlacesLink(places.toTemplate());
        rns.setVehiclesLink(root.clone().path("vehicles").toTemplate());
        rns.setConnectionsLink(root.clone().path("connections").toTemplate());
        if(admin == 1) {
            rns.setPlaces(getPlaces(admin, null, null, null));
            rns.setVehicles(getVehicles(admin, null, null, null, null, null, null));
        }
        return rns;
    }

    @GET
    @Path("/places")
    @ApiOperation(value = "getPlaces", notes = "searches places"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 401, message = "Not Auth"),
            @ApiResponse(code = 404, message = "Not Found"),
    })
    @Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    public Places getPlaces(@QueryParam("admin") int admin,
                            @QueryParam("type") String type,
                            @QueryParam("keyword") String keyword,
                            @QueryParam("placeID") String placeID
    ) {
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
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 404, message = "Not Found"),
    })
    @Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    public Place getPlace(@PathParam("id") long id) {
        Place place = service.getPlace(id, null);
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
        Connections conns = service.getConnections();
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
        Connection connection = service.getConnection(id);
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
    public Vehicles getVehicles(@QueryParam("admin") int admin,
                                @QueryParam("type") String type,
                                @QueryParam("keyword") String keyword,
                                @QueryParam("state") String state,
                                @QueryParam("entryTime") String entryTime,
                                @QueryParam("position") String position,
                                @QueryParam("plateID") String plateID
    ) {
        Vehicles vs = null;
        System.out.println("Admin:"+admin);
        System.out.println("Position:"+position);
        if(admin == 1) {
            if(type != null && !type.isEmpty()) {
                switch (type.toLowerCase()) {
                    case "car": {
                        vs = service.getVehicles(SearchVehicles.CAR, keyword, state, entryTime, position, plateID);
                        break;
                    }
                    case "truck": {
                        vs = service.getVehicles(SearchVehicles.TRUCK, keyword, state, entryTime, position, plateID);
                        break;
                    }
                    case "caravan": {
                        vs = service.getVehicles(SearchVehicles.CARAVAN, keyword, state, entryTime, position, plateID);
                        break;
                    }
                    case "shuttle": {
                        vs = service.getVehicles(SearchVehicles.SHUTTLE, keyword, state, entryTime, position, plateID);
                        break;
                    }
                    default: {
                        System.out.println("DEFAULT");
                        vs = service.getVehicles(SearchVehicles.ALL, keyword, state, entryTime, position, plateID);
                        break;
                    }
                }
            } else {
                vs = service.getVehicles(SearchVehicles.ALL, keyword, state, entryTime, position, plateID);
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
    public Vehicle getVehicle(@PathParam("id") long id) {
        Vehicle vehicle = service.getVehicle(id, null);
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
            @ApiResponse(code = 406, message = "Unknown Place"),
            @ApiResponse(code = 409, message = "Not correct gateType"),
            @ApiResponse(code = 410, message = "Entrance Refused"),
    })
    @Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    public Response createVehicle(Vehicle vehicle) {

        long id = service.getNextVehicle();
        UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(Long.toString(id));
        URI self = builder.build();
        vehicle.setSelf(self.toString());

        Vehicle created = service.addVehicle(id, vehicle);
        switch (created.getState()) {
            case "REFUSED":
                return Response.created(self).status(410).build();
            case "UNKNOWN_PLACE":
                return Response.created(self).status(406).build();
            case "WRONG_GATE_TYPE":
                return Response.created(self).status(409).build();
            case "ERROR":
                return Response.created(self).status(400).build();
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
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 404, message = "Not Found"),
    })
    @Consumes({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    public Vehicle updateVehicle(@PathParam("id") long id,
                                 @QueryParam("state") String state,
                                 @QueryParam("move") String move,
                                 Vehicle vehicle) {
        System.out.println("State: "+state);
        System.out.println("Move: "+move);
        Vehicle updated = service.updateVehicle(id, state, move);
        if (updated==null)
            throw new NotFoundException();
        return updated;
    }

    @DELETE
    @Path("/vehicles/{id}")
    @ApiOperation(value = "deleteVehicle", notes = "delete vehicle"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Deleted"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 406, message = "Unknown Place"),
            @ApiResponse(code = 409, message = "Not correct gateType"),
            @ApiResponse(code = 410, message = "Entrance Refused"),
    })
    public Response deleteVehicle(@PathParam("id") long id, @QueryParam("outGate") String outGate) {
        Vehicle vehicle = service.deleteVehicle(id, outGate);

        switch (vehicle.getState()) {
            case "NULL":
                return Response.status(404).build();
            case "UNKNOWN_PLACE":
                return Response.status(406).build();
            case "WRONG_GATE_TYPE":
                return Response.status(409).build();
            case "ERROR":
                return Response.status(400).build();
            default: case "REMOVED":
                return Response.status(200).build();
        }
    }

}
