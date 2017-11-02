package incubator.scb.ui;

import incubator.Pair;
import incubator.scb.ScbField;
import incubator.scb.ValidationResult;
import incubator.ui.AutoUpdateJComboBox;

import javax.swing.DefaultCellEditor;
import javax.swing.table.TableCellEditor;

import org.apache.commons.lang.StringUtils;

/**
 * Table field that displays an enumeration value as text.
 * @param <T> the type of SCB
 * @param <E> the type of enumeration
 */
public class ScbTableModelEnumTextField<T, E extends Enum<E>>
		extends ScbTableModelField<T, E, ScbField<T, E>> {
	/**
	 * Combo box used for editing.
	 */
	private AutoUpdateJComboBox<E> m_combo;
	
	/**
	 * Creates a new enumeration text field.
	 * @param cof the field
	 */
	public ScbTableModelEnumTextField(ScbField<T, E> cof) {
		this(cof, false);
	}

	/**
	 * Creates a new enumeration text field.
	 * @param cof the field
	 * @param editable is the field editable?
	 */
	public ScbTableModelEnumTextField(ScbField<T, E> cof,
			boolean editable) {
		super(cof, editable);
		
		m_combo = new AutoUpdateJComboBox<>(cof.value_type(), true);
	}
	
	@Override
	public Object display_object(T obj) {
		return convert_enum(cof().get(obj));
	}
	
	@Override
	public Pair<ValidationResult, E> from_display(T obj, Object display) {
		E e = convert_to_enum((String) display);
		cof().set(obj, e);
		return new Pair<>(ValidationResult.make_valid(), e);
	}
	
	@Override
	public TableCellEditor cell_editor() {
		return new DefaultCellEditor(m_combo);
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
	
	/**
	 * Converts texto to an enum. The default implementation calls
	 * the <code>valueOf</code> method.
	 * @param t the text
	 * @return the converted enum value
	 */
	public E convert_to_enum(String t) {
		if (StringUtils.trimToNull(t) == null) {
			return null;
		}
		
		for (E e : cof().value_type().getEnumConstants()) {
			if (convert_enum(e).equals(t)) {
				return e;
			}
		}
		
		return null;
	}
}
