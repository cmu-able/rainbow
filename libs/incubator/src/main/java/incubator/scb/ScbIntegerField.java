package incubator.scb;


/**
 * Field in an SCB that holds an integer value.
 * @param <T> the bean class
 */
public abstract class ScbIntegerField<T> extends ScbField<T, Integer> {
	/**
	 * Creates a new integer field.
	 * @param name the field's name
	 * @param can_set can the field's value be set?
	 * @param help a help message for the field (optional)
	 */
	public ScbIntegerField(String name, boolean can_set, String help) {
		super(name, can_set, help, Integer.class);
	}
}
