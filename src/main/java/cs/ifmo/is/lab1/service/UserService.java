package cs.ifmo.is.lab1.service;

import cs.ifmo.is.lab1.bean.UserBean;
import cs.ifmo.is.lab1.model.AdminRequest;
import cs.ifmo.is.lab1.model.Ring;
import cs.ifmo.is.lab1.model.User;
import cs.ifmo.is.lab1.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;

public class UserService implements Serializable {
    private UserRepository userRepository;
    private EntityManagerFactory entityManagerFactory;
    public UserService() {
        userRepository = new UserRepository();
        initializeAdmin();
//        initializeUser();
    }

    public void registerUser(User user) {
        boolean isPasswordUnique = isPasswordUnique(user.getPassword());
        boolean isUsernameUnique = isUsernameUnique(user.getUsername());

        if (isPasswordUnique && isUsernameUnique) {
            userRepository.saveUser(user);
        } else if (!isPasswordUnique) {
            throw new IllegalArgumentException("Такой пароль уже существует в системе!");
        } else {
            throw new IllegalArgumentException("Такой никнейм уже существует в системе!");
        }
    }

    public User findUserByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    public Boolean authenticate(String username, String password) {
        User user = findUserByUsername(username);
        if (user == null) {
            return null;
        }
        return user.getPassword().equals(hashPassword(password));
    }

//    public boolean isPasswordUnique(String password) {
//        return userRepository.findUserByUsername(hashPassword(password)) == null;
//    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    public void createInitialAdmin(String username, String password) {
        User admin = new User();
        admin.setUsername(username);
        admin.setPassword(password);
        admin.setRole(User.Role.ADMIN);
        registerUser(admin);
    }

    public void createInitialUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(User.Role.USER);
        user.setColor(generateRandomColor());
        registerUser(user);
    }

    public void requestAdminRole(User user) {
        AdminRequest request = new AdminRequest();
        request.setUser(user);
        request.setStatus(AdminRequest.RequestStatus.PENDING);
        userRepository.saveAdminRequest(request);
    }

    public List<AdminRequest> getAllAdminRequests() {
        return userRepository.getAllAdminRequests();
    }

    public List<AdminRequest> getAdminRequestsByUser(User user) {
        return userRepository.getAdminRequestsByUser(user);
    }

    public void approveRequest(AdminRequest adminRequest) {
        userRepository.approveRequest(adminRequest);
    }

    private void initializeAdmin() {
        if (userRepository.findUserByUsername("a") == null) {
            createInitialAdmin("a", "a");
            createInitialAdmin("u", "u");
            createInitialAdmin("t", "t");
        }
    }

    private void initializeUser() {
        if (userRepository.findUserByUsername("u") == null) {
            createInitialUser("u", "u");
        }
    }

    public void close() {
        userRepository.close();
    }
    public boolean isPasswordUnique(String password) {
        return userRepository.isPasswordUnique(password);
    }

    public boolean isUsernameUnique(String username) {
        return userRepository.isUsernameUnique(username);
    }
    public String generateRandomColor() {
        return String.format("#%06x", new Random().nextInt(0xFFFFFF + 1));
    }

}
