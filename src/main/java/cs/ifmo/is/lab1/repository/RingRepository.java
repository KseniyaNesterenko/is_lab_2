package cs.ifmo.is.lab1.repository;

import cs.ifmo.is.lab1.model.*;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.util.Date;
import java.util.List;

public class RingRepository {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("IsLab1");
    @Inject
    private BookCreatureHistoryRepository bookCreatureHistoryRepository;

    private void saveAuditLog(Auditable bookCreature, BookCreatureHistory.ChangeType changeType) {
        BookCreatureHistory.ObjectType objectType = null;

        if (bookCreature instanceof Ring) {
            objectType = BookCreatureHistory.ObjectType.RING;
        } else if (bookCreature instanceof MagicCity) {
            objectType = BookCreatureHistory.ObjectType.MAGIC_CITY;
        } else if (bookCreature instanceof BookCreature) {
            objectType = BookCreatureHistory.ObjectType.BOOK_CREATURE;
        }

        BookCreatureHistory history = new BookCreatureHistory(
                bookCreature.getId(),
                objectType,
                new Date(),
                bookCreature.getUser().getId(),
                changeType
        );

        bookCreatureHistoryRepository.persist(history);
    }
    public void create(Ring ring) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(ring);
            em.getTransaction().commit();
            saveAuditLog(ring, BookCreatureHistory.ChangeType.CREATE);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Ring> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("Ring.selectAll", Ring.class).getResultList();
        } finally {
            em.close();
        }
    }

    public Ring findById(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Ring.class, id);
        } finally {
            em.close();
        }
    }

    public void update(Ring ring) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(ring);
            em.getTransaction().commit();
            saveAuditLog(ring, BookCreatureHistory.ChangeType.UPDATE);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Ring ring = em.find(Ring.class, id);
            if (ring != null) {
                em.remove(ring);
            }
            em.getTransaction().commit();
            saveAuditLog(ring, BookCreatureHistory.ChangeType.DELETE);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Ring> findAll(int page, int pageSize, String filterId, String filterName, String filterPower, String sortField, boolean sortAscending) {
        EntityManager em = emf.createEntityManager();
        try {
            StringBuilder queryString = new StringBuilder("SELECT r FROM Ring r WHERE 1=1");
            if (filterId != null && !filterId.isEmpty()) {
                queryString.append(" AND CAST(r.id AS TEXT) LIKE :filterId");
            }
            if (filterName != null && !filterName.isEmpty()) {
                queryString.append(" AND r.name LIKE :filterName");
            }
            if (filterPower != null && !filterPower.isEmpty()) {
                queryString.append(" AND CAST(r.power AS TEXT) LIKE :filterPower");
            }
            if (sortField != null) {
                queryString.append(" ORDER BY r.").append(sortField);
                if (!sortAscending) {
                    queryString.append(" DESC");
                }
            }

            TypedQuery<Ring> query = em.createQuery(queryString.toString(), Ring.class);
            if (filterId != null && !filterId.isEmpty()) {
                query.setParameter("filterId", "%" + filterId + "%");
            }
            if (filterName != null && !filterName.isEmpty()) {
                query.setParameter("filterName", "%" + filterName + "%");
            }
            if (filterPower != null && !filterPower.isEmpty()) {
                query.setParameter("filterPower", "%" + filterPower + "%");
            }
            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } finally {
            em.close();
        }
    }


    public long count(String filterId, String filterName, String filterPower) {
        EntityManager em = emf.createEntityManager();
        try {
            StringBuilder queryString = new StringBuilder("SELECT COUNT(r) FROM Ring r WHERE 1=1");
            if (filterId != null && !filterId.isEmpty()) {
                queryString.append(" AND CAST(r.id AS TEXT) LIKE :filterId");
            }
            if (filterName != null && !filterName.isEmpty()) {
                queryString.append(" AND r.name LIKE :filterName");
            }
            if (filterPower != null) {
                queryString.append(" AND CAST(r.power AS TEXT) LIKE :filterPower");
            }
            TypedQuery<Long> query = em.createQuery(queryString.toString(), Long.class);
            if (filterId != null && !filterId.isEmpty()) {
                query.setParameter("filterId", "%" + filterId + "%");
            }
            if (filterName != null && !filterName.isEmpty()) {
                query.setParameter("filterName", "%" + filterName + "%");
            }
            if (filterPower != null) {
                query.setParameter("filterPower", "%" + filterPower + "%");
            }
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    public long count() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT COUNT(r) FROM Ring r", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    public boolean isLinkedToOtherObjects(Integer ringId) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(b) FROM BookCreature b WHERE b.ring.id = :ringId", Long.class);
            query.setParameter("ringId", ringId);
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }
}
