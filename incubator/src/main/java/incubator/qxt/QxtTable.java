package incubator.qxt;

import incubator.obscol.ObservableList;
import incubator.ui.VetoableListSelectionListener;
import incubator.ui.VetoableListSelectionSupport;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * The <code>QxtTable</code> is the main class in the <code>qxt</code> package.
 * It defines the table component. The following properties can be used to
 * monitor the table:
 * <ul>
 * <li><code>editing</code>: is a line being edited? (Lines can only be edited
 * if line mode is used)
 * </ul>
 * 
 * @param <T> the type of beans in the table
 */
public class QxtTable<T> extends JXTable {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(QxtTable.class);

	/**
	 * Is the table in line mode?
	 */
	private boolean inLineMode;

	/**
	 * Control used to handle table edits.
	 */
	private final TableEditController editCtrl;

	/**
	 * Control used to track changes.
	 */
	private final ChangeController<T> changeCtrl;

	/**
	 * Table configuration.
	 */
	private final TableConfiguration config;

	/**
	 * Popup provider (if any).
	 */
	private PopupProvider<T> popupProvider;

	/**
	 * Support for selection changes.
	 */
	private final VetoableListSelectionSupport vetoSelectionSupport;

	/**
	 * Properties need to set up their columns. But they can only do it once so
	 * we keep track of which properties have already set up their columns.
	 */
	private Set<AbstractQxtProperty<?>> propertiesAlreadySetup;

	/**
	 * Property listener registered to be informed of changes on properties.
	 */
	private AbstractQxtPropertyListener propertyListener;

	/**
	 * Creates a new table.
	 * 
	 * @param data the data used as support for the table
	 * @param beanClass the class representing the type of bean
	 * @param properties the properties of the bean used as columns
	 */
	public QxtTable(ObservableList<T> data, Class<T> beanClass,
			AbstractQxtProperty<?>... properties) {
		super(new QxtTableModel<>(data, beanClass, properties));

		inLineMode = false;
		config = new TableConfiguration(this);
		popupProvider = null;
		vetoSelectionSupport = new VetoableListSelectionSupport(
				getSelectionModel());
		propertiesAlreadySetup = new HashSet<>();

		/*
		 * model coordinates -- what does this comment mean?!
		 */
		editCtrl = new TableEditController(this);

		setSurrendersFocusOnKeystroke(true);
		changeCtrl = new ChangeController<>();
		getQxtModel().setTable(this);

		setupListeners();

		/*
		 * Register listeners on the properties so that we know when they
		 * change.
		 */
		propertyListener = new AbstractQxtPropertyListener() {
			@Override
			public void propertyDescriptionChanged(
					AbstractQxtProperty<?> property) {
				reviewColumns();
			}
		};

		for (AbstractQxtProperty<?> p : properties) {
			p.addAbstractQxtPropertyListener(propertyListener);
		}

		reviewColumns();
	}

	/**
	 * Sets up required listeners.
	 */
	private void setupListeners() {
		/*
		 * FIXME: Filter pipelines have been deprecated. I think this code will
		 * save changes to the sort order of columns in order to save them.
		 * We'll have to detect them some other way.
		 */
		// getFilters().addPipelineListener(new PipelineListener() {
		// @Override
		// public void contentsChanged(PipelineEvent arg0) {
		// saveConfiguration();
		// }
		// });

		getColumnModel().addColumnModelListener(new TableColumnModelListener() {
			@Override
			public void columnMarginChanged(ChangeEvent e) {
				saveConfiguration();
			}

			@Override
			public void columnMoved(TableColumnModelEvent e) {
				if (e.getFromIndex() != e.getToIndex()) {
					saveConfiguration();
				}
			}

			@Override
			public void columnSelectionChanged(ListSelectionEvent e) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			public void columnAdded(TableColumnModelEvent e) {
				reviewColumns();
				saveConfiguration();
			}

			@Override
			public void columnRemoved(TableColumnModelEvent e) {
				reviewColumns();
				saveConfiguration();
			}
		});

		getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent lse) {
						if (inLineMode) {
							ensureInSingleSelectionMode();
						}
					}
				});

		editCtrl.addListener(new TableEditController.Listener() {
			@Override
			public void editingCanceled(int row, int col) {
				doEditingCanceled(row);
			}

			@Override
			public void editingStarted(int row, int col) {
				doEditingStarted(row);
			}

			@Override
			public void editingStopped(int row, int col) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			public void editingStartedFailed(int row, int col) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			public boolean tryEditingStarted(int row, int col) {
				return tryStartEditingLine(row);
			}

		});

		VetoableListSelectionListener vlsl;
		vlsl = new VetoableListSelectionListener() {
			@Override
			public boolean canChangeSelection(ListSelectionEvent e) {
				return doSelectionChanged();
			}
		};
		vetoSelectionSupport.addVetoableListSelectionListener(vlsl);

		addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			public void mouseExited(MouseEvent e) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					doPopup(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				/*
				 * Nothing to do.
				 */
			}
		});

		addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					doEscapePressed();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				/*
				 * Nothing to do.
				 */
			}

			@Override
			public void keyTyped(KeyEvent e) {
				/*
				 * Nothing to do.
				 */
			}

		});
	}

	/**
	 * Obtains the current object responsible for selection support.
	 * 
	 * @return the vetoable selection support object
	 */
	VetoableListSelectionSupport getVetoSelectionSupport() {
		return vetoSelectionSupport;
	}

	/**
	 * Obtains the object responsible for handling changes.
	 * 
	 * @return the change controller
	 */
	ChangeController<T> getChangeController() {
		return changeCtrl;
	}

	/**
	 * Obtains the object responsible for handling edits.
	 * 
	 * @return the edit controller
	 */
	TableEditController getEditController() {
		return editCtrl;
	}

	/**
	 * Obtains the property that corresponds to a given column.
	 * 
	 * @param col the column number in model coordinates
	 * 
	 * @return the property
	 */
	AbstractQxtProperty<?> getProperty(int col) {
		return getQxtModel().getProperty(col);
	}

	/**
	 * Ensures that the table is in single selection mode, changing the current
	 * selection mode if required.
	 */
	private void ensureInSingleSelectionMode() {
		ListSelectionModel lsm = getSelectionModel();

		if (lsm.getSelectionMode() != ListSelectionModel.SINGLE_SELECTION) {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				}
			});
		}
	}

	/**
	 * Invoked to check whether the selection can change.
	 * 
	 * @return can change?
	 */
	private boolean doSelectionChanged() {
		LOGGER.debug("doSelectionChanged()");
		if (!inLineMode) {
			return true;
		}

		int currentRow = getCurrentSelectedModelRow();
		LOGGER.trace("doSelectionChanged: currentRow=" + currentRow);

		if (!commitLineChangesIfAny()) {
			LOGGER.trace("doSelectionChanged: commitLineChangesIfAny()=="
					+ "false");
			return false;
		}

		return true;
	}

	/**
	 * Tries to set the selection to the given row.
	 * 
	 * @param mrow the row number is model coordinates
	 * 
	 * @return have we been able to set the selection?
	 */
	boolean setSelection(int mrow) {
		LOGGER.debug("setSelection(mrow=" + mrow + ")");
		if (mrow == -1) {
			setRowSelectionInterval(-1, -1);
		} else {
			int vrow = convertRowIndexToView(mrow);
			LOGGER.trace("setSelection: vrow=" + vrow);
			ListSelectionModel lsm = getSelectionModel();
			lsm.setValueIsAdjusting(false);
			lsm.setSelectionInterval(vrow, vrow);
		}

		int crow = getCurrentSelectedModelRow();
		LOGGER.trace("setSelection: crow=" + crow);
		return crow == mrow;
	}

	/**
	 * Invoked when editing of a row has been started.
	 * 
	 * @param row the row that has started editing
	 */
	private void doEditingStarted(int row) {
		assert row >= 0;

		if (!inLineMode) {
			return;
		}

		T t = getQxtModel().getObject(row);
		if (changeCtrl.isChanging()) {
			/*
			 * If we're already changing an object, it must be the same object
			 * we're editing right now. If we are editing an object and
			 * immediately start editing another one, we'll catch that in the
			 * "try editing" part which runs before. The "try editing" part will
			 * commit the currently changing object.
			 */
			assert changeCtrl.getInChange() == t;

		} else {
			changeCtrl.startChanging(t);
		}

		firePropertyChange("editing", false, true);
	}

	/**
	 * Invoked when editing was canceled.
	 * 
	 * @param row the row whose editing was canceled.
	 */
	private void doEditingCanceled(int row) {
		LOGGER.debug("doEditingCanceled(row=" + row + ")");
		assert row >= 0;

		if (!inLineMode) {
			return;
		}

		T t = getQxtModel().getObject(row);
		assert changeCtrl.getInChange() == t;

		changeCtrl.rollback();

		firePropertyChange("editing", true, false);
	}

	/**
	 * Reviews the column structure setting the default cell renderer if none is
	 * defined and setting the status icon renderer if required. It also updates
	 * column descriptions if required.
	 */
	private void reviewColumns() {
		QxtTableModel<T> model = getQxtModel();

		TableColumnModel tcm = getColumnModel();
		for (int i = 0; i < tcm.getColumnCount(); i++) {
			TableColumnExt ext = getColumnExt(i);

			AbstractQxtProperty<?> p = model.getProperty(i);
			if (p == null) {
				/*
				 * The only case in which a property has a null column is when
				 * it is the status icon column. We need to make sure the
				 * renderer is correctly defined.
				 */
				TableCellRenderer tcr = ext.getCellRenderer();
				if (tcr == null || !(tcr instanceof StatusIconRenderer)) {
					ext.setCellRenderer(new StatusIconRenderer());
				}
				continue;
			}

			/*
			 * Set up the column if we haven't already done so.
			 */
			if (!propertiesAlreadySetup.contains(p)) {
				propertiesAlreadySetup.add(p);
				p.setup(ext);
			}

			/*
			 * Also make sure the cell editor is defined.
			 */
			TableCellEditor tce = ext.getCellEditor();
			if (tce == null) {
				tce = new DefaultCellEditor(new JTextField());
				ext.setCellEditor(tce);
			}

			/*
			 * Update column description if necessary.
			 */
			if (!p.getDisplay().equals(ext.getTitle())) {
				ext.setTitle(p.getDisplay());
			}
		}
	}

	/**
	 * Adds a line editor listener.
	 * 
	 * @param listener the listener to add
	 */
	public void addLineEditorListener(LineEditorListener<T> listener) {
		getQxtModel().addLineEditorListener(listener);
	}

	/**
	 * Removes a line editor listener.
	 * 
	 * @param listener the listener to remove
	 */
	public void removeLineEditorListener(LineEditorListener<T> listener) {
		getQxtModel().removeLineEditorListener(listener);
	}

	/**
	 * Obtains the data being edited.
	 * 
	 * @return the data
	 */
	public ObservableList<T> getData() {
		return getQxtModel().getData();
	}

	/**
	 * Obtains the associated table model.
	 * 
	 * @return the table model
	 */
	private QxtTableModel<T> getQxtModel() {
		TableModel tm = getModel();

		@SuppressWarnings("unchecked")
		QxtTableModel<T> qtm = (QxtTableModel<T>) tm;
		return qtm;
	}

	/**
	 * Sets (or unsets) the line factory. This is a precondition to enable line
	 * creation. Therefore the factory cannot be removed if automatic line
	 * creation is enabled and must be set before automatic line creation is
	 * enabled.
	 * 
	 * @param factory the line factory or <code>null</code> to remove a line
	 * factory
	 * 
	 * @see #setAutomaticLineCreationEnabled(boolean)
	 */
	public void setLineFactory(LineFactory<T> factory) {
		/*
		 * Note: reviewNewLineState requires the factory to still be registered.
		 */
		if (isAutomaticLineCreationEnabled() && factory == null) {
			throw new IllegalStateException("Cannot remove line factory "
					+ "with automatic line creation enabled.");
		}

		getQxtModel().setLineFactory(factory);
	}

	/**
	 * Obtains the current line factory.
	 * 
	 * @return the line factory or <code>null</code> if none
	 */
	public LineFactory<T> getLineFactory() {
		return getQxtModel().getLineFactory();
	}

	/**
	 * Sets whether automatic line creation is enabled. To enable line creation
	 * we must be in line mode and the line factory must be defined.
	 * 
	 * @param enabled should automatic line creation be enabled?
	 * 
	 * @see #setInLineMode(boolean)
	 * @see #setLineFactory(LineFactory)
	 */
	public void setAutomaticLineCreationEnabled(boolean enabled) {
		if (enabled && getQxtModel().getLineFactory() == null) {
			throw new IllegalStateException("Cannot enable automatic line "
					+ "creation if no line factory is defined.");
		}

		if (enabled && !inLineMode) {
			throw new IllegalStateException("Cannot enable automatic line "
					+ "creation if not in line mode.");
		}

		getQxtModel().setAutomaticLineCreationEnabled(enabled);
	}

	/**
	 * Determines whether automatic line creation is enabled.
	 * 
	 * @return is automatic line creation enabled?
	 */
	public boolean isAutomaticLineCreationEnabled() {
		return getQxtModel().isAutomaticLineCreationEnabled();
	}

	/**
	 * Obtains a property with a given name ensuring that it has a given type.
	 * 
	 * @param <E> the property data type
	 * @param name the property name
	 * @param clazz the property data type class
	 * 
	 * @return the property or <code>null</code> if not found
	 */
	public <E> AbstractQxtProperty<E> getProperty(String name, Class<E> clazz) {
		if (name == null) {
			throw new IllegalArgumentException("name == null");
		}

		if (clazz == null) {
			throw new IllegalArgumentException("clazz == null");
		}

		return getQxtModel().getProperty(name, clazz);
	}

	// /**
	// * Sets the table filter.
	// *
	// * @param filter the filter or <code>null</code> if no filter should be
	// * provided
	// */
	// public void setQxtFilter(QxtFilter<T> filter) {
	// if (filter == null) {
	// setFilters(null);
	// } else {
	// WrapperQxtFilter<T> f = new WrapperQxtFilter<T>(getQxtModel(),
	// filter);
	// FilterPipeline fp = new FilterPipeline(f);
	// setFilters(fp);
	// }
	// }

	/**
	 * Obtains a list with all selected objects.
	 * 
	 * @return a list with all selected objects
	 */
	public List<T> listSelected() {
		QxtTableModel<T> model = getQxtModel();
		List<T> l = new ArrayList<>();
		int[] rows = getSelectedRows();
		for (int r : rows) {
			r = convertRowIndexToModel(r);
			l.add(model.getObject(r));
		}

		return l;
	}

	/**
	 * Determines if the table is in line mode.
	 * 
	 * @return is in line mode?
	 */
	public boolean isInLineMode() {
		return inLineMode;
	}

	/**
	 * Sets the table in or out of line mode. Line mode is precondition for
	 * automatic line creation, auto bean copy and line validation. Line mode
	 * requires single selection so the selection mode may be changed by this
	 * method.
	 * 
	 * @param inLineMode should the table be placed in line mode?
	 * 
	 * @see #setAutoBeanCopyMode(boolean)
	 * @see #setAutomaticLineCreationEnabled(boolean)
	 * @see #setValidator(Validator)
	 */
	public void setInLineMode(boolean inLineMode) {
		/*
		 * Can't have automatic line creation enabled if not in line mode.
		 */
		if (!inLineMode && this.inLineMode
				&& getQxtModel().isAutomaticLineCreationEnabled()) {
			setAutomaticLineCreationEnabled(false);
		}

		/*
		 * Can't have auto-copy if not in line mode.
		 */
		if (!inLineMode && this.inLineMode
				&& getQxtModel().isAutoBeanCopyMode()) {
			setAutoBeanCopyMode(false);
		}

		/*
		 * Can't have validator if not in line mode.
		 */
		if (!inLineMode && this.inLineMode
				&& getQxtModel().getValidator() != null) {
			getQxtModel().setValidator(null);
		}

		this.inLineMode = inLineMode;
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	/**
	 * <p>
	 * This method is invoked by the editor controller to try to start editing a
	 * line in the table. Editing the line requires that the line is currently
	 * selected.
	 * </p>
	 * <p>
	 * This always returns <code>true</code> as there is no reason for currently
	 * stopping a line from being edited. However, we do perform some sanity
	 * checks.
	 * </p>
	 * 
	 * @param line the line to start editing (in model coordinates)
	 * 
	 * @return can the line be edited?
	 */
	private boolean tryStartEditingLine(int line) {
		LOGGER.debug("tryStartEditingLine(line=" + line + ")");

		if (!inLineMode) {
			return true;
		}

		/*
		 * We can't be editing a cell right now because the stop/cancel event
		 * must occur before the start of the next edit.
		 */
		assert !editCtrl.isEditing();

		/*
		 * This line must be selected because the edit controller guarantees
		 * that we only edit selected lines (the selection is changed before
		 * editing starts if required).
		 */
		assert getCurrentSelectedModelRow() == line;

		// /*
		// * Start by checking if we're already editing a different line
		// and,
		// * if we are, commit it.
		// */
		// T toEdit = getQxtModel().getObject(line);
		// if (changeCtrl.isChanging()) {
		// T chobj = changeCtrl.getInChange();
		// if (chobj != toEdit) {
		// int chrow = getQxtModel().findRow(chobj, -1);
		// assert chrow >= 0;
		// if (!commitLine(chrow)) {
		// return false;
		// }
		// }
		// }
		//
		// /*
		// * Check that the object is still in the same position. commitLine
		// may
		// * modify the data set so object position may shift. If it does
		// we'll
		// * just cancel the editing as the table will visually change.
		// */
		// int newLine = getQxtModel().findRow(toEdit, line);
		// if (line != newLine) {
		// return false;
		// }

		return true;
	}

	/**
	 * Commits the changes to the currently editing line (if any). If the
	 * current line is the one selected, just ignore it.
	 * 
	 * @return commited ok? (or <code>true</code> if nothing done)
	 */
	private boolean commitLineChangesIfAny() {
		LOGGER.debug("commitLineChangesIfAny()");

		/*
		 * We don't care about line commits if we're not in line mode or no
		 * object has been changed. There can be no line changes in this
		 * situation.
		 */
		if (!inLineMode || !changeCtrl.isChanging()) {
			return true;
		}

		T editObj = changeCtrl.getInChange();
		int objRow = getQxtModel().findRow(editObj);
		int currentRow = getCurrentSelectedModelRow();
		LOGGER.trace("commitLineChangesIfAny: objRow=" + objRow
				+ ", currentRow=" + currentRow);

		if (objRow == currentRow) {
			return true;
		}

		return commitLine(objRow);
	}

	/**
	 * Commits a changed line. Line commit may fail if a validator is currently
	 * active. This method will inform the change controller to commit the line.
	 * 
	 * @param line the line to commit in model coordinates
	 * 
	 * @return has the line been committed ok?
	 */
	private boolean commitLine(int line) {
		LOGGER.debug("commitLine(line=" + line + ")");
		QxtTableModel<T> model = getQxtModel();

		/*
		 * We can't commit new lines. If we have no bugs lurking around, new
		 * lines are converted to extra lines as soon as they begin being
		 * edited.
		 */
		boolean isNew = model.isNewLine(line);
		assert !isNew;

		if (!getQxtModel().isValid(line)) {
			LOGGER.trace("getQxtModel().isValid(line=" + line + ") == "
					+ "false");
			return false;
		}

		T t = model.getObject(line);
		assert changeCtrl.getInChange() == t;
		boolean ok = changeCtrl.commit();

		if (ok) {
			firePropertyChange("editing", true, false);
		}

		return ok;
	}

	/**
	 * Forces a commit on the current line being edited (if a line is being
	 * edited).
	 * 
	 * @return was the commit successful? (Returns <code>true</code> if no line
	 * is being edited)
	 */
	public boolean forceCommitIfNecessary() {
		assert changeCtrl != null;

		/*
		 * First, try to stop editing if any editing is being done.
		 */
		if (isEditing()) {
			TableCellEditor editor = getCellEditor();
			assert editor != null;
			if (!editor.stopCellEditing()) {
				return false;
			}
		}

		if (!changeCtrl.isChanging()) {
			return true;
		}

		return changeCtrl.commit();
	}

	/**
	 * Sets whether the status column should be shown or not.
	 * 
	 * @param show should the status column be shown?
	 */
	public void setShowStatusColumn(boolean show) {
		getQxtModel().setShowStatusColumn(show);
	}

	/**
	 * Determines whether the status column should be shown or not.
	 * 
	 * @return should the status column be shown?
	 */
	public boolean getShowStatusColumn() {
		return getQxtModel().getShowStatusColumn();
	}

	/**
	 * Sets the title for the status column.
	 * 
	 * @param title the title (may be <code>null</code> in which case the
	 * default title will be used)
	 */
	public void setStatusColumnTitle(String title) {
		getQxtModel().setStatusColumnTitle(title);
	}

	/**
	 * Obtains the current title for the status column.
	 * 
	 * @return the title (may be <code>null</code> if the default title is being
	 * used)
	 */
	public String getStatusColumnTitle() {
		return getQxtModel().getStatusColumnTitle();
	}

	/**
	 * Sets the line validator to use. Note that the table must be set in line
	 * mode in order to use a validator.
	 * 
	 * @param validator the validator or <code>null</code> if no validator
	 * should be used
	 * 
	 * @see #setInLineMode(boolean)
	 */
	public void setValidator(Validator<T> validator) {
		if (validator != null && !inLineMode) {
			throw new IllegalStateException(
					"Validator can only be set in line " + "mode.");
		}

		getQxtModel().setValidator(validator);
	}

	/**
	 * Obtains the current line validator.
	 * 
	 * @return the validator or <code>null</code> if none
	 */
	public Validator<T> getValidator() {
		return getQxtModel().getValidator();
	}

	/**
	 * Sets the current bean copier to use.
	 * 
	 * @param copier the bean copier (may be <code>null</code> to use the
	 * default)
	 */
	public void setBeanCopier(BeanCopier<T> copier) {
		getQxtModel().setBeanCopier(copier);
	}

	/**
	 * Obtains the current bean copier to use.
	 * 
	 * @return the bean copier to use
	 */
	public BeanCopier<T> getBeanCopier() {
		return getQxtModel().getBeanCopier();
	}

	/**
	 * Sets whether auto bean copy mode should be used. Note that auto bean copy
	 * mode can only be used if the table is in line mode.
	 * 
	 * @param autoBeanCopyMode should auto bean copy mode be used?
	 * 
	 * @see #setInLineMode(boolean)
	 */
	public void setAutoBeanCopyMode(boolean autoBeanCopyMode) {
		if (!inLineMode) {
			throw new IllegalStateException("Can only activate auto bean "
					+ "copy mode if the table is in line mode");
		}

		getQxtModel().setAutoBeanCopyMode(autoBeanCopyMode);
	}

	/**
	 * Determines if the table is in auto bean copy mode.
	 * 
	 * @return is the table in auto bean copy mode?
	 */
	public boolean isAutoBeanCopyMode() {
		return getQxtModel().isAutoBeanCopyMode();
	}

	/**
	 * Sets the configuration code used to store the table preferences.
	 * 
	 * @param code can be <code>null</code> in which case no preferences will be
	 * saved or loaded
	 */
	public void setConfigurationCode(String code) {
		config.setPreferencesCode(code);
	}

	/**
	 * Gets the current configuration code used to store table preferences.
	 * 
	 * @return the current code or <code>null</code> if none
	 */
	public String getConfigurationCode() {
		return config.getPreferencesCode();
	}

	/**
	 * Sets the class used to determine the preferences prefix.
	 * 
	 * @param clazz the class to use (can be <code>null</code> in which case a
	 * default one will be used)
	 */
	public void setConfigurationClass(Class<?> clazz) {
		config.setPreferencesClass(clazz);
	}

	/**
	 * Gets the class used to determine the preferences prefix.
	 * 
	 * @return the class to use
	 */
	public Class<?> getConfigurationClass() {
		return config.getPreferencesClass();
	}

	/**
	 * Saves the table configuration if a configuration class and a
	 * configuration code are defined.
	 * 
	 * @see #setConfigurationClass(Class)
	 * @see #setConfigurationCode(String)
	 */
	private void saveConfiguration() {
		config.saveConfiguration();
	}

	/**
	 * Loads the table configuration if a configuration class and a
	 * configuration code are defined.
	 * 
	 * @see #setConfigurationClass(Class)
	 * @see #setConfigurationCode(String)
	 */
	public void loadConfiguration() {
		config.loadConfiguration();
	}

	/**
	 * Sets the current popup provider.
	 * 
	 * @param provider the provider or <code>null</code> to disable the popup
	 * provider
	 */
	public void setPopupProvider(PopupProvider<T> provider) {
		this.popupProvider = provider;
	}

	/**
	 * Obtains the current popup provider.
	 * 
	 * @return the popup provider (or <code>null</code> if none)
	 */
	public PopupProvider<T> getPopupProvider() {
		return popupProvider;
	}

	/**
	 * Handles a popup event. The popup provider may be or not set. It may also
	 * happen that the current selection does not match the row at the point of
	 * the mouse event. In that case, this method will attempt to change the
	 * selection before showing the popup. If changing the selection fails, the
	 * popup is not shown.
	 * 
	 * @param e the event
	 */
	private void doPopup(MouseEvent e) {
		if (popupProvider == null) {
			return;
		}

		Point cpoint = e.getPoint();
		int crow = rowAtPoint(cpoint);
		if (crow == -1) {
			JPopupMenu menu = popupProvider.getNonRowMenu();
			if (menu != null) {
				menu.show(this, cpoint.x, cpoint.y);
			}
		} else {
			int mrow = convertRowIndexToModel(crow);
			if (getCurrentSelectedModelRow() != mrow
					&& popupProvider.popupRequiresSelection()) {
				/*
				 * Setting the selection doesn't trigger a stop editing in
				 * JTable.
				 */
				if (editCtrl.isEditing()) {
					TableCellEditor tce = getCellEditor();
					assert tce != null;
					if (!tce.stopCellEditing()) {
						/*
						 * If we fail to stop editing, can't show the popup!
						 */
						return;
					}
				}

				setSelection(mrow);
				if (getCurrentSelectedModelRow() != mrow) {
					/*
					 * We may fail if we fail to commit the line and are unable
					 * to change the current selection.
					 */
					return;
				}
			}

			JPopupMenu menu = popupProvider.getRowMenu(getQxtModel().getObject(
					mrow));
			if (menu != null) {
				menu.show(this, cpoint.x, cpoint.y);
			}
		}
	}

	/**
	 * Obtains the selected object.
	 * 
	 * @return the selected object or <code>null</code> if none
	 */
	public T getSelectedObject() {
		int row = getCurrentSelectedModelRow();
		if (row == -1) {
			return null;
		}

		return getQxtModel().getObject(row);
	}

	/**
	 * Obtains the current selected row.
	 * 
	 * @return the current selected row or <code>-1</code> if none
	 */
	public int getCurrentSelectedModelRow() {
		int row = getSelectedRow();
		if (row == -1) {
			return -1;
		}

		ListSelectionModel lsm = getSelectionModel();
		int min = lsm.getMinSelectionIndex();
		int max = lsm.getMaxSelectionIndex();
		LOGGER.trace("getCurrentSelectedModelRow(): row=" + row
				+ ", model min=" + min + ", model max=" + max);

		int mrow = convertRowIndexToModel(row);
		LOGGER.trace("getCurrentSelectedModelRow(): model row=" + mrow);
		return mrow;
	}

	/**
	 * Defines what is the preferred number of rows that should be visible on
	 * the table.
	 * 
	 * @param rows the number of rows
	 */
	public void setPreferredViewableNumberOfRows(int rows) {
		if (rows <= 0) {
			throw new IllegalArgumentException("rows <= 0");
		}

		Dimension preferredSize = getPreferredSize();
		preferredSize.height = getRowHeight() * rows;
		setPreferredSize(preferredSize);

		preferredSize = getPreferredScrollableViewportSize();
		preferredSize.height = getRowHeight() * rows;
		setPreferredScrollableViewportSize(preferredSize);
	}

	/**
	 * Determines if a line is being edited.
	 * 
	 * @return is a line being edited?
	 */
	public boolean isEditingLine() {
		assert changeCtrl != null;

		return changeCtrl.isChanging();
	}

	/**
	 * The user has pressed escape. Cancel editing the current line if any line
	 * is being edited.
	 */
	private void doEscapePressed() {
		if (changeCtrl.isChanging()) {
			changeCtrl.rollback();
		}
	}

	/**
	 * Adds a new property to the table.
	 * 
	 * @param property the property
	 */
	public void addProperty(AbstractQxtProperty<?> property) {
		if (property == null) {
			throw new IllegalArgumentException("property == null");
		}

		getQxtModel().addProperty(property);
		reviewColumns();

		property.addAbstractQxtPropertyListener(propertyListener);
	}

	/**
	 * Removes a property from the table.
	 * 
	 * @param property the property
	 */
	public void removeProperty(AbstractQxtProperty<?> property) {
		if (property == null) {
			throw new IllegalArgumentException("property == null");
		}

		getQxtModel().removeProperty(property);
		reviewColumns();

		property.removeAbstractQxtPropertyListener(propertyListener);
	}

	/**
	 * Table cell renderer used to show the status icon.
	 */
	private static class StatusIconRenderer extends DefaultTableCellRenderer {
		/**
		 * Serial version UID.
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Component c = super.getTableCellRendererComponent(table, null,
					isSelected, hasFocus, row, column);
			assert c instanceof JLabel;
			((JLabel) c).setIcon((Icon) value);
			return c;
		}
	}
}
