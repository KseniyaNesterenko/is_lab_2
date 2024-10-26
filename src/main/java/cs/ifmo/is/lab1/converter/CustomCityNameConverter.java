package cs.ifmo.is.lab1.converter;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

@FacesConverter("customCityNameConverter")
public class CustomCityNameConverter implements Converter<String> {
    @Override
    public String getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        if ("Mordor".equals(value)) {
            throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Такое имя выбрать нельзя!", "НЕПРАВИЛЬНО☺️"));
        }

        return value;
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, String s) {
        return s;
    }
}
