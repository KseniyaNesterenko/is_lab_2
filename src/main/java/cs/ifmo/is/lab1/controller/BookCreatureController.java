package cs.ifmo.is.lab1.controller;

import cs.ifmo.is.lab1.bean.FileImportBean;
import cs.ifmo.is.lab1.dto.PaginatedResponse;
import cs.ifmo.is.lab1.model.BookCreature;
import cs.ifmo.is.lab1.model.BookCreatureType;
import cs.ifmo.is.lab1.model.ImportHistory;
import cs.ifmo.is.lab1.model.User;
import cs.ifmo.is.lab1.service.BookCreatureService;
import cs.ifmo.is.lab1.service.ImportHistoryService;
import io.minio.errors.MinioException;
import jakarta.inject.Inject;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.PessimisticLockException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.io.InputStream;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
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

    @Inject
    private ImportHistoryService importHistoryService;

    @POST
    @Path("/import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response importFile(@FormDataParam("fileContent") InputStream fileInputStream,
                               @HeaderParam("File-Name") String fileName) {
        System.out.println("Entering importFile method...");

        try {
            if (fileInputStream == null) {
                System.out.println("No file provided for import.");
                importHistoryService.saveImportHistory(fileImportBean.getCurrentUser(), "Неуспешно", 0, "-");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Пожалуйста, выберите файл для импорта.")
                        .build();
            }

            if (fileName == null || fileName.isEmpty()) {
                fileName = "uploaded_file_" + System.currentTimeMillis() + ".json";
            }
            System.out.println("File name: " + fileName);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] temp = new byte[8192];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(temp)) != -1) {
                buffer.write(temp, 0, bytesRead);
            }

            byte[] fileData = buffer.toByteArray();
            User currentUser = fileImportBean.getCurrentUser();
            if (currentUser == null) {
                importHistoryService.saveImportHistory(null, "Неуспешно", 0, "-");
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Пользователь не авторизован.")
                        .build();
            }

            List<BookCreature> bookCreatures = fileImportBean.parseFile(new ByteArrayInputStream(fileData));
            fileImportBean.checkForDuplicatesInFile(bookCreatures);
            List<String> duplicateNamesInDb = fileImportBean.checkForDuplicatesInDatabase(bookCreatures);
            if (!duplicateNamesInDb.isEmpty()) {
                importHistoryService.saveImportHistory(currentUser, "Неуспешно", 0, "-");
                return Response.status(Response.Status.CONFLICT)
                        .entity("Ошибка: Следующие имена уже существуют в базе данных: " + String.join(", ", duplicateNamesInDb))
                        .build();
            }

            ImportHistory importHistory = importHistoryService.saveImportHistory(currentUser, "В процессе", 0, fileName);

            for (BookCreature bookCreature : bookCreatures) {
                bookCreature.setImported(true);
                bookCreature.setImportHistory(importHistory);
            }

            bookCreatureService.importBookCreatures(
                    bookCreatures,
                    new ByteArrayInputStream(fileData),
                    fileName,
                    fileData.length
            );

            int addedObjects = bookCreatures.size();
            String status = (addedObjects > 0) ? "Успешно" : "Неуспешно";
            importHistory.setStatus(status);
            importHistory.setAddedObjects(addedObjects);

            if (addedObjects > 0) {
                importHistoryService.saveImportHistory(currentUser, "Успешно", addedObjects, fileName);
            }

            return Response.ok("Импорт успешно завершён. Загружено объектов: " + bookCreatures.size()).build();

        } catch (Exception e) {
            importHistoryService.saveImportHistory(fileImportBean.getCurrentUser(), "Неуспешно", 0, "-");
            return handleImportException(e);
        }
    }

    private Response handleImportException(Exception e) {
        Throwable rootCause = getRootCause(e);

        if (rootCause instanceof org.postgresql.util.PSQLException) {
            System.out.println("Database error: " + rootCause.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Ошибка базы данных: " + rootCause.getMessage())
                    .build();
        } else if (rootCause instanceof org.eclipse.persistence.exceptions.DatabaseException) {
            System.out.println("Persistence error: " + rootCause.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Ошибка сохранения данных: " + rootCause.getMessage())
                    .build();
        } else if (rootCause instanceof java.net.ConnectException) {
            System.out.println("File storage connection error: " + rootCause.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("Отказ файлового хранилища. Проверьте подключение.")
                    .build();
        } else {
            System.out.println("Unexpected error: " + rootCause.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Неизвестная ошибка: " + rootCause.getMessage())
                    .build();
        }
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause != cause.getCause()) {
            cause = cause.getCause();
        }
        return cause;
    }

}