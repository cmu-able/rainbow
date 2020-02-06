package incubator.ui.bean;

import java.awt.Component;

import javax.swing.JTable;

/**
 * Class that provides utility methods to format components.
 */
public class ProviderUtils {
	/**
	 * Defines the background and foreground colors of the component having
	 * in consideration the selection state.
	 * @param table the table
	 * @param component the component
	 * @param is_selected is it selected
	 */
	public static void set_text_selection_colors(JTable table,
			Component component, boolean is_selected) {
		if (is_selected) {
			component.setBackground(table.getSelectionBackground());
			component.setForeground(table.getSelectionForeground());
		} else {
			component.setBackground(table.getBackground());
			component.setForeground(table.getForeground());
		}
	}
}
