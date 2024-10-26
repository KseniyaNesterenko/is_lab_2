package cs.ifmo.is.lab1.validator;
import cs.ifmo.is.lab1.model.BookCreature;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.component.UIInput;

import java.util.function.Function;
import java.util.function.Predicate;

public class Validator {
    private BookCreature bookCreature;

    public void validateAge(AjaxBehaviorEvent event) {
        validateNumber(event, "age", Long::parseLong, null, "Invalid Y coordinate", "Y обязательно к заполнению");
    }

    public void validateX(AjaxBehaviorEvent event) {
        validateNumber(event, "x", Integer::parseInt, x -> x.doubleValue() < 488, "Invalid X coordinate", "X coordinate is required");
    }

    public void validateY(AjaxBehaviorEvent event) {
        validateNumber(event, "y", Integer::parseInt, null, "Invalid Y coordinate", "Y обязательно к заполнению");
    }

    public void validateCityArea(AjaxBehaviorEvent event) {
        validateNumber(event, "cityArea", Double::parseDouble, area -> area.doubleValue() > 0, "Invalid city area", "City area is required");
    }

    public void validateCityPopulation(AjaxBehaviorEvent event) {
        validateNumber(event, "cityPopulation", Integer::parseInt, population -> population.doubleValue() > 0, "Invalid city population", "City population is required");
    }

    public void validateCityPopulationDensity(AjaxBehaviorEvent event) {
        validateNumber(event, "cityPopulationDensity", Integer::parseInt, density -> density.doubleValue() > 0, "Invalid city population density", "City population density is required");
    }

    public void validateAttackLevel(AjaxBehaviorEvent event) {
        validateNumber(event, "attackLevel", Float::parseFloat, level -> level.doubleValue() > 0, "Invalid attack level", "Attack level is required");
    }

    public void validateDefenseLevel(AjaxBehaviorEvent event) {
        validateNumber(event, "defenseLevel", Float::parseFloat, level -> level.doubleValue() > 0, "Invalid defense level", "Defense level is required");
    }

    public void validateRingName(AjaxBehaviorEvent event) {
        validateString(event, "ringName", "Ring Введите имя!");
    }

    public void validateRingPower(AjaxBehaviorEvent event) {
        validateNumber(event, "ringPower", Integer::parseInt, power -> power.doubleValue() > 0, "Invalid ring power", "Ring power is required");
    }

    public void validateMagicCityAndRingId(AjaxBehaviorEvent event) {
        validateNumber(event, "ringPower", Integer::parseInt, power -> power.doubleValue() > 0, "Invalid ring power", "Ring power is required");
    }

    private void validateNumber(AjaxBehaviorEvent event, String fieldName, Function<String, Number> parser, Predicate<Number> condition, String invalidMessage, String requiredMessage) {
        UIInput input = (UIInput) event.getSource();
        String inputValue = (String) input.getSubmittedValue();

        if (inputValue != null && !inputValue.isEmpty()) {
            try {
                Number value = parser.apply(inputValue);
                if (condition == null || condition.test(value)) {
                    FacesContext.getCurrentInstance().getMessages(fieldName).remove();
                } else {
                    FacesContext.getCurrentInstance().addMessage(fieldName, new FacesMessage(FacesMessage.SEVERITY_ERROR, invalidMessage, invalidMessage));
                }
            } catch (NumberFormatException e) {
                FacesContext.getCurrentInstance().addMessage(fieldName, new FacesMessage(FacesMessage.SEVERITY_ERROR, invalidMessage, invalidMessage));
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(fieldName, new FacesMessage(FacesMessage.SEVERITY_ERROR, requiredMessage, requiredMessage));
        }
    }

    private void validateString(AjaxBehaviorEvent event, String fieldName, String requiredMessage) {
        UIInput input = (UIInput) event.getSource();
        String inputValue = (String) input.getSubmittedValue();

        if (inputValue != null && !inputValue.isEmpty()) {
            FacesContext.getCurrentInstance().getMessages(fieldName).remove();
        } else {
            FacesContext.getCurrentInstance().addMessage(fieldName, new FacesMessage(FacesMessage.SEVERITY_ERROR, requiredMessage, requiredMessage));
        }
    }

    public boolean validateAllFields() {
        boolean isValid = true;

        if (bookCreature.getName() == null || bookCreature.getName().isEmpty()) {
            addErrorMessage("name", "Введите имя!");
            isValid = false;
        }

        Object x = bookCreature.getCoordinates().getX();
        if (String.valueOf(bookCreature.getCoordinates().getX()) == null || bookCreature.getCoordinates().getX() >= 488 || !(x instanceof Integer)) {
            addErrorMessage("x", "X обязательно к заполнению и должно быть меньше 488");
            isValid = false;
        }

        Object y = bookCreature.getCoordinates().getY();
        if (String.valueOf(bookCreature.getCoordinates().getY()) == null || !(y instanceof Integer)) {
            addErrorMessage("y", "Y обязательно к заполнению");
            isValid = false;
        }

        if (bookCreature.getAge() == null || !(bookCreature.getAge() instanceof Long) || (bookCreature.getAge() < 0)) {
            addErrorMessage("age", "Age обязательно к заполнению");
            isValid = false;
        }

        if (bookCreature.getCreatureType() == null) {
            addErrorMessage("creatureType", "Creature Type обязательно к заполнению");
            isValid = false;
        }

        if (bookCreature.getCreatureLocation().getName() == null || bookCreature.getCreatureLocation().getName().isEmpty()) {
            addErrorMessage("cityName", "Magic City Введите имя!");
            isValid = false;
        }

        if (bookCreature.getCreatureLocation().getArea() == null || bookCreature.getCreatureLocation().getArea() <= 0) {
            addErrorMessage("cityArea", "Magic City Поле Area обязательно к заполнению и должно быть больше 0");
            isValid = false;
        }

        Object p = bookCreature.getCreatureLocation().getPopulation();
        if (bookCreature.getCreatureLocation().getPopulation() == null || bookCreature.getCreatureLocation().getPopulation() <= 0 || !(p instanceof Integer)) {
            addErrorMessage("cityPopulation", "Magic City Поле Population обязательно к заполнению и должно быть больше 0");
            isValid = false;
        }

        Object d = bookCreature.getCreatureLocation().getPopulationDensity();
        if (String.valueOf(bookCreature.getCreatureLocation().getPopulationDensity()) == null || bookCreature.getCreatureLocation().getPopulationDensity() <= 0 || !(d instanceof Integer)) {
            addErrorMessage("cityPopulationDensity", "Magic City Поле Population Density обязательно к заполнению и должно быть больше 0");
            isValid = false;
        }

        if (bookCreature.getAttackLevel() == null || bookCreature.getAttackLevel() <= 0) {
            addErrorMessage("attackLevel", "Attack Level обязательно к заполнению и больше 0");
            isValid = false;
        }

        if (bookCreature.getDefenseLevel() == null || bookCreature.getDefenseLevel() <= 0) {
            addErrorMessage("defenseLevel", "Defense Level обязательно к заполнению и больше 0e");
            isValid = false;
        }

        if (bookCreature.getRing().getName() == null || bookCreature.getRing().getName().isEmpty()) {
            addErrorMessage("ringName", "Введите имя кольца!");
            isValid = false;
        }

        Object power = bookCreature.getRing().getPower();
        if (bookCreature.getRing().getPower() == null || bookCreature.getRing().getPower() <= 0 || !(power instanceof Integer)) {
            addErrorMessage("ringPower", "Ring Power обязательно к заполнению и больше 0");
            isValid = false;
        }

        return isValid;
    }

    private void addErrorMessage(String fieldName, String message) {
        FacesContext.getCurrentInstance().addMessage(fieldName, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
    }
}
