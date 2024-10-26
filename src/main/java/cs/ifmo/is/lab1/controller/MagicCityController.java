package cs.ifmo.is.lab1.controller;

import cs.ifmo.is.lab1.dto.PaginatedResponse;
import cs.ifmo.is.lab1.model.MagicCity;
import cs.ifmo.is.lab1.service.MagicCityService;
import jakarta.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

@Path("/magicCities")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MagicCityController {

    @Inject
    private MagicCityService magicCityService;

    @POST
    public Response create(MagicCity magicCity) {
        magicCityService.create(magicCity);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/{id}")
    public Response findById(@PathParam("id") Integer id) {
        MagicCity magicCity = magicCityService.findById(id);
        if (magicCity == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(magicCity).build();
    }

    @PUT
    @Path("/{id}")
    public Response update(@PathParam("id") Integer id, MagicCity magicCity) {
        magicCity.setId(id);
        magicCityService.update(magicCity);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        magicCityService.delete(id);
        return Response.ok().build();
    }

    @GET
    public Response findAll(@QueryParam("page") @DefaultValue("1") int page,
                            @QueryParam("pageSize") @DefaultValue("10") int pageSize,
                            @QueryParam("filterId") String filterId,
                            @QueryParam("filterName") String filterName,
                            @QueryParam("filterArea") String filterArea,
                            @QueryParam("filterPopulation") String filterPopulation,
                            @QueryParam("filterPopulationDensity") String filterPopulationDensity,
                            @QueryParam("filterEstablishmentDate") String filterEstablishmentDate,
                            @QueryParam("filterGovernor") String filterGovernor,
                            @QueryParam("filterCapital") Boolean filterCapital,
                            @QueryParam("sortField") String sortField,
                            @QueryParam("sortAscending") @DefaultValue("true") boolean sortAscending) {
        List<MagicCity> magicCities = magicCityService.findAll(page, pageSize, filterId, filterName, filterArea, filterPopulation, filterPopulationDensity, filterEstablishmentDate, filterGovernor, filterCapital, sortField, sortAscending);
        long total = magicCityService.count(filterId, filterName, filterArea, filterPopulation, filterPopulationDensity, filterEstablishmentDate, filterGovernor, filterCapital);
        return Response.ok(new PaginatedResponse<>(magicCities, total, page, pageSize)).build();
    }
}
