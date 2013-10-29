package incubator.scb;


/**
 * Field in an SCB that holds a long value.
 * @param <T> the bean class
 */
public abstract class ScbLongField<T> extends ScbField<T, Long> {
	/**
	 * Creates a new integer field.
	 * @param name the field's name
	 * @param can_set can the field's value be set?
	 * @param help a help message for the field (optional)
	 */
	public ScbLongField(String name, boolean can_set, String help) {
		super(name, can_set, help, Long.class);
	}
}
