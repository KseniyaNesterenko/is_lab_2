package cs.ifmo.is.lab1.controller;

import cs.ifmo.is.lab1.dto.PaginatedResponse;
import cs.ifmo.is.lab1.model.MagicCity;
import cs.ifmo.is.lab1.model.Ring;
import cs.ifmo.is.lab1.service.MagicCityService;
import cs.ifmo.is.lab1.service.RingService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

@Path("/ringCities")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RingController {

    @Inject
    private RingService ringService;

    @POST
    public Response create(Ring ring) {
        ringService.create(ring);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Integer id) {
        Ring ring  = ringService.findById(id);
        if (ring == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(ring).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, Ring ring) {
        ring.setId(id);
        ringService.update(ring);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
       ringService.delete(id);
        return Response.ok().build();
    }

    @GET
    public Response findAll(@QueryParam("page") @DefaultValue("1") int page,
                            @QueryParam("pageSize") @DefaultValue("10") int pageSize,
                            @QueryParam("filterId") String filterId,
                            @QueryParam("filterName") String filterName,
                            @QueryParam("filterPower") String filterPower,
                            @QueryParam("sortField") String sortField,
                            @QueryParam("sortAscending") @DefaultValue("true") boolean sortAscending) {
        List<Ring> rings = ringService.findAll(page, pageSize, filterId, filterName, filterPower, sortField, sortAscending);
        long total = ringService.count(filterId, filterName, filterPower);
        return Response.ok(new PaginatedResponse<>(rings, total, page, pageSize)).build();
    }
}
