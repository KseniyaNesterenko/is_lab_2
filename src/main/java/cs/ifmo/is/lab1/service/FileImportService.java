//package cs.ifmo.is.lab1.service;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import cs.ifmo.is.lab1.model.BookCreature;
//import cs.ifmo.is.lab1.model.User;
//import jakarta.enterprise.context.ApplicationScoped;
//import jakarta.faces.context.FacesContext;
//import jakarta.inject.Inject;
//import jakarta.servlet.http.Part;
//import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.*;
//
//@ApplicationScoped
//public class FileImportService {
//
//    @Inject
//    private BookCreatureService bookCreatureService;
//
//    @Inject
//    private ImportHistoryService importHistoryService;
//
//    public void importBookCreatures(InputStream uploadedFileInputStream, FormDataContentDisposition fileDetail) {
//        User currentUser = getCurrentUser();
//
//        if (currentUser == null) {
//            throw new RuntimeException("User not found in session");
//        }
//
//        int addedObjects = 0;
//
//        try {
//            // Parse file and prepare book creatures
//            List<BookCreature> bookCreatures = parseFile(uploadedFileInputStream, currentUser);
//
//            // Check for duplicates
//            checkForDuplicatesInFile(bookCreatures);
//            List<String> duplicateNamesInDb = checkForDuplicatesInDatabase(bookCreatures);
//
//            if (!duplicateNamesInDb.isEmpty()) {
//                throw new IllegalArgumentException("The following names already exist in the database: " + String.join(", ", duplicateNamesInDb));
//            }
//
//            // Validate each book creature
//            for (BookCreature bookCreature : bookCreatures) {
//                if (!validateBookCreature(bookCreature)) {
//                    throw new IllegalArgumentException("Invalid data found. Import canceled.");
//                }
//            }
//
//            // Save to the database
//            bookCreatureService.importBookCreatures(bookCreatures);
//            addedObjects = bookCreatures.size();
//
//            // Log the import result
//            String status = (addedObjects > 0) ? "Success" : "Failure";
//            importHistoryService.saveImportHistory(currentUser, status, addedObjects);
//
//        } catch (IllegalArgumentException e) {
//            importHistoryService.saveImportHistory(currentUser, "Failure", addedObjects);
//            throw e;
//        } catch (Exception e) {
//            importHistoryService.saveImportHistory(currentUser, "Failure", addedObjects);
//            throw new RuntimeException("Error importing data", e);
//        }
//    }
//
//    private User getCurrentUser() {
//        FacesContext facesContext = FacesContext.getCurrentInstance();
//        if (facesContext != null) {
//            return (User) facesContext.getExternalContext().getSessionMap().get("user");
//        }
//        return null; // Handle as per your application's needs
//    }
//
//    private void checkForDuplicatesInFile(List<BookCreature> bookCreatures) {
//        Set<String> seenNames = new HashSet<>();
//        List<String> duplicateNames = new ArrayList<>();
//
//        for (BookCreature creature : bookCreatures) {
//            if (!seenNames.add(creature.getName())) {
//                duplicateNames.add(creature.getName());
//            }
//        }
//
//        if (!duplicateNames.isEmpty()) {
//            throw new IllegalArgumentException("Файл содержит дубликаты имён: " + String.join(", ", duplicateNames));
//        }
//    }
//
//    private List<String> checkForDuplicatesInDatabase(List<BookCreature> bookCreatures) {
//        List<String> duplicateNames = new ArrayList<>();
//        for (BookCreature bookCreature : bookCreatures) {
//            if (bookCreatureService.isNameExists(bookCreature.getName())) {
//                duplicateNames.add(bookCreature.getName());
//            }
//        }
//        return duplicateNames;
//    }
//
//    private List<BookCreature> parseFile(InputStream inputStream, User currentUser) {
//        final List<BookCreature> bookCreaturesFromFile = new ArrayList<>();
//        ObjectMapper mapper = new ObjectMapper();
//
//        try {
//            List<BookCreature> parsedCreatures = mapper.readValue(inputStream, new TypeReference<List<BookCreature>>() {});
//
//            for (BookCreature bookCreature : parsedCreatures) {
//                bookCreature.setUser(currentUser);
//                // Дальнейшая обработка и валидация данных
//
//                // Добавление обработанных объектов в коллекцию
//                bookCreaturesFromFile.add(bookCreature);
//            }
//        } catch (IOException e) {
//            throw new RuntimeException("Ошибка при парсинге файла: " + e.getMessage(), e);
//        }
//
//        return bookCreaturesFromFile;
//    }
//
//    private boolean validateBookCreature(BookCreature bookCreature) {
//        boolean isValid = true;
//
//        if (bookCreature.getName() == null || bookCreature.getName().isEmpty()) {
//            isValid = false;
//        }
//
//        Object x = bookCreature.getCoordinates().getX();
//        if (String.valueOf(bookCreature.getCoordinates().getX()) == null || bookCreature.getCoordinates().getX() >= 488 || !(x instanceof Integer)) {
//            isValid = false;
//        }
//
//        Object y = bookCreature.getCoordinates().getY();
//        if (String.valueOf(bookCreature.getCoordinates().getY()) == null || !(y instanceof Integer)) {
//            isValid = false;
//        }
//
//        if (bookCreature.getAge() == null || !(bookCreature.getAge() instanceof Long) || (bookCreature.getAge() < 0)) {
//            isValid = false;
//        }
//
//        if (bookCreature.getCreatureType() == null) {
//            isValid = false;
//        }
//
//        if (Objects.equals(bookCreature.getCreatureLocation().getName(), "Mordor")) {
//            isValid = false;
//        }
//
//        if (bookCreature.getCreatureLocation().getName() == null || bookCreature.getCreatureLocation().getName().isEmpty()) {
//            isValid = false;
//        }
//
//        if (bookCreature.getCreatureLocation().getArea() == null || bookCreature.getCreatureLocation().getArea() <= 0) {
//            isValid = false;
//        }
//
//        Object p = bookCreature.getCreatureLocation().getPopulation();
//        if (bookCreature.getCreatureLocation().getPopulation() == null || bookCreature.getCreatureLocation().getPopulation() <= 0 || !(p instanceof Integer)) {
//            isValid = false;
//        }
//
//        Object d = bookCreature.getCreatureLocation().getPopulationDensity();
//        if (String.valueOf(bookCreature.getCreatureLocation().getPopulationDensity()) == null || bookCreature.getCreatureLocation().getPopulationDensity() <= 0 || !(d instanceof Integer)) {
//            isValid = false;
//        }
//
//        if (bookCreature.getAttackLevel() == null || bookCreature.getAttackLevel() <= 0) {
//            isValid = false;
//        }
//
//        if (bookCreature.getDefenseLevel() == null || bookCreature.getDefenseLevel() <= 0) {
//            isValid = false;
//        }
//
//        if (bookCreature.getRing().getName() == null || bookCreature.getRing().getName().isEmpty()) {
//            isValid = false;
//        }
//
//        Object power = bookCreature.getRing().getPower();
//        if (bookCreature.getRing().getPower() == null || bookCreature.getRing().getPower() <= 0 || !(power instanceof Integer)) {
//            isValid = false;
//        }
//
//        return isValid;
//    }
//}
