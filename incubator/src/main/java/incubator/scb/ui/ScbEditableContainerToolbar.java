package incubator.scb.ui;

import incubator.pval.Ensure;
import incubator.scb.Scb;
import incubator.scb.ScbFactoryContainer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Tool bar that provides add and remove operations on top of a
 * {@link ScbFactoryContainer}.
 * @param <T> the SCB type
 */
@SuppressWarnings("serial")
public class ScbEditableContainerToolbar<T extends Scb<T>> extends JToolBar {
	/**
	 * The container.
	 */
	private ScbFactoryContainer<T> m_container;
	
	/**
	 * The table.
	 */
	private ScbTable<T> m_table;
	
	/**
	 * The add button.
	 */
	private JButton m_add;
	
	/**
	 * The remove button.
	 */
	private JButton m_remove;
	
	/**
	 * Creates a new editable toolbar.
	 * @param container the container, <code>null</code> if none
	 * @param table the table
	 */
	public ScbEditableContainerToolbar(ScbFactoryContainer<T> container,
			ScbTable<T> table) {
		Ensure.not_null(table, "table == null");
		setFloatable(false);
		
		m_container = container;
		m_table = table;
		
		m_add = new JButton("Add");
		add(m_add);
		m_add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (m_container != null) {
					m_container.new_scb();
				}
			}
		});
		
		m_remove = new JButton("Remove");
		add(m_remove);
		m_remove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				T t = m_table.selected();
				if (t != null && m_container != null) {
					m_container.remove_scb(t);
				}
			}
		});
		
		m_table.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				m_remove.setEnabled(m_table.selected() != null);
			}
		});
		
		set_up_enables();
	}
	
	/**
	 * Changes the container associated with the tool bar.
	 * @param container the container which can be <code>null</code>
	 */
	public void switch_container(ScbFactoryContainer<T> container) {
		m_container = container;
		set_up_enables();
	}
	
	/**
	 * Sets up the button enabled / disabled state.
	 */
	private void set_up_enables() {
		m_add.setEnabled(m_container != null);
		m_remove.setEnabled(m_container != null && m_table.selected() != null);
	}
}
