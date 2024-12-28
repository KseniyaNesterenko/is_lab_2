package cs.ifmo.is.lab1.bean;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cs.ifmo.is.lab1.model.*;
import cs.ifmo.is.lab1.service.BookCreatureService;
import cs.ifmo.is.lab1.service.FileStorageService;
import cs.ifmo.is.lab1.service.ImportHistoryService;
import cs.ifmo.is.lab1.service.UserService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.postgresql.util.PSQLException;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

import static com.fasterxml.jackson.databind.util.ClassUtil.getRootCause;

@Named
@RequestScoped
public class FileImportBean implements Serializable {

    private BookCreature bookCreature;
    @Inject
    private BookCreatureService bookCreatureService;

    @Inject
    private ImportHistoryService importHistoryService;
    @Inject
    private FileStorageService fileStorageService;
    private Part uploadedFile;

    public Part getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(Part uploadedFile) {
        this.uploadedFile = uploadedFile;
    }



    @Inject
    private HttpServletRequest request;

    @Inject
    private UserService userService;

    public User getCurrentUser() {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User sessionUser = (User) session.getAttribute("user");
            if (sessionUser != null) {
                return sessionUser;
            }
        }

        String username = request.getHeader("X-Username");
        String password = request.getHeader("X-Password");

        if (username != null && password != null) {
            User user = userService.findUserByUsername(username);
            if (user != null && user.getPassword().equals(password)) {
                return user;
            }
        }

        return null;
    }


    public void importBookCreatures() {
        if (uploadedFile == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Пожалуйста, выберите файл для импорта.", null));
            return;
        }

        System.out.println("Uploaded file size: " + uploadedFile.getSize());
        System.out.println("Uploaded file content type: " + uploadedFile.getContentType());

        User currentUser = getCurrentUser();
        int addedObjects = 0;

        try (InputStream inputStream = uploadedFile.getInputStream()) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] temp = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(temp)) != -1) {
                buffer.write(temp, 0, bytesRead);
            }
            byte[] fileData = buffer.toByteArray();

            String uploadedFileName = fileStorageService.uploadFile(uploadedFile.getSubmittedFileName(),
                    new ByteArrayInputStream(fileData), uploadedFile.getSize());

            List<BookCreature> bookCreatures = parseFile(new ByteArrayInputStream(fileData));


            checkForDuplicatesInFile(bookCreatures);

            List<String> duplicateNamesInDb = checkForDuplicatesInDatabase(bookCreatures);
            if (!duplicateNamesInDb.isEmpty()) {
                addErrorMessage("Ошибка: Следующие имена уже существуют в базе данных: " + String.join(", ", duplicateNamesInDb));
                importHistoryService.saveImportHistory(currentUser, "Неуспешно", addedObjects);
                return;
            }

            for (BookCreature bookCreature : bookCreatures) {
                if (!validateBookCreature(bookCreature)) {
                    addErrorMessage("Ошибка: Один или несколько объектов содержат некорректные данные. Импорт отменен.");
                    importHistoryService.saveImportHistory(currentUser, "Неуспешно", addedObjects);
                    return;
                }
            }

            bookCreatureService.importBookCreatures(bookCreatures, new ByteArrayInputStream(fileData),
                    uploadedFile.getSubmittedFileName(), uploadedFile.getSize());

            addedObjects = bookCreatures.size();

            String status = (addedObjects > 0) ? "Успешно" : "Неуспешно";
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    addedObjects > 0 ? "Импорт успешно завершен." : "Импорт не завершён", null));
            importHistoryService.saveImportHistory(currentUser, status, addedObjects);

        } catch (Exception e) {
            handleImportException(e, currentUser, addedObjects);
        }
    }

    private void handleImportException(Exception e, User currentUser, int addedObjects) {
        Throwable rootCause = getRootCause(e);

        if (rootCause instanceof org.postgresql.util.PSQLException psqlException) {
            addErrorMessage("Отказ БД: ошибка SQL. Сообщение: " + psqlException.getMessage());
        } else if (rootCause instanceof org.eclipse.persistence.exceptions.DatabaseException) {
            addErrorMessage("Отказ БД: ошибка базы данных. Сообщение: " + rootCause.getMessage());
        } else if (rootCause instanceof java.net.ConnectException) {
            addErrorMessage("Отказ файлового хранилища. Пожалуйста, проверьте подключение.");
        } else {
            addErrorMessage("Ошибка при импорте: " + rootCause.getMessage());
        }

        importHistoryService.saveImportHistory(currentUser, "Неуспешно", addedObjects);
    }

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }


    public List<String> checkForDuplicatesInDatabase(List<BookCreature> bookCreatures) throws SQLException {
        List<String> duplicateNames = new ArrayList<>();
        for (BookCreature bookCreature : bookCreatures) {
            if (bookCreatureService.isNameExists(bookCreature.getName())) {
                duplicateNames.add("Существо: " + bookCreature.getName());
            }

            if (bookCreature.getCreatureLocation() != null) {
                MagicCity city = bookCreature.getCreatureLocation();
                if (bookCreatureService.isCityNameExists(city.getName())) {
                    duplicateNames.add("Город: " + city.getName());
                }
            }
        }
        return duplicateNames;
    }

    public void checkForDuplicatesInFile(List<BookCreature> bookCreatures) {
        Set<String> seenNames = new HashSet<>();
        Set<String> seenCityNames = new HashSet<>();
        List<String> duplicateNames = new ArrayList<>();

        for (BookCreature creature : bookCreatures) {
            if (!seenNames.add(creature.getName())) {
                duplicateNames.add(creature.getName());
            }

            if (creature.getCreatureLocation() != null && !creature.getCreatureLocation().getName().isEmpty()) {
                String cityName = creature.getCreatureLocation().getName();
                if (!seenCityNames.add(cityName)) {
                    duplicateNames.add("Город: " + cityName);
                }
            }
        }

        if (!duplicateNames.isEmpty()) {
            throw new IllegalArgumentException("Файл содержит дубликаты: " + String.join(", ", duplicateNames));
        }
    }


    public List<BookCreature> parseFile(InputStream inputStream) {
        final List<BookCreature> bookCreaturesFromFile = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            List<BookCreature> parsedCreatures = mapper.readValue(inputStream, new TypeReference<List<BookCreature>>() {});
            User currentUser = getCurrentUser();

            for (BookCreature bookCreature : parsedCreatures) {
                bookCreature.setUser(currentUser);

                if (bookCreature.getCreatureLocation() != null) {
                    MagicCity newCity = new MagicCity();
                    MagicCity providedCity = bookCreature.getCreatureLocation();

                    newCity.setName(providedCity.getName());
                    newCity.setArea(providedCity.getArea());
                    newCity.setPopulation(providedCity.getPopulation());
                    newCity.setPopulationDensity(providedCity.getPopulationDensity());
                    newCity.setGovernor(providedCity.getGovernor());
                    newCity.setCapital(providedCity.isCapital());
                    newCity.setEstablishmentDate(providedCity.getEstablishmentDate());
                    newCity.setUser(currentUser);

                    bookCreature.setCreatureLocation(newCity);
                }

                if (bookCreature.getRing() != null) {
                    Ring newRing = new Ring();
                    Ring providedRing = bookCreature.getRing();

                    newRing.setName(providedRing.getName());
                    newRing.setPower(providedRing.getPower());
                    newRing.setUser(currentUser);

                    bookCreature.setRing(newRing);
                }

                if (!validateBookCreature(bookCreature)) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN,
                                    "Некоторые данные не прошли валидацию. Проверьте ввод.",
                                    null));
                    return Collections.emptyList();
                }

                bookCreaturesFromFile.add(bookCreature);
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при парсинге файла: " + e.getMessage(), e);
        }

        return bookCreaturesFromFile;
    }

    private boolean validateBookCreature(BookCreature bookCreature) {
        boolean isValid = true;

        if (bookCreature.getName() == null || bookCreature.getName().trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("name", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Введите имя!", "Введите имя!"));
            isValid = false;
        }

        Object x = bookCreature.getCoordinates().getX();
        if (String.valueOf(bookCreature.getCoordinates().getX()) == null || bookCreature.getCoordinates().getX() >= 488 || !(x instanceof Integer)) {
            FacesContext.getCurrentInstance().addMessage("x", new FacesMessage(FacesMessage.SEVERITY_ERROR, "X обязательно к заполнению и должно быть меньше 488", "X обязательно к заполнению и должно быть меньше 488"));
            isValid = false;
        }

        Object y = bookCreature.getCoordinates().getY();
        if (String.valueOf(bookCreature.getCoordinates().getY()) == null || !(y instanceof Integer)) {
            FacesContext.getCurrentInstance().addMessage("y", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Y обязательно к заполнению", "Y обязательно к заполнению"));
            isValid = false;
        }

        if (bookCreature.getAge() == null || !(bookCreature.getAge() instanceof Long) || (bookCreature.getAge() < 0)) {
            FacesContext.getCurrentInstance().addMessage("age", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Age обязательно к заполнению и должно быть > 0", "Age обязательно к заполнению и должно быть > 0"));
            isValid = false;
        }

        if (bookCreature.getCreatureType() == null) {
            FacesContext.getCurrentInstance().addMessage("creatureType", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Creature Type обязательно к заполнению", "Creature Type обязательно к заполнению"));
            isValid = false;
        }

        if (Objects.equals(bookCreature.getCreatureLocation().getName(), "Mordor")) {
            FacesContext.getCurrentInstance().addMessage("cityName", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Нельзя выбрать такое имя для города!", "Нельзя выбрать такое имя для города!"));
            isValid = false;
        }

        if (bookCreature.getCreatureLocation().getName() == null || bookCreature.getCreatureLocation().getName().trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("cityName", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Magic City Введите имя!", "Magic City Введите имя!"));
            isValid = false;
        }

        if (bookCreature.getCreatureLocation().getArea() == null || bookCreature.getCreatureLocation().getArea() <= 0) {
            FacesContext.getCurrentInstance().addMessage("cityArea", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Magic City Поле Area обязательно к заполнению и должно быть больше 0", "Magic City Поле Area обязательно к заполнению и должно быть больше 0"));
            isValid = false;
        }

        Object p = bookCreature.getCreatureLocation().getPopulation();
        if (bookCreature.getCreatureLocation().getPopulation() == null || bookCreature.getCreatureLocation().getPopulation() <= 0 || !(p instanceof Integer)) {
            FacesContext.getCurrentInstance().addMessage("cityPopulation", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Magic City Поле Population обязательно к заполнению и должно быть больше 0", "Magic City Поле Population обязательно к заполнению и должно быть больше 0"));
            isValid = false;
        }

        Object d = bookCreature.getCreatureLocation().getPopulationDensity();
        if (String.valueOf(bookCreature.getCreatureLocation().getPopulationDensity()) == null || bookCreature.getCreatureLocation().getPopulationDensity() <= 0 || !(d instanceof Integer)) {
            FacesContext.getCurrentInstance().addMessage("cityPopulationDensity", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Magic City Поле Population Density обязательно к заполнению и должно быть больше 0", "Magic City Поле Population Density обязательно к заполнению и должно быть больше 0"));
            isValid = false;
        }

        if (bookCreature.getAttackLevel() == null || bookCreature.getAttackLevel() <= 0) {
            FacesContext.getCurrentInstance().addMessage("attackLevel", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Attack Level обязательно к заполнению и больше 0", "Attack Level обязательно к заполнению и больше 0"));
            isValid = false;
        }

        if (bookCreature.getDefenseLevel() == null || bookCreature.getDefenseLevel() <= 0) {
            FacesContext.getCurrentInstance().addMessage("defenseLevel", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Defense Level обязательно к заполнению и больше 0e", "Defense Level обязательно к заполнению и больше 0e"));
            isValid = false;
        }

        if (bookCreature.getRing().getName() == null || bookCreature.getRing().getName().trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("ringName", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ring Введите имя!", "Ring Введите имя!"));
            isValid = false;
        }

        Object power = bookCreature.getRing().getPower();
        if (bookCreature.getRing().getPower() == null || bookCreature.getRing().getPower() <= 0 || !(power instanceof Integer)) {
            FacesContext.getCurrentInstance().addMessage("ringPower", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ring Power обязательно к заполнению и больше 0", "Ring Power обязательно к заполнению и больше 0"));
            isValid = false;
        }

        return isValid;
    }
}
