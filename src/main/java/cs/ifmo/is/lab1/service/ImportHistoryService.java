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
            em.getTransaction().begin(); // Начало транзакции

            ImportHistory importHistory = new ImportHistory();
            importHistory.setUser(user);
            importHistory.setStatus(status);
            importHistory.setAddedObjects(addedObjects);

            em.persist(importHistory); // Сохранение объекта
            em.getTransaction().commit(); // Завершение транзакции

        } catch (Exception e) {
            // В случае ошибки откатываем транзакцию
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e; // Пробрасываем исключение дальше
        } finally {
            // Закрываем EntityManager в любом случае
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
}
