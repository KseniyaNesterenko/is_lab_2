package cs.ifmo.is.lab1.service;

import cs.ifmo.is.lab1.model.Ring;
import cs.ifmo.is.lab1.repository.RingRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.io.Serializable;
import java.util.List;

@Stateless
public class RingService implements Serializable {

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("IsLab1");

    @Inject
    private RingRepository ringRepository;

    public void create(Ring ring) {
        ringRepository.create(ring);
    }

    public List<Ring> findAll() {
        return ringRepository.findAll();
    }

    public Ring findById(Integer id) {
        return ringRepository.findById(id);
    }

    public void update(Ring ring) {
        ringRepository.update(ring);
    }

    public String delete(Integer id) {
        if (isLinkedToOtherObjects(id)) {
            return "Вы не можете удалить этот объект! Он связан с другими объектами!";
        }
        ringRepository.delete(id);
        return null;
    }

    public List<Ring> findAll(int page, int pageSize, String filterId, String filterName, String filterPower, String sortField, boolean sortAscending) {
        return ringRepository.findAll(page, pageSize, filterId, filterName, filterPower, sortField, sortAscending);
    }

    public long count(String filterId, String filterName, String filterPower) {
        return ringRepository.count(filterId, filterName, filterPower);
    }

    public long count() {
        return ringRepository.count();
    }

    public boolean isLinkedToOtherObjects(Integer ringId) {
        return ringRepository.isLinkedToOtherObjects(ringId);
    }
}
