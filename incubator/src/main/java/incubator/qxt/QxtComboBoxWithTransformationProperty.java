package incubator.qxt;

import incubator.obscol.ObservableList;
import incubator.obscol.ObservableListListener;
import incubator.obscol.WrapperObservableList;
import incubator.ui.AutoUpdateJComboBox;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTree;

import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * Property that obtains the value from an observable list using a
 * transformation function.
 * 
 * @param <T> the data type to place in the property
 * @param <R> the data type of the list
 */
public class QxtComboBoxWithTransformationProperty<T, R> extends
		QxtRealProperty<T> {
	/**
	 * The auto update combo box.
	 */
	private AutoUpdateJComboBox<String> autoCombo;

	/**
	 * Transformer that converts type R in T.
	 */
	private TypeTransformation<R, T> typeTransformation;

	/**
	 * Data to show in the combo box.
	 */
	private ObservableList<String> comboBoxData;

	/**
	 * The data used in the combo box.
	 */
	private ObservableList<R> data;

	/**
	 * Values converted from R to T.
	 */
	private List<T> converted;

	/**
	 * Creates a new property.
	 * 
	 * @param name the property name
	 * @param display the property display name
	 * @param propertyClass the class of the property value type
	 * @param data the data displayed in the combo box
	 * @param typeClass the type of the data
	 * @param typeTransformation a transformation from the list type to the
	 * property type
	 * @param stringTransformation a transformation that is used to convert
	 * values in the list to strings to show in the combo box
	 */
	public QxtComboBoxWithTransformationProperty(String name, String display,
			Class<T> propertyClass, ObservableList<R> data, Class<R> typeClass,
			TypeTransformation<R, T> typeTransformation,
			TypeTransformation<R, String> stringTransformation) {
		super(name, display, propertyClass);

		init(data, typeClass, typeTransformation, stringTransformation);
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
	 * @param typeClass the type of the data
	 * @param typeTransformation a transformation from the list type to the
	 * property type
	 * @param stringTransformation a transformation that is used to convert
	 * values in the list to strings to show in the combo box
	 */
	private void init(ObservableList<R> data, Class<R> typeClass,
			final TypeTransformation<R, T> typeTransformation,
			final TypeTransformation<R, String> stringTransformation) {
		if (data == null) {
			throw new IllegalArgumentException("data == null");
		}

		if (typeClass == null) {
			throw new IllegalArgumentException("typeClass == null");
		}

		if (typeTransformation == null) {
			throw new IllegalArgumentException("typeTransformation == null");
		}

		if (stringTransformation == null) {
			throw new IllegalArgumentException("stringTransformation == null");
		}

		this.typeTransformation = typeTransformation;
		this.comboBoxData = new WrapperObservableList<>(
				new ArrayList<String>());
		this.data = data;
		this.converted = new ArrayList<>();
		data.addObservableListListener(new ObservableListListener<R>() {
			@Override
			public void elementAdded(R e, int idx) {
				comboBoxData.add(idx, stringTransformation.transform(e));
				converted.add(idx, typeTransformation.transform(e));
			}

			@Override
			public void elementChanged(R oldE, R newE, int idx) {
				comboBoxData.set(idx, stringTransformation.transform(newE));
				converted.add(idx, typeTransformation.transform(newE));
			}

			@Override
			public void elementRemoved(R e, int idx) {
				comboBoxData.remove(idx);
				converted.remove(idx);
			}

			@Override
			public void listCleared() {
				comboBoxData.clear();
				converted.clear();
			}
		});

		for (R r : data) {
			comboBoxData.add(stringTransformation.transform(r));
			converted.add(typeTransformation.transform(r));
		}

		autoCombo = new AutoUpdateJComboBox<>(comboBoxData);
	}

	@Override
	void setup(TableColumnExt tc) {
		StringValue sv = new StringValue() {
			/**
			 * Version for serialization.
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String getString(Object arg0) {
				T t = getPropertyClass().cast(arg0);
				for (int i = 0; i < converted.size(); i++) {
					if (converted.get(i).equals(t)) {
						return comboBoxData.get(i);
					}
				}

				assert false;
				return null;
			}

		};

		tc.setCellRenderer(new DefaultTableRenderer(sv));
		tc.setCellEditor(new DefaultCellEditor(autoCombo) {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getCellEditorValue() {
				return autoCombo.getSelected();
			}

			@Override
			public Component getTableCellEditorComponent(JTable table,
					Object value, boolean isSelected, int row, int column) {
				return super.getTableCellEditorComponent(table, value,
						isSelected, row, column);
			}

			@Override
			public Component getTreeCellEditorComponent(JTree tree,
					Object value, boolean isSelected, boolean expanded,
					boolean leaf, int row) {
				return super.getTreeCellEditorComponent(tree, value,
						isSelected, expanded, leaf, row);
			}
		});
	}

	/**
	 * Obtains the data used in the combo box.
	 * 
	 * @return the data used
	 */
	public ObservableList<R> getData() {
		return data;
	}

	@Override
	protected Object convertFromEditorValue(Object value) {
		if (!(value instanceof String)) {
			assert false;
		}

		for (int i = 0; i < comboBoxData.size(); i++) {
			if (comboBoxData.get(i) == value) {
				return typeTransformation.transform(data.get(i));
			}
		}

		assert false;
		return null;
	}
}
