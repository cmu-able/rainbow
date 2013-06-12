package incubator.qxt;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.table.TableColumnExt;

/*
 * TODO: There is only patial support for tab cycling on edit (it doesn't
 * currently work).
 */

/**
 * <p>
 * This class handles table cell editing in a much more powerful (and
 * specific) way as required by <code>QxtTable</code>. It keeps
 * automatically track of columns and informs when cell editing has started,
 * stopped and canceled. It will also require cell selection for editing to
 * start and will allow listeners to veto the editing start.
 * </p>
 * <p>
 * There are some weired situations in which JTable doesn't call editing
 * canceled but in which cell editing is canceled. A possible situation is
 * when we click the table header while editing a cell. We only detect these
 * situations when e get an editing start event while we think we're still
 * editing. In these situations we simulate an editing canceled event.
 * </p>
 */
class TableEditController {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(TableEditController.class);

	/**
	 * The row currently being edited in model coordinates (<code>-1</code>
	 * if none).
	 */
	private int editingRow;

	/**
	 * The column currently being edited in model coordinates (
	 * <code>-1</code> if none).
	 */
	private int editingCol;

	/**
	 * Registered listeneres.
	 */
	private final List<Listener> listeners;

	/**
	 * The table we're associated with.
	 */
	private final QxtTable<?> table;

	/**
	 * The list of known columns.
	 */
	private Set<TableColumnExt> columns;

	/**
	 * The property change listener we have registered on the table columns.
	 */
	private final PropertyChangeListener columnPcl;

	/**
	 * The cell editor decorator we're currently editing (<code>null</code>
	 * if we're not currently editing).
	 */
	private TableCellEditorDecorator decorator;

	/**
	 * Creates a new edit controller.
	 * 
	 * @param table the table
	 */
	TableEditController(QxtTable<?> table) {
		assert table != null;

		editingRow = -1;
		editingCol = -1;
		this.table = table;
		listeners = new ArrayList<>();
		columns = new HashSet<>();
		decorator = null;

		TableColumnModel tcm = table.getColumnModel();
		tcm.addColumnModelListener(new TableColumnModelListener() {
			@Override
			public void columnAdded(TableColumnModelEvent e) {
				reviewColumns();
			}

			@Override
			public void columnMarginChanged(ChangeEvent e) {
				reviewColumns();
			}

			@Override
			public void columnMoved(TableColumnModelEvent e) {
				reviewColumns();
			}

			@Override
			public void columnRemoved(TableColumnModelEvent e) {
				reviewColumns();
			}

			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {
				reviewColumns();
			}
		});

		columnPcl = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				reviewColumns();
			}
		};

		table.getVetoSelectionSupport().addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						doSelectionChanged();
					}
				});

		reviewColumns();
	}

	/**
	 * Invoked when table selection has changed.
	 */
	private void doSelectionChanged() {
		LOGGER.debug("doSelectionChanged()");
		if (!isEditing()) {
			LOGGER.trace("doSelectionChange: !isEditing()");
			return;
		}

		int crow = table.getCurrentSelectedModelRow();
		LOGGER.trace("doSelectionChanged: crow=" + crow + ",editingRow="
				+ editingRow);
		if (crow == editingRow) {
			return;
		}

		if (isEditing() && table.getEditorComponent() == null) {
			/*
			 * Ok, so the table has stopped editing and didn't tell us about
			 * it. This may happen (although I think this is a bug :)). Anyway,
			 * workaround provided below.
			 */
			cancelEditing();
		}
	}

	/**
	 * Adds a listener.
	 * 
	 * @param l the listener
	 */
	void addListener(Listener l) {
		assert l != null;

		listeners.add(l);
	}

	/**
	 * Removes a listener.
	 * 
	 * @param l the listener
	 */
	void removeListener(Listener l) {
		assert l != null;

		listeners.remove(l);
	}

	/**
	 * Obtains the row currently being edited.
	 * 
	 * @return the row in model coordinates (<code>-1</code> if none)
	 */
	int getEditingRow() {
		return editingRow;
	}

	/**
	 * Obtains the column currently being edited.
	 * 
	 * @return the column in model coordinates (<code>-1</code> if none)
	 */
	int getEditingColumn() {
		return editingCol;
	}

	/**
	 * Determines whether we're currently editing a cell.
	 * 
	 * @return are we editing a cell?
	 */
	boolean isEditing() {
		return editingRow != -1;
	}

	/**
	 * Starts editing a cell. This method is invoked when we're sure we can
	 * start editing.
	 * 
	 * @param tced the decorator involved
	 * @param row the row in model coordinates
	 * @param col the column in model coordinates
	 */
	private void startEditing(TableCellEditorDecorator tced, int row, int col) {
		assert row >= 0;
		assert col >= 0;
		assert editingRow == -1;
		assert editingCol == -1;

		editingRow = row;
		editingCol = col;
		decorator = tced;

		for (Listener l : new ArrayList<>(listeners)) {
			l.editingStarted(editingRow, editingCol);
		}
	}

	/**
	 * Stops editing a cell. This method is invoked when the cell editor has
	 * stopped editing.
	 */
	private void stopEditing() {
		/*
		 * When using combo boxes, for some reason an editing stopped event
		 * is fired when the component is initialized which kinda screws up
		 * the state machine. So we just ignore stop editings if we're not
		 * editing.
		 */
		if (editingRow == -1) {
			return;
		}

		assert editingRow >= 0;
		assert editingCol >= 0;
		assert decorator != null;

		int row = editingRow;
		int col = editingCol;

		editingRow = -1;
		editingCol = -1;
		decorator = null;

		for (Listener l : new ArrayList<>(listeners)) {
			l.editingStopped(row, col);
		}

	}

	/**
	 * Invoked when cell editing has been canceled.
	 */
	void cancelEditing() {
		assert editingRow >= 0;
		assert editingCol >= 0;

		int row = editingRow;
		int col = editingCol;

		editingRow = -1;
		editingCol = -1;

		for (Listener l : new ArrayList<>(listeners)) {
			l.editingCanceled(row, col);
		}

		decorator = null;
	}

	/**
	 * Invoked when there is a change in the table structure or in a table
	 * column. This method reviews the column editors and installs the
	 * decorators if required. It also keeps the property change listener
	 * installed on the correct column set.
	 */
	private void reviewColumns() {
		int colcnt = table.getColumnCount();
		Set<TableColumnExt> current = new HashSet<>();
		for (int i = 0; i < colcnt; i++) {
			TableColumnExt tce = table.getColumnExt(i);

			AbstractQxtProperty<?> p = table.getProperty(i);
			if (p == null) {
				continue;
			}

			current.add(tce);
			if (columns.contains(tce)) {
				tce.removePropertyChangeListener(columnPcl);
			}

			TableCellEditor editor = tce.getCellEditor();

			if (editor == null) {
				editor = new DefaultCellEditor(new JTextField());
			}

			if (!(editor instanceof TableCellEditorDecorator)) {
				tce.setCellEditor(new TableCellEditorDecorator(editor, p));
			}

			tce.addPropertyChangeListener(columnPcl);
		}

		Set<TableColumnExt> unused = columns;
		columns = current;
		unused.removeAll(columns);
		for (TableColumnExt tce : unused) {
			TableCellEditor ed = tce.getCellEditor();
			if (ed instanceof TableCellEditorDecorator) {
				((TableCellEditorDecorator) ed).dispose();
			}

			tce.removePropertyChangeListener(columnPcl);
		}
	}

	/**
	 * Tries to start editing a cell (fires notifications for listeners
	 * which may abort).
	 * 
	 * @param row the row to edit (in model coordinates)
	 * @param col the column to edit (in model coordinate)
	 * 
	 * @return is editing started allowed?
	 */
	private boolean fireTryEditingStarted(int row, int col) {
		LOGGER.debug("fireTryEditingStarted(row=" + row + ",col=" + col
				+ ")");

		/*
		 * Firstly we must be sure we have the selection.
		 */
		int crow = table.getCurrentSelectedModelRow();
		LOGGER.trace("fireTryEditingStarted: crow=" + crow);
		if (crow != row) {
			LOGGER.trace("Row being edited is not the selected row, try to "
					+ "change the selected row.");
			boolean changed = table.setSelection(row);
			LOGGER.trace("fireTryEditingStarted: changed=" + changed);
			if (!changed) {
				/*
				 * We failed to set the selection, so bail out.
				 */
				LOGGER
						.trace("Changing the selected row has failed. Bail out "
								+ "cancelling editing start.");
				return false;
			}
		}

		crow = table.getCurrentSelectedModelRow();
		assert crow == row;
		LOGGER.trace("fireTryEditingStarted: selection changed "
				+ "successfully to line " + crow + ". Informing "
				+ "listeners to see if we can start editing.");
		int idx = 0;
		List<Listener> lcp = new ArrayList<>(listeners);
		for (; idx < lcp.size(); idx++) {
			if (!lcp.get(idx).tryEditingStarted(row, col)) {
				LOGGER.trace("fireTryEditingStarted: vetoed");
				for (idx--; idx >= 0; idx--) {
					lcp.get(idx).editingStartedFailed(row, col);
				}

				return false;
			}
		}

		LOGGER.trace("fireTryEditingStarted: editing started was accepted "
				+ "by all listeners. Proceeding.");
		return true;
	}

	/**
	 * Listener implemented by classes that want to be informed of changes
	 * in table editing.
	 */
	interface Listener {
		/**
		 * Invoked when the user tries to start editing a cell.
		 * 
		 * @param row the editing row (in model coordinates)
		 * @param col the editing column (in model coordinate)
		 * 
		 * @return can editing start?
		 */
		boolean tryEditingStarted(int row, int col);

		/**
		 * Invoked after <code>tryEditignStarted</code> if a listener has
		 * canceled editing to inform that editing will not start.
		 * 
		 * @param row the editing row (in model coordinates)
		 * @param col the editing column (in model coordinate)
		 */
		void editingStartedFailed(int row, int col);

		/**
		 * Invoked to inform that editing on the given cell has started.
		 * 
		 * @param row the editing row (in model coordinates)
		 * @param col the editing column (in model coordinate)
		 */
		void editingStarted(int row, int col);

		/**
		 * Invoked to inform that editing on the given cell has stopped.
		 * 
		 * @param row the editing row (in model coordinates)
		 * @param col the editing column (in model coordinate)
		 */
		void editingStopped(int row, int col);

		/**
		 * Invoked to inform that editing on the given cell has been
		 * canceled.
		 * 
		 * @param row the editing row (in model coordinates)
		 * @param col the editing column (in model coordinate)
		 */
		void editingCanceled(int row, int col);
	}

	/**
	 * Decorator placed on top of a table cell editor to detect cell editing
	 * and to allow canceling cell editing. The decorator also changes the
	 * default behavior and allows editing to start with a single click.
	 */
	private final class TableCellEditorDecorator implements TableCellEditor {
		/**
		 * The real cell editor.
		 */
		private final TableCellEditor delegate;

		/**
		 * The component currently being edited.
		 */
		private Component editComponent;

		/**
		 * Is the key listener installed on the component?
		 */
		private boolean installed;

		/**
		 * The property corresponding to this column.
		 */
		private final AbstractQxtProperty<?> property;

		/**
		 * The cell editor listener to register on the component.
		 */
		private final CellEditorListener cel;

		/**
		 * Creates a new decorator.
		 * 
		 * @param delegate the cell editor we're delegating to (the one
		 * doing the real work)
		 * @param property the property corresponding to this column
		 */
		private TableCellEditorDecorator(TableCellEditor delegate,
				AbstractQxtProperty<?> property) {
			assert delegate != null;
			assert property != null;

			this.delegate = delegate;
			this.property = property;

			editComponent = null;
			installed = false;
			cel = new CellEditorListener() {
				@Override
				public void editingCanceled(ChangeEvent e) {
					doEditingCanceled();
				}

				@Override
				public void editingStopped(ChangeEvent e) {
					doEditingStopped();
				}
			};

			delegate.addCellEditorListener(cel);
		}

		/**
		 * Disposes this cell editor decorator.
		 */
		private void dispose() {
			delegate.removeCellEditorListener(cel);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int col) {
			LOGGER.debug("getTableCellEditorComponent(row=" + row + ",col="
					+ col + ")");

			int mrow = table.convertRowIndexToModel(row);
			int mcol = table.convertColumnIndexToModel(col);
			LOGGER.trace("getTableCellEditorComponent: mrow=" + mrow
					+ ",mcol=" + mcol);

			if (isEditing()) {
				simulateCancelEvent();
			}

			if (!fireTryEditingStarted(mrow, mcol)) {
				LOGGER.trace("getTableCellEditorComponent: "
						+ "!fireTryEditingStarted");
				/*
				 * It is not clear in the documentation whether we can return
				 * null if we don't want cell editing to be done. It seems
				 * JTable accepts it so we just do it.
				 */
				return null;
			}

			assert !installed;
			Component c = delegate.getTableCellEditorComponent(table, value,
					isSelected, row, col);
			editComponent = c;
			if (property.supportsTab()) {
				// c.addKeyListener(tabKeyListener);
				installed = true;
			}

			startEditing(this, mrow, mcol);
			return c;
		}

		/**
		 * Invoked when we must cancel cell editing.
		 */
		private void doEditingCanceled() {
			assert editComponent != null;
			if (installed) {
				// editComponent.removeKeyListener(tabKeyListener);
				installed = false;
			}

			editComponent = null;
			cancelEditing();
		}

		/**
		 * Invoked when editing has been stopped.
		 */
		private void doEditingStopped() {
			if (installed) {
				// editComponent.removeKeyListener(tabKeyListener);
				installed = false;
			}

			editComponent = null;
			stopEditing();
		}

		@Override
		public void addCellEditorListener(CellEditorListener listener) {
			delegate.addCellEditorListener(listener);
		}

		@Override
		public void cancelCellEditing() {
			delegate.cancelCellEditing();
		}

		@Override
		public Object getCellEditorValue() {
			return delegate.getCellEditorValue();
		}

		@Override
		public boolean isCellEditable(EventObject anEvent) {
			if (anEvent instanceof MouseEvent) {
				MouseEvent me = (MouseEvent) anEvent;
				if (me.getID() == MouseEvent.MOUSE_PRESSED) {
					return true;
				}
			}

			return delegate.isCellEditable(anEvent);
		}

		@Override
		public void removeCellEditorListener(CellEditorListener listener) {
			delegate.removeCellEditorListener(listener);
		}

		@Override
		public boolean shouldSelectCell(EventObject anEvent) {
			return delegate.shouldSelectCell(anEvent);
		}

		@Override
		public boolean stopCellEditing() {
			assert editComponent != null;

			boolean hasStopped = delegate.stopCellEditing();
			return hasStopped;
		}
	}

	/**
	 * Invoked as a workaround for the cancelling not being reported bug
	 * (see class comment for details).
	 */
	private void simulateCancelEvent() {
		LOGGER.debug("simulateCancelEvent()");

		assert isEditing();
		assert decorator != null;

		decorator.doEditingCanceled();
	}
}
