package incubator.scb;

import incubator.pval.Ensure;
import incubator.scb.ValidationResult;

/**
 * Class representing a field of a SCB (a statically checked bean).
 *
 * @param <T> the bean class
 * @param <V> the field's type
 */
public abstract class ScbField<T, V> {
	/**
	 * Field name.
	 */
	private String m_name;
	
	/**
	 * Can the field be set?
	 */
	private boolean m_can_set;
	
	/**
	 * Help text for the field.
	 */
	private String m_help;
	
	/**
	 * Creates a new field describing an SCB.
	 * @param name the field's name
	 * @param can_set can the field be set? If <code>false</code> the field
	 * is read-only
	 * @param help the field's help text (optional)
	 */
	public ScbField(String name, boolean can_set, String help) {
		Ensure.notNull(name);
		m_name = name;
		m_can_set = can_set;
		m_help = help;
	}
	
	/**
	 * Obtains the field's name.
	 * @return the field's name
	 */
	public String name() {
		return m_name;
	}
	
	/**
	 * Checks whether a value is valid for a field
	 * @param value the value
	 * @return is the value valid?
	 */
	public ValidationResult valid(V value) {
		return ValidationResult.make_valid();
	}
	
	/**
	 * Obtains a help text for the field.
	 * @return a help text or <code>null</code> if none is defined
	 */
	public String help() {
		return m_help;
	}
	
	/**
	 * Obtains whether the field can be set or not.
	 * @return whether the field can be set
	 */
	public boolean can_set() {
		return m_can_set;
	}
	
	/**
	 * Sets the value of the field in an object. This method will never be
	 * invoked if {@link #can_set()} returns <code>false</code>.
	 * @param t the object
	 * @param value the value to set; this value must have been previously
	 * validated using {@link #valid(Object)}
	 */
	public abstract void set(T t, V value);
	
	/**
	 * Gets the value of the field in an object.
	 * @param t the object
	 * @return the value
	 */
	public abstract V get(T t);
}
