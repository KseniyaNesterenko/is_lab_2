package cs.ifmo.is.lab1.bean;
import cs.ifmo.is.lab1.model.BookCreatureHistory;
import cs.ifmo.is.lab1.model.ImportHistory;
import cs.ifmo.is.lab1.model.User;
import cs.ifmo.is.lab1.repository.BookCreatureHistoryRepository;
import cs.ifmo.is.lab1.service.ImportHistoryService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;

@Named
@RequestScoped
public class ImportHistoryBean {

    @Inject
    private ImportHistoryService importHistoryService;

    private List<ImportHistory> importHistory;

    private User currentUser;
    private boolean isAdmin;

    @PostConstruct
    public void init() {
        // Получаем текущего пользователя из сессии
        currentUser = (User) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");

        // Проверяем роль пользователя
        isAdmin = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole().toString());

        // Загружаем историю импорта
        importHistory = importHistoryService.getImportHistory(currentUser, isAdmin);
    }

    public List<ImportHistory> getImportHistory() {
        return importHistory;
    }
}
