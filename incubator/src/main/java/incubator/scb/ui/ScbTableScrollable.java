package incubator.scb.ui;

import incubator.pval.Ensure;

import java.awt.BorderLayout;
import java.util.Comparator;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 * Component wrapping an SCP table in a scrollable pane.
 * @param <T> the bean type
 */
@SuppressWarnings("serial")
public class ScbTableScrollable<T> extends JPanel {
	/**
	 * The table.
	 */
	private ScbTable<T> m_table;
	
	/**
	 * Creates a new scrollable component. This will also create the table
	 * itself.
	 * @param model the model
	 */
	public ScbTableScrollable(ScbTableModel<T, ? extends Comparator<T>> model) {
		Ensure.notNull(model);
		
		m_table = new ScbTable<>(model);
		
		setLayout(new BorderLayout());
		
		JScrollPane sp = new JScrollPane();
		add(sp, BorderLayout.CENTER);
		
		sp.setVerticalScrollBarPolicy(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		sp.setHorizontalScrollBarPolicy(
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		sp.setViewportView(m_table);
	}
	
	/**
	 * Obtains the table component.
	 * @return the table
	 */
	public ScbTable<T> table() {
		return m_table;
	}
}
