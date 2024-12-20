package cs.ifmo.is.lab1.bean;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cs.ifmo.is.lab1.model.*;
import cs.ifmo.is.lab1.service.BookCreatureService;
import cs.ifmo.is.lab1.service.ImportHistoryService;
import cs.ifmo.is.lab1.service.UserService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

@Named
@RequestScoped
public class FileImportBean implements Serializable {

    private BookCreature bookCreature;
    @Inject
    private BookCreatureService bookCreatureService;

    @Inject
    private ImportHistoryService importHistoryService;

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
        System.out.println("Uploaded file size: " + uploadedFile.getSize());
        System.out.println("Uploaded file content type: " + uploadedFile.getContentType());

        if (uploadedFile != null) {
            User currentUser = getCurrentUser();
            int addedObjects = 0;
            try (InputStream inputStream = uploadedFile.getInputStream()) {
                List<BookCreature> bookCreatures = parseFile(inputStream);

                checkForDuplicatesInFile(bookCreatures);

                List<String> duplicateNamesInDb = checkForDuplicatesInDatabase(bookCreatures);

                if (!duplicateNamesInDb.isEmpty()) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Ошибка: Следующие имена уже существуют в базе данных: " + String.join(", ", duplicateNamesInDb),
                                    null));
                    importHistoryService.saveImportHistory(currentUser, "Неуспешно", addedObjects);
                    return;
                }

                for (BookCreature bookCreature : bookCreatures) {
                    if (!validateBookCreature(bookCreature)) {
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                        "Ошибка: Один или несколько объектов содержат некорректные данные. Импорт отменен.",
                                        null));
                        importHistoryService.saveImportHistory(currentUser, "Неуспешно", addedObjects);
                        return;
                    }
                }

                bookCreatureService.importBookCreatures(bookCreatures);
                addedObjects = bookCreatures.size();

                String status = (addedObjects > 0) ? "Успешно" : "Неуспешно";
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        addedObjects > 0 ? "Импорт успешно завершен." : "Импорт не завершён", null));
                importHistoryService.saveImportHistory(currentUser, status, addedObjects);

            } catch (IllegalArgumentException e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Ошибка валидации: " + e.getMessage(), null));
                importHistoryService.saveImportHistory(currentUser, "Неуспешно", addedObjects);
            } catch (IOException e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Ошибка при чтении файла.", null));
                importHistoryService.saveImportHistory(currentUser, "Неуспешно", addedObjects);
            } catch (Exception e) {
                e.printStackTrace();
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Ошибка при импорте данных!", null));
                importHistoryService.saveImportHistory(currentUser, "Неуспешно", addedObjects);
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Пожалуйста, выберите файл для импорта.", null));
        }
    }

    public List<String> checkForDuplicatesInDatabase(List<BookCreature> bookCreatures) {
        List<String> duplicateNames = new ArrayList<>();
        for (BookCreature bookCreature : bookCreatures) {
            if (bookCreatureService.isNameExists(bookCreature.getName())) {
                duplicateNames.add(bookCreature.getName());
            }
        }
        return duplicateNames;
    }

    private String extractDuplicateValue(String message) {
        String prefix = "Key (name)=";
        int startIndex = message.indexOf(prefix);
        if (startIndex != -1) {
            startIndex += prefix.length();
            int endIndex = message.indexOf(" already exists", startIndex);
            if (endIndex != -1) {
                return message.substring(startIndex, endIndex).trim();
            }
        }
        return "неизвестное значение";
    }

    public void checkForDuplicatesInFile(List<BookCreature> bookCreatures) {
        Set<String> seenNames = new HashSet<>();
        List<String> duplicateNames = new ArrayList<>();

        for (BookCreature creature : bookCreatures) {
            if (!seenNames.add(creature.getName())) {
                duplicateNames.add(creature.getName());
            }
        }

        if (!duplicateNames.isEmpty()) {
            throw new IllegalArgumentException("Файл содержит дубликаты имён: " + String.join(", ", duplicateNames));
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

    public boolean validateBookCreature(BookCreature bookCreature) {
        boolean isValid = true;

        // Проверка имени существа
        if (bookCreature.getName() == null || bookCreature.getName().trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("name", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Введите имя!", "Введите имя!"));
            isValid = false;
        }

        // Проверка координат X
        Object x = bookCreature.getCoordinates().getX();
        if (String.valueOf(x).trim().isEmpty() || !(x instanceof Integer) || (Integer) x >= 488) {
            FacesContext.getCurrentInstance().addMessage("x", new FacesMessage(FacesMessage.SEVERITY_ERROR, "X обязательно к заполнению и должно быть меньше 488", "X обязательно к заполнению и должно быть меньше 488"));
            isValid = false;
        }

        // Проверка координат Y
        Object y = bookCreature.getCoordinates().getY();
        if (String.valueOf(y).trim().isEmpty() || !(y instanceof Integer)) {
            FacesContext.getCurrentInstance().addMessage("y", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Y обязательно к заполнению", "Y обязательно к заполнению"));
            isValid = false;
        }

        // Проверка возраста
        Object age = bookCreature.getAge();
        if (String.valueOf(age).trim().isEmpty() || !(age instanceof Long) || (Long) age < 0) {
            FacesContext.getCurrentInstance().addMessage("age", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Age обязательно к заполнению и должно быть > 0", "Age обязательно к заполнению и должно быть > 0"));
            isValid = false;
        }

        // Проверка типа существа
        if (bookCreature.getCreatureType() == null || String.valueOf(bookCreature.getCreatureType()).trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("creatureType", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Creature Type обязательно к заполнению", "Creature Type обязательно к заполнению"));
            isValid = false;
        }

        // Проверка имени локации существа
        String locationName = bookCreature.getCreatureLocation().getName();
        if (Objects.equals(locationName, "Mordor")) {
            FacesContext.getCurrentInstance().addMessage("cityName", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Нельзя выбрать такое имя для города!", "Нельзя выбрать такое имя для города!"));
            isValid = false;
        }
        if (locationName == null || locationName.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("cityName", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Magic City Введите имя!", "Magic City Введите имя!"));
            isValid = false;
        }

        // Проверка площади локации
        Object area = bookCreature.getCreatureLocation().getArea();
        if (String.valueOf(area).trim().isEmpty() || !(area instanceof Integer) || (Integer) area <= 0) {
            FacesContext.getCurrentInstance().addMessage("cityArea", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Magic City Поле Area обязательно к заполнению и должно быть больше 0", "Magic City Поле Area обязательно к заполнению и должно быть больше 0"));
            isValid = false;
        }

        // Проверка населения локации
        Object population = bookCreature.getCreatureLocation().getPopulation();
        if (String.valueOf(population).trim().isEmpty() || !(population instanceof Integer) || (Integer) population <= 0) {
            FacesContext.getCurrentInstance().addMessage("cityPopulation", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Magic City Поле Population обязательно к заполнению и должно быть больше 0", "Magic City Поле Population обязательно к заполнению и должно быть больше 0"));
            isValid = false;
        }

        // Проверка плотности населения
        Object density = bookCreature.getCreatureLocation().getPopulationDensity();
        if (String.valueOf(density).trim().isEmpty() || !(density instanceof Integer) || (Integer) density <= 0) {
            FacesContext.getCurrentInstance().addMessage("cityPopulationDensity", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Magic City Поле Population Density обязательно к заполнению и должно быть больше 0", "Magic City Поле Population Density обязательно к заполнению и должно быть больше 0"));
            isValid = false;
        }

        // Проверка уровня атаки
        Object attackLevel = bookCreature.getAttackLevel();
        if (String.valueOf(attackLevel).trim().isEmpty() || !(attackLevel instanceof Integer) || (Integer) attackLevel <= 0) {
            FacesContext.getCurrentInstance().addMessage("attackLevel", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Attack Level обязательно к заполнению и больше 0", "Attack Level обязательно к заполнению и больше 0"));
            isValid = false;
        }

        // Проверка уровня защиты
        Object defenseLevel = bookCreature.getDefenseLevel();
        if (String.valueOf(defenseLevel).trim().isEmpty() || !(defenseLevel instanceof Integer) || (Integer) defenseLevel <= 0) {
            FacesContext.getCurrentInstance().addMessage("defenseLevel", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Defense Level обязательно к заполнению и больше 0", "Defense Level обязательно к заполнению и больше 0"));
            isValid = false;
        }

        // Проверка имени кольца
        String ringName = bookCreature.getRing().getName();
        if (ringName == null || ringName.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("ringName", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ring Введите имя!", "Ring Введите имя!"));
            isValid = false;
        }

        // Проверка силы кольца
        Object ringPower = bookCreature.getRing().getPower();
        if (String.valueOf(ringPower).trim().isEmpty() || !(ringPower instanceof Integer) || (Integer) ringPower <= 0) {
            FacesContext.getCurrentInstance().addMessage("ringPower", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ring Power обязательно к заполнению и больше 0", "Ring Power обязательно к заполнению и больше 0"));
            isValid = false;
        }

        return isValid;
    }

}
