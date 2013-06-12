package incubator.scb;

/**
 * Field containing an enumeration value.
 * @param <T> the type of SCB
 * @param <E> the type of enumeration
 */
public abstract class ScbEnumField<T, E extends Enum<E>>
		extends ScbField<T, E> {
	/**
	 * Creates a new enumeration field.
	 * @param name the field name
	 * @param can_set can the field be set?
	 * @param help help text for the field
	 */
	public ScbEnumField(String name, boolean can_set, String help) {
		super(name, can_set, help);
	}
}
