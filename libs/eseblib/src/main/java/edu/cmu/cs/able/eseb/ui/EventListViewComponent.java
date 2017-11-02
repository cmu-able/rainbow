package edu.cmu.cs.able.eseb.ui;

import incubator.pval.Ensure;

import java.awt.BorderLayout;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;

import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Component that shows a list of events.
 */
public class EventListViewComponent extends JPanel {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The table with the data received.
	 */
	private JTable m_table;
	
	/**
	 * Data to show.
	 */
	private List<Store> m_data;
	
	/**
	 * Date formatter.
	 */
	private DateFormat m_format;
	
	/**
	 * Maximum table size.
	 */
	private int m_max_size;

	/**
	 * Creates a new component.
	 */
	public EventListViewComponent() {
		setLayout(new BorderLayout());
		
		m_data = new ArrayList<>();
		m_format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS");
		
		JScrollPane scroll = new JScrollPane();
		add(scroll, BorderLayout.CENTER);
		scroll.setHorizontalScrollBarPolicy(
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		m_table = new JTable(new Model());
		scroll.setViewportView(m_table);
		m_max_size = 100;
	}
	
	/**
	 * Adds a new value to the view component.
	 * @param value the value
	 */
	public void add(DataValue value) {
		add(new Date(), value);
	}
	
	/**
	 * Adds a new value to the view component with a given date.
	 * @param d the date
	 * @param value the value
	 */
	public void add(Date d, DataValue value) {
		Ensure.not_null(value);
		Ensure.not_null(d);
		Store s = new Store();
		s.when = d;
		s.value = value;
		m_data.add(s);
		((Model) m_table.getModel()).fireTableRowsInserted(m_data.size() - 1,
				m_data.size() - 1);
		if (m_data.size() > m_max_size) {
			m_data.remove(0);
			((Model) m_table.getModel()).fireTableRowsDeleted(0, 0);
		}
	}
	
	/**
	 * Line of data stored in the component.
	 */
	private static class Store {
		/**
		 * When was the data added?
		 */
		private Date when;
		
		/**
		 * Data added.
		 */
		private DataValue value;
	}
	
	/**
	 * Data model to use in the table.
	 */
	private class Model extends AbstractTableModel {
		/**
		 * Version for serialization.
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return m_data.size();
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) {
				return "Date";
			} else {
				return "Value";
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Store s = m_data.get(rowIndex);
			if (columnIndex == 0) {
				return m_format.format(s.when);
			} else {
				return s.value.toString();
			}
		}
	}
}
