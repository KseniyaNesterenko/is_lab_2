package cs.ifmo.is.lab1.repository;

import cs.ifmo.is.lab1.model.*;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.util.Date;
import java.util.List;

@Stateless
public class MagicCityRepository {

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


    public void create(MagicCity magicCity) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(magicCity);
            em.getTransaction().commit();
            saveAuditLog(magicCity, BookCreatureHistory.ChangeType.CREATE);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<MagicCity> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("MagicCity.selectAll", MagicCity.class).getResultList();
        } finally {
            em.close();
        }
    }

    public MagicCity findById(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(MagicCity.class, id);
        } finally {
            em.close();
        }
    }

    public void update(MagicCity magicCity) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(magicCity);
            em.getTransaction().commit();
            saveAuditLog(magicCity, BookCreatureHistory.ChangeType.UPDATE);
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
            MagicCity magicCity = em.find(MagicCity.class, id);
            if (magicCity != null) {
                em.remove(magicCity);
            }
            em.getTransaction().commit();
            saveAuditLog(magicCity, BookCreatureHistory.ChangeType.DELETE);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<MagicCity> findAll(int page, int pageSize, String filterId, String filterName, String filterArea, String filterPopulation, String filterPopulationDensity, String filterEstablishmentDate, String filterGovernor, Boolean filterCapital, String sortField, boolean sortAscending) {
        EntityManager em = emf.createEntityManager();
        try {
            StringBuilder queryString = new StringBuilder("SELECT m FROM MagicCity m WHERE 1=1");
            if (filterId != null && !filterId.isEmpty()) {
                queryString.append(" AND CAST(m.id AS TEXT) LIKE :filterId");
            }
            if (filterName != null && !filterName.isEmpty()) {
                queryString.append(" AND m.name LIKE :filterName");
            }
            if (filterArea != null && !filterArea.isEmpty()) {
                queryString.append(" AND CAST(m.area AS TEXT) LIKE :filterArea");
            }
            if (filterPopulation != null) {
                queryString.append(" AND CAST(m.population AS TEXT) LIKE :filterPopulation");
            }
            if (filterPopulationDensity != null) {
                queryString.append(" AND CAST(m.populationDensity AS TEXT) LIKE :filterPopulationDensity");
            }
            if (filterEstablishmentDate != null) {
                queryString.append(" AND CAST(m.establishmentDate AS TEXT) LIKE :filterEstablishmentDate");
            }
            if (filterGovernor != null && !filterGovernor.isEmpty()) {
                queryString.append(" AND m.governor LIKE :filterGovernor");
            }
            if (filterCapital != null) {
                queryString.append(" AND CAST(m.capital AS TEXT) LIKE :filterCapital");
            }
            if (sortField != null) {
                queryString.append(" ORDER BY m.").append(sortField);
                if (!sortAscending) {
                    queryString.append(" DESC");
                }
            }
            TypedQuery<MagicCity> query = em.createQuery(queryString.toString(), MagicCity.class);
            if (filterId != null && !filterId.isEmpty()) {
                query.setParameter("filterId", "%" + filterId + "%");
            }
            if (filterName != null && !filterName.isEmpty()) {
                query.setParameter("filterName", "%" + filterName + "%");
            }
            if (filterArea != null && !filterArea.isEmpty()) {
                query.setParameter("filterArea", "%" + filterArea + "%");
            }
            if (filterPopulation != null) {
                query.setParameter("filterPopulation", "%" + filterPopulation + "%");
            }
            if (filterPopulationDensity != null) {
                query.setParameter("filterPopulationDensity", "%" + filterPopulationDensity + "%");
            }
            if (filterEstablishmentDate != null) {
                query.setParameter("filterEstablishmentDate", "%" + filterEstablishmentDate + "%");
            }
            if (filterGovernor != null && !filterGovernor.isEmpty()) {
                query.setParameter("filterGovernor", "%" + filterGovernor + "%");
            }
            if (filterCapital != null) {
                query.setParameter("filterCapital", "%" + filterCapital + "%");
            }
            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public long count(String filterId, String filterName, String filterArea, String filterPopulation, String filterPopulationDensity, String filterEstablishmentDate, String filterGovernor, Boolean filterCapital) {
        EntityManager em = emf.createEntityManager();
        try {
            StringBuilder queryString = new StringBuilder("SELECT COUNT(m) FROM MagicCity m WHERE 1=1");
            if (filterId != null && !filterId.isEmpty()) {
                queryString.append(" AND CAST(m.id AS TEXT) LIKE :filterId");
            }
            if (filterName != null && !filterName.isEmpty()) {
                queryString.append(" AND m.name LIKE :filterName");
            }
            if (filterArea != null && !filterArea.isEmpty()) {
                queryString.append(" AND CAST(m.area AS TEXT) LIKE :filterArea");
            }
            if (filterPopulation != null) {
                queryString.append(" AND CAST(m.population AS TEXT) LIKE :filterPopulation");
            }
            if (filterPopulationDensity != null) {
                queryString.append(" AND CAST(m.populationDensity AS TEXT) LIKE :filterPopulationDensity");
            }
            if (filterEstablishmentDate != null) {
                queryString.append(" AND CAST(m.establishmentDate AS TEXT) LIKE :filterEstablishmentDate");
            }
            if (filterGovernor != null && !filterGovernor.isEmpty()) {
                queryString.append(" AND m.governor LIKE :filterGovernor");
            }
            if (filterCapital != null) {
                queryString.append(" AND CAST(m.capital AS TEXT) LIKE :filterCapital");
            }
            TypedQuery<Long> query = em.createQuery(queryString.toString(), Long.class);
            if (filterId != null && !filterId.isEmpty()) {
                query.setParameter("filterId", "%" + filterId + "%");
            }
            if (filterName != null && !filterName.isEmpty()) {
                query.setParameter("filterName", "%" + filterName + "%");
            }
            if (filterArea != null && !filterArea.isEmpty()) {
                query.setParameter("filterArea", "%" + filterArea + "%");
            }
            if (filterPopulation != null) {
                query.setParameter("filterPopulation", filterPopulation);
            }
            if (filterPopulationDensity != null) {
                query.setParameter("filterPopulationDensity", filterPopulationDensity);
            }
            if (filterEstablishmentDate != null) {
                query.setParameter("filterEstablishmentDate", "%" + filterEstablishmentDate + "%");
            }
            if (filterGovernor != null && !filterGovernor.isEmpty()) {
                query.setParameter("filterGovernor", "%" + filterGovernor + "%");
            }
            if (filterCapital != null) {
                query.setParameter("filterCapital", filterCapital);
            }
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    public long count() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT COUNT(m) FROM MagicCity m", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    public boolean isLinkedToOtherObjects(Integer magicCityId) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(b) FROM BookCreature b WHERE b.creatureLocation.id = :magicCityId", Long.class);
            query.setParameter("magicCityId", magicCityId);
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

}
