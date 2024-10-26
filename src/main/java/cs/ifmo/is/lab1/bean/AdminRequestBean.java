package cs.ifmo.is.lab1.bean;

import cs.ifmo.is.lab1.model.AdminRequest;
import cs.ifmo.is.lab1.model.User;
import jakarta.annotation.Resource;
import jakarta.inject.Named;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.List;
import jakarta.enterprise.context.SessionScoped;
import jakarta.transaction.UserTransaction;
import org.postgresql.util.PSQLException;

@Named
@SessionScoped
public class AdminRequestBean implements Serializable {

    @Resource
    private UserTransaction userTransaction;
    @PersistenceContext
    private EntityManager entityManager;
    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("IsLab1");
    private List<AdminRequest> adminRequests;

    public List<AdminRequest> getAdminRequests() {
        if (adminRequests == null) {
            loadAdminRequests();
        }
        return adminRequests;
    }

    private void loadAdminRequests() {
        TypedQuery<AdminRequest> query = entityManager.createQuery(
                "SELECT r FROM AdminRequest r WHERE r.status = :status", AdminRequest.class);
        query.setParameter("status", AdminRequest.RequestStatus.PENDING);
        adminRequests = query.getResultList();
    }



    public boolean hasPendingRequests() {
        loadAdminRequests();
        return !adminRequests.isEmpty();
    }


    public void approveRequest(AdminRequest adminRequest) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            adminRequest.setStatus(AdminRequest.RequestStatus.APPROVED);
            adminRequest = em.merge(adminRequest);
            User user = em.find(User.class, adminRequest.getUser().getId());
            if (user != null) {
                user.setRole(User.Role.ADMIN);
                try {
                    user = em.merge(user);
                } catch (PersistenceException e) {
                    if (e.getCause() instanceof PSQLException && e.getCause().getMessage().contains("duplicate key value violates unique constraint")) {
                        System.out.println("Duplicate key ignored: " + e.getCause().getMessage());
                    } else {
                        throw e;
                    }
                }
            } else {
                System.out.println("User not found for ID: " + adminRequest.getUser().getId());
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }


    public void rejectRequest(AdminRequest adminRequest) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            adminRequest.setStatus(AdminRequest.RequestStatus.REJECTED);
            adminRequest = em.merge(adminRequest);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }


}
