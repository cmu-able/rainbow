package incubator.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComboBox;

import org.jdesktop.swingx.renderer.StringValue;

import incubator.obscol.ObservableList;
import incubator.obscol.ObservableListListener;
import incubator.obscol.WrapperObservableList;
import incubator.pval.Ensure;

/**
 * Combo box that uses an observes a list and updates itself automatically
 * when the list is updated.
 * 
 * @param <T> the type of objects on the combo box
 */
public class AutoUpdateJComboBox<T> extends JComboBox<Object> {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Converter used to transform objects into strings.
	 */
	private StringValue converter;

	/**
	 * The set of data being used in the combo box.
	 */
	private ObservableList<T> data;

	/**
	 * What kind of <code>null</code> support does the combo box has?
	 */
	private NullSupport nullSupport;

	/**
	 * Creates a new combo box based on the set of observable data.
	 * 
	 * @param data the observable data set (cannot be <code>null</code>)
	 */
	public AutoUpdateJComboBox(ObservableList<T> data) {
		this(data, null);
	}
	
	/**
	 * Creates a new combo box whose data comes from an enumeration.
	 * @param e_class the enumeration class
	 * @param sort should the enumeration values be sorted?
	 */
	public AutoUpdateJComboBox(Class<T> e_class, boolean sort) {
		this(make_list(e_class, sort));
	}
	
	/**
	 * Creates an observable list with all values from an enumeration.
	 * @param e_class the enumeration class
	 * @param sort should the values be sorted according to their
	 * <code>toString</code> method?
	 * @return the list
	 */
	private static <T> ObservableList<T> make_list(Class<T> e_class,
			boolean sort) {
		Ensure.not_null(e_class, "e_class == null");
		Ensure.is_true(e_class.isEnum(), "e_class (" + e_class.toString()
				+ ") is not an enumeration.");
		
		List<T> values = new ArrayList<>();
		for (T t : e_class.getEnumConstants()) {
			values.add(t);
		}
		
		if (sort) {
			Collections.sort(values, new Comparator<T>() {
				@Override
				public int compare(T o1, T o2) {
					return o1.toString().compareTo(o2.toString());
				}
			});
		}
		
		return new WrapperObservableList<>(values);
	}

	/**
	 * Creates a new combo box based on a set of observable data.
	 * 
	 * @param data the observable data set (cannot be <code>null</code>)
	 * @param objectConverter an optional object that will convert objects
	 * into strings. If none is provided, the <code>toString</code> method
	 * is used to display the objects in the combo box
	 */
	public AutoUpdateJComboBox(ObservableList<T> data,
			StringValue objectConverter) {
		if (data == null) {
			throw new IllegalArgumentException("data == null");
		}

		this.data = data;

		if (objectConverter == null) {
			converter = new StringValue() {
				private static final long serialVersionUID = 1L;

				@Override
				public String getString(Object object) {
					return object.toString();
				}
			};
		} else {
			converter = objectConverter;
		}

		data.addObservableListListener(new ObservableListListener<T>() {
			@Override
			public void elementAdded(T e, int idx) {
				if (nullSupport == NullSupport.NULL_AT_BEGINING) {
					idx++;
				}

				insertItemAt(converter.getString(e), idx);
			}

			@Override
			public void elementChanged(T oldE, T newE, int idx) {
				if (nullSupport == NullSupport.NULL_AT_BEGINING) {
					idx++;
				}

				removeItemAt(idx);
				insertItemAt(converter.getString(newE), idx);
			}

			@Override
			public void elementRemoved(T e, int idx) {
				if (nullSupport == NullSupport.NULL_AT_BEGINING) {
					idx++;
				}

				removeItemAt(idx);
			}

			@Override
			public void listCleared() {
				int startIdx = 0;
				int endIdx = getItemCount() - 1;

				if (nullSupport == NullSupport.NULL_AT_BEGINING) {
					startIdx++;
				}

				if (nullSupport == NullSupport.NULL_AT_END) {
					endIdx--;
				}

				for (int i = endIdx; i >= startIdx; i--) {
					removeItemAt(i);
				}
			}
		});

		nullSupport = NullSupport.NULL_NOT_ALLOWED;

		for (int i = 0; i < data.size(); i++) {
			addItem(converter.getString(data.get(i)));
		}
	}

	/**
	 * Define how <code>null</code> is supported. Invoking this method with
	 * a setting that allows <code>null</code> will insert the
	 * <code>null</code> value in the combo box (without inserting it into
	 * the list).
	 * 
	 * @param support how <code>null</code> should be supported
	 * @param nullValue what value should be displayed as <code>null</code>?
	 * (<code>null</code> is allowed is this parameter)
	 */
	public void setNullSupport(NullSupport support, String nullValue) {
		if (support == null) {
			throw new IllegalArgumentException("support == null");
		}

		if (support == nullSupport) {
			return;
		}

		/*
		 * Remove the old null support element (if any).
		 */
		if (nullSupport == NullSupport.NULL_AT_BEGINING) {
			removeItemAt(0);
		}

		if (nullSupport == NullSupport.NULL_AT_END) {
			int nullPos = getItemCount() - 1;
			removeItemAt(nullPos);
		}

		/*
		 * Add the new null support element (if any).
		 */
		nullSupport = support;
		if (nullSupport == NullSupport.NULL_AT_BEGINING) {
			insertItemAt(nullValue, 0);
		}

		if (nullSupport == NullSupport.NULL_AT_END) {
			addItem(nullValue);
		}
	}

	/**
	 * Obtains the selected item.
	 * 
	 * @return the selected item or <code>null</code> if none
	 */
	public final T getSelected() {
		int idx = getSelectedIndex();
		if (idx == -1) {
			return null;
		}

		if (nullSupport == NullSupport.NULL_AT_BEGINING) {
			if (idx == 0) {
				return null;
			}

			idx--;
		}

		if (nullSupport == NullSupport.NULL_AT_END
				&& idx == getItemCount() - 1) {
			return null;
		}

		return data.get(idx);
	}

	/**
	 * Selects the object from the list.
	 * 
	 * @param object being selected.
	 */
	public final void setSelected(Object object) {
		if (object == null) {
			if (nullSupport == NullSupport.NULL_AT_BEGINING) {
				setSelectedIndex(0);
			} else if (nullSupport == NullSupport.NULL_AT_END) {
				setSelected(getItemCount() - 1);
			} else {
				throw new IllegalStateException(
						"object == null but not is not " + "allowed.");
			}
		} else {
			int idx = data.indexOf(object);
			if (idx == -1) {
				throw new IllegalStateException(
						"Object does not belong to the " + "list.");
			}

			if (nullSupport == NullSupport.NULL_AT_BEGINING) {
				idx++;
				assert idx > 0;
			}

			assert idx >= 0 && idx < getItemCount();
			if (nullSupport == NullSupport.NULL_AT_END) {
				assert idx != getItemCount() - 1;
			}

			setSelectedIndex(idx);
		}
	}

	/**
	 * Obtains the data set.
	 * 
	 * @return the data set. This data set is the one used internally so
	 * changes to it will be shown in the combo box
	 */
	public final ObservableList<T> getData() {
		return data;
	}

	/**
	 * Enum defining how <code>null</code> selection is supported in the
	 * combo box.
	 */
	public enum NullSupport {
		/**
		 * <code>null</code> selection is not supported.
		 */
		NULL_NOT_ALLOWED,

		/**
		 * <code>null</code> selection is allowed and should be placed at
		 * the beginning.
		 */
		NULL_AT_BEGINING,

		/**
		 * <code>null</code> selection is allowed and should be placed at
		 * the end.
		 */
		NULL_AT_END
	}
}
