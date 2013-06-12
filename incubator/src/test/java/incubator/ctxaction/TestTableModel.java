package incubator.ctxaction;

import javax.swing.table.AbstractTableModel;

import incubator.ctxaction.RowContextTableModel;

/**
 * Table model used for tests.
 */
public class TestTableModel extends AbstractTableModel
		implements RowContextTableModel {
	/**
	 * Version for serialization.
	 */
	public static final long serialVersionUID = 1;
	
	/**
	 * Model data.
	 */
	public Object data[];
	
	/**
	 * Creates a new model.
	 * 
	 * @param data the model data
	 */
	public TestTableModel(Object data[]) {
		this.data = data;
	}
	
	@Override
	public int getColumnCount() {
		return 1;
	}
	
	@Override
	public int getRowCount() {
		return data.length;
	}
	
	@Override
	public Object getValueAt(int arg0, int arg1) {
		return data[arg0];
	}

	@Override
	public Object getRowContextObject(int row) {
		return data[row];
	}
}
