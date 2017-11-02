package incubator.qxt;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTree;

import org.apache.commons.lang.ObjectUtils;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.TableColumnExt;

import incubator.obscol.ObservableList;
import incubator.ui.AutoUpdateJComboBox;

/**
 * Property editing data with an autoupdate combo box.
 * 
 * @param <T> the data type
 */
public class QxtComboBoxProperty<T> extends QxtRealProperty<T> {
	/**
	 * The auto update combo box.
	 */
	private AutoUpdateJComboBox<T> autoCombo;

	/**
	 * Converter that transform T into string.
	 */
	private StringValue stringValue;

	/**
	 * Creates a new property.
	 * 
	 * @param name the property name
	 * @param display the property display name
	 * @param propertyClass the class of the property value type
	 * @param data the data displayed in the combo box
	 */
	public QxtComboBoxProperty(String name, String display,
			Class<T> propertyClass, ObservableList<T> data) {
		super(name, display, propertyClass);

		init(data, null);
	}

	/**
	 * Creates a new property.
	 * 
	 * @param name the property name
	 * @param display the property display name
	 * @param propertyClass the class of the property value type
	 * @param data the data displayed in the combo box
	 * @param converter the object used to transform data into strings. If
	 * <code>null</code> the standard <code>toString</code> method is used
	 */
	public QxtComboBoxProperty(String name, String display,
			Class<T> propertyClass, ObservableList<T> data,
			StringValue converter) {
		super(name, display, propertyClass);

		init(data, converter);
	}

	/**
	 * Creates a new property.
	 * 
	 * @param name the property name
	 * @param display the property display name
	 * @param propertyClass the class of the property value type
	 * @param readOnly is the property read only?
	 * @param data the data displayed in the combo box
	 */
	public QxtComboBoxProperty(String name, String display,
			Class<T> propertyClass, boolean readOnly, ObservableList<T> data) {
		super(name, display, readOnly, propertyClass);

		init(data, null);
	}

	/**
	 * Creates a new property.
	 * 
	 * @param name the property name
	 * @param display the property display name
	 * @param propertyClass the class of the property value type
	 * @param readOnly is the property read only?
	 * @param data the data displayed in the combo box
	 * @param converter the object used to transform data into strings. If
	 * <code>null</code> the standard <code>toString</code> method is used
	 */
	public QxtComboBoxProperty(String name, String display,
			Class<T> propertyClass, boolean readOnly,
			ObservableList<T> data, StringValue converter) {
		super(name, display, readOnly, propertyClass);

		init(data, converter);
	}

	/**
	 * Define how <code>null</code> is supported in the combo box.
	 * 
	 * @param nullSupport should <code>null</code> be supported (and how)
	 * @param nullValue what value to use as <code>null</code> (may be
	 * <code>null</code>)
	 */
	public void setNullSupport(AutoUpdateJComboBox.NullSupport nullSupport,
			String nullValue) {
		if (nullSupport == null) {
			throw new IllegalArgumentException("nullSupport == null");
		}

		autoCombo.setNullSupport(nullSupport, nullValue);
	}

	/**
	 * Initializes the combo box property.
	 * 
	 * @param data the observable data used for the combo box
	 * @param converter the object used to transform data into strings. If
	 * <code>null</code> the standard <code>toString</code> method is used
	 */
	private void init(ObservableList<T> data, StringValue converter) {
		if (data == null) {
			throw new IllegalArgumentException("data == null");
		}

		if (converter == null) {
			converter = new StringValue() {
				private static final long serialVersionUID = 1L;

				@Override
				public String getString(Object value) {
					return ObjectUtils.toString(value);
				}
			};
		}

		autoCombo = new AutoUpdateJComboBox<>(data, converter);
		stringValue = converter;
	}

	@Override
	void setup(TableColumnExt tc) {
		tc.setCellRenderer(new DefaultTableRenderer(stringValue));
		tc.setCellEditor(new DefaultCellEditor(autoCombo) {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getCellEditorValue() {
				return autoCombo.getSelected();
			}

			@Override
			public Component getTableCellEditorComponent(JTable table,
					Object value, boolean isSelected, int row, int column) {
				return super.getTableCellEditorComponent(table, stringValue
						.getString(value), isSelected, row, column);
			}

			@Override
			public Component getTreeCellEditorComponent(JTree tree,
					Object value, boolean isSelected, boolean expanded,
					boolean leaf, int row) {
				return super.getTreeCellEditorComponent(tree, stringValue
						.getString(value), isSelected, expanded, leaf, row);
			}
		});
	}

	/**
	 * Obtains the data used in the combo box.
	 * 
	 * @return the data used
	 */
	public ObservableList<T> getData() {
		return autoCombo.getData();
	}
}
