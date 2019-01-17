package it.polito.dp2.RNS.sol3.service.resources;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.polito.dp2.RNS.sol3.service.service.rnsService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * Copyright by Jacopx on 2019-01-16.
 */
@Path("/RnsSystem")
@Api(value = "/RnsSystem")
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
    public rnsSystem getRnsSystem() {
        rnsSystem rns = new rnsSystem();
        UriBuilder root = uriInfo.getAbsolutePathBuilder();
        biblio.setSelf(root.toTemplate());
        UriBuilder items = root.clone().path("items");
        biblio.setItems(items.toTemplate());
        biblio.setJournals(root.clone().path("journals").toTemplate());
        biblio.setArticles(items.clone().path("articles").toTemplate());
        biblio.setBooks(items.clone().path("books").toTemplate());
        return biblio;
    }


}
