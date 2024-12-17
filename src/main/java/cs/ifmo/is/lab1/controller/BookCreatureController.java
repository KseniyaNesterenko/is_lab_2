package cs.ifmo.is.lab1.controller;

import cs.ifmo.is.lab1.dto.PaginatedResponse;
import cs.ifmo.is.lab1.model.BookCreature;
import cs.ifmo.is.lab1.model.BookCreatureType;
import cs.ifmo.is.lab1.service.BookCreatureService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/bookCreatures")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookCreatureController {

    @Inject
    private BookCreatureService bookCreatureService;

    public BookCreatureController() {
        System.out.println("BookCreatureController is initialized");
    }

    @POST
    public Response create(BookCreature bookCreature) {
        bookCreatureService.create(bookCreature);
        System.out.println("CREATE request");
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Integer id) {
        BookCreature bookCreature = bookCreatureService.findById(Math.toIntExact(Long.valueOf(id)));
        if (bookCreature == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(bookCreature).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, BookCreature bookCreature) {
        bookCreature.setId(id);
        bookCreatureService.update(bookCreature);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        bookCreatureService.delete(id);
        return Response.ok().build();
    }

    @GET
    public Response findAll(@QueryParam("page") @DefaultValue("1") int page,
                            @QueryParam("pageSize") @DefaultValue("10") int pageSize,
                            @QueryParam("filterName") Integer filterId,
                            @QueryParam("filterName") String filterName,
                            @QueryParam("filterAge") Long filterAge,
                            @QueryParam("filterCoordinatesX") String filterCoordinatesX,
                            @QueryParam("filterCoordinatesY") String filterCoordinatesY,
                            @QueryParam("filterCreationDate") String filterCreationDate,
                            @QueryParam("filterCreatureType") String filterCreatureType,
                            @QueryParam("filterCreatureLocation") String filterCreatureLocation,
                            @QueryParam("filterAttackLevel") String filterAttackLevel,
                            @QueryParam("filterDefenseLevel") String filterDefenseLevel,
                            @QueryParam("filterRing") String filterRing,
                            @QueryParam("sortField") String sortField,
                            @QueryParam("sortAscending") @DefaultValue("true") boolean sortAscending) {
        System.out.println("GET request to findAll");
        List<BookCreatureType> matchingCreatureTypes = getMatchingCreatureTypes(filterCreatureType);
        List<BookCreature> bookCreatures = bookCreatureService.findAll(page, pageSize, filterId, filterName, filterAge, filterCoordinatesX, filterCoordinatesY, filterCreationDate, matchingCreatureTypes, filterCreatureLocation, filterAttackLevel, filterDefenseLevel, filterRing, sortField, sortAscending);
        long total = bookCreatureService.count(filterId, filterName, filterAge, filterCoordinatesX, filterCoordinatesY, filterCreationDate, matchingCreatureTypes, filterCreatureLocation, filterAttackLevel, filterDefenseLevel, filterRing);
        return Response.ok(new PaginatedResponse<>(bookCreatures, total, page, pageSize)).build();
    }


    private List<BookCreatureType> getMatchingCreatureTypes(String creatureTypeString) {
        List<BookCreatureType> matchingTypes = new ArrayList<>();
        if (creatureTypeString != null && !creatureTypeString.isEmpty()) {
            for (BookCreatureType type : BookCreatureType.values()) {
                if (type.name().toLowerCase().contains(creatureTypeString.toLowerCase())) {
                    matchingTypes.add(type);
                }
            }
        }
        return matchingTypes;
    }

    @GET
    @Path("/test")
    public Response test() {
        return Response.ok("Controller is working!").build();
    }

}