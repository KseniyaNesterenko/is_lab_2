package cs.ifmo.is.lab1.bean;

import cs.ifmo.is.lab1.model.AdminRequest;
import cs.ifmo.is.lab1.model.BookCreature;
import cs.ifmo.is.lab1.model.User;
import cs.ifmo.is.lab1.service.BookCreatureService;
import cs.ifmo.is.lab1.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.io.Serializable;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Named
@SessionScoped
public class UserBean implements Serializable {
    private List<AdminRequest> adminRequests;

    @Inject
    private UserService userService;

    @Inject
    private BookCreatureService bookCreatureService;


    private String username;
    private String password;
    private User user;
    @PersistenceContext
    private EntityManager entityManager;



    public String login() {
        Boolean authResult = userService.authenticate(username, password);
        if (authResult == null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Username does not exist", "Username does not exist"));
            return null;
        } else if (authResult) {
            User user = userService.findUserByUsername(username);
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("user", user);
            this.user = user;
            String sessionId = UUID.randomUUID().toString();
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("sessionId", sessionId);
            return "index?faces-redirect=true&sessionId=" + sessionId;
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Incorrect password", "Incorrect password"));
            return null;
        }
    }

    public boolean userExists(String username) {
        return userService.findUserByUsername(username) != null;
    }

    public boolean passwordExists(String password) {
        return !userService.isPasswordUnique(password);
    }

    public boolean usernameExists(String username) {
        return !userService.isUsernameUnique(username);
    }



    public String register() {
        if (username == null || username.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Введите имя!", "Username cannot be empty."));
            return null;
        }

        if (password == null || password.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Введите пароль!", "Password cannot be empty."));
            return null;
        }

        if (passwordExists(password)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Такой пароль уже существует в системе!", "Password already exists. Please choose a different password."));
            return null;
        }
        if (usernameExists(username)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Такой никнейм уже существует в системе!",
                            "Username already exists. Please choose a different username."));
            return null;

        } else {
            try {
                User user = new User();
                user.setUsername(username);
                user.setPassword(password);
                user.setRole(User.Role.USER);
                user.setColor(generateRandomColor());
                userService.registerUser(user);
                return "login?faces-redirect=true";
            } catch (IllegalArgumentException e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
                return null;
            }
        }
    }
    public String generateRandomColor() {
        return String.format("#%06x", new Random().nextInt(0xFFFFFF + 1));
    }



    public String registerPage() {
        return "register?faces-redirect=true";
    }

    public String loginPage() {
        return "login?faces-redirect=true";
    }

    public String mainPage() {
        return "index?faces-redirect=true";
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        this.user = null;
        return "login?faces-redirect=true";
    }

    public boolean isAdmin() {
        return user != null && user.getRole() == User.Role.ADMIN;
    }



    public boolean hasPendingRequest() {
        if (user != null) {
            List<AdminRequest> requests = userService.getAdminRequestsByUser(user);
            for (AdminRequest request : requests) {
                if (request.getStatus() == AdminRequest.RequestStatus.PENDING) {
                    return true;
                }
            }
        }
        return false;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @PostConstruct
    public void init() {
        loadAdminRequests();
    }

    public void loadAdminRequests() {
        this.adminRequests = userService.getAllAdminRequests();
    }

    public List<AdminRequest> getAdminRequests() {
        return adminRequests;
    }

    public String getAdminRequestStatus() {
        if (user == null) {
            return "No user logged in";
        }
        List<AdminRequest> requests = entityManager.createQuery(
                        "SELECT ar FROM AdminRequest ar WHERE ar.user = :user ORDER BY ar.id DESC", AdminRequest.class)
                .setParameter("user", user)
                .getResultList();

        if (requests.isEmpty()) {
            return "У вас нет уведомлений";
        }

        AdminRequest request = requests.get(0);
        String status = request.getStatus().name();
        switch (status) {
            case "PENDING":
                return "Заявка на админа: Ожидание";
            case "APPROVED":
                return "Заявка на админа: Одобрено";
            case "REJECTED":
                return "Заявка на админа: Отклонено";
            default:
                return "Заявка на админа: " + status;
        }
    }

    public String requestAdminRole() {
        if (user != null) {
            if (hasPendingRequest()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Вы уже отправили заявку!", null));
                return null;
            }
            userService.requestAdminRole(user);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Заявка на администратора отправлена", null));
            return "index?faces-redirect=true";
        } else {
            return "failure";
        }
    }



}
