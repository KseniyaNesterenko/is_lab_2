package cs.ifmo.is.lab1.controller;

import cs.ifmo.is.lab1.bean.FileImportBean;
import cs.ifmo.is.lab1.dto.PaginatedResponse;
import cs.ifmo.is.lab1.model.BookCreature;
import cs.ifmo.is.lab1.model.BookCreatureType;
import cs.ifmo.is.lab1.model.User;
import cs.ifmo.is.lab1.service.BookCreatureService;
import jakarta.inject.Inject;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PessimisticLockException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Path("/bookCreatures")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 5,
        maxRequestSize = 1024 * 1024 * 10
)

public class BookCreatureController {

    @Inject
    private BookCreatureService bookCreatureService;

    @Inject
    private FileImportBean fileImportBean;

    public BookCreatureController() {
        System.out.println("BookCreatureController is initialized");
    }

    @POST
    public Response create(BookCreature bookCreature) {
        try {
            bookCreatureService.create(bookCreature);
            System.out.println("CREATE request");
            return Response.status(Response.Status.CREATED).build();
        } catch (EntityExistsException e) {
            System.out.println("CREATE request failed: " + e.getMessage());
            return Response.status(Response.Status.CONFLICT)
                    .entity("Существо с таким именем уже существует")
                    .build();
        } catch (Exception e) {
            System.out.println("CREATE request failed: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Ошибка сервера: " + e.getMessage())
                    .build();
        }
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
        try {
            bookCreatureService.update(bookCreature);
            System.out.println("UPDATE request");
            return Response.ok().build();
        } catch (PessimisticLockException e) {
            System.out.println("UPDATE request failed: " + e.getMessage());
            return Response.status(Response.Status.CONFLICT)
                    .entity("Объект уже обновляется другим пользователем")
                    .build();
        } catch (EntityNotFoundException e) {
            System.out.println("UPDATE request failed: " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Существо с ID " + id + " не найдено")
                    .build();
        } catch (Exception e) {
            System.out.println("UPDATE request failed: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Ошибка сервера: " + e.getMessage())
                    .build();
        }
    }



    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Integer id) {
        try {
            bookCreatureService.delete(id);
            System.out.println("DELETE request");
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Некорректный ID: " + e.getMessage())
                    .build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Существо не найдено в системе")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An unexpected error occurred: " + e.getMessage())
                    .build();
        }
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


    @POST
    @Path("/import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response importFile(@FormDataParam("file") InputStream fileInputStream) {
        try {
            if (fileInputStream == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("No file provided for import.")
                        .build();
            }

            User currentUser = fileImportBean.getCurrentUser();
            if (currentUser == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("User not authorized.")
                        .build();
            }

            List<BookCreature> bookCreatures = fileImportBean.parseFile(fileInputStream);
            for (BookCreature creature : bookCreatures) {
                creature.setUser(currentUser);
                System.out.println(currentUser.getUsername());
            }

            fileImportBean.checkForDuplicatesInFile(bookCreatures);
            List<String> duplicateNamesInDb = fileImportBean.checkForDuplicatesInDatabase(bookCreatures);
            if (!duplicateNamesInDb.isEmpty()) {
                return Response.status(Response.Status.CONFLICT)
                        .entity("Duplicate names found in database: " + String.join(", ", duplicateNamesInDb))
                        .build();
            }

            bookCreatureService.importBookCreatures(bookCreatures);
            return Response.ok("Imported " + bookCreatures.size() + " BookCreatures").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error during file import: " + e.getMessage())
                    .build();
        }
    }


}