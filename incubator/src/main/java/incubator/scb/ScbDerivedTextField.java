package incubator.scb;

import incubator.pval.Ensure;

/**
 * Text field in a SCB that is not obtained directly from the bean but derived
 * from another field.
 * @param <T> the type of bean
 * @param <V> the type of value this field's value is derived from 
 * @param <F> the field this value is derived from
 */
public abstract class ScbDerivedTextField<T, V, F extends ScbField<T, V>>
		extends ScbTextField<T> {
	/**
	 * The field this one derives from.
	 */
	private F m_field;

	/**
	 * Creates a new field.
	 * @param name the field name
	 * @param can_set can the field be set? Even if <code>true</code> this will
	 * only be considered as <code>true</code> if the field this derives from
	 * (<code>field</code>) can also be set
	 * @param help optional help text; if <code>null</code> the value from
	 * <code>field</code> is used
	 * @param field the field data comes frmo
	 */
	public ScbDerivedTextField(String name, boolean can_set, String help,
			F field) {
		super(name, can_set && field.can_set(), help == null? field.help() :
				help);
		m_field = field;
	}
	
	/**
	 * Converts a value from this field to the derived field.
	 * @param v the value to convert
	 * @return the converted value
	 * @throws ConversionFailedException conversion failed
	 */
	protected abstract V convert_to_derived(String v)
			throws ConversionFailedException;
	
	/**
	 * Converts a value from the derived field to this one.
	 * @param v the derived field value
	 * @return the converted value
	 */
	protected abstract String convert_from_dervied(V v);

	@Override
	public ValidationResult valid(String value) {
		V v;
		try {
			v = convert_to_derived(value);
		} catch (ConversionFailedException e) {
			return ValidationResult.make_invalid(e.getMessage());
		}
		
		return m_field.valid(v);
	}

	@Override
	public void set(T t, String value) {
		Ensure.not_null(t);
		
		try {
			m_field.set(t, convert_to_derived(value));
		} catch (ConversionFailedException e) {
			throw new IllegalArgumentException("Invalid value '" + value
					+ "'.", e);
		}
	}

	@Override
	public String get(T t) {
		Ensure.not_null(t);
		return convert_from_dervied(m_field.get(t));
	}
}
