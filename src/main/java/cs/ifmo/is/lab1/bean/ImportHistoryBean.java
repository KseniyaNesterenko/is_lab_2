package cs.ifmo.is.lab1.bean;

import cs.ifmo.is.lab1.model.ImportHistory;
import cs.ifmo.is.lab1.model.User;
import cs.ifmo.is.lab1.service.ImportHistoryService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

import java.util.List;
import java.util.Map;

@Named
@RequestScoped
public class ImportHistoryBean {

    @Inject
    private ImportHistoryService importHistoryService;

    private LazyDataModel<ImportHistory> lazyImportHistory;
    private User currentUser;
    private boolean isAdmin;

    @PostConstruct
    public void init() {
        currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
        isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole().toString());
        setupLazyModel();
    }

    private void setupLazyModel() {
        lazyImportHistory = new LazyDataModel<>() {
            @Override
            public List<ImportHistory> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                int page = first / pageSize + 1;
                return importHistoryService.getImportHistoryPaginated(currentUser, isAdmin, page, pageSize);
            }

            @Override
            public int count(Map<String, FilterMeta> filterBy) {
                return importHistoryService.getTotalImportHistoryCount(currentUser, isAdmin);
            }
        };
    }

    public LazyDataModel<ImportHistory> getLazyImportHistory() {
        return lazyImportHistory;
    }
}
