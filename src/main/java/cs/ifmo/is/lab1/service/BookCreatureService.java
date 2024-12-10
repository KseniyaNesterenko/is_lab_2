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
public class BookCreatureService implements Serializable {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("IsLab1");

    @Inject
    private BookCreatureRepository bookCreatureRepository;

    @Inject
    private BookCreatureHistoryRepository bookCreatureHistoryRepository;

    @Inject
    private MagicCityRepository magicCityRepository;
    @Inject
    private RingRepository ringRepository;


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


    public void update(BookCreature bookCreature) {
        bookCreatureRepository.update(bookCreature);
    }

    public void update(MagicCity magicCity) {
        bookCreatureRepository.update(magicCity);
    }

    public void delete(Integer id) {
        bookCreatureRepository.delete(id);
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

    @Transactional
    public void importBookCreatures(List<BookCreature> bookCreatures) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            for (BookCreature bookCreature : bookCreatures) {
                validateBookCreature(bookCreature);
                em.persist(bookCreature);
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

    // Метод для проверки валидации объекта
    private void validateBookCreature(BookCreature bookCreature) {
        if (bookCreature.getName() == null || bookCreature.getName().isEmpty()) {
            throw new IllegalArgumentException("Имя существа не может быть пустым.");
        }
        if (bookCreature.getAge() == null || bookCreature.getAge() <= 0) {
            throw new IllegalArgumentException("Возраст существа должен быть положительным числом.");
        }
        // Добавьте другие проверки в соответствии с требованиями предметной области
    }

}
