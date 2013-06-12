package incubator.scb.ui;

import incubator.scb.ScbEnumField;

/**
 * Table field that displays an enumeration value as text.
 * @param <T> the type of SCB
 * @param <E> the type of enumeration
 */
public class ScbTableModelEnumTextField<T, E extends Enum<E>>
		extends ScbTableModelField<T, E, ScbEnumField<T, E>> {
	/**
	 * Creates a new enumeration text field.
	 * @param cof the field
	 */
	public ScbTableModelEnumTextField(ScbEnumField<T, E> cof) {
		super(cof);
	}

	@Override
	public Object display_object(T obj) {
		return convert_enum(cof().get(obj));
	}
	
	/**
	 * Converts an enum to text. The default implementation calls
	 * the <code>toString</code> method.
	 * @param e the enum to convert
	 * @return the converted value
	 */
	public String convert_enum(E e) {
		return e == null? "" : e.toString();
	}
}
