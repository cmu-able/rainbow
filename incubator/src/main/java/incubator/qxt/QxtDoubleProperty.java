package incubator.qxt;

import incubator.ui.RegexValidationDocument;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;

import org.jdesktop.swingx.table.TableColumnExt;

/**
 * Property supporting double values. The objects must have either
 * <code>Double</code> or <code>double</code> types.
 */
public class QxtDoubleProperty extends QxtRealProperty<Double> {
	/**
	 * Creates a new property.
	 * 
	 * @param name the property name
	 * @param display the property display name
	 */
	public QxtDoubleProperty(String name, String display) {
		super(name, display, Double.class);
	}

	/**
	 * Creates a new property.
	 * 
	 * @param name the property name
	 * @param display the property display name
	 * @param readOnly is the property read only?
	 */
	public QxtDoubleProperty(String name, String display, boolean readOnly) {
		super(name, display, readOnly, Double.class);
	}

	@Override
	void setup(TableColumnExt tc) {
		assert tc != null;

		JTextField tf = new JTextField();
		tf.setDocument(new RegexValidationDocument(
				"(\\+|\\-)?\\d*(\\.(\\d+)?)?"));
		tc.setCellEditor(new DefaultCellEditor(tf));
	}

	@Override
	protected Object convertFromEditorValue(Object value) {
		if (!(value instanceof String)) {
			return null;
		}

		try {
			Double dval = new Double((String) value);
			return dval;
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
