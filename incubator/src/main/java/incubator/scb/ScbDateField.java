package incubator.scb;

import java.util.Date;

/**
 * Field in an SCB that holds a date value.
 * @param <T> the bean class
 */
public abstract class ScbDateField<T> extends ScbField<T, Date> {
	/**
	 * Creates a new date field.
	 * @param name the field's name
	 * @param can_set can the field's value be set?
	 * @param help a help message for the field (optional)
	 */
	public ScbDateField(String name, boolean can_set, String help) {
		super(name, can_set, help);
	}
	
	@Override
	public ValidationResult valid(Date value) {
		return ValidationResult.make_valid();
	}
}
