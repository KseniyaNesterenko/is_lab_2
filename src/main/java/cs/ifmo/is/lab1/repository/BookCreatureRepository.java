package cs.ifmo.is.lab1.repository;

import cs.ifmo.is.lab1.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class BookCreatureRepository {

    @PersistenceUnit(unitName = "IsLab1")
    private EntityManagerFactory emf;
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

    @Transactional
    public void create(BookCreature bookCreature) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            boolean exists = !em.createQuery(
                            "SELECT bc FROM BookCreature bc WHERE bc.name = :name", BookCreature.class)
                    .setParameter("name", bookCreature.getName())
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .setMaxResults(1)
                    .getResultList()
                    .isEmpty();

            if (exists) {
                em.getTransaction().rollback();
                throw new EntityExistsException("Существо с таким именем уже существует");
            }

            em.persist(bookCreature);
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



    public void createShort(BookCreature bookCreature) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            MagicCity existingMagicCity = em.find(MagicCity.class, bookCreature.getCreatureLocation().getId());
            if (existingMagicCity != null) {
                bookCreature.setCreatureLocation(existingMagicCity);
            }

            Ring existingRing = em.find(Ring.class, bookCreature.getRing().getId());
            if (existingRing != null) {
                bookCreature.setRing(existingRing);
            }

            em.persist(bookCreature);
            em.getTransaction().commit();
            saveAuditLog(bookCreature, BookCreatureHistory.ChangeType.CREATE);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }


    public List<BookCreature> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createNamedQuery("BookCreature.selectAll", BookCreature.class).getResultList();
        } finally {
            em.close();
        }
    }

    public BookCreature findById(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(BookCreature.class, id);
        } finally {
            em.close();
        }
    }

    @Transactional
    public void update(BookCreature bookCreature) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // Применяем пессимистическую блокировку
            BookCreature existingBookCreature = em.find(
                    BookCreature.class,
                    bookCreature.getId(),
                    LockModeType.PESSIMISTIC_WRITE
            );

            if (existingBookCreature == null) {
                throw new EntityNotFoundException("Существо с ID " + bookCreature.getId() + " не найдено");
            }

            // Обновляем поля сущности
            existingBookCreature.setName(bookCreature.getName());
            existingBookCreature.setAge(bookCreature.getAge());
            existingBookCreature.getCreatureLocation().setName(bookCreature.getCreatureLocation().getName());

            // Обновляем объект в БД
            em.merge(existingBookCreature);  // merge или update объекта, который уже заблокирован

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

            BookCreature bookCreature = em.find(BookCreature.class, id);
            if (bookCreature == null) {
                throw new EntityNotFoundException("Entity not found for ID: " + id);
            }
            em.remove(bookCreature);
            saveAuditLog(bookCreature, BookCreatureHistory.ChangeType.DELETE);

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

    public List<BookCreature> findAll(int page, int pageSize, Integer filterId, String filterName, Long filterAge, String filterCoordinatesX, String filterCoordinatesY, String filterCreationDate, List<BookCreatureType> filterCreatureTypes, String filterCreatureLocation, String filterAttackLevel, String filterDefenseLevel, String filterRing, String sortField, boolean sortAscending) {
        EntityManager em = emf.createEntityManager();
        try {
            StringBuilder queryString = new StringBuilder("SELECT b FROM BookCreature b WHERE 1=1");
            if (filterId != null) {
                queryString.append(" AND CAST(b.id AS TEXT) LIKE :filterId");
            }
            if (filterName != null && !filterName.isEmpty()) {
                queryString.append(" AND b.name LIKE :filterName");
            }
            if (filterAge != null) {
                queryString.append(" AND CAST(b.age AS TEXT) LIKE :filterAge");
            }
            if (filterCoordinatesX != null && !filterCoordinatesX.isEmpty()) {
                queryString.append(" AND CAST(b.coordinates.x AS TEXT) LIKE :filterCoordinatesX");
            }
            if (filterCoordinatesY != null && !filterCoordinatesY.isEmpty()) {
                queryString.append(" AND CAST(b.coordinates.y AS TEXT) LIKE :filterCoordinatesY");
            }
            if (filterCreationDate != null && !filterCreationDate.isEmpty()) {
                queryString.append(" AND CAST(b.creationDate AS TEXT) LIKE :filterCreationDate");
            }
            if (filterCreatureTypes != null && !filterCreatureTypes.isEmpty()) {
                queryString.append(" AND b.creatureType IN :filterCreatureTypes");
            }
            if (filterCreatureLocation != null && !filterCreatureLocation.isEmpty()) {
                queryString.append(" AND CAST(b.creatureLocation.id AS TEXT) LIKE :filterCreatureLocation");
            }
            if (filterAttackLevel != null && !filterAttackLevel.isEmpty()) {
                queryString.append(" AND CAST(b.attackLevel AS TEXT) LIKE :filterAttackLevel");
            }
            if (filterDefenseLevel != null && !filterDefenseLevel.isEmpty()) {
                queryString.append(" AND CAST(b.defenseLevel AS TEXT) LIKE :filterDefenseLevel");
            }
            if (filterRing != null && !filterRing.isEmpty()) {
                queryString.append(" AND CAST(b.ring.id AS TEXT) LIKE :filterRing");
            }
            if (sortField != null) {
                queryString.append(" ORDER BY b.").append(sortField);
                if (!sortAscending) {
                    queryString.append(" DESC");
                }
            }
            TypedQuery<BookCreature> query = em.createQuery(queryString.toString(), BookCreature.class);
            if (filterId != null) {
                query.setParameter("filterId", "%" + filterId + "%");
            }
            if (filterName != null && !filterName.isEmpty()) {
                query.setParameter("filterName", "%" + filterName + "%");
            }
            if (filterAge != null) {
                query.setParameter("filterAge", "%" + filterAge + "%");
            }
            if (filterCoordinatesX != null && !filterCoordinatesX.isEmpty()) {
                query.setParameter("filterCoordinatesX", "%" + filterCoordinatesX + "%");
            }
            if (filterCoordinatesY != null && !filterCoordinatesY.isEmpty()) {
                query.setParameter("filterCoordinatesY", "%" + filterCoordinatesY + "%");
            }
            if (filterCreationDate != null && !filterCreationDate.isEmpty()) {
                query.setParameter("filterCreationDate", "%" + filterCreationDate + "%");
            }
            if (filterCreatureTypes != null && !filterCreatureTypes.isEmpty()) {
                query.setParameter("filterCreatureTypes", filterCreatureTypes);
            }
            if (filterCreatureLocation != null && !filterCreatureLocation.isEmpty()) {
                query.setParameter("filterCreatureLocation", "%" + filterCreatureLocation + "%");
            }
            if (filterAttackLevel != null && !filterAttackLevel.isEmpty()) {
                query.setParameter("filterAttackLevel", "%" + filterAttackLevel + "%");
            }
            if (filterDefenseLevel != null && !filterDefenseLevel.isEmpty()) {
                query.setParameter("filterDefenseLevel", "%" + filterDefenseLevel + "%");
            }
            if (filterRing != null && !filterRing.isEmpty()) {
                query.setParameter("filterRing", "%" + filterRing + "%");
            }
            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public long count(Integer filterId, String filterName, Long filterAge, String filterCoordinatesX, String filterCoordinatesY, String filterCreationDate, List<BookCreatureType> filterCreatureTypes, String filterCreatureLocation, String filterAttackLevel, String filterDefenseLevel, String filterRing) {
        EntityManager em = emf.createEntityManager();
        try {
            StringBuilder queryString = new StringBuilder("SELECT COUNT(b) FROM BookCreature b WHERE 1=1");
            if (filterId != null) {
                queryString.append(" AND CAST(b.id AS TEXT) LIKE :filterId");
            }
            if (filterName != null && !filterName.isEmpty()) {
                queryString.append(" AND b.name LIKE :filterName");
            }
            if (filterAge != null) {
                queryString.append(" AND CAST(b.age AS TEXT) LIKE :filterAge");
            }
            if (filterCoordinatesX != null && !filterCoordinatesX.isEmpty()) {
                queryString.append(" AND CAST(b.coordinates.x AS TEXT) LIKE :filterCoordinatesX");
            }
            if (filterCoordinatesY != null && !filterCoordinatesY.isEmpty()) {
                queryString.append(" AND CAST(b.coordinates.y AS TEXT) LIKE :filterCoordinatesY");
            }
            if (filterCreationDate != null && !filterCreationDate.isEmpty()) {
                queryString.append(" AND CAST(b.creationDate AS TEXT) LIKE :filterCreationDate");
            }
            if (filterCreatureTypes != null && !filterCreatureTypes.isEmpty()) {
                queryString.append(" AND b.creatureType IN :filterCreatureTypes");
            }
            if (filterCreatureLocation != null && !filterCreatureLocation.isEmpty()) {
                queryString.append(" AND b.creatureLocation.name LIKE :filterCreatureLocation");
            }
            if (filterAttackLevel != null && !filterAttackLevel.isEmpty()) {
                queryString.append(" AND CAST(b.attackLevel AS TEXT) LIKE :filterAttackLevel");
            }
            if (filterDefenseLevel != null && !filterDefenseLevel.isEmpty()) {
                queryString.append(" AND CAST(b.defenseLevel AS TEXT) LIKE :filterDefenseLevel");
            }
            if (filterRing != null && !filterRing.isEmpty()) {
                queryString.append(" AND CAST(b.ring.id AS TEXT) LIKE :filterRing");
            }
            TypedQuery<Long> query = em.createQuery(queryString.toString(), Long.class);
            if (filterId != null) {
                query.setParameter("filterId", "%" + filterId + "%");
            }
            if (filterName != null && !filterName.isEmpty()) {
                query.setParameter("filterName", "%" + filterName + "%");
            }
            if (filterAge != null) {
                query.setParameter("filterAge", "%" + filterAge + "%");
            }
            if (filterCoordinatesX != null && !filterCoordinatesX.isEmpty()) {
                query.setParameter("filterCoordinatesX", "%" + filterCoordinatesX + "%");
            }
            if (filterCoordinatesY != null && !filterCoordinatesY.isEmpty()) {
                query.setParameter("filterCoordinatesY", "%" + filterCoordinatesY + "%");
            }
            if (filterCreationDate != null && !filterCreationDate.isEmpty()) {
                query.setParameter("filterCreationDate", "%" + filterCreationDate + "%");
            }
            if (filterCreatureTypes != null && !filterCreatureTypes.isEmpty()) {
                query.setParameter("filterCreatureTypes", filterCreatureTypes);
            }
            if (filterCreatureLocation != null && !filterCreatureLocation.isEmpty()) {
                query.setParameter("filterCreatureLocation", "%" + filterCreatureLocation + "%");
            }
            if (filterAttackLevel != null && !filterAttackLevel.isEmpty()) {
                query.setParameter("filterAttackLevel", "%" + filterAttackLevel + "%");
            }
            if (filterDefenseLevel != null && !filterDefenseLevel.isEmpty()) {
                query.setParameter("filterDefenseLevel", "%" + filterDefenseLevel + "%");
            }
            if (filterRing != null && !filterRing.isEmpty()) {
                query.setParameter("filterRing", "%" + filterRing + "%");
            }
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    public long count() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT COUNT(b) FROM BookCreature b", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    public List<MagicCity> findAllMagicCities() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT m FROM MagicCity m", MagicCity.class).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Ring> findAllRings() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT r FROM Ring r", Ring.class).getResultList();
        } finally {
            em.close();
        }
    }

    public MagicCity findMagicCityById(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(MagicCity.class, id);
        } finally {
            em.close();
        }
    }

    public Ring findRingById(Integer id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Ring.class, id);
        } finally {
            em.close();
        }
    }

    public List<BookCreature> findByUserId(Integer userId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT b FROM BookCreature b WHERE b.user.id = :userId", BookCreature.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<BookCreature> deleteByAttackLevel(Float attackLevel, Integer userId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            List<BookCreature> creatures = em.createQuery("SELECT b FROM BookCreature b WHERE b.attackLevel = :attackLevel AND b.user.id = :userId", BookCreature.class)
                    .setParameter("attackLevel", attackLevel)
                    .setParameter("userId", userId)
                    .getResultList();
            for (BookCreature creature : creatures) {
                em.remove(creature);
            }
            em.getTransaction().commit();
            return creatures;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }


    public BookCreature getMinCoordinatesCreature() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT b FROM BookCreature b ORDER BY b.coordinates.x ASC, b.coordinates.y ASC", BookCreature.class)
                    .setMaxResults(1)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    public List<String> getUniqueRingNames() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT DISTINCT b.ring.name FROM BookCreature b", String.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void takeRingsFromHobbits(Integer userId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            List<BookCreature> hobbits = em.createQuery("SELECT b FROM BookCreature b WHERE b.creatureType = :creatureType AND b.user.id = :userId", BookCreature.class)
                    .setParameter("creatureType", BookCreatureType.HOBBIT)
                    .setParameter("userId", userId)
                    .getResultList();
            for (BookCreature hobbit : hobbits) {
                hobbit.setRing(null);
                em.merge(hobbit);
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


    public void moveHobbitsWithRingsToMordor(Integer userId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            List<BookCreature> hobbitsWithRings = em.createQuery("SELECT b FROM BookCreature b WHERE b.creatureType = :creatureType AND b.ring IS NOT NULL AND b.user.id = :userId", BookCreature.class)
                    .setParameter("creatureType", BookCreatureType.HOBBIT)
                    .setParameter("userId", userId)
                    .getResultList();
            MagicCity mordor;
            try {
                mordor = em.createQuery("SELECT m FROM MagicCity m WHERE m.name = :name", MagicCity.class)
                        .setParameter("name", "Mordor")
                        .getSingleResult();
            } catch (NoResultException e) {
                throw new IllegalArgumentException("Мордор исчез!");
            }
            for (BookCreature hobbit : hobbitsWithRings) {
                hobbit.setCreatureLocation(mordor);
                em.merge(hobbit);
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

    public List<BookCreature> getHobbitsWithRingsInMordor() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT b FROM BookCreature b WHERE b.creatureType = :creatureType AND b.ring IS NOT NULL AND b.creatureLocation.name = :locationName", BookCreature.class)
                    .setParameter("creatureType", BookCreatureType.HOBBIT)
                    .setParameter("locationName", "Mordor")
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<BookCreature> findByMagicCityId(Integer magicCityId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT b FROM BookCreature b WHERE b.creatureLocation.id = :magicCityId", BookCreature.class)
                    .setParameter("magicCityId", magicCityId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<BookCreature> findByRingId(Integer ringId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT b FROM BookCreature b WHERE b.ring.id = :ringId", BookCreature.class)
                    .setParameter("ringId", ringId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

}