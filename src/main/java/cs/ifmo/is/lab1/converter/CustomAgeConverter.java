package cs.ifmo.is.lab1.converter;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

@FacesConverter("customLongConverter")
public class CustomAgeConverter implements Converter<Long> {

    @Override
    public Long getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        try {
            Long parsedValue = Long.parseLong(value);
            if (parsedValue <= 0) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Поле age должно быть больше 0!", "НЕПРАВИЛЬНО☺️"));
            }
            return parsedValue;
        } catch (NumberFormatException e) {
            throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Некорректный ввод поля age!", "НЕПРАВИЛЬНО☺️"));
        }
    }


    @Override
    public String getAsString(FacesContext context, UIComponent component, Long value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }
}