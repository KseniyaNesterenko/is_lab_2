package cs.ifmo.is.lab1.bean;

import cs.ifmo.is.lab1.dto.PaginatedResponse;
import cs.ifmo.is.lab1.model.BookCreature;
import cs.ifmo.is.lab1.model.BookCreatureType;
import cs.ifmo.is.lab1.model.MagicCity;
import cs.ifmo.is.lab1.model.User;
import cs.ifmo.is.lab1.service.BookCreatureService;
import cs.ifmo.is.lab1.service.MagicCityService;
import cs.ifmo.is.lab1.validator.Validator;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Named
@SessionScoped
public class MagicCityBean implements Serializable {

    @Inject
    private MagicCityService magicCityService;
    @Inject
    private BookCreatureService bookCreatureService;

    private List<BookCreature> bookCreatures;

    private BookCreature bookCreature;

    private List<MagicCity> magicCities;
    private MagicCity magicCity;
    private PaginatedResponse<MagicCity> paginatedResponse;
    private int page = 1;
    private int pageSize = 5;
    private String sortField = "id";
    private boolean sortAscending = true;
    
    private String filterName;
    private String filterArea;
    private String filterPopulation;
    private String filterPopulationDensity;
    private String filterEstablishmentDate;
    private String filterGovernor;
    private Boolean filterCapital;


    @PostConstruct
    public void init() {
        if (magicCity == null) {
            magicCity = new MagicCity();
            magicCity.setName("Default City");
            magicCity.setArea(100.0);
            magicCity.setPopulation(1000);
            magicCity.setEstablishmentDate(new Date());
            magicCity.setGovernor(MagicCity.GovernorType.GOLLUM);
            magicCity.setCapital(true);
            magicCity.setPopulationDensity(10);
        }
        addDefaultMagicCitiesIfNotExist();
        loadBookCreatures();
    }

    public String addToDatabase() {
        if (validateAllFields()) {
            MagicCity newMagicCity = new MagicCity();
            newMagicCity.setName(this.magicCity.getName());
            newMagicCity.setArea(this.magicCity.getArea());
            newMagicCity.setPopulation(this.magicCity.getPopulation());
            newMagicCity.setEstablishmentDate(new Date());
            newMagicCity.setGovernor(this.magicCity.getGovernor());
            newMagicCity.setCapital(this.magicCity.isCapital());
            newMagicCity.setPopulationDensity(this.magicCity.getPopulationDensity());
            User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
            newMagicCity.setUser(currentUser);

            magicCityService.create(newMagicCity);
            loadPaginatedMagicCities();

            return "magicCity?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please correct the errors in the form.", "Please correct the errors in the form."));
            return null;
        }
    }

    private void addDefaultMagicCitiesIfNotExist() {
        List<MagicCity> existingCities = magicCityService.findAll();
        if (existingCities.isEmpty()) {
            for (int i = 0; i < 5; i++) {
                MagicCity defaultMagicCity = new MagicCity();
                defaultMagicCity.setName("Example City " + (i + 1));
                defaultMagicCity.setArea(100.0);
                defaultMagicCity.setPopulation(1000);
                defaultMagicCity.setEstablishmentDate(new Date());
                defaultMagicCity.setGovernor(MagicCity.GovernorType.GOLLUM);
                defaultMagicCity.setCapital(true);
                defaultMagicCity.setPopulationDensity(10);

                magicCityService.create(defaultMagicCity);
            }
        }
    }
    

    public void loadMagicCities() {
        magicCities = magicCityService.findAll();
    }

    public List<MagicCity> getMagicCities() {
        if (magicCities == null) {
            loadMagicCities();
        }
        return magicCities;
    }

    public PaginatedResponse<MagicCity> getPaginatedResponse() {
        if (paginatedResponse == null) {
            loadPaginatedMagicCities();
        }
        return paginatedResponse;
    }

    public void previousPage() {
        if (page > 1) {
            page--;
            loadPaginatedMagicCities();
        }
    }

    public void nextPage() {
        if (page * pageSize < magicCityService.count()) {
            page++;
            loadPaginatedMagicCities();
        }
    }

    public void loadPaginatedMagicCities() {
        paginatedResponse = new PaginatedResponse<>(
                magicCityService.findAll(page, pageSize, filterId, filterName, filterArea, filterPopulation, filterPopulationDensity, filterEstablishmentDate, filterGovernor, filterCapital, sortField, sortAscending),
                magicCityService.count(filterId, filterName, filterArea, filterPopulation, filterPopulationDensity, filterEstablishmentDate, filterGovernor, filterCapital),
                page, pageSize
        );
    }


    public String edit(Integer id) {
        MagicCity magicCity = magicCityService.findById(id);
        User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
        if (magicCity.getUser().getId().equals(currentUser.getId()) || currentUser.getRole() == User.Role.ADMIN) {
            this.magicCity = magicCity;
            return "editCity?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Это не ваш объект, вы не можете его редактировать", "Это не ваш объект, вы не можете его редактировать"));
            return null;
        }
    }

    public String delete(Integer id) {
        MagicCity magicCity = magicCityService.findById(id);
        User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");

        if (magicCity == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "MagicCity not found.", "MagicCity not found."));
            return null;
        }

        if (magicCity.getUser().getId().equals(currentUser.getId()) || currentUser.getRole() == User.Role.ADMIN) {
            String errorMessage = magicCityService.delete(id);
            if (errorMessage != null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMessage, errorMessage));
                return null;
            }
            loadPaginatedMagicCities();
            return "magicCity?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "You do not have permission to delete this record.", "You do not have permission to delete this record."));
            return null;
        }
    }


    public String create() {
        try {
            User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
            magicCity.setUser(currentUser);
            magicCityService.create(magicCity);
            loadPaginatedMagicCities();
            return "magicCity?faces-redirect=true";
        } catch (Exception e) {
            throw e;
        }
    }

    public String update() {
        if (validateAllFields()) {
            try {
                User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
                magicCity.setUser(currentUser);
                magicCityService.update(magicCity);
                loadPaginatedMagicCities();
                return "magicCity?faces-redirect=true";
            } catch (Exception e) {
                throw e;
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please correct the errors in the form.", "Please correct the errors in the form."));
            return null;
        }
    }

    public MagicCity getMagicCityById(Integer id) {
        return magicCityService.findById(id);
    }

    public String getMagicCityInfoById(Integer id) {
        magicCity = magicCityService.findById(id);
        loadBookCreatures();
        return "cityInfo?faces-redirect=true";
    }


    public void sort(String field) {
        if (sortField.equals(field)) {
            sortAscending = !sortAscending;
        } else {
            sortField = field;
            sortAscending = true;
        }
        loadPaginatedMagicCities();
    }

    private String filterId;

    public String getFilterId() {
        return filterId;
    }

    public void setFilterId(String filterId) {
        this.filterId = filterId;
        loadPaginatedMagicCities();
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
        loadPaginatedMagicCities();
    }

    public String getFilterArea() {
        return filterArea;
    }

    public void setFilterArea(String filterArea) {
        this.filterArea = filterArea;
        loadPaginatedMagicCities();
    }

    public String getFilterPopulation() {
        return filterPopulation;
    }

    public void setFilterPopulation(String filterPopulation) {
        this.filterPopulation = filterPopulation;
        loadPaginatedMagicCities();
    }

    public String getFilterPopulationDensity() {
        return filterPopulationDensity;
    }

    public void setFilterPopulationDensity(String filterPopulationDensity) {
        this.filterPopulationDensity = filterPopulationDensity;
        loadPaginatedMagicCities();
    }




    public void setFilterEstablishmentDate(String filterEstablishmentDate) {
        this.filterEstablishmentDate = filterEstablishmentDate;
        loadPaginatedMagicCities();
    }


    public String getFilterEstablishmentDate() {
        return filterEstablishmentDate;
    }


    public void setFilterGovernor(String filterGovernor) {
        this.filterGovernor = filterGovernor;
        loadPaginatedMagicCities();
    }

    public String getFilterGovernor() {
        return filterGovernor;
    }

    public Boolean getFilterCapital() {
        return filterCapital;
    }

    public void setFilterCapital(Boolean filterCapital) {
        this.filterCapital = filterCapital;
        loadPaginatedMagicCities();
    }

    private Validator validator = new Validator();


    public void validateCityArea(AjaxBehaviorEvent event) {
        validator.validateCityArea(event);
    }

    public void validateCityPopulation(AjaxBehaviorEvent event) {
        validator.validateCityPopulation(event);
    }

    public void validateCityPopulationDensity(AjaxBehaviorEvent event) {
        validator.validateCityPopulationDensity(event);
    }

    public String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }
   

    public boolean isUserLoggedIn() {
        User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
        return currentUser != null;
    }

    public boolean validateAllFields() {
        boolean isValid = true;

        if (magicCity.getName() == null || magicCity.getName().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("name", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Введите имя!", "Введите имя!"));
            isValid = false;
        }

        if (magicCity.getArea() == null || magicCity.getArea() <= 0) {
            FacesContext.getCurrentInstance().addMessage("area", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Поле Area обязательно к заполнению и должно быть больше 0", "Поле Area обязательно к заполнению и должно быть больше 0"));
            isValid = false;
        }

        if (magicCity.getPopulation() == null || magicCity.getPopulation() <= 0) {
            FacesContext.getCurrentInstance().addMessage("population", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Поле Population обязательно к заполнению и должно быть больше 0", "Поле Population обязательно к заполнению и должно быть больше 0"));
            isValid = false;
        }

        if (String.valueOf(magicCity.getPopulationDensity()) == null || magicCity.getPopulationDensity() <= 0) {
            FacesContext.getCurrentInstance().addMessage("populationDensity", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Поле Population Density обязательно к заполнению и должно быть больше 0", "Поле Population Density обязательно к заполнению и должно быть больше 0"));
            isValid = false;
        }

        if (magicCity.getGovernor() == null || String.valueOf(magicCity.getGovernor()).isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("governor", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Поле Governor обязательно к заполнению", "Поле Governor обязательно к заполнению"));
            isValid = false;
        }

        return isValid;
    }

    public MagicCityService getMagicCityService() {
        return magicCityService;
    }

    public MagicCity getMagicCity() {
        return magicCity;
    }

    public void setMagicCity(MagicCity magicCity) {
        this.magicCity = magicCity;
    }

    public void setMagicCityService(MagicCityService magicCityService) {
        this.magicCityService = magicCityService;
    }

    public void setMagicCities(List<MagicCity> magicCities) {
        this.magicCities = magicCities;
    }

    public void setPaginatedResponse(PaginatedResponse<MagicCity> paginatedResponse) {
        this.paginatedResponse = paginatedResponse;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public boolean isSortAscending() {
        return sortAscending;
    }

    public void setSortAscending(boolean sortAscending) {
        this.sortAscending = sortAscending;
    }

    public Validator getValidator() {
        return validator;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    public void sortByEstablishmentDate() {
        if (sortField.equals("establishmentDate")) {
            sortAscending = !sortAscending;
        } else {
            sortField = "establishmentDate";
            sortAscending = true;
        }
        loadPaginatedMagicCities();
    }


    public List<BookCreature> getBookCreatures() {
        if (bookCreatures == null) {
            loadBookCreatures();
        }
        return bookCreatures;
    }

    private void loadBookCreatures() {
        if (magicCity != null && magicCity.getId() != null) {
            bookCreatures = bookCreatureService.findByMagicCityId(magicCity.getId());
        }
    }
    



}
