package incubator.scb.ui;

import java.util.Comparator;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;

/**
 * Table that displays SCBs.
 * @param <T> the bean type
 */
@SuppressWarnings("serial")
public class ScbTable<T> extends JTable {
	/**
	 * The model backing the table.
	 */
	private ScbTableModel<T, ? extends Comparator<T>> m_model;
	
	/**
	 * Creates a new table.
	 * @param model the model backing the table
	 */
	public ScbTable(ScbTableModel<T, ? extends Comparator<T>> model) {
		super(model);
		
		m_model = model;
		
		getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
	}
	
	/**
	 * Obtains the currently selected SCB, <code>null</code> if none
	 * @return the object
	 */
	public T selected() {
		int min = getSelectionModel().getMinSelectionIndex();
		if (min == -1) {
			return null;
		}
		
		return m_model.object(min);
	}
}
