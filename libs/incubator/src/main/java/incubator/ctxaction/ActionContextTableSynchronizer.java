package incubator.ctxaction;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

/**
 * Class which is able to keep an action context synchronized with selected
 * rows on a table. The class will keep a value on the context defined if
 * any row is selected. The key is defined in this class and the value is
 * the object provided by the model. If several lines are selected, an array
 * is defined in the context (instead of a single value).
 */
public class ActionContextTableSynchronizer {
	/**
	 * The table model.
	 */
	private final RowContextTableModel model;

	/**
	 * The table.
	 */
	private final JTable table;

	/**
	 * The context to synchronize.
	 */
	private final ActionContext context;

	/**
	 * Context key to keep updated.
	 */
	private final String key;

	/**
	 * Creates a new synchronizer.
	 * 
	 * @param table the table. The table model must implement the
	 * {@link RowContextTableModel} interface
	 * @param context the action context to keep synchronized
	 * @param key the context key to use
	 */
	public ActionContextTableSynchronizer(JTable table,
			ActionContext context, String key) {
		if (table == null) {
			throw new IllegalArgumentException("table == null");
		}

		if (context == null) {
			throw new IllegalArgumentException("context == null");
		}

		if (key == null) {
			throw new IllegalArgumentException("key == null");
		}

		this.table = table;
		this.context = context;
		this.key = key;

		TableModel model = table.getModel();
		if (model == null || !(model instanceof RowContextTableModel)) {
			throw new IllegalArgumentException(
					"Table model must implement the "
							+ "RowContextTableModel interface.");
		}

		this.model = (RowContextTableModel) model;

		ListSelectionListener listener = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				synchronize();
			}
		};

		this.table.getSelectionModel().addListSelectionListener(listener);
		synchronize();
	}

	/**
	 * Synchronizes the context with the table selection.
	 */
	private void synchronize() {
		int[] rows = table.getSelectedRows();
		if (rows.length == 0) {
			context.clear(key);
			return;
		}

		Object[] data = new Object[rows.length];
		for (int i = 0; i < data.length; i++) {
			int mrow = table.convertRowIndexToModel(rows[i]);
			data[i] = model.getRowContextObject(mrow);
		}

		if (rows.length == 1) {
			context.set(key, data[0]);
			return;
		}

		context.set(key, data);
	}

	/**
	 * This method does nothing but prevents checkstyle from complaining.
	 */
	public void dummy() {
		/*
		 * No code here. Dummy method.
		 */
	}
}
