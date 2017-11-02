package incubator.scb.ui;

import incubator.pval.Ensure;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Default renderer used in SCB tables. This renderer performs just like
 * its sueprclass with the exception if the value is an icon in which case it
 * displays the icon.
 */
@SuppressWarnings("serial")
public class ScbTableDefaultRenderer extends DefaultTableCellRenderer {
	/**
	 * Creates a new renderer.
	 */
	public ScbTableDefaultRenderer() {
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean selected, boolean focused, int row, int col) {
		Ensure.not_null(table);
		
		setText("");
		setIcon(null);
		
		if (value instanceof Component) {
			return (Component) value;
		}
		
		Component r = null;
		if (value == null || !(value instanceof ImageIcon)) {
			r = super.getTableCellRendererComponent(table, value,
					selected, focused, row, col);
			Ensure.not_null(r, "r == null");
		} else {
			r = super.getTableCellRendererComponent(table, "", selected,
					focused, row, col);
			Ensure.same(r, this, "super.getTableCellRendererComponent did "
					+ "not return this");
			setIcon((ImageIcon) value);
		}
		
		return r;
	}
}
