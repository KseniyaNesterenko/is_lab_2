package cs.ifmo.is.lab1.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import jakarta.faces.convert.FacesConverter;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;


@FacesConverter("dateConverter")
public class CustomDateConverter implements Converter<Date> {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Override
    public Date getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
            return sdf.parse(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format", e);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Date value) {
        if (value == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        return sdf.format(value);
    }
}
