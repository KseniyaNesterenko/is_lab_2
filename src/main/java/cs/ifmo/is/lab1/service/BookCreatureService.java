package cs.ifmo.is.lab1.service;

import cs.ifmo.is.lab1.model.*;
import cs.ifmo.is.lab1.repository.BookCreatureHistoryRepository;
import cs.ifmo.is.lab1.repository.BookCreatureRepository;
import cs.ifmo.is.lab1.repository.MagicCityRepository;
import cs.ifmo.is.lab1.repository.RingRepository;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class BookCreatureService implements Serializable {
    public BookCreatureService() {
    }

    @PersistenceUnit(unitName = "IsLab1")
    private EntityManagerFactory emf;

    @Inject
    private BookCreatureRepository bookCreatureRepository;

    @Inject
    private BookCreatureHistoryRepository bookCreatureHistoryRepository;

    @Inject
    private MagicCityRepository magicCityRepository;
    @Inject
    private RingRepository ringRepository;

    @Transactional
    public void create(BookCreature bookCreature) {
        bookCreatureRepository.create(bookCreature);
    }


    public void create(MagicCity magicCity) {
        magicCityRepository.create(magicCity);
    }
    public void createRing(Ring ring) {
        ringRepository.create(ring);
    }

    public void createShort(BookCreature bookCreature) {
        bookCreatureRepository.createShort(bookCreature);
    }

    public List<BookCreature> findAll() {
        return bookCreatureRepository.findAll();
    }

    public BookCreature findById(Integer id) {
        return bookCreatureRepository.findById(id);
    }


    @Transactional
    public void update(BookCreature bookCreature) {
        try {
            bookCreatureRepository.update(bookCreature);
        } catch (PessimisticLockException e) {
            System.out.println("Пессимистическая блокировка не удалась: " + e.getMessage());
            throw e;
        } catch (EntityNotFoundException e) {
            System.out.println("Обновление не удалось: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.out.println("Обновление не удалось: " + e.getMessage());
            throw new RuntimeException("Ошибка обновления: " + e.getMessage(), e);
        }
    }




    public void update(MagicCity magicCity) {
        bookCreatureRepository.update(magicCity);
    }


    public void delete(Integer id) {
        try {
            bookCreatureRepository.delete(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Некорректный ID", e);
        } catch (EntityNotFoundException e) {
            throw new EntityNotFoundException("Существо не найдено в системе");
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred during deletion.", e);
        }
    }

    public List<BookCreature> findAll(int page, int pageSize, Integer filterId, String filterName, Long filterAge, String filterCoordinatesX, String filterCoordinatesY, String filterCreationDate, List<BookCreatureType> filterCreatureTypes, String filterCreatureLocation, String filterAttackLevel, String filterDefenseLevel, String filterRing, String sortField, boolean sortAscending) {
        return bookCreatureRepository.findAll(page, pageSize, filterId, filterName, filterAge, filterCoordinatesX, filterCoordinatesY, filterCreationDate, filterCreatureTypes, filterCreatureLocation, filterAttackLevel, filterDefenseLevel, filterRing, sortField, sortAscending);
    }

    public long count(Integer filterId, String filterName, Long filterAge, String filterCoordinatesX, String filterCoordinatesY, String filterCreationDate, List<BookCreatureType> filterCreatureTypes, String filterCreatureLocation, String filterAttackLevel, String filterDefenseLevel, String filterRing) {
        return bookCreatureRepository.count(filterId, filterName, filterAge, filterCoordinatesX, filterCoordinatesY, filterCreationDate, filterCreatureTypes, filterCreatureLocation, filterAttackLevel, filterDefenseLevel, filterRing);
    }

    public long count() {
        return bookCreatureRepository.count();
    }

    public List<MagicCity> findAllMagicCities() {
        return bookCreatureRepository.findAllMagicCities();
    }

    public List<Ring> findAllRings() {
        return bookCreatureRepository.findAllRings();
    }

    public MagicCity findMagicCityById(Integer id) {
        return bookCreatureRepository.findMagicCityById(id);
    }

    public Ring findRingById(Integer id) {
        return bookCreatureRepository.findRingById(id);
    }

    public List<BookCreature> findByUserId(Integer userId) {
        return bookCreatureRepository.findByUserId(userId);
    }

    public List<BookCreature> deleteByAttackLevel(Float attackLevel, Integer userId) {
        return bookCreatureRepository.deleteByAttackLevel(attackLevel, userId);
    }


    public BookCreature getMinCoordinatesCreature() {
        return bookCreatureRepository.getMinCoordinatesCreature();
    }

    public List<String> getUniqueRingNames() {
        return bookCreatureRepository.getUniqueRingNames();
    }

    public void takeRingsFromHobbits(Integer userId) {
        bookCreatureRepository.takeRingsFromHobbits(userId);
    }


    public void moveHobbitsWithRingsToMordor(Integer userId) {
        bookCreatureRepository.moveHobbitsWithRingsToMordor(userId);
    }

    public List<BookCreature> getHobbitsWithRingsInMordor() {
        return bookCreatureRepository.getHobbitsWithRingsInMordor();
    }
    public List<BookCreature> findByMagicCityId(Integer magicCityId) {
        return bookCreatureRepository.findByMagicCityId(magicCityId);
    }

    public List<BookCreature> findByRingId(Integer ringId) {
        return bookCreatureRepository.findByRingId(ringId);
    }

    public void addDefaultBookCreaturesIfNotExist(User currentUser) {
        List<BookCreature> existingCreatures = findByUserId(currentUser.getId());
        if (existingCreatures.isEmpty()) {
            for (int i = 0; i < 6; i++) {
                BookCreature defaultBookCreature = new BookCreature();
                defaultBookCreature.setName("Example " + (i + 1));
                defaultBookCreature.setCoordinates(new Coordinates(2, 2));
                defaultBookCreature.setCreationDate(new Date());
                defaultBookCreature.setAge(2L);
                defaultBookCreature.setCreatureType(BookCreatureType.HOBBIT);
                defaultBookCreature.setCreatureLocation(new MagicCity("TEST", 100.0, 1000, new Date(), MagicCity.GovernorType.GOLLUM, true, 10, currentUser));
                defaultBookCreature.setAttackLevel(5F);
                defaultBookCreature.setDefenseLevel(5F);
                defaultBookCreature.setRing(new Ring("Example Ring " + (i + 1), 10, currentUser));
                defaultBookCreature.setUser(currentUser);

                create(defaultBookCreature);
            }
            MagicCity magicCity = new MagicCity("Mordor", 100.0, 1000, new Date(), MagicCity.GovernorType.GOLLUM, true, 10, currentUser);
            create(magicCity);
        }
    }

    public void initializeBookCreature(BookCreature bookCreature, User currentUser) {
        bookCreature.setName("Default Name");
        bookCreature.setCoordinates(new Coordinates(2, 2));
        bookCreature.setCreationDate(new Date());
        bookCreature.setAge(2L);
        bookCreature.setCreatureType(BookCreatureType.HOBBIT);
        bookCreature.setCreatureLocation(new MagicCity("Default City", 100.0, 1000, new Date(), MagicCity.GovernorType.GOLLUM, true, 10, currentUser));
        bookCreature.setAttackLevel(5F);
        bookCreature.setDefenseLevel(5F);
        bookCreature.setRing(new Ring("Default Ring", 10, currentUser));
    }

    public void addToDatabase(BookCreature bookCreature, User currentUser) {
        BookCreature newBookCreature = new BookCreature();
        newBookCreature.setName(bookCreature.getName());
        newBookCreature.setCoordinates(new Coordinates(bookCreature.getCoordinates().getX(), bookCreature.getCoordinates().getY()));
        newBookCreature.setCreationDate(new Date());
        newBookCreature.setAge(bookCreature.getAge());
        newBookCreature.setCreatureType(bookCreature.getCreatureType());
        newBookCreature.setAttackLevel(bookCreature.getAttackLevel());
        newBookCreature.setDefenseLevel(bookCreature.getDefenseLevel());

        newBookCreature.setRing(new Ring(bookCreature.getRing().getName(), bookCreature.getRing().getPower(), currentUser));
        newBookCreature.setCreatureLocation(new MagicCity(bookCreature.getCreatureLocation().getName(), bookCreature.getCreatureLocation().getArea(),
                bookCreature.getCreatureLocation().getPopulation(), new Date(), bookCreature.getCreatureLocation().getGovernor(),
                bookCreature.getCreatureLocation().isCapital(), bookCreature.getCreatureLocation().getPopulationDensity(), currentUser));
        newBookCreature.setUser(currentUser);

        create(newBookCreature);
    }

    public void addDefaultBookCreature(User currentUser) {
        BookCreature defaultBookCreature = new BookCreature();
        defaultBookCreature.setName("Example");
        defaultBookCreature.setCoordinates(new Coordinates(2, 2));
        defaultBookCreature.setCreationDate(new Date());
        defaultBookCreature.setAge(2L);
        defaultBookCreature.setCreatureType(BookCreatureType.HOBBIT);
        defaultBookCreature.setAttackLevel(5F);
        defaultBookCreature.setDefenseLevel(5F);
        defaultBookCreature.setRing(new Ring("Example Ring", 10, currentUser));
        defaultBookCreature.setCreatureLocation(new MagicCity("Example City", 100.0, 1000, new Date(), MagicCity.GovernorType.GOLLUM, true, 10, currentUser));
        defaultBookCreature.setUser(currentUser);

        create(defaultBookCreature);
    }

//    @Transactional
    public void importBookCreatures(List<BookCreature> bookCreatures) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            for (BookCreature bookCreature : bookCreatures) {
                validateBookCreature(bookCreature);

                try {
                    em.persist(bookCreature);
                    em.flush();
                } catch (PersistenceException e) {
                    if (isDuplicateException(e)) {
                        throw new RuntimeException("Дубликат объекта: " + bookCreature.toString(), e);
                    } else {
                        throw e;
                    }
                }
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Ошибка при импорте: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    private boolean isDuplicateException(Exception e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause.getMessage().contains("unique constraint")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }



    private void validateBookCreature(BookCreature bookCreature) {
        if (bookCreature.getName() == null || bookCreature.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Имя существа не может быть пустым.");
        }

        Object x = bookCreature.getCoordinates().getX();
        if (x == null || !(x instanceof Integer) || (Integer) x >= 488) {
            throw new IllegalArgumentException("X обязательно к заполнению, должно быть меньше 488 и числом.");
        }

        Object y = bookCreature.getCoordinates().getY();
        if (y == null || !(y instanceof Integer)) {
            throw new IllegalArgumentException("Y обязательно к заполнению и должно быть числом.");
        }

        if (bookCreature.getAge() == null || !(bookCreature.getAge() instanceof Long) || bookCreature.getAge() < 0) {
            throw new IllegalArgumentException("Возраст обязательно к заполнению и должен быть положительным числом.");
        }

        if (bookCreature.getCreatureType() == null) {
            throw new IllegalArgumentException("Тип существа обязателен к заполнению.");
        }

        if ("Mordor".equals(bookCreature.getCreatureLocation().getName())) {
            throw new IllegalArgumentException("Город не может называться Mordor.");
        }

        if (bookCreature.getCreatureLocation().getName() == null || bookCreature.getCreatureLocation().getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Имя города обязательно к заполнению.");
        }

        if (bookCreature.getCreatureLocation().getArea() == null || bookCreature.getCreatureLocation().getArea() <= 0) {
            throw new IllegalArgumentException("Площадь города обязательна к заполнению и должна быть больше 0.");
        }

        Object population = bookCreature.getCreatureLocation().getPopulation();
        if (population == null || !(population instanceof Integer) || (Integer) population <= 0) {
            throw new IllegalArgumentException("Население города обязательно к заполнению и должно быть положительным числом.");
        }

        Object populationDensity = bookCreature.getCreatureLocation().getPopulationDensity();
        if (populationDensity == null || !(populationDensity instanceof Integer) || (Integer) populationDensity <= 0) {
            throw new IllegalArgumentException("Плотность населения города обязательна к заполнению и должна быть положительным числом.");
        }

        if (bookCreature.getAttackLevel() == null || bookCreature.getAttackLevel() <= 0) {
            throw new IllegalArgumentException("Уровень атаки обязателен к заполнению и должен быть больше 0.");
        }

        if (bookCreature.getDefenseLevel() == null || bookCreature.getDefenseLevel() <= 0) {
            throw new IllegalArgumentException("Уровень защиты обязателен к заполнению и должен быть больше 0.");
        }

        if (bookCreature.getRing().getName() == null || bookCreature.getRing().getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Имя кольца обязательно к заполнению.");
        }

        Object ringPower = bookCreature.getRing().getPower();
        if (ringPower == null || !(ringPower instanceof Integer) || (Integer) ringPower <= 0) {
            throw new IllegalArgumentException("Сила кольца обязательна к заполнению и должна быть положительным числом.");
        }
    }

    public boolean isNameExists(String name) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            List<BookCreature> results = em.createQuery(
                            "SELECT bc FROM BookCreature bc WHERE bc.name = :name", BookCreature.class
                    )
                    .setParameter("name", name)
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .setMaxResults(1)
                    .getResultList();

            em.getTransaction().commit();
            return !results.isEmpty();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }




}
