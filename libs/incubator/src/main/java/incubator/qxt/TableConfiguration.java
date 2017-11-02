package incubator.qxt;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.SortOrder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * <p>
 * Class which is capable of reading and writing all persistent table
 * configuration to the user preferences. This class maintains the following
 * information of the table: column width, column order and sort order.
 * </p>
 * <p>
 * Configuration information is maintained in a preference node of a class
 * given. All parameters share a prefix which allows several different table
 * configurations to reside on the same node. If no class is given, a
 * default class name is used to obtain the preferences. Preferences are
 * stored in the user nodes (not system nodes).
 * </p>
 */
class TableConfiguration {
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
	 * Default class used to save the preferences.
	 */
	private static final Class<?> DEFAULT_CLASS = QxtTable.class;

	/**
	 * Class used to obtain the preferences.
	 */
	private Class<?> clazz;

	/**
	 * A prefix code used to distinguish multiple table configurations for
	 * the same class.
	 */
	private String code;

	/**
	 * The table this configuration is associated with.
	 */
	private final QxtTable<?> table;

	/**
	 * Set to <code>true</code> while configuring the table. Used to ignore
	 * save requests (usually due to table changing).
	 */
	private boolean working;

	/**
	 * Creates a new object responsible for handling a table's
	 * configuration.
	 * 
	 * @param table the table
	 */
	TableConfiguration(QxtTable<?> table) {
		assert table != null;

		clazz = DEFAULT_CLASS;
		code = null;
		this.table = table;
		working = false;
	}

	/**
	 * Sets the class used to obtain the preferences node.
	 * 
	 * @param clazz the class or <code>null</code> to use the default one
	 */
	void setPreferencesClass(Class<?> clazz) {
		if (clazz == null) {
			clazz = DEFAULT_CLASS;
		}

		this.clazz = clazz;
	}

	/**
	 * Obtains the preference class.
	 * 
	 * @return the class
	 */
	Class<?> getPreferencesClass() {
		return clazz;
	}

	/**
	 * Sets the preferences code.
	 * 
	 * @param code the preferences code (<code>null</code> disables saving)
	 */
	void setPreferencesCode(String code) {
		this.code = code;
	}

	/**
	 * Obtains the code used as prefix for the configuration.
	 * 
	 * @return the code or <code>null</code> if none
	 */
	String getPreferencesCode() {
		return code;
	}

	/**
	 * Saves the current table configuration (ignored if the prefix code is
	 * not defined).
	 */
	void saveConfiguration() {
		/*
		 * We ignore the request if the prefix code is not defined or if
		 * we're currently configuring the table.
		 */
		if (code == null || working) {
			return;
		}

		try {
			Preferences prefs = Preferences.userNodeForPackage(clazz);
			prefs.sync();

			/*
			 * Remove all existing preferences.
			 */
			for (String k : prefs.keys()) {
				if (k.startsWith(code + SEPARATOR)) {
					prefs.remove(k);
				}
			}

			/*
			 * Put the column count.
			 */
			TableColumnModel tcm = table.getColumnModel();
			int ccount = tcm.getColumnCount();

			prefs.putInt(globalKey(PARAM_COLUMN_COUNT), ccount);

			/*
			 * Put information on the currently sorted column and sorting order
			 * (if any).
			 */
			TableColumn sorted = table.getSortedColumn();
			boolean asc = true;
			if (sorted != null) {
				int midx = sorted.getModelIndex();
				int vidx = table.convertColumnIndexToView(midx);
				asc = table.getSortOrder(vidx) == SortOrder.ASCENDING;
				prefs.putInt(globalKey(PARAM_COLUMN_SORT), vidx);
				prefs.putBoolean(globalKey(PARAM_COLUMN_ASCENDING), asc);
			}

			/*
			 * Write column state information.
			 */
			for (int i = 0; i < ccount; i++) {
				TableColumn tc = tcm.getColumn(i);
				int cwidth = tc.getWidth();
				prefs.putInt(columnKey(i, PARAM_COLUMN_WIDTH), cwidth);
				int vidx = table.convertColumnIndexToView(i);
				prefs.putInt(columnKey(i, PARAM_COLUMN_INDEX), vidx);
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
	void loadConfiguration() {
		/*
		 * We ignore the request if the prefix code is not defined or if
		 * we're currently configuring the table.
		 */
		if (code == null || working) {
			return;
		}

		working = true;
		try {
			Preferences prefs = Preferences.userNodeForPackage(clazz);
			prefs.sync();

			TableColumnModel tcm = table.getColumnModel();
			int realCCount = tcm.getColumnCount();

			int ccount = prefs.getInt(globalKey(PARAM_COLUMN_COUNT), -1);
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
						.getInt(columnKey(i, PARAM_COLUMN_INDEX), -1);
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

					table.moveColumn(order[i], i);
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
				int cwidth = prefs.getInt(columnKey(i, PARAM_COLUMN_WIDTH),
						-1);
				if (cwidth > 0) {
					tc.setPreferredWidth(cwidth);
				}
			}

			/*
			 * Set the column order if necessary.
			 */
			int sortIdx = prefs.getInt(globalKey(PARAM_COLUMN_SORT), -1);
			boolean ascending = prefs.getBoolean(
					globalKey(PARAM_COLUMN_ASCENDING), false);
			if (sortIdx >= 0) {
				SortOrder sorder;
				if (ascending) {
					sorder = SortOrder.ASCENDING;
				} else {
					sorder = SortOrder.DESCENDING;
				}
				table.setSortOrder(sortIdx, sorder);
			}
		} catch (BackingStoreException e) {
			e.printStackTrace();
			return;
		} finally {
			working = false;
		}
	}

	/**
	 * Obtains the name of the key used to access a parameter which does not
	 * depend on a column.
	 * 
	 * @param param the parameter
	 * 
	 * @return the preferences key
	 */
	private String globalKey(String param) {
		assert code != null;
		return code + SEPARATOR + param;
	}

	/**
	 * Obtains the name of the key used to access a parameter which depends
	 * on a column.
	 * 
	 * @param idx the column index
	 * @param param the parameter
	 * 
	 * @return the preferences key
	 */
	private String columnKey(int idx, String param) {
		assert code != null;
		return code + SEPARATOR + idx + SEPARATOR + param;
	}
}
