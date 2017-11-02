package incubator.scb;


/**
 * Field in an SCB that holds a boolean value.
 * @param <T> the bean class
 */
public abstract class ScbBooleanField<T> extends ScbField<T, Boolean> {
	/**
	 * Creates a new date field.
	 * @param name the field's name
	 * @param can_set can the field's value be set?
	 * @param help a help message for the field (optional)
	 */
	public ScbBooleanField(String name, boolean can_set, String help) {
		super(name, can_set, help, Boolean.class);
	}
	
	@Override
	public ValidationResult valid(Boolean value) {
		return ValidationResult.make_valid();
	}
}
