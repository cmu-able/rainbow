package incubator.ui;

import incubator.pval.Ensure;

import java.awt.EventQueue;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.jdesktop.swingx.JXTable;

/**
 * <p>
 * Class which is capable of reading and writing all persistent table
 * configuration to the user preferences. This class maintains the following
 * information of the table: column width, column order and sort order (if
 * the table is a <code>JXTable</code>).
 * </p>
 * <p>
 * Configuration information is maintained in a preference node of a class
 * given. All parameters share a prefix which allows several different table
 * configurations to reside on the same node. Preferences are
 * stored in the user nodes (not system nodes).
 * </p>
 */
public class TablePreferencesConfiguration {
	/**
	 * Separates the prefix from the parameter names to define the key.
	 */
	private static final String SEPARATOR = ":";

	/**
	 * Parameter name with the table column count.
	 */
	private static final String PARAM_COLUMN_COUNT = "column-count";

	/**
	 * Parameter name with the column width.
	 */
	private static final String PARAM_COLUMN_WIDTH = "column-width";

	/**
	 * Parameter name with the column position. (In which index is the
	 * column placed?)
	 */
	private static final String PARAM_COLUMN_INDEX = "column-index";

	/**
	 * Parameter with the index of the column current used for sorting (if
	 * any).
	 */
	private static final String PARAM_COLUMN_SORT = "column-sort";

	/**
	 * Parameter indicating if sorting is ascending.
	 */
	private static final String PARAM_COLUMN_ASCENDING = "column-sort-asc";

	/**
	 * Class used to obtain the preferences.
	 */
	private Class<?> m_clazz;

	/**
	 * A prefix code used to distinguish multiple table configurations for
	 * the same class.
	 */
	private String m_code;

	/**
	 * The table this configuration is associated with.
	 */
	private final JTable m_table;

	/**
	 * Set to <code>true</code> while configuring the table. Used to ignore
	 * multiple save requests (usually due to table changing).
	 */
	private boolean m_working;

	/**
	 * Creates a new object responsible for handling a table's
	 * configuration. The initial configuration for the table is read from
	 * the preferences. Listeners to the table are added and will keep the
	 * preferences synchronized with the table.
	 * @param table the table
	 * @param clazz class used to obtain preferences
	 * @param code a code to identify the table
	 */
	public TablePreferencesConfiguration(JTable table, Class<?> clazz,
			String code) {
		Ensure.not_null(table, "table == null");
		Ensure.not_null(clazz, "clazz == null");
		Ensure.not_null(code, "code == null");

		m_clazz = clazz;
		m_code = code;
		m_table = table;
		m_working = false;
		
		table.getColumnModel().addColumnModelListener(
				new TableColumnModelListener() {
			@Override
			public void columnSelectionChanged(ListSelectionEvent arg0) {
				/*
				 * We don't care about selection.
				 */
			}
			
			@Override
			public void columnRemoved(TableColumnModelEvent arg0) {
				ask_save();
			}
			
			@Override
			public void columnMoved(TableColumnModelEvent arg0) {
				ask_save();
			}
			
			@Override
			public void columnMarginChanged(ChangeEvent arg0) {
				ask_save();
			}
			
			@Override
			public void columnAdded(TableColumnModelEvent arg0) {
				ask_save();
			}
		});
		
		load_configuration();
	}

	/**
	 * Makes a request to save the table configuration unless another request
	 * is already pending.
	 */
	private void ask_save() {
		if (m_working) {
			return;
		}
		
		m_working = true;
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				save_configuration();
				m_working = false;
			}
		});
	}

	/**
	 * Saves the current table configuration.
	 */
	void save_configuration() {
		try {
			Preferences prefs = Preferences.userNodeForPackage(m_clazz);
			prefs.sync();

			/*
			 * Remove all existing preferences.
			 */
			for (String k : prefs.keys()) {
				if (k.startsWith(m_code + SEPARATOR)) {
					prefs.remove(k);
				}
			}

			/*
			 * Put the column count.
			 */
			TableColumnModel tcm = m_table.getColumnModel();
			int ccount = tcm.getColumnCount();

			prefs.putInt(global_key(PARAM_COLUMN_COUNT), ccount);

			/*
			 * Put information on the currently sorted column and sorting order
			 * (if any).
			 */
			if (m_table instanceof JXTable) {
				JXTable xtable = (JXTable) m_table;
				TableColumn sorted = xtable.getSortedColumn();
				boolean asc = true;
				if (sorted != null) {
					int midx = sorted.getModelIndex();
					int vidx = xtable.convertColumnIndexToView(midx);
					asc = xtable.getSortOrder(vidx) == SortOrder.ASCENDING;
					prefs.putInt(global_key(PARAM_COLUMN_SORT), vidx);
					prefs.putBoolean(global_key(PARAM_COLUMN_ASCENDING), asc);
				}
			}

			/*
			 * Write column state information.
			 */
			for (int i = 0; i < ccount; i++) {
				TableColumn tc = tcm.getColumn(i);
				int cwidth = tc.getWidth();
				prefs.putInt(column_key(i, PARAM_COLUMN_WIDTH), cwidth);
				int vidx = m_table.convertColumnIndexToView(i);
				prefs.putInt(column_key(i, PARAM_COLUMN_INDEX), vidx);
			}

			prefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
			return;
		}
	}

	/**
	 * Configures the table with information from the user preferences.
	 */
	private void load_configuration() {
		Ensure.is_false(m_working, "m_working == true");
		m_working = true;
		try {
			Preferences prefs = Preferences.userNodeForPackage(m_clazz);
			prefs.sync();

			TableColumnModel tcm = m_table.getColumnModel();
			int realCCount = tcm.getColumnCount();

			int ccount = prefs.getInt(global_key(PARAM_COLUMN_COUNT), -1);
			if (ccount != realCCount) {
				/*
				 * Column count doesn't match so the preferences are not
				 * applicable.
				 */
				return;
			}

			/*
			 * Order contains the new column order.
			 */
			int[] order = new int[ccount];
			for (int i = 0; i < order.length; i++) {
				order[i] = -1;
			}

			for (int i = 0; i < ccount; i++) {
				int vidx = prefs
						.getInt(column_key(i, PARAM_COLUMN_INDEX), -1);
				if (vidx >= 0 && vidx < order.length) {
					order[vidx] = i;
				}
			}

			/*
			 * Reorder the columns if everything is ok. Reordering can be quite
			 * tricky because the indexes change while we move. For instance,
			 * imagine we want the column order to be:
			 * 
			 * [D A C B].
			 * 
			 * The value or the "order" array is [3 0 2 1].
			 * 
			 * Since the starting column order is [A B C D] the first thing
			 * to do is move column at index "3" (D) to index "0" which leads
			 * to:
			 * 
			 * [D A B C]
			 * 
			 * Now note that while column A initially was at index "0" and had
			 * to be moved to index "1", it now requires no move because it has
			 * already shifted. Column C, however, still has to move from index
			 * "3" to index "2". Note, however, that column's "C" initial
			 * index was "2" but now, due to the shift, it is at index 3.
			 * 
			 * So what we do is increase all indexes of all columns which have
			 * shifted, so, after the first move [D A B C] we'll update the
			 * "order" array to [3 1 3 2].
			 * 
			 * This way we can see that column "B" needs no move (index "1" to
			 * index "1") and column D (index "3" has to move to index "2").
			 */
			boolean isOk = true;
			for (int i = 0; i < order.length; i++) {
				if (order[i] == -1) {
					isOk = false;
					break;
				}
			}

			if (isOk) {
				for (int i = 0; i < order.length; i++) {
					if (order[i] == i) {
						continue;
					}

					m_table.moveColumn(order[i], i);
					for (int j = i + 1; j < order.length; j++) {
						if (order[j] < order[i]) {
							order[j]++;
						}
					}
				}
			}

			/*
			 * Resize the columns.
			 */
			for (int i = 0; i < ccount; i++) {
				TableColumn tc = tcm.getColumn(i);
				int cwidth = prefs.getInt(column_key(i, PARAM_COLUMN_WIDTH),
						-1);
				if (cwidth > 0) {
					tc.setPreferredWidth(cwidth);
				}
			}

			/*
			 * Set the column order if necessary.
			 */
			if (m_table instanceof JXTable) {
				int sortIdx = prefs.getInt(global_key(PARAM_COLUMN_SORT), -1);
				boolean ascending = prefs.getBoolean(
						global_key(PARAM_COLUMN_ASCENDING), false);
				if (sortIdx >= 0) {
					SortOrder sorder;
					if (ascending) {
						sorder = SortOrder.ASCENDING;
					} else {
						sorder = SortOrder.DESCENDING;
					}
					((JXTable) m_table).setSortOrder(sortIdx, sorder);
				}
			}
		} catch (BackingStoreException e) {
			e.printStackTrace();
			return;
		} finally {
			m_working = false;
		}
	}

	/**
	 * Obtains the name of the key used to access a parameter which does not
	 * depend on a column.
	 * @param param the parameter
	 * @return the preferences key
	 */
	private String global_key(String param) {
		Ensure.not_null(param, "param == null");
		return m_code + SEPARATOR + param;
	}

	/**
	 * Obtains the name of the key used to access a parameter which depends
	 * on a column.
	 * @param idx the column index
	 * @param param the parameter
	 * @return the preferences key
	 */
	private String column_key(int idx, String param) {
		Ensure.greater_equal(idx, 0, "idx < 0");
		Ensure.not_null(param, "param == null");
		return m_code + SEPARATOR + idx + SEPARATOR + param;
	}
}
