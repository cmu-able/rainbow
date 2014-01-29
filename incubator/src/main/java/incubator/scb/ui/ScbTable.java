package incubator.scb.ui;

import incubator.pval.Ensure;
import incubator.scb.Scb;

import java.util.Comparator;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumnModel;

/**
 * Table that displays SCBs.
 * @param <T> the bean type
 */
@SuppressWarnings("serial")
public class ScbTable<T extends Scb<T>> extends JTable {
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
		
		TableColumnModel tcm = getColumnModel();
		tcm.addColumnModelListener(new TableColumnModelListener() {
			@Override
			public void columnSelectionChanged(ListSelectionEvent arg0) {/**/}
			@Override
			public void columnRemoved(TableColumnModelEvent arg0) {
				set_renderer_and_editor();
			}
			@Override
			public void columnMoved(TableColumnModelEvent arg0) {/**/}
			@Override
			public void columnMarginChanged(ChangeEvent arg0) {/**/}
			@Override
			public void columnAdded(TableColumnModelEvent arg0) {
				set_renderer_and_editor();
			}
		});
		
		set_renderer_and_editor();
	}
	
	/**
	 * Sets the renderer and editor for all columns in the model.
	 */
	private void set_renderer_and_editor() {
		int cc = m_model.getColumnCount();
		
		TableColumnModel tcm = getColumnModel();
		Ensure.equals(cc, tcm.getColumnCount(), "cc != column count");
		
		for (int i = 0; i < tcm.getColumnCount(); i++) {
			tcm.getColumn(i).setCellRenderer(m_model.field(i).cell_renderer());
			tcm.getColumn(i).setCellEditor(m_model.field(i).cell_editor());
		}
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
