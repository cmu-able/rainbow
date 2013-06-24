package incubator.ui.bean;

import java.awt.Component;

import javax.swing.JTable;

/**
 * Interface implemented by classes that provide the component to show in the
 * table.
 */
public interface BeanTableComponentProvider {
	/**
	 * Obtains the component to show.
	 * @param table the table
	 * @param info information for rendering
	 * @param is_selected is the cell selected?
	 * @param has_focus does the cell have focus?
	 * @param row row number
	 * @param column column number
	 * @return a component configured for display
	 */
	public Component getComponentForBean(JTable table, BeanRendererInfo info,
			boolean is_selected, boolean has_focus, int row, int column);
}
