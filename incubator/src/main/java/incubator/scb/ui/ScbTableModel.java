package incubator.scb.ui;

import incubator.pval.Ensure;
import incubator.scb.ScbContainer;
import incubator.scb.ScbContainerListener;
import incubator.scb.ScbDateField;
import incubator.scb.ScbDerivedTextFromDateField;
import incubator.scb.ScbField;
import incubator.scb.ScbTextField;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Abstract table model in which each row corresponds to an SCB.
 * @param <T> the bean type
 * @param <C> the bean comparator used for sorting
 */
@SuppressWarnings("serial")
public class ScbTableModel<T, C extends Comparator<T>>
		extends AbstractTableModel {
	/**
	 * All fields.
	 */
	private List<ScbTableModelField<T, ?, ?>> m_fields;
	
	/**
	 * Objects in the table, sorted.
	 */
	private List<T> m_objects;
	
	/**
	 * The object comparator.
	 */
	private C m_comparator;
	
	/**
	 * The container listener.
	 */
	private ScbContainerListener<T> m_listener;
	
	/**
	 * The container.
	 */
	private ScbContainer<T> m_container;
	
	/**
	 * Creates a new table model.
	 * @param container the SCB container; if <code>null</code> no data is
	 * shown
	 * @param comparator the comparator
	 */
	public ScbTableModel(ScbContainer<T> container, C comparator) {
		Ensure.notNull(comparator);
		m_fields = new ArrayList<>();
		m_objects = new ArrayList<>();
		m_comparator = comparator;
		
		if (container != null) {
			for (T t : container.all_scbs()) {
				add(t);
			}
		}
		
		m_listener = new ScbContainerListener<T>() {
			@Override
			public void scb_added(final T t) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						add(t);
					}
				});
			}

			@Override
			public void scb_removed(final T t) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						remove(t);
					}
				});
			}
			
			@Override
			public void scb_updated(final T t) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						update(t);
					}
				});
			}
		};
		
		m_container = container;
		
		if (container != null) {
			container.add_listener(m_listener);
		}
	}
	
	/**
	 * Switches the container being shown by this table model.
	 * @param new_container the new container to show; <code>null</code> means
	 * no data should be shown
	 */
	public void switch_container(ScbContainer<T> new_container) {
		if (new_container == m_container) {
			return;
		}
		
		if (m_container != null) {
			m_container.remove_listener(m_listener);
		}
		
		if (new_container != null) {
			new_container.add_listener(m_listener);
		}
		
		m_container = new_container;
		
		for (T t : new ArrayList<>(m_objects)) {
			remove(t);
		}
		
		if (new_container != null) {
			for (T t : new_container.all_scbs()) {
				add(t);
			}
		}
	}
	
	/**
	 * Adds a field to the table model. This method should be invoked in the
	 * sub class constructor or immediately after construction.
	 * @param f the field
	 */
	public final void add_field(ScbTableModelField<T, ?, ?> f) {
		Ensure.notNull(f);
		m_fields.add(f);
	}
	
	/**
	 * Adds a non-editable field to the table model inferring its type
	 * automatically.
	 * @param f the field to add
	 */
	public final void add_field_auto(ScbField<T, ?> f) {
		add_field_auto(f, false);
	}
	
	/**
	 * Adds a field to the table model inferring its type automatically.
	 * @param f the field to add
	 * @param editable is the field editable?
	 */
	public final void add_field_auto(ScbField<T, ?> f, boolean editable) {
		if (f instanceof ScbTextField) {
			@SuppressWarnings("unchecked")
			ScbTextField<T> stf = (ScbTextField<T>) f;
			add_field(new ScbTableModelTextField<>(stf, editable));
		} else if (f instanceof ScbDateField) {
			@SuppressWarnings("unchecked")
			ScbDateField<T> sdf = (ScbDateField<T>) f;
			add_field(new ScbTableModelTextField<>(
					new ScbDerivedTextFromDateField<>(sdf), editable));
		} else {
			Ensure.isTrue(false);
		}
	}
	
	/**
	 * Adds a new object to the table.
	 * @param obj the object to add
	 */
	private void add(T obj) {
		Ensure.notNull(obj);
		
		int idx;
		for (idx = 0; idx < m_objects.size(); idx++) {
			if (m_comparator.compare(m_objects.get(idx), obj) > 0) {
				break;
			}
		}
		
		m_objects.add(idx, obj);
		fireTableRowsInserted(idx, idx);
	}
	
	/**
	 * Removes an object from the table.
	 * @param obj the object
	 */
	private void remove(T obj) {
		Ensure.notNull(obj);
		
		int idx = m_objects.indexOf(obj);
		Ensure.isTrue(idx >= 0);
		
		m_objects.remove(idx);
		fireTableRowsDeleted(idx, idx);
	}
	
	/**
	 * An object has been updated.
	 * @param obj the object
	 */
	private void update(T obj) {
		Ensure.notNull(obj);
		
		int idx = m_objects.indexOf(obj);
		Ensure.isTrue(idx >= 0);
		
		boolean same_position = true;
		T prev = null;
		T next = null;
		if (idx > 0) {
			prev = m_objects.get(idx - 1);
		}
		
		if (idx < m_objects.size() - 1) {
			next = m_objects.get(idx + 1);
		}
		
		if (prev != null && m_comparator.compare(prev, obj) > 0) {
			same_position = false;
		}
		
		if (next != null && m_comparator.compare(next, obj) < 0) {
			same_position = false;
		}
		
		if (!same_position) {
			remove(obj);
			add(obj);
		} else {
			fireTableRowsUpdated(idx, idx);
		}
	}
	
	@Override
	public int getColumnCount() {
		return m_fields.size();
	}
	
	@Override
	public String getColumnName(int col) {
		Ensure.isTrue(col >= 0);
		Ensure.isTrue(col < m_fields.size());
		
		return m_fields.get(col).name();
	}
	
	@Override
	public int getRowCount() {
		return m_objects.size();
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		Ensure.isTrue(row >= 0);
		Ensure.isTrue(row < m_objects.size());
		Ensure.isTrue(col >= 0);
		Ensure.isTrue(col < m_fields.size());
		
		T obj = m_objects.get(row);
		return m_fields.get(col).display_object(obj);
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
		Ensure.isTrue(row >= 0);
		Ensure.isTrue(row < m_objects.size());
		Ensure.isTrue(col >= 0);
		Ensure.isTrue(col < m_fields.size());
		
		return m_fields.get(col).editable();
	}
	
	@Override
	public void setValueAt(Object value, int row, int col) {
		Ensure.is_true(row >= 0);
		Ensure.is_true(row < m_objects.size());
		Ensure.is_true(col >= 0);
		Ensure.is_true(col < m_fields.size());
		Ensure.is_true(m_fields.get(col).editable());
		
		m_fields.get(col).from_display(m_objects.get(row), value);
	}
	
	/**
	 * Obtains the object at a specified index.
	 * @param idx the index
	 * @return the object
	 */
	public T object(int idx) {
		Ensure.isTrue(idx >= 0);
		Ensure.isTrue(idx < m_objects.size());
		return m_objects.get(idx);
	}
}
