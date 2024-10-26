package cs.ifmo.is.lab1.service;

import cs.ifmo.is.lab1.model.MagicCity;
import cs.ifmo.is.lab1.repository.MagicCityRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Stateless
public class MagicCityService implements Serializable {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("IsLab1");

    @Inject
    private MagicCityRepository magicCityRepository;

    public void create(MagicCity magicCity) {
        magicCityRepository.create(magicCity);
    }

    public List<MagicCity> findAll() {
        return magicCityRepository.findAll();
    }

    public MagicCity findById(Integer id) {
        return magicCityRepository.findById(id);
    }

    public void update(MagicCity magicCity) {
        magicCityRepository.update(magicCity);
    }

    public String delete(Integer id) {
        if (isLinkedToOtherObjects(id)) {
            return "Вы не можете удалить этот объект! Он связан с объектом Book Creature!";
        }
        magicCityRepository.delete(id);
        return null;
    }


    public List<MagicCity> findAll(int page, int pageSize, String filterId, String filterName, String filterArea, String filterPopulation, String filterPopulationDensity, String filterEstablishmentDate, String filterGovernor, Boolean filterCapital, String sortField, boolean sortAscending) {
        return magicCityRepository.findAll(page, pageSize, filterId, filterName, filterArea, filterPopulation, filterPopulationDensity, filterEstablishmentDate, filterGovernor, filterCapital, sortField, sortAscending);
    }

    public long count(String filterId, String filterName, String filterArea, String filterPopulation, String filterPopulationDensity, String filterEstablishmentDate, String filterGovernor, Boolean filterCapital) {
        return magicCityRepository.count(filterId, filterName, filterArea, filterPopulation, filterPopulationDensity, filterEstablishmentDate, filterGovernor, filterCapital);
    }

    public long count() {
        return magicCityRepository.count();
    }

    public boolean isLinkedToOtherObjects(Integer magicCityId) {
        return magicCityRepository.isLinkedToOtherObjects(magicCityId);
    }

}
