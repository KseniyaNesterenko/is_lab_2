package cs.ifmo.is.lab1.converter;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

@FacesConverter("customPopulationConverter")
public class CustomPopulation implements Converter<Integer> {

    @Override
    public Integer getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        try {
            Integer parsedValue = Integer.parseInt(value);

            if (parsedValue <= 0) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Значение поля Magic City Population Density: должно быть больше 0!", "НЕПРАВИЛЬНО☺️"));
            }
            return parsedValue;

        } catch (NumberFormatException e) {
            throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Некорректный ввод поля Magic City Population Density!", "НЕПРАВИЛЬНО☺️"));
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Integer value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }
}
