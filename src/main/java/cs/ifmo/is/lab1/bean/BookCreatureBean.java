package cs.ifmo.is.lab1.bean;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cs.ifmo.is.lab1.dto.PaginatedResponse;
import cs.ifmo.is.lab1.model.*;
import cs.ifmo.is.lab1.service.BookCreatureService;
import cs.ifmo.is.lab1.service.ImportHistoryService;
import cs.ifmo.is.lab1.service.UserService;
import cs.ifmo.is.lab1.validator.Validator;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIInput;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.Part;
import org.primefaces.model.file.UploadedFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

@Named
@SessionScoped
public class BookCreatureBean implements Serializable {

    @Inject
    private BookCreatureService bookCreatureService;
    @Inject
    private UserService userService;

    @Inject
    private ImportHistoryService  importHistoryService;

    private List<BookCreature> bookCreatures;
    private BookCreature bookCreature;
    private PaginatedResponse<BookCreature> paginatedResponse;
    private int page = 1;
    private int pageSize = 5;
    private String sortField = "id";
    private boolean sortAscending = true;

    private Integer filterId;
    private String filterName;
    private Long filterAge;
    private String filterCoordinatesX;
    private String filterCoordinatesY;
    private String filterCreationDate;
    private String filterCreatureType;
    private String filterCreatureLocation;
    private String filterAttackLevel;
    private String filterDefenseLevel;
    private String filterRing;

    @PostConstruct
    public void init() {
        if (bookCreature == null) {
            bookCreature = new BookCreature();
            bookCreature.setName("Default Name");
            bookCreature.setCoordinates(new Coordinates(2, 2));
            bookCreature.setCreationDate(new Date());
            bookCreature.setAge(2L);
            bookCreature.setCreatureType(BookCreatureType.HOBBIT);
            User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
            bookCreature.setCreatureLocation(new MagicCity("Default City", 100.0, 1000, new Date(), MagicCity.GovernorType.GOLLUM, true, 10, currentUser));
            bookCreature.setAttackLevel(5F);
            bookCreature.setDefenseLevel(5F);
            bookCreature.setRing(new Ring("Default Ring", 10, currentUser));
        }
        addDefaultBookCreaturesIfNotExist();
    }




    public void updateBookCreatures() {
        loadPaginatedBookCreatures();

    }

    private void addDefaultBookCreaturesIfNotExist() {
        User currentUser = new User();
        currentUser.setId(1);
        currentUser.setUsername("a");

        List<BookCreature> existingCreatures = bookCreatureService.findByUserId(currentUser.getId());
        if (existingCreatures.isEmpty()) {
            for (int i = 0; i < 3; i++) {
                BookCreature defaultBookCreature = new BookCreature();
                defaultBookCreature.setName("Example " + (i + 1));
                defaultBookCreature.setCoordinates(new Coordinates(60+i*20, 60+i*20));
                defaultBookCreature.setCreationDate(new Date());
                defaultBookCreature.setAge(2L);
                defaultBookCreature.setCreatureType(BookCreatureType.HOBBIT);
                defaultBookCreature.setCreatureLocation(new MagicCity("TEST", 100.0, 1000, new Date(), MagicCity.GovernorType.HOBBIT, true, 10, currentUser));
                defaultBookCreature.setAttackLevel(5F);
                defaultBookCreature.setDefenseLevel(5F);
                defaultBookCreature.setRing(new Ring("Example Ring " + (i + 1), 10, currentUser));
                defaultBookCreature.setUser(currentUser);

                bookCreatureService.create(defaultBookCreature);
            }
            MagicCity magicCity = new MagicCity("Mordor", 100.0, 1000, new Date(), MagicCity.GovernorType.HOBBIT, true, 10, currentUser);
            bookCreatureService.create(magicCity);

        }
    }

    public String addToDatabase() {
        if (validateAllFields()) {
            BookCreature newBookCreature = new BookCreature();
            newBookCreature.setName(this.bookCreature.getName());
            newBookCreature.setCoordinates(new Coordinates(this.bookCreature.getCoordinates().getX(), this.bookCreature.getCoordinates().getY()));
            newBookCreature.setCreationDate(new Date());
            newBookCreature.setAge(this.bookCreature.getAge());
            newBookCreature.setCreatureType(this.bookCreature.getCreatureType());
            newBookCreature.setAttackLevel(this.bookCreature.getAttackLevel());
            newBookCreature.setDefenseLevel(this.bookCreature.getDefenseLevel());

            User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
            newBookCreature.setRing(new Ring(this.bookCreature.getRing().getName(), this.bookCreature.getRing().getPower(), currentUser));
            newBookCreature.setCreatureLocation(new MagicCity(this.bookCreature.getCreatureLocation().getName(), this.bookCreature.getCreatureLocation().getArea(),
                    this.bookCreature.getCreatureLocation().getPopulation(), new Date(), this.bookCreature.getCreatureLocation().getGovernor(),
                    this.bookCreature.getCreatureLocation().isCapital(), this.bookCreature.getCreatureLocation().getPopulationDensity(), currentUser));
            newBookCreature.setUser(currentUser);

            bookCreatureService.create(newBookCreature);
            loadPaginatedBookCreatures();

            return "index?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please correct the errors in the form.", "Please correct the errors in the form."));
            return null;
        }
    }


    public String addDefaultBookCreature() {
        BookCreature defaultBookCreature = new BookCreature();
        defaultBookCreature.setName("Example");
        defaultBookCreature.setCoordinates(new Coordinates(2, 2));
        defaultBookCreature.setCreationDate(new Date());
        defaultBookCreature.setAge(2L);
        defaultBookCreature.setCreatureType(BookCreatureType.HOBBIT);
        defaultBookCreature.setAttackLevel(5F);
        defaultBookCreature.setDefenseLevel(5F);
        User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
        defaultBookCreature.setRing(new Ring("Example Ring", 10, currentUser));
        defaultBookCreature.setCreatureLocation(new MagicCity("Example City", 100.0, 1000, new Date(), MagicCity.GovernorType.HOBBIT, true, 10, currentUser));
        defaultBookCreature.setUser(currentUser);

        bookCreatureService.create(defaultBookCreature);
        loadPaginatedBookCreatures();
        return "index?faces-redirect=true";
    }

    public String addToDatabaseShort() {
        if (validateAllFieldsShort()) {
            BookCreature bookCreature = new BookCreature();
            bookCreature.setName(this.bookCreature.getName());
            bookCreature.setCoordinates(new Coordinates(this.bookCreature.getCoordinates().getX(), this.bookCreature.getCoordinates().getY()));
            bookCreature.setCreationDate(new Date());
            bookCreature.setAge(this.bookCreature.getAge());
            bookCreature.setCreatureType(this.bookCreature.getCreatureType());


            MagicCity magicCity = bookCreatureService.findMagicCityById(this.magicCityId);
            if (magicCity != null) {
                bookCreature.setCreatureLocation(magicCity);
            } else {
                FacesContext.getCurrentInstance().addMessage("magicCityId", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Magic City ID is required and must exist", "Magic City ID is required and must exist"));
                return null;
            }

            bookCreature.setAttackLevel(this.bookCreature.getAttackLevel());
            bookCreature.setDefenseLevel(this.bookCreature.getDefenseLevel());

            Ring ring = bookCreatureService.findRingById(this.ringId);
            if (ring != null) {
                bookCreature.setRing(ring);
            } else {
                FacesContext.getCurrentInstance().addMessage("ringId", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ring ID is required and must exist", "Ring ID is required and must exist"));
                return null;
            }

            User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
            bookCreature.setUser(currentUser);

            bookCreatureService.createShort(bookCreature);
            loadPaginatedBookCreatures();
            return "index?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please correct the errors in the form.", "Please correct the errors in the form."));
            return null;
        }
    }

    public boolean isUserLoggedIn() {
        User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
        return currentUser != null;
    }

    public void loadBookCreatures() {
        bookCreatures = bookCreatureService.findAll();
    }

    public List<BookCreature> getBookCreatures() {
        if (bookCreatures == null) {
            loadBookCreatures();
        }
        return bookCreatures;
    }

    public PaginatedResponse<BookCreature> getPaginatedResponse() {
        if (paginatedResponse == null) {
            loadPaginatedBookCreatures();
        }
        return paginatedResponse;
    }

    public void previousPage() {
        if (page > 1) {
            page--;
            loadPaginatedBookCreatures();
        }
    }

    public void nextPage() {
        if (page * pageSize < bookCreatureService.count()) {
            page++;
            loadPaginatedBookCreatures();
        }
    }

    public void loadPaginatedBookCreatures() {
        List<BookCreatureType> matchingCreatureTypes = getMatchingCreatureTypes(filterCreatureType);
        paginatedResponse = new PaginatedResponse<>(
                bookCreatureService.findAll(page, pageSize, filterId, filterName, filterAge, filterCoordinatesX, filterCoordinatesY, filterCreationDate, matchingCreatureTypes, filterCreatureLocation, filterAttackLevel, filterDefenseLevel, filterRing, sortField, sortAscending),
                bookCreatureService.count(filterId, filterName, filterAge, filterCoordinatesX, filterCoordinatesY, filterCreationDate, matchingCreatureTypes, filterCreatureLocation, filterAttackLevel, filterDefenseLevel, filterRing),
                page, pageSize
        );
    }

    private Integer creatureId;

    public Integer getCreatureId() {
        return creatureId;
    }

    public void setCreatureId(Integer creatureId) {
        this.creatureId = creatureId;
    }

    public String edit(Integer id) {
        if (id == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid ID provided.", "Invalid ID provided."));
            return null;
        }

        System.out.println("Editing book creature with ID: " + id);
        BookCreature bookCreature = bookCreatureService.findById(id);
        User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
        if (bookCreature.getUser().getId().equals(currentUser.getId()) || currentUser.getRole() == User.Role.ADMIN) {
            this.bookCreature = bookCreature;
            if (this.bookCreature.getRing() == null) {
                this.bookCreature.setRing(new Ring());
            }
            return "edit?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Это не ваш объект, вы не можете его редактировать", "Это не ваш объект, вы не можете его редактировать"));
            return null;
        }
    }

    public String delete(Integer id) {
        BookCreature bookCreature = bookCreatureService.findById(id);
        User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
        if (bookCreature.getUser().getId().equals(currentUser.getId()) || currentUser.getRole() == User.Role.ADMIN) {
            bookCreatureService.delete(id);
            loadPaginatedBookCreatures();
            return "index?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "You do not have permission to delete this record.", "You do not have permission to delete this record."));
            return null;
        }
    }

    public String create() {
        try {
            User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
            bookCreature.setUser(currentUser);
            bookCreatureService.create(bookCreature);
            loadPaginatedBookCreatures();
            return "index?faces-redirect=true";
        } catch (Exception e) {
            throw e;
        }
    }

    public String update() {
        if (validateAllFields()) {
            try {
                User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");

                // Проверяем, что текущий пользователь имеет право редактировать объект
                if (bookCreature.getUser().getId().equals(currentUser.getId()) || currentUser.getRole() == User.Role.ADMIN) {
                    // Не изменяем поле user, если оно уже установлено
                    if (bookCreature.getUser() == null) {
                        bookCreature.setUser(currentUser);
                    }

                    if (bookCreature.getRing() == null || bookCreature.getRing().getId() == null) {
                        Ring newRing = new Ring();
                        newRing.setName(bookCreature.getRing().getName());
                        newRing.setPower(bookCreature.getRing().getPower());
                        newRing.setUser(currentUser);
                        bookCreatureService.createRing(newRing);
                        bookCreature.setRing(newRing);
                    } else {
                        bookCreature.getRing().setUser(currentUser);
                    }

                    bookCreatureService.update(bookCreature);
                    loadPaginatedBookCreatures();
                    return "index?faces-redirect=true";
                } else {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Это не ваш объект, вы не можете его редактировать", "Это не ваш объект, вы не можете его редактировать"));
                    return null;
                }
            } catch (Exception e) {
                throw e;
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please correct the errors in the form.", "Please correct the errors in the form."));
            return null;
        }
    }




    public BookCreature getBookCreatureById(Integer id) {
        return bookCreatureService.findById(id);
    }

    public List<BookCreatureType> getBookCreatureTypes() {
        return Arrays.asList(BookCreatureType.values());
    }

    public List<MagicCity.GovernorType> getGovernorTypes() {
        return Arrays.asList(MagicCity.GovernorType.values());
    }

    public String getBookCreatureInfoById(Integer id) {
        bookCreature = bookCreatureService.findById(id);
        return "bookCreatureInfo?faces-redirect=true";
    }

    public void sort(String field) {
        if (sortField.equals(field)) {
            sortAscending = !sortAscending;
        } else {
            sortField = field;
            sortAscending = true;
        }
        loadPaginatedBookCreatures();
    }

    public Integer getFilterId() {
        return filterId;
    }

    public void setFilterId(Integer filterId) {
        this.filterId = filterId;
        loadPaginatedBookCreatures();
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
        loadPaginatedBookCreatures();
    }

    public Long getFilterAge() {
        return filterAge;
    }

    public void setFilterAge(Long filterAge) {
        this.filterAge = filterAge;
        loadPaginatedBookCreatures();
    }

    public String getFilterCoordinatesX() {
        return filterCoordinatesX;
    }

    public void setFilterCoordinatesX(String filterCoordinatesX) {
        this.filterCoordinatesX = filterCoordinatesX;
        loadPaginatedBookCreatures();
    }

    public String getFilterCoordinatesY() {
        return filterCoordinatesY;
    }

    public void setFilterCoordinatesY(String filterCoordinatesY) {
        this.filterCoordinatesY = filterCoordinatesY;
        loadPaginatedBookCreatures();
    }

    public String getFilterCreationDate() {
        return filterCreationDate;
    }

    public void setFilterCreationDate(String filterCreationDate) {
        this.filterCreationDate = filterCreationDate;
        loadPaginatedBookCreatures();
    }

    public String getFilterCreatureType() {
        return filterCreatureType;
    }

    public void setFilterCreatureType(String filterCreatureType) {
        this.filterCreatureType = filterCreatureType;
        loadPaginatedBookCreatures();
    }

    public String getFilterCreatureLocation() {
        return filterCreatureLocation;
    }

    public void setFilterCreatureLocation(String filterCreatureLocation) {
        this.filterCreatureLocation = filterCreatureLocation;
        loadPaginatedBookCreatures();
    }

    public String getFilterAttackLevel() {
        return filterAttackLevel;
    }

    public void setFilterAttackLevel(String filterAttackLevel) {
        this.filterAttackLevel = filterAttackLevel;
        loadPaginatedBookCreatures();
    }

    public String getFilterDefenseLevel() {
        return filterDefenseLevel;
    }

    public void setFilterDefenseLevel(String filterDefenseLevel) {
        this.filterDefenseLevel = filterDefenseLevel;
        loadPaginatedBookCreatures();
    }

    public String getFilterRing() {
        return filterRing;
    }

    public void setFilterRing(String filterRing) {
        this.filterRing = filterRing;
        loadPaginatedBookCreatures();
    }

    public BookCreatureType getBookCreatureTypeFromString(String creatureTypeString) {
        if (creatureTypeString == null || creatureTypeString.isEmpty()) {
            return null;
        }
        try {
            return BookCreatureType.valueOf(creatureTypeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public List<BookCreatureType> getMatchingCreatureTypes(String creatureTypeString) {
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

    public String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    private Validator validator = new Validator();

    public void validateAge(AjaxBehaviorEvent event) {
        validator.validateAge(event);
    }

    public void validateX(AjaxBehaviorEvent event) {
        validator.validateX(event);
    }

    public void validateY(AjaxBehaviorEvent event) {
        validator.validateY(event);
    }

    public void validateCityArea(AjaxBehaviorEvent event) {
        validator.validateCityArea(event);
    }

    public void validateCityPopulation(AjaxBehaviorEvent event) {
        validator.validateCityPopulation(event);
    }

    public void validateCityPopulationDensity(AjaxBehaviorEvent event) {
        validator.validateCityPopulationDensity(event);
    }

    public void validateAttackLevel(AjaxBehaviorEvent event) {
        validator.validateAttackLevel(event);
    }

    public void validateDefenseLevel(AjaxBehaviorEvent event) {
        validator.validateDefenseLevel(event);
    }

    public void validateRingName(AjaxBehaviorEvent event) {
        validator.validateRingName(event);
    }

    public void validateRingPower(AjaxBehaviorEvent event) {
        validator.validateRingPower(event);
    }

    public void validateMagicCityAndRingId(AjaxBehaviorEvent event) {
        validator.validateMagicCityAndRingId(event);
    }

    public boolean validateAllFields() {
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
            FacesContext.getCurrentInstance().addMessage("age", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Age обязательно к заполнению", "Age обязательно к заполнению"));
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
    public boolean validateAllFieldsShort() {
        boolean isValid = true;

        if (bookCreature.getName() == null || bookCreature.getName().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("name", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Поле Name is обязательно к заполнению", "Введите имя!"));
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
            FacesContext.getCurrentInstance().addMessage("age", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Age обязательно к заполнению", "Age обязательно к заполнению"));
            isValid = false;
        }

        if (bookCreature.getCreatureType() == null) {
            FacesContext.getCurrentInstance().addMessage("creatureType", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Creature Type обязательно к заполнению", "Creature Type обязательно к заполнению"));
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
        // Проверка существования MagicCity по ID
        if (magicCityId == null || bookCreatureService.findMagicCityById(magicCityId) == null) {
            FacesContext.getCurrentInstance().addMessage("magicCityId", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Magic City ID is required and must exist", "Magic City ID is required and must exist"));
            isValid = false;
        }

        // Проверка существования Ring по ID
        if (ringId == null || bookCreatureService.findRingById(ringId) == null) {
            FacesContext.getCurrentInstance().addMessage("ringId", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ring ID is required and must exist", "Ring ID is required and must exist"));
            isValid = false;
        }

        return isValid;
    }

    private Integer magicCityId;
    private Integer ringId;
    private int magicCityPage = 0;
    private int ringPage = 0;
    private static final int PAGE_SIZE = 5;



    // Геттеры и сеттеры для magicCityId и ringId
    public Integer getMagicCityId() {
        return magicCityId;
    }

    public void setMagicCityId(Integer magicCityId) {
        this.magicCityId = magicCityId;
    }

    public Integer getRingId() {
        return ringId;
    }

    public void setRingId(Integer ringId) {
        this.ringId = ringId;
    }

    // Геттеры для magicCities и rings
    public List<MagicCity> getMagicCities() {
        List<MagicCity> magicCities = bookCreatureService.findAllMagicCities();
        int fromIndex = magicCityPage * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, magicCities.size());
        return magicCities.subList(fromIndex, toIndex);
    }

    public List<Ring> getRings() {
        List<Ring> rings = bookCreatureService.findAllRings();
        int fromIndex = ringPage * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, rings.size());
        return rings.subList(fromIndex, toIndex);
    }

    public int getMagicCityPage() {
        return magicCityPage;
    }

    public void setMagicCityPage(int magicCityPage) {
        this.magicCityPage = magicCityPage;
    }

    public int getRingPage() {
        return ringPage;
    }

    public void setRingPage(int ringPage) {
        this.ringPage = ringPage;
    }

    public int getMagicCityTotalPages() {
        return (int) Math.ceil((double) bookCreatureService.findAllMagicCities().size() / PAGE_SIZE);
    }

    public int getRingTotalPages() {
        return (int) Math.ceil((double) bookCreatureService.findAllRings().size() / PAGE_SIZE);
    }

    public BookCreatureService getBookCreatureService() {
        return bookCreatureService;
    }

    public BookCreature getBookCreature() {
        return bookCreature;
    }

    public void setBookCreature(BookCreature bookCreature) {
        this.bookCreature = bookCreature;
    }

    public void nextMagicCityPage() {
        if (magicCityPage < getMagicCityTotalPages() - 1) {
            magicCityPage++;
        }
    }

    public void previousMagicCityPage() {
        if (magicCityPage > 0) {
            magicCityPage--;
        }
    }

    public void nextRingPage() {
        if (ringPage < getRingTotalPages() - 1) {
            ringPage++;
        }
    }

    public void previousRingPage() {
        if (ringPage > 0) {
            ringPage--;
        }
    }
    private Float filterAttackLevelFloat;

    private String deleteResult;
    private String minCoordinatesResult;
    private String uniqueRingsResult;
    private String takeRingsResult;
    private String moveHobbitsResult;
    private List<BookCreature> deletedCreatures;
    // Getters and setters for filterAttackLevel

    public void deleteByAttackLevel(String attackLevel) {
        try {
            Float attackLevelFloat = Float.parseFloat(attackLevel);
            if (attackLevelFloat < 0) {
                deleteResult = "Некорректный ввод. Число не может быть отрицательным.";
                return;
            }
            User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
            deletedCreatures = bookCreatureService.deleteByAttackLevel(attackLevelFloat, currentUser.getId());
            deleteResult = "Объекты с attackLevel " + attackLevelFloat + " удалены.";
        } catch (NumberFormatException e) {
            deleteResult = "Некорректный ввод. Пожалуйста, введите число с плавающей точкой.";
        } catch (Exception e) {
            deleteResult = "Ошибка при удалении объектов: " + e.getMessage();
        }
    }


    public List<BookCreature> getDeletedCreatures() {
        return deletedCreatures;
    }

    public void setDeletedCreatures(List<BookCreature> deletedCreatures) {
        this.deletedCreatures = deletedCreatures;
    }

    public BookCreature getMinCoordinatesCreature() {
        try {
            BookCreature creature = bookCreatureService.getMinCoordinatesCreature();
            minCoordinatesResult = " " + creature.getCoordinates().getX() + ", " + creature.getCoordinates().getY();
            return creature;
        } catch (Exception e) {
            minCoordinatesResult = "Ошибка при получении минимальных координат: " + e.getMessage();
            return null;
        }
    }

    public List<String> getUniqueRingNames() {
        try {
            List<String> uniqueRings = bookCreatureService.getUniqueRingNames();
            uniqueRingsResult = " " + String.join(", ", uniqueRings);
            return uniqueRings;
        } catch (Exception e) {
            uniqueRingsResult = "Ошибка при получении уникальных колец: " + e.getMessage();
            return null;
        }
    }

    public void takeRingsFromHobbits() {
        try {
            User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
            bookCreatureService.takeRingsFromHobbits(currentUser.getId());
            takeRingsResult = "Кольца забраны у хоббитов.";
        } catch (Exception e) {
            takeRingsResult = "Ошибка при заборе колец у хоббитов: " + e.getMessage();
        }
    }

    public void moveHobbitsWithRingsToMordor() {
        try {
            User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
            bookCreatureService.moveHobbitsWithRingsToMordor(currentUser.getId());
            moveHobbitsResult = "Хоббиты с кольцами перемещены в Мордор.";
        } catch (Exception e) {
            moveHobbitsResult = "Ошибка при перемещении хоббитов в Мордор. Мордор исчез. ";
        }
    }

    public List<BookCreature> getHobbitsWithRingsInMordor() {
        return bookCreatureService.getHobbitsWithRingsInMordor();
    }
    public String getMoveHobbitsResult() {
        return moveHobbitsResult;
    }
    public Float getFilterAttackLevelFloat() {
        return filterAttackLevelFloat;
    }

    public String getDeleteResult() {
        return deleteResult;
    }

    public String getMinCoordinatesResult() {
        return minCoordinatesResult;
    }

    public String getUniqueRingsResult() {
        return uniqueRingsResult;
    }

    public String getTakeRingsResult() {
        return takeRingsResult;
    }
}