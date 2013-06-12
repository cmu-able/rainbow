package incubator.scb.ui;

import incubator.pval.Ensure;
import incubator.scb.ScbField;
import incubator.scb.ValidationResult;

/**
 * Field in an SCB table.
 * @param <T> the bean type
 * @param <F> the SCB field type
 * @param <V> the type of field value
 */
public abstract class ScbTableModelField<T, V, F extends ScbField<T, V>> {
	/**
	 * The field configuration.
	 */
	private F m_cof;
	
	/**
	 * Is the field editable?
	 */
	private boolean m_editable;
	
	/**
	 * Creates a new, non-editable, field.
	 * @param cof the configuration object field
	 */
	public ScbTableModelField(F cof) {
		this(cof, false);
	}
	
	/**
	 * Creates a new field.
	 * @param cof the configuration object field
	 * @param editable is the field editable?
	 */
	public ScbTableModelField(F cof, boolean editable) {
		Ensure.notNull(cof);
		m_cof = cof;
		m_editable = editable;
	}
	
	/**
	 * Obtains the field name.
	 * @return the field name
	 */
	public String name() {
		return m_cof.name();
	}
	
	/**
	 * Obtains the configuration object field.
	 * @return the field
	 */
	public F cof() {
		return m_cof;
	}
	
	/**
	 * Obtains whether the field is editable or not.
	 * @return is the field editable?
	 */
	public boolean editable() {
		return m_editable;
	}
	
	/**
	 * Returns the field as a table display object.
	 * @param obj the object hose value should be displayed
	 * @return the display object
	 */
	public abstract Object display_object(T obj);
	
	/**
	 * Sets the value of the field based on a display object. Subclasses that
	 * implement editable fields should override this method.
	 * @param obj the object
	 * @param display the display object
	 * @return the result of validating the value (the field is only set if
	 * validation succeeds)
	 */
	public ValidationResult from_display(T obj, Object display) {
		throw new UnsupportedOperationException();
	}
}
