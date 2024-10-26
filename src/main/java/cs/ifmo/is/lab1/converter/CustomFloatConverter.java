package cs.ifmo.is.lab1.converter;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

@FacesConverter("customFloatConverter")
public class CustomFloatConverter implements Converter<Float> {

    @Override
    public Float getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        String coordinateType = null;
        try {
            Float parsedValue = Float.parseFloat(value);
            coordinateType = (String) component.getAttributes().get("coordinateType");
            if ("attack".equals(coordinateType) && parsedValue <= 0) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Значение поля Attack Level должно быть больше 0!", "НЕПРАВИЛЬНО☺️"));
            }
            if ("defense".equals(coordinateType) && parsedValue <= 0) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Значение поля Defense Level должно быть больше 0!", "НЕПРАВИЛЬНО☺️"));
            }
            return parsedValue;
        } catch (NumberFormatException e) {
            throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Некорректный ввод поля" + coordinateType + "!", "НЕПРАВИЛЬНО☺️"));
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Float value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }
}
