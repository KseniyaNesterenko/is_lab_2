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
    public ImportHistory saveImportHistory(User user, String status, int addedObjects, String fileName) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            ImportHistory importHistory = new ImportHistory();
            importHistory.setUser(user);
            importHistory.setStatus(status);
            importHistory.setAddedObjects(addedObjects);
            importHistory.setFileName(fileName); // Сохранение имени файла

            em.persist(importHistory);
            em.getTransaction().commit();

            return importHistory;  // Возвращаем объект импорта
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
        List<String> friendlyStatuses = List.of("Успешно", "Неуспешно");

        if (isAdmin) {
            return em.createQuery(
                            "SELECT i FROM ImportHistory i WHERE i.status IN :statuses ORDER BY i.timestamp ASC",
                            ImportHistory.class)
                    .setParameter("statuses", friendlyStatuses)
                    .getResultList();
        } else {
            return em.createQuery(
                            "SELECT i FROM ImportHistory i WHERE i.user = :user AND i.status IN :statuses ORDER BY i.timestamp ASC",
                            ImportHistory.class)
                    .setParameter("user", user)
                    .setParameter("statuses", friendlyStatuses)
                    .getResultList();
        }
    }

    public List<ImportHistory> getImportHistoryPaginated(User user, boolean isAdmin, int page, int pageSize) {
        EntityManager em = emf.createEntityManager();
        int startPosition = (page - 1) * pageSize;
        List<String> friendlyStatuses = List.of("Успешно", "Неуспешно");

        if (isAdmin) {
            return em.createQuery(
                            "SELECT i FROM ImportHistory i WHERE i.status IN :statuses ORDER BY i.timestamp ASC",
                            ImportHistory.class)
                    .setParameter("statuses", friendlyStatuses)
                    .setFirstResult(startPosition)
                    .setMaxResults(pageSize)
                    .getResultList();
        } else {
            return em.createQuery(
                            "SELECT i FROM ImportHistory i WHERE i.user = :user AND i.status IN :statuses ORDER BY i.timestamp ASC",
                            ImportHistory.class)
                    .setParameter("user", user)
                    .setParameter("statuses", friendlyStatuses)
                    .setFirstResult(startPosition)
                    .setMaxResults(pageSize)
                    .getResultList();
        }
    }

    public int getTotalImportHistoryCount(User user, boolean isAdmin) {
        EntityManager em = emf.createEntityManager();
        List<String> friendlyStatuses = List.of("Успешно", "Неуспешно");

        try {
            if (isAdmin) {
                return ((Long) em.createQuery(
                                "SELECT COUNT(i) FROM ImportHistory i WHERE i.status IN :statuses")
                        .setParameter("statuses", friendlyStatuses)
                        .getSingleResult()).intValue();
            } else {
                return ((Long) em.createQuery(
                                "SELECT COUNT(i) FROM ImportHistory i WHERE i.user = :user AND i.status IN :statuses")
                        .setParameter("user", user)
                        .setParameter("statuses", friendlyStatuses)
                        .getSingleResult()).intValue();
            }
        } finally {
            em.close();
        }
    }
}
