package incubator.qxt;

/**
 * Property allowing editing strings.
 */
public class QxtStringProperty extends QxtRealProperty<String> {
	/**
	 * Creates a new property.
	 * 
	 * @param name the property name
	 * @param display the property display name
	 */
	public QxtStringProperty(String name, String display) {
		super(name, display, String.class);
	}

	/**
	 * Creates a new property.
	 * 
	 * @param name the property name
	 * @param display the property display name
	 * @param readOnly is the property read only?
	 */
	public QxtStringProperty(String name, String display, boolean readOnly) {
		super(name, display, readOnly, String.class);
	}
}
