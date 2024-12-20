package cs.ifmo.is.lab1.service;
import cs.ifmo.is.lab1.model.*;
import cs.ifmo.is.lab1.repository.BookCreatureHistoryRepository;
import cs.ifmo.is.lab1.repository.BookCreatureRepository;
import cs.ifmo.is.lab1.repository.MagicCityRepository;
import cs.ifmo.is.lab1.repository.RingRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Stateless
public class ImportHistoryService implements Serializable {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("IsLab1");
    public void saveImportHistory(User user, String status, int addedObjects) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            ImportHistory importHistory = new ImportHistory();
            importHistory.setUser(user);
            importHistory.setStatus(status);
            importHistory.setAddedObjects(addedObjects);

            em.persist(importHistory);
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


    public List<ImportHistory> getImportHistory(User user, boolean isAdmin) {
        EntityManager em = emf.createEntityManager();
        if (isAdmin) {
            return em.createQuery("SELECT i FROM ImportHistory i ORDER BY i.timestamp ASC", ImportHistory.class)
                    .getResultList();
        } else {
            return em.createQuery("SELECT i FROM ImportHistory i WHERE i.user = :user ORDER BY i.timestamp ASC", ImportHistory.class)
                    .setParameter("user", user)
                    .getResultList();
        }
    }

    public List<ImportHistory> getImportHistoryPaginated(User user, boolean isAdmin, int page, int pageSize) {
        EntityManager em = emf.createEntityManager();
        int startPosition = (page - 1) * pageSize;

        if (isAdmin) {
            return em.createQuery("SELECT i FROM ImportHistory i ORDER BY i.timestamp ASC", ImportHistory.class)
                    .setFirstResult(startPosition)
                    .setMaxResults(pageSize)
                    .getResultList();
        } else {
            return em.createQuery("SELECT i FROM ImportHistory i WHERE i.user = :user ORDER BY i.timestamp ASC", ImportHistory.class)
                    .setParameter("user", user)
                    .setFirstResult(startPosition)
                    .setMaxResults(pageSize)
                    .getResultList();
        }
    }

    public int getTotalImportHistoryCount(User user, boolean isAdmin) {
        EntityManager em = emf.createEntityManager();
        try {
            if (isAdmin) {
                return ((Long) em.createQuery("SELECT COUNT(i) FROM ImportHistory i").getSingleResult()).intValue();
            } else {
                return ((Long) em.createQuery("SELECT COUNT(i) FROM ImportHistory i WHERE i.user = :user")
                        .setParameter("user", user)
                        .getSingleResult()).intValue();
            }
        } finally {
            em.close();
        }
    }

}
