package incubator.qxt;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;

import org.jdesktop.swingx.table.TableColumnExt;

import incubator.ui.RegexValidationDocument;

/**
 * Property supporting integer values. The objects must have either
 * <code>Integer</code> or <code>int</code> types.
 */
public class QxtIntegerProperty extends QxtRealProperty<Integer> {
	/**
	 * Creates a new property.
	 * 
	 * @param name the property name
	 * @param display the property display name
	 */
	public QxtIntegerProperty(String name, String display) {
		super(name, display, Integer.class);
	}

	@Override
	void setup(TableColumnExt tc) {
		assert tc != null;

		JTextField tf = new JTextField();
		tf.setDocument(new RegexValidationDocument("(\\+|\\-)?\\d*"));
		tc.setCellEditor(new DefaultCellEditor(tf));
	}

	@Override
	protected Object convertFromEditorValue(Object value) {
		if (!(value instanceof String)) {
			return null;
		}

		try {
			Integer ival = new Integer((String) value);
			return ival;
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
