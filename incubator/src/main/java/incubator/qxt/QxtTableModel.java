package incubator.qxt;

import incubator.ctxaction.RowContextTableModel;
import incubator.obscol.ObservableList;
import incubator.obscol.ObservableListListener;
import incubator.obscol.WrapperObservableList;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * <p>
 * The <code>QxtTableModel</code> class keeps track of the data model behind a
 * <code>qxt</code>. In its essence, the model contains an observable list of
 * beans and registers itself on the list propagating changes on the list to the
 * table.
 * </p>
 * <p>
 * The <code>QxtTableModel</code> class also registers itself as property
 * listener of every bean displayed (if it allows property listeners) to detect
 * property changes and force a table update.
 * </p>
 * <p>
 * <code>JTable</code> does not like rows to be inserted or removed while cell
 * editing is taking place. Consequently, the model will register itself as
 * listener of the <code>ChangeController</code> and will suspend firing events
 * while a cell is being edited. Note that events will be queued and fired
 * later. In order to do this, the model uses a second list which is kept in
 * sync with the original one using the <code>SuspendableListSynchronizer</code>
 * class. When editing is started the synchronizer is suspended as changes on
 * the copy are not performed.
 * </p>
 * <p>
 * The model also handles the *new* empty line automatically. When this
 * functionality is activated (through the
 * <code>setAutomaticLineCreationEnabled</code> method), two new "features" are
 * enabled: the new line and the editing line. The new line always contains a
 * new, empty bean obtained from a {@link LineFactory}. When the new line is
 * changed, it is moved to an editing line and a new line is created. When the
 * editing line is committed, it is added to the observable list. Note that both
 * the new line and the editing line are <em>not</em> in the observable list.
 * They are handled separately by the model. The model uses a listener on the
 * <code>ChangeController</code> to keep track of editing, commits and roll
 * backs.
 * </p>
 * <p>
 * The model can also simulate a pseudo-column, the status column. This column
 * (enabled through the <code>setShowStatusColumn</code> method) will show an
 * icon with the state of the column. The column state is represented in the
 * {@link StatusIconChooser.StatusType} enum and is computed automatically by
 * the model (the icon chooser will be probed for an icon to use in each
 * status).
 * </p>
 * <p>
 * A validator can be added to the model. If defined (using the
 * <code>setValidator</code> method}, the validator will be used to check
 * whether a line is or not valid. This is used both when showing the line as
 * well as when the table probes the model using the <code>isValid</code>
 * method.
 * </p>
 * <p>
 * The model is also responsible for making copies of the beans when editing
 * starts and putting them back if editing is canceled. Bean copies are made
 * using a {@link BeanCopier} object provided to the <code>setBeanCopier</code>
 * method.
 * </p>
 * 
 * @param <T> the bean type
 */
class QxtTableModel<T> extends AbstractTableModel implements
		RowContextTableModel {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Logger used.
	 */
	private static final Logger LOGGER = Logger.getLogger(QxtTableModel.class);

	/**
	 * The list of properties of the bean to use (each property is used for a
	 * column).
	 */
	private final List<AbstractQxtProperty<?>> properties;

	/**
	 * Data used to back the model. This data may not be up-to-date with the
	 * real data model if changes have occurred and the table is editing a cell.
	 */
	private final ObservableList<T> data;

	/**
	 * Real data model provided to the model.
	 */
	private final ObservableList<T> realData;

	/**
	 * Synchronizer used to keep the data model in sync with the real data
	 * model.
	 */
	private final SuspendableListSynchronizer<T> synchronizer;

	/**
	 * The property change listeners used to detect changes in the beans. One of
	 * these is created by the model and others may be added by external
	 * entities (typically properties).
	 */
	private final Set<PropertyChangeListener> pcls;

	/**
	 * The bean class.
	 */
	private final Class<T> beanClass;

	/**
	 * The bean class method used to add a property listener ( <code>null</code>
	 * if none).
	 */
	private transient Method addListenerMethod;

	/**
	 * The bean class method used to remove a property listener (
	 * <code>null</code> if none).
	 */
	private transient Method removeListenerMethod;

	/**
	 * The set of all beans in which the model has registered as listener.
	 */
	private final Set<T> registered;

	/**
	 * Extra line being edited (this is not a new line -- it was a new line that
	 * the user has began to edit but still hasn't committed). This may be
	 * <code>null</code> if none.
	 */
	private T extraEditing;

	/**
	 * The new, empty line, if any.
	 */
	private T extraLine;

	/**
	 * The defined status icon chooser (it is never <code>null</code> if the
	 * status column is being shown).
	 */
	private StatusIconChooser<T> iconChooser;

	/**
	 * Should the status icon columnn be displayed?
	 */
	private boolean showStatusColumn;

	/**
	 * The title for the status column.
	 */
	private String statusColumnTitle;

	/**
	 * A line validator (if any).
	 */
	private Validator<T> validator;

	/**
	 * The table to which the model is associated.
	 */
	private QxtTable<T> table;

	/**
	 * Is automatic line creation enabled?
	 */
	private boolean automaticLineCreationEnabled;

	/**
	 * The line factory to use (<code>null</code> if none).
	 */
	private LineFactory<T> lineFactory;

	/**
	 * Is auto copy mode enabled?
	 */
	private boolean autoBeanCopyMode;

	/**
	 * The bean copier to use (never <code>null</code>, even if bean copy is not
	 * activated).
	 */
	private BeanCopier<T> beanCopier;

	/**
	 * The local copy of the bean being edited (a copy of the bean before
	 * editing was done).
	 */
	private T localCopy;

	/**
	 * Should table change events be suspended? (While this flag is
	 * <code>true</code> all model change events are ignored).
	 */
	private boolean suspendFires;

	/**
	 * Listeners that should be informed of changes in the model lines.
	 */
	private final List<LineEditorListener<T>> lineEditorListeners;

	/**
	 * Creates a table model.
	 * 
	 * @param data the observable list with the table data
	 * @param beanClass the bean class
	 * @param properties the properties to use as table columns
	 */
	QxtTableModel(ObservableList<T> data, Class<T> beanClass,
			AbstractQxtProperty<?>... properties) {
		if (data == null) {
			throw new IllegalArgumentException("data == null");
		}

		if (beanClass == null) {
			throw new IllegalArgumentException("beanClass == null");
		}

		if (properties == null) {
			throw new IllegalArgumentException("properties == null");
		}

		if (properties.length == 0) {
			throw new IllegalArgumentException("properties.length == 0");
		}

		this.data = new WrapperObservableList<>(new ArrayList<T>());
		this.realData = data;
		this.synchronizer = new SuspendableListSynchronizer<>(realData,
				this.data);
		this.properties = new ArrayList<>(
				Arrays.asList(properties));
		this.beanClass = beanClass;
		this.registered = new HashSet<>();
		this.extraEditing = null;
		this.extraLine = null;
		this.iconChooser = null;
		this.showStatusColumn = false;
		this.statusColumnTitle = null;
		this.automaticLineCreationEnabled = false;
		this.lineFactory = null;
		this.beanCopier = new DefaultBeanCopier<>(beanClass);
		this.autoBeanCopyMode = false;
		this.localCopy = null;
		this.suspendFires = false;
		this.lineEditorListeners = new ArrayList<>();

		try {
			Method add = beanClass.getMethod("addPropertyChangeListener",
					PropertyChangeListener.class);
			Method rem = beanClass.getMethod("removePropertyChangeListener",
					PropertyChangeListener.class);

			if (add != null & rem != null) {
				addListenerMethod = add;
				removeListenerMethod = rem;
			}
		} catch (SecurityException e) {
			// Ok, we'll just won't use them.
			assert true;
		} catch (NoSuchMethodException e) {
			// Ok, we'll just won't use them.
			assert true;
		}

		PropertyChangeListener pcl = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt == null) {
					throw new IllegalArgumentException("evt == null");
				}

				Object src = evt.getSource();
				if (src == null) {
					throw new IllegalArgumentException(
							"evt.getSource() == null");
				}

				if (!QxtTableModel.this.beanClass.isInstance(src)) {
					throw new IllegalArgumentException("Object {" + src + "} "
							+ " is not an instance of "
							+ QxtTableModel.this.beanClass + ".");
				}

				@SuppressWarnings("unchecked")
				T t = (T) src;

				if (!QxtTableModel.this.data.contains(t)) {
					assert removeListenerMethod != null;
					/*
					 * This object was somehow removed from the model and we
					 * weren't informed. This might indicate some sort of bug
					 * but we can't handle it here so just unregister from the
					 * object.
					 */
					unregisterListener(t);
				}

				int idx = QxtTableModel.this.data.indexOf(src);
				assert idx >= 0;
				fireTableRowsUpdated(idx, idx);
			}
		};
		pcls = new HashSet<>();
		pcls.add(pcl);

		this.data.addObservableListListener(new ObservableListListener<T>() {
			@Override
			public void elementAdded(T e, int idx) {
				fireTableRowsInserted(idx, idx);
				registerListener(e);
			}

			@Override
			public void elementChanged(T oldE, T newE, int idx) {
				fireTableRowsUpdated(idx, idx);
				unregisterListener(oldE);
				registerListener(newE);
			}

			@Override
			public void elementRemoved(T e, int idx) {
				fireTableRowsDeleted(idx, idx);
				unregisterListener(e);
			}

			@Override
			public void listCleared() {
				fireTableDataChanged();
				unregisterAllListeners();
			}
		});

		for (AbstractQxtProperty<?> p : properties) {
			p.init(beanClass, this);
		}

		for (T t : data) {
			registerListener(t);
		}
	}

	/**
	 * Invoked by the table as soon as the table is created. Since the model is
	 * created before the table but it requires the table, this method should be
	 * called ASAP.
	 * 
	 * @param table the table
	 */
	void setTable(QxtTable<T> table) {
		assert table != null;
		assert this.table == null;

		this.table = table;

		table.getChangeController().addListener(
				new ChangeController.Listener<T>() {
					@Override
					public void changeCommitted(T t) {
						doEditingStopped(t, false);
					}

					@Override
					public void changeRolledBack(T t) {
						doEditingStopped(t, true);
					}

					@Override
					public void changeStarting(T t) {
						doEditingStarted(t);
					}

					@Override
					public void changeNotCommitted(T t) {
						int row = findRow(t);
						assert row >= 0;
						doChangeNotCommitted(t, row);
					}

					@Override
					public boolean tryCommit(T t) {
						int row = findRow(t);
						assert row >= 0;
						return doTryChangeCommitted(t, row);
					}
				});
	}

	@Override
	public int getColumnCount() {
		int cols = properties.size();
		if (showStatusColumn) {
			cols++;
		}

		return cols;
	}

	@Override
	public String getColumnName(int col) {
		validateColumnLimits(col);

		if (showStatusColumn) {
			if (col == 0) {
				return StringUtils.stripToEmpty(statusColumnTitle);
			}

			col--;
		}

		return properties.get(col).getDisplay();
	}

	@Override
	public int getRowCount() {
		int count = data.size();
		if (extraEditing != null) {
			count++;
		}

		if (extraLine != null) {
			count++;
		}

		return count;
	}

	/**
	 * Checks whether a column number is valid and throws
	 * <code>IllegalArgumentException</code> if invalid.
	 * 
	 * @param col the column number
	 */
	private void validateColumnLimits(int col) {
		if (col < 0 || col >= getColumnCount()) {
			throw new IllegalArgumentException("col < 0 || "
					+ "col >= properties.size()");
		}
	}

	/**
	 * Checks whether a row number is valid and throws
	 * <code>IllegalArgumentException</code> if invalid.
	 * 
	 * @param row the row number
	 */
	private void validateRowLimits(int row) {
		if (row < 0 || row >= getRowCount()) {
			throw new IllegalArgumentException(
					"row < 0 || row >= getRowCount()");
		}
	}

	/**
	 * Checks whether a row and column numbers are valid and throws
	 * <code>IllegalArgumentException</code> if invalid.
	 * 
	 * @param row the row number
	 * @param col the column number
	 */
	private void validateLimits(int row, int col) {
		validateColumnLimits(col);
		validateRowLimits(row);
	}

	/**
	 * Obtains the property index corresponding to a column. These may difer if
	 * the status colum is showing.
	 * 
	 * @param col the column number
	 * 
	 * @return the property index of <code>-1</code> if the column doesn't
	 * correspond to any property (it corresponds to the status column)
	 */
	int getPropertyIndex(int col) {
		if (showStatusColumn) {
			if (col == 0) {
				return -1;
			}

			col--;
		}

		return col;
	}

	/**
	 * Obtains the property that corresponds to a column.
	 * 
	 * @param col the column number
	 * 
	 * @return the property or <code>null</code> if none (the column is the
	 * status column)
	 */
	AbstractQxtProperty<?> getProperty(int col) {
		col = getPropertyIndex(col);
		if (col == -1) {
			return null;
		}

		return properties.get(col);
	}

	@Override
	public Object getValueAt(int row, int col) {
		validateLimits(row, col);

		T t = getObject(row);
		AbstractQxtProperty<?> prop = getProperty(col);
		if (prop == null) {
			/*
			 * It must be the status column.
			 */
			ChangeController<T> cc = table.getChangeController();
			boolean isChanged = cc.getInChange() == getObject(row);

			assert iconChooser != null;
			StatusIconChooser.StatusType type;
			if (isNewLine(row)) {
				type = StatusIconChooser.StatusType.EMPTY;
			} else if (isExtraEditing(row)) {
				type = StatusIconChooser.StatusType.NEW;
			} else if (isChanged) {
				if (validator != null && !validator.isValid(t)) {
					type = StatusIconChooser.StatusType.INVALID;
				} else {
					type = StatusIconChooser.StatusType.EDITING;
				}
			} else {
				type = StatusIconChooser.StatusType.NORMAL;
			}

			return iconChooser.chooseIcon(t, type);
		}

		return prop.getValue(t);
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		validateLimits(row, col);

		AbstractQxtProperty<?> prop = getProperty(col);
		if (prop == null) {
			return false;
		}

		return prop.isEditable(getObject(row));
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		validateLimits(row, col);

		AbstractQxtProperty<?> prop = getProperty(col);
		if (prop == null) {
			return;
		}

		try {
			prop.setValue(getObject(row), value);
		} catch (PropertyAccessException e) {
			/*
			 * We'll ignore property vetos.
			 */
			if (e.getCause() instanceof InvocationTargetException) {
				InvocationTargetException ite = (InvocationTargetException) e
						.getCause();
				if (ite.getTargetException() instanceof PropertyVetoException) {
					return;
				}
			}

			throw e;
		}
	}

	/**
	 * Obtains the data being edited in the table.
	 * 
	 * @return the data
	 */
	ObservableList<T> getData() {
		return data;
	}

	/**
	 * Obtains the object that corresponds to a row. The row may be the new line
	 * or extra editing row.
	 * 
	 * @param row the row number
	 * 
	 * @return the object
	 */
	T getObject(int row) {
		validateRowLimits(row);

		if (row < data.size()) {
			return data.get(row);
		} else if (row == data.size() && extraEditing != null) {
			return extraEditing;
		} else if (row == data.size()) {
			assert extraLine != null;
			return extraLine;
		} else if (row == data.size() + 1) {
			assert extraEditing != null;
			assert extraLine != null;
			return extraLine;
		} else {
			assert false;
			return null;
		}
	}

	@Override
	public Object getRowContextObject(int row) {
		return getObject(row);
	}

	/**
	 * Obtains a property with a given name and data type.
	 * 
	 * @param <E> the property data type
	 * @param name the property name
	 * @param clazz the property's data type class
	 * 
	 * @return the property or <code>null</code> if none
	 */
	<E> AbstractQxtProperty<E> getProperty(String name, Class<E> clazz) {
		assert name != null;
		assert clazz != null;

		for (AbstractQxtProperty<?> p : properties) {
			if (!p.getName().equals(name)) {
				continue;
			}

			if (!p.getPropertyClass().equals(clazz)) {
				continue;
			}

			@SuppressWarnings("unchecked")
			AbstractQxtProperty<E> qpe = (AbstractQxtProperty<E>) p;

			return qpe;
		}

		return null;
	}

	/**
	 * Registers the property change listener on a object, if the bean supports
	 * registering property change listeners.
	 * 
	 * @param t the object
	 */
	private void registerListener(T t) {
		if (registered.contains(t)) {
			return;
		}

		if (addListenerMethod != null) {
			try {
				registered.add(t);
				for (PropertyChangeListener pcl : pcls) {
					addListenerMethod.invoke(t, pcl);
				}
			} catch (Exception e) {
				/*
				 * Encapsulate and rethrow!
				 */
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Unregisters the property change listener on a object, if the bean
	 * supports unregistering property change listeners.
	 * 
	 * @param t the object
	 */
	private void unregisterListener(T t) {
		if (!registered.contains(t)) {
			return;
		}

		if (removeListenerMethod != null) {
			try {
				registered.remove(t);
				for (PropertyChangeListener pcl : pcls) {
					removeListenerMethod.invoke(t, pcl);
				}
			} catch (Exception e) {
				/*
				 * Encapsulate and rethrow!
				 */
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Unregisters the property listener from all beans.
	 */
	private void unregisterAllListeners() {
		for (T t : new HashSet<>(registered)) {
			unregisterListener(t);
		}
	}

	/**
	 * Adds the extra line. There must be no extra line at the moment.
	 */
	private void addExtraLine() {
		assert extraLine == null;
		extraLine = lineFactory.makeLine();
		int idx = getRowCount() - 1;
		fireTableRowsInserted(idx, idx);
	}

	/**
	 * Removes the extra line.
	 * 
	 * @return the current extra line
	 */
	private T removeExtraLine() {
		assert extraLine != null;
		assert extraEditing == null;

		T line = extraLine;

		extraLine = null;
		int idx = getRowCount() - 1;
		fireTableRowsDeleted(idx, idx);

		return line;
	}

	/**
	 * Moves the current extra line to editing (as a result of a user edit) and
	 * creates a new line in the extra line.
	 * 
	 * @param newExtraLine the new extra line to add
	 */
	private void moveExtraLineToEditing(T newExtraLine) {
		LOGGER.debug("moveExtraLineToEditing(newExtraLine={" + newExtraLine
				+ "})");
		assert extraLine != null;
		assert extraEditing == null;

		extraEditing = extraLine;
		extraLine = newExtraLine;

		int idx = getRowCount() - 1;
		fireTableRowsInserted(idx, idx);
	}

	/**
	 * Removes the extra editing line.
	 * 
	 * @return the extra editing line
	 */
	private T removeExtraEditing() {
		LOGGER.debug("removeExtraEditing()");

		assert extraEditing != null;
		assert extraLine != null;

		T editing = extraEditing;

		int idx = getRowCount() - 2;
		extraEditing = null;
		fireTableRowsDeleted(idx, idx);

		return editing;
	}

	/**
	 * Determines if a row the new, empty line.
	 * 
	 * @param row the row number
	 * 
	 * @return is it the empty row?
	 */
	boolean isNewLine(int row) {
		if (extraLine != null && row == getRowCount() - 1) {
			return true;
		}

		return false;
	}

	/**
	 * Determines if a row is the extra editing row.
	 * 
	 * @param row the row number
	 * 
	 * @return is it the extra editing row?
	 */
	boolean isExtraEditing(int row) {
		if (extraEditing != null && row == getRowCount() - 2) {
			assert extraLine != null;
			return true;
		}

		return false;
	}

	/**
	 * Determines if a row is the extra editing or the new row.
	 * 
	 * @param row the row number
	 * 
	 * @return is it the extra editing or the new row?
	 */
	boolean isExtraEditingOrNew(int row) {
		return isNewLine(row) || isExtraEditing(row);
	}

	/**
	 * Obtains the row number where an object is located.
	 * 
	 * @param t the object
	 * 
	 * @return the row number
	 */
	int findRow(T t) {
		assert t != null;

		int count = getRowCount();

		for (int i = 0; i < count; i++) {
			if (getObject(i) == t) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Sets whether the status column should be shown.
	 * 
	 * @param showStatusColumn should the status column be shown?
	 */
	void setShowStatusColumn(boolean showStatusColumn) {
		if (showStatusColumn == this.showStatusColumn) {
			return;
		}

		this.showStatusColumn = showStatusColumn;
		if (iconChooser == null) {
			iconChooser = new DefaultStatusIconChooser<>();
		}

		fireTableStructureChanged();
	}

	/**
	 * Obtains whether the status column is visible or not.
	 * 
	 * @return is visible?
	 */
	boolean getShowStatusColumn() {
		return showStatusColumn;
	}

	/**
	 * Sets the title for the status column.
	 * 
	 * @param title the title, can be <code>null</code> (the default will be
	 * used)
	 */
	void setStatusColumnTitle(String title) {
		if (!ObjectUtils.equals(title, statusColumnTitle)) {
			statusColumnTitle = title;
			fireTableStructureChanged();
		}
	}

	/**
	 * Obtains the title of the status column.
	 * 
	 * @return the title (<code>null</code> if not defined)
	 */
	String getStatusColumnTitle() {
		return statusColumnTitle;
	}

	/**
	 * Editing of an object has started. This will suspend model
	 * synchronization, will make a copy of the bean (if bean copying is active)
	 * and will move the current line from new to editing (if the line is the
	 * new line). This method cannot be invoked on a new line if the current
	 * editing is not <code>null</code> (current editing must be committed or
	 * rolled back before editing the newline can begin).
	 * 
	 * @param t the object that has started editing
	 */
	private void doEditingStarted(T t) {
		LOGGER.debug("doEditingStarted(t={" + t + "})");

		assert t != null;
		int row = findRow(t);
		LOGGER.trace("doEditingStarted: row=" + row);
		assert row >= 0;

		boolean isSpecial = isExtraEditingOrNew(row);
		LOGGER.trace("doEditingStarted: isSpecial=" + isSpecial);
		assert localCopy == null;
		assert beanCopier != null;

		if (isNewLine(row)) {
			LOGGER.trace("doEditingStarted: isNewLine(row)");
			assert lineFactory != null;
			moveExtraLineToEditing(lineFactory.makeLine());
		}

		if (!isSpecial && autoBeanCopyMode) {
			localCopy = beanCopier.copy(t);
		}

		doChangeStarting(t, row);

		fireTableRowsUpdated(row, row);
		synchronizer.setSuspended(true);
	}

	/**
	 * Editing of a bean has stopped.
	 * 
	 * @param t the bean
	 * @param canceled has the bean editing been canceled? (If
	 * <code>false</code> it means editing has been committed)
	 */
	private void doEditingStopped(T t, boolean canceled) {
		LOGGER.debug("doEditingStopped(t={" + t + "},canceled=" + canceled
				+ ")");

		synchronizer.setSuspended(false);

		assert t != null;
		int row = findRow(t);
		LOGGER.trace("doEditingStopped: row=" + row);
		assert row >= 0;

		if (canceled) {
			doChangeRolledBack(t, row);
		} else {
			doChangeCommitted(t, row);
		}

		/*
		 * If the status icon column is visible, we might have to force an
		 * update of the row to ensure the icon is redrawn.
		 */
		if (showStatusColumn) {
			fireTableRowsUpdated(row, row);
		}

		/*
		 * We never edit new rows because as soon as we start, we move them to
		 * editing.
		 */
		assert !isNewLine(row);

		if (isExtraEditing(row)) {
			LOGGER.trace("doEditingStopped: isExtraEditing(row)");
			assert localCopy == null;

			/*
			 * There is one quirk here. We want to remove the current line as an
			 * extra edit and add it to the end of the "data". However, for
			 * external users this is a no-op because nothing really changes. So
			 * what we do is not notifying when we remove the extra edit and
			 * disable the notification of additions to the list.
			 */
			if (!canceled) {
				suspendFires = true;
			}

			try {
				T extra = removeExtraEditing();
				assert extra == t;
				if (!canceled) {
					data.add(extra);
				} else {
					assert lineFactory != null;
					lineFactory.destroyLine(extra);
				}
			} finally {
				suspendFires = false;
			}
		} else {
			if (localCopy != null) {
				if (canceled) {
					beanCopier.revert(t, localCopy);
				}

				localCopy = null;
			}
		}
	}

	/**
	 * Defines the validator to use.
	 * 
	 * @param validator the validator (can be <code>null</code> to disable the
	 * validator)
	 */
	void setValidator(Validator<T> validator) {
		this.validator = validator;
	}

	/**
	 * Obtains the current validator.
	 * 
	 * @return the current validator or <code>null</code> if none
	 */
	Validator<T> getValidator() {
		return validator;
	}

	/**
	 * Determines if a line is valid. If no validator has been defined this
	 * method returns <code>true</code>.
	 * 
	 * @param line the row number to validate
	 * 
	 * @return is the line valid?
	 */
	boolean isValid(int line) {
		boolean valid = false;
		if (validator == null || validator.isValid(getObject(line))) {
			valid = true;
		}

		return valid;
	}

	/**
	 * Defines the line factory. A line factory is mandatory if automatic line
	 * creation is enabled.
	 * 
	 * @param factory the factory
	 */
	void setLineFactory(LineFactory<T> factory) {
		if (factory == lineFactory) {
			return;
		}

		assert factory != null || !automaticLineCreationEnabled;

		lineFactory = factory;
	}

	/**
	 * Obtains the current line factor.
	 * 
	 * @return the current line factory or <code>null</code> if none
	 */
	LineFactory<T> getLineFactory() {
		return lineFactory;
	}

	/**
	 * Sets whether automatic line creation is enabled or not. Line creation can
	 * only be enabled if a line factory is defined.
	 * 
	 * @param automaticLineCreationEnabled enable?
	 */
	void setAutomaticLineCreationEnabled(boolean automaticLineCreationEnabled) {
		if (this.automaticLineCreationEnabled == automaticLineCreationEnabled) {
			return;
		}

		this.automaticLineCreationEnabled = automaticLineCreationEnabled;

		if (automaticLineCreationEnabled) {
			addExtraLine();
		} else {
			T t = removeExtraLine();
			lineFactory.destroyLine(t);
		}
	}

	/**
	 * Determines whether line creation is enabled.
	 * 
	 * @return is enabled?
	 */
	boolean isAutomaticLineCreationEnabled() {
		return automaticLineCreationEnabled;
	}

	/**
	 * Defines the bean copier to use.
	 * 
	 * @param copier the copier to use. If <code>null</code> the default bean
	 * copier will be used
	 */
	public void setBeanCopier(BeanCopier<T> copier) {
		if (copier == null) {
			beanCopier = new DefaultBeanCopier<>(beanClass);
		} else {
			beanCopier = copier;
		}
	}

	/**
	 * Obtains the current bean copier.
	 * 
	 * @return the bean copier
	 */
	public BeanCopier<T> getBeanCopier() {
		return beanCopier;
	}

	/**
	 * Sets whether auto bean copy mode is on or off.
	 * 
	 * @param autoBeanCopyMode is on?
	 */
	public void setAutoBeanCopyMode(boolean autoBeanCopyMode) {
		this.autoBeanCopyMode = autoBeanCopyMode;
	}

	/**
	 * Determines whether auto bean copy mode is on.
	 * 
	 * @return is auto bean copy mode on?
	 */
	public boolean isAutoBeanCopyMode() {
		return autoBeanCopyMode;
	}

	@Override
	public void fireTableCellUpdated(int arg0, int arg1) {
		if (!suspendFires) {
			super.fireTableCellUpdated(arg0, arg1);
		}
	}

	@Override
	public void fireTableChanged(TableModelEvent arg0) {
		if (!suspendFires) {
			super.fireTableChanged(arg0);
		}
	}

	@Override
	public void fireTableDataChanged() {
		if (!suspendFires) {
			super.fireTableDataChanged();
		}
	}

	@Override
	public void fireTableRowsDeleted(int arg0, int arg1) {
		if (!suspendFires) {
			super.fireTableRowsDeleted(arg0, arg1);
		}
	}

	@Override
	public void fireTableRowsInserted(int arg0, int arg1) {
		if (!suspendFires) {
			super.fireTableRowsInserted(arg0, arg1);
		}
	}

	@Override
	public void fireTableRowsUpdated(int arg0, int arg1) {
		if (!suspendFires) {
			super.fireTableRowsUpdated(arg0, arg1);
		}
	}

	@Override
	public void fireTableStructureChanged() {
		if (!suspendFires) {
			super.fireTableStructureChanged();
		}
	}

	/**
	 * Adds a new property change listener to all beans known to the model. The
	 * model will keep the listener registered on new beans and will also remove
	 * it from beans that are removed from the model.
	 * 
	 * @param pcl the change listener
	 */
	void addBeanPropertyChangeListener(PropertyChangeListener pcl) {
		assert pcl != null;

		/*
		 * We remove all listeners and add all again. This is not really
		 * intelligent but much simpler :)
		 */
		unregisterAllListeners();

		pcls.add(pcl);

		/*
		 * Register the listeners on all known beans.
		 */
		for (T t : data) {
			registerListener(t);
		}
	}

	/**
	 * Removes a listener from the beans and stops adding it to new beans.
	 * 
	 * @param pcl the listener
	 */
	void removeBeanPropertyChangeListener(PropertyChangeListener pcl) {
		assert pcl != null;

		unregisterAllListeners();

		pcls.remove(pcl);

		/*
		 * Register the listeners on all known beans.
		 */
		for (T t : data) {
			registerListener(t);
		}
	}

	/**
	 * Adds a line editor listener.
	 * 
	 * @param listener the listener to add
	 */
	void addLineEditorListener(LineEditorListener<T> listener) {
		if (listener == null) {
			throw new IllegalArgumentException("listener == null");
		}

		lineEditorListeners.add(listener);
	}

	/**
	 * Removes a line editor listener.
	 * 
	 * @param listener the listener to remove
	 */
	void removeLineEditorListener(LineEditorListener<T> listener) {
		if (listener == null) {
			throw new IllegalArgumentException("listener == null");
		}

		lineEditorListeners.remove(listener);
	}

	/**
	 * Invoked by the change controller when an object has started to be
	 * changed.
	 * 
	 * @param t the object
	 * @param row the object's row
	 */
	private void doChangeStarting(T t, int row) {
		LOGGER.debug("doChangeStarting(t=" + t + ", row=" + row + ")");

		assert t != null;
		assert row >= 0;

		for (LineEditorListener<T> l : new ArrayList<>(
				lineEditorListeners)) {
			l.lineEditingStarted(t, row);
		}
	}

	/**
	 * Invoked by the change controller to try to commit changes to an object.
	 * 
	 * @param t the changed object
	 * @param row the object's row
	 * 
	 * @return has the object been committed successfully?
	 */
	private boolean doTryChangeCommitted(T t, int row) {
		LOGGER.debug("doTryChangeCommitted(t=" + t + ", row=" + row + ")");

		assert t != null;
		assert row >= 0;

		boolean ok = true;
		for (LineEditorListener<T> l : new ArrayList<>(
				lineEditorListeners)) {
			ok &= l.tryLineEditingCommitted(t, row);
		}

		return ok;
	}

	/**
	 * Invoked by the change controller when a commit to a change object was not
	 * done successfully.
	 * 
	 * @param t the object
	 * @param row the object's row
	 */
	private void doChangeNotCommitted(T t, int row) {
		LOGGER.debug("doChangeNotCommitted(t=" + t + ", row=" + row + ")");

		assert t != null;
		assert row >= 0;

		for (LineEditorListener<T> l : new ArrayList<>(
				lineEditorListeners)) {
			l.lineEditingCommitFailed(t, row);
		}
	}

	/**
	 * Invoked by the change controller when changes to an object have been
	 * committed.
	 * 
	 * @param t the changed object
	 * @param row the object's row
	 */
	private void doChangeCommitted(T t, int row) {
		LOGGER.debug("doChangeCommitted(t=" + t + ", row=" + row + ")");

		assert t != null;
		assert row >= 0;

		for (LineEditorListener<T> l : new ArrayList<>(
				lineEditorListeners)) {
			l.lineEditingCommitted(t, row);
		}
	}

	/**
	 * Invoked by the change controller when changes to an object have been
	 * rolled back.
	 * 
	 * @param t the changed object
	 * @param row the object's row
	 */
	private void doChangeRolledBack(T t, int row) {
		LOGGER.debug("doChangeCommitted(t=" + t + ", row=" + row + ")");

		assert t != null;
		assert row >= 0;

		for (LineEditorListener<T> l : new ArrayList<>(
				lineEditorListeners)) {
			l.lineEditingCanceled(t, row);
		}
	}

	@Override
	public Class<?> getColumnClass(int col) {
		AbstractQxtProperty<?> property = getProperty(col);
		if (property == null) {
			return super.getColumnClass(col);
		}

		return property.getPropertyClass();
	}

	/**
	 * Adds a new property to the end of the list of properties.
	 * 
	 * @param property the property to add
	 */
	public void addProperty(AbstractQxtProperty<?> property) {
		if (property == null) {
			throw new IllegalArgumentException("property == null");
		}

		properties.add(property);
		fireTableStructureChanged();
	}

	/**
	 * Removes a property from the list of properties.
	 * 
	 * @param property the property to remove
	 */
	public void removeProperty(AbstractQxtProperty<?> property) {
		if (property == null) {
			throw new IllegalArgumentException("property == null");
		}

		if (!properties.remove(property)) {
			throw new IllegalStateException("Property '" + property.getName()
					+ "' was not registered.");
		}

		fireTableStructureChanged();
	}
}
