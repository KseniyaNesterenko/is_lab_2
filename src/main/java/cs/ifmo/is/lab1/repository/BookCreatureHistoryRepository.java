package cs.ifmo.is.lab1.repository;

import cs.ifmo.is.lab1.model.BookCreatureHistory;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.List;

@Stateless
public class BookCreatureHistoryRepository {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("IsLab1");

    @Transactional
    public void persist(BookCreatureHistory history) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(history);
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

    public List<BookCreatureHistory> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("BookCreatureHistory.selectAll", BookCreatureHistory.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<BookCreatureHistory> findAllPaginated(int first, int pageSize) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<BookCreatureHistory> query = em.createNamedQuery("BookCreatureHistory.selectAll", BookCreatureHistory.class);
            query.setFirstResult(first);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public long count() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("BookCreatureHistory.count", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }
}
