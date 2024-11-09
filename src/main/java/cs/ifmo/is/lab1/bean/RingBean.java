package cs.ifmo.is.lab1.bean;

import com.google.gson.Gson;
import cs.ifmo.is.lab1.dto.PaginatedResponse;
import cs.ifmo.is.lab1.model.BookCreature;
import cs.ifmo.is.lab1.model.Ring;
import cs.ifmo.is.lab1.model.User;
import cs.ifmo.is.lab1.service.BookCreatureService;
import cs.ifmo.is.lab1.service.RingService;
import cs.ifmo.is.lab1.validator.Validator;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
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
public class RingBean implements Serializable {

    @Inject
    private RingService ringService;
    @Inject
    private BookCreatureService bookCreatureService;

    private List<BookCreature> bookCreatures;

    private Ring ring;

    private List<Ring> rings;
    private PaginatedResponse<Ring> paginatedResponse;
    private int page = 1;
    private int pageSize = 5;
    private String sortField = "id";
    private boolean sortAscending = true;

    private String filterId;
    private String filterName;
    private String filterPower;

    private Integer currentRingId;

    public Integer getCurrentRingId() {
        return currentRingId;
    }

    public void setCurrentRingId(Integer currentRingId) {
        this.currentRingId = currentRingId;
    }

    @PostConstruct
    public void init() {
        if (ring == null) {
            ring = new Ring();
            ring.setName("Default Ring");
            ring.setPower(100);
        }
        addDefaultRingsIfNotExist();
        loadBookCreatures();
    }

    public String addToDatabase() {
        if (validateAllFields()) {
            Ring newRing = new Ring();
            newRing.setName(this.ring.getName());
            newRing.setPower(this.ring.getPower());
            User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
            newRing.setUser(currentUser);

            ringService.create(newRing);
            loadPaginatedRings();

            return "ring?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please correct the errors in the form.", "Please correct the errors in the form."));
            return null;
        }
    }

    private void addDefaultRingsIfNotExist() {
        List<Ring> existingRings = ringService.findAll();
        if (existingRings.isEmpty()) {
            for (int i = 0; i < 5; i++) {
                Ring defaultRing = new Ring();
                defaultRing.setName("Example Ring " + (i + 1));
                defaultRing.setPower(100);

                ringService.create(defaultRing);
            }
        }
    }

    public String navigateToAddPage() {
        return "addRing?faces-redirect=true";
    }

    public void loadRings() {
        rings = ringService.findAll();
    }

    public List<Ring> getRings() {
        if (rings == null) {
            loadRings();
        }
        System.out.println("Rings: " + rings);
        return rings;
    }

    public PaginatedResponse<Ring> getPaginatedResponse() {
        if (paginatedResponse == null) {
            loadPaginatedRings();
        }
        return paginatedResponse;
    }

    public void previousPage() {
        if (page > 1) {
            page--;
            loadPaginatedRings();
        }
    }

    public void nextPage() {
        if (page * pageSize < ringService.count()) {
            page++;
            loadPaginatedRings();
        }
    }

    public void loadPaginatedRings() {
        paginatedResponse = new PaginatedResponse<>(
                ringService.findAll(page, pageSize, filterId, filterName, filterPower, sortField, sortAscending),
                ringService.count(filterId, filterName, filterPower),
                page, pageSize
        );
    }

    public String edit(Integer id) {
        Ring ring = ringService.findById(id);
        User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
        if (ring.getUser().getId().equals(currentUser.getId()) || currentUser.getRole() == User.Role.ADMIN) {
            this.ring = ring;
            return "editRing?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Это не ваш объект, вы не можете его редактировать", "Это не ваш объект, вы не можете его редактировать"));
            return null;
        }
    }

    public String delete(Integer id) {
        Ring ring = ringService.findById(id);
        User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");

        if (ring == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ring not found.", "Ring not found."));
            return null;
        }

        if (ring.getUser().getId().equals(currentUser.getId()) || currentUser.getRole() == User.Role.ADMIN) {
            String errorMessage = ringService.delete(id);
            if (errorMessage != null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMessage, errorMessage));
                return null;
            }
            loadPaginatedRings();
            return "ring?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "You do not have permission to delete this record.", "You do not have permission to delete this record."));
            return null;
        }
    }

    public String create() {
        try {
            User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
            ring.setUser(currentUser);
            ringService.create(ring);
            loadPaginatedRings();
            return "ring?faces-redirect=true";
        } catch (Exception e) {
            throw e;
        }
    }

    public String update() {
        if (validateAllFields()) {
            try {
                User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
                ring.setUser(currentUser);
                ringService.update(ring);
                loadPaginatedRings();
                return "ring?faces-redirect=true";
            } catch (Exception e) {
                throw e;
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please correct the errors in the form.", "Please correct the errors in the form."));
            return null;
        }
    }

    public Ring getRingById(Integer id) {
        return ringService.findById(id);
    }

    public String getRingInfoById(Integer id) {
        ring = ringService.findById(id);
        loadBookCreatures();
        return "ringInfo?faces-redirect=true";
    }

    public void sort(String field) {
        if (sortField.equals(field)) {
            sortAscending = !sortAscending;
        } else {
            sortField = field;
            sortAscending = true;
        }
        loadPaginatedRings();
    }

    public String getFilterId() {
        return filterId;
    }

    public void setFilterId(String filterId) {
        this.filterId = filterId;
        loadPaginatedRings();
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
        loadPaginatedRings();
    }

    public String getFilterPower() {
        return filterPower;
    }

    public void setFilterPower(String filterPower) {
        this.filterPower = filterPower;
        loadPaginatedRings();
    }

    private Validator validator = new Validator();

    public void validateRingPower(AjaxBehaviorEvent event) {
        validator.validateRingPower(event);
    }

    public boolean isUserLoggedIn() {
        User currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
        return currentUser != null;
    }

    public boolean validateAllFields() {
        boolean isValid = true;

        if (ring.getName() == null || ring.getName().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage("name", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Введите имя!", "Введите имя!"));
            isValid = false;
        }

        if (ring.getPower() == null || ring.getPower() <= 0) {
            FacesContext.getCurrentInstance().addMessage("power", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Power is required and must be positive", "Power is required and must be positive"));
            isValid = false;
        }

        return isValid;
    }

    public RingService getRingService() {
        return ringService;
    }

    public Ring getRing() {
        return ring;
    }

    public void setRing(Ring ring) {
        this.ring = ring;
    }

    public void setRingService(RingService ringService) {
        this.ringService = ringService;
    }

    public void setRings(List<Ring> rings) {
        this.rings = rings;
    }

    public void setPaginatedResponse(PaginatedResponse<Ring> paginatedResponse) {
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


    public List<BookCreature> getBookCreatures() {
        if (bookCreatures == null) {
            loadBookCreatures();
        }
        return bookCreatures;
    }

    private void loadBookCreatures() {
        if (ring != null && ring.getId() != null) {
            bookCreatures = bookCreatureService.findByRingId(ring.getId());
        }
    }
}
