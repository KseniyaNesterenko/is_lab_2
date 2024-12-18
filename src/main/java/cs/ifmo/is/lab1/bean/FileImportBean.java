package cs.ifmo.is.lab1.bean;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cs.ifmo.is.lab1.model.*;
import cs.ifmo.is.lab1.service.BookCreatureService;
import cs.ifmo.is.lab1.service.ImportHistoryService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
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

    public void importBookCreatures() {
        if (uploadedFile != null) {
            User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
            int addedObjects = 0;
            try (InputStream inputStream = uploadedFile.getInputStream()) {
                List<BookCreature> bookCreatures = parseFile(inputStream);

                // Проверяем дубликаты в самом файле
                checkForDuplicatesInFile(bookCreatures);

                // Проверяем дубликаты в базе данных
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

    private List<String> checkForDuplicatesInDatabase(List<BookCreature> bookCreatures) {
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

    private void checkForDuplicatesInFile(List<BookCreature> bookCreatures) {
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

    private List<BookCreature> parseFile(InputStream inputStream) {
        final List<BookCreature> bookCreaturesFromFile = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            List<BookCreature> parsedCreatures = mapper.readValue(inputStream, new TypeReference<List<BookCreature>>() {});
            User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");

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

        if (bookCreature.getName() == null || bookCreature.getName().isEmpty()) {
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

        if (bookCreature.getCreatureLocation().getName() == null || bookCreature.getCreatureLocation().getName().isEmpty()) {
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

        if (bookCreature.getRing().getName() == null || bookCreature.getRing().getName().isEmpty()) {
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

    public boolean validateAllFields() {
        return validateBookCreature(this.bookCreature);
    }
}
