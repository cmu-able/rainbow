package incubator.ui.bean;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

/**
 * Table that uses a bean model.
 */
public class BeanTable extends JTable {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The model.
	 */
	private BeanTableModel m_model;
	
	/**
	 * Creates a new table.
	 * @param model the model
	 */
	public BeanTable(BeanTableModel model) {
		super(model);
		this.m_model = model;
		
		JTableHeader header = getTableHeader();
		header.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int col = getColumnModel().getColumnIndexAtX(e.getX());
				BeanTable.this.m_model.invert_sort(col);
			}

			@Override
			public void mouseEntered(MouseEvent e) { /* */ }
			@Override
			public void mouseExited(MouseEvent e) { /* */ }
			@Override
			public void mousePressed(MouseEvent e) { /* */ }
			@Override
			public void mouseReleased(MouseEvent e) { /* */ }
		});
		
		BeanTableRenderer renderer = new BeanTableRenderer();
		int ccount = getColumnCount();
		for (int i = 0; i < ccount; i++) {
			TableColumn tc = getColumnModel().getColumn(i);
			tc.setCellRenderer(renderer);
		}
	}
}
