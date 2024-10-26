package cs.ifmo.is.lab1.converter;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

@FacesConverter("customIntegerConverter")
public class CustomCoordinatesConverter implements Converter<Integer> {

    @Override
    public Integer getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        String coordinateType = null;
        try {
            Integer parsedValue = Integer.parseInt(value);
            coordinateType = (String) component.getAttributes().get("coordinateType");

            if ("x".equals(coordinateType) && parsedValue > 488) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "X должно быть меньше 488!", "НЕПРАВИЛЬНО☺️"));
            }
            if ("p".equals(coordinateType) && parsedValue <= 0) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Значение Magic City Population должно быть больше 0!", "НЕПРАВИЛЬНО☺️"));
            }
            if ("attack".equals(coordinateType) && parsedValue <= 0) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Значение поля Attack Level должно быть больше 0!", "НЕПРАВИЛЬНО☺️"));
            }
            return parsedValue;

        } catch (NumberFormatException e) {
            throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Некорректный ввод поля " + coordinateType + "!", "НЕПРАВИЛЬНО☺️"));
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
