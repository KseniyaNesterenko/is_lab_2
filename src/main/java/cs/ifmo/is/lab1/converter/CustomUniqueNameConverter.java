package cs.ifmo.is.lab1.converter;

import cs.ifmo.is.lab1.service.BookCreatureService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;

@FacesConverter("customUniqueNameConverter")
public class CustomUniqueNameConverter implements Converter<String> {

    @Override
    public String getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        // Проверяем, существует ли имя в базе данных
        if (isNameExists(value.trim())) {
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Invalid name",
                    "The name '" + value + "' is already taken. Please choose another name."
            );
            throw new ConverterException(message);
        }

        return value;
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, String s) {
        return s;
    }

    @PersistenceUnit(unitName = "IsLab1")
    private EntityManagerFactory emf;
    public boolean isNameExists(String name) {
        EntityManager em = emf.createEntityManager();
        try {
            Long count = em.createQuery(
                    "SELECT COUNT(bc) FROM BookCreature bc WHERE bc.name = :name", Long.class
            ).setParameter("name", name).getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }
}
