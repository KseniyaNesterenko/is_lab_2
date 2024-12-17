package cs.ifmo.is.lab1.repository;

import cs.ifmo.is.lab1.model.AdminRequest;
import cs.ifmo.is.lab1.model.MagicCity;
import cs.ifmo.is.lab1.model.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.List;

@Stateless
public class UserRepository {
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    private User user;

    public UserRepository() {
        entityManagerFactory = Persistence.createEntityManagerFactory("IsLab1");
        entityManager = entityManagerFactory.createEntityManager();
    }

    public void saveUser(User user) {
        entityManager.getTransaction().begin();
        entityManager.persist(user);
        entityManager.getTransaction().commit();
    }

    public User findUserByUsername(String username) {
        try {
            return entityManager.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public List<AdminRequest> getAllAdminRequests() {
        return entityManager.createQuery("SELECT ar FROM AdminRequest ar", AdminRequest.class).getResultList();
    }

    public List<AdminRequest> getAdminRequestsByUser(User user) {
        return entityManager.createQuery("SELECT ar FROM AdminRequest ar WHERE ar.user = :user", AdminRequest.class)
                .setParameter("user", user)
                .getResultList();
    }

    public void saveAdminRequest(AdminRequest request) {
        entityManager.getTransaction().begin();
        entityManager.persist(request);
        entityManager.getTransaction().commit();
    }

    public void close() {
        entityManager.close();
        entityManagerFactory.close();
    }

    public void approveRequest(AdminRequest adminRequest) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.getTransaction().begin();

            adminRequest.setStatus(AdminRequest.RequestStatus.APPROVED);
            adminRequest = em.merge(adminRequest);
            System.out.println("AdminRequest merged: " + adminRequest);

            User user = em.find(User.class, adminRequest.getUser().getId());
            if (user != null) {
                user.setRole(User.Role.ADMIN);
                user = em.merge(user);
                System.out.println("User merged: " + user);
            } else {
                System.out.println("User not found for ID: " + adminRequest.getUser().getId());
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
    public boolean isPasswordUnique(String password) {
        try {
            return entityManager.createQuery("SELECT u FROM User u WHERE u.password = :password", User.class)
                    .setParameter("password", password)
                    .getSingleResult() == null;
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isUsernameUnique(String username) {
        try {
            return entityManager.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult() == null;
        } catch (Exception e) {
            return true;
        }
    }


}
