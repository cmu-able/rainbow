package incubator.koolform;

import incubator.obscol.ObservableList;
import incubator.ui.AutoUpdateJComboBox;
import incubator.ui.RegexValidationDocument;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.renderer.StringValue;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;

/**
 * <p>
 * The <code>KoolForm</code> class implements a panel which is able to present a
 * form. A form is a two-column list of fields in which data fields are on the
 * right and labels on the left.
 * </p>
 * <p>
 * This class makes creating forms much easier than a raw panel. It contains
 * several factory methods for commons components (all of which may be
 * overridable) as well as methods to add components with scroll panes added
 * around.
 * </p>
 * <p>
 * Components are added to the form one by one and are displayed graphically in
 * the order by which they were added (first component on top, last component on
 * bottom).
 * </p>
 * <p>
 * <code>KoolForm</code>s also allow registration of listeners which are
 * informed of changes in the form.
 * </p>
 * <p>
 * Several methods on this class have specific functionality depending on the
 * components used on the form:
 * <ul>
 * <li><code>getComponentValue</code></li>
 * <li><code>setComponentValue</code></li>
 * <li><code>installListener</code></li>
 * </ul>
 * The currently supported components are.
 * <ul>
 * <li>Subclasses of <code>JTextComponent</code></li>
 * <li><code>JXDatePicker</code></li>
 * <li><code>JLabel</code></li>
 * <li><code>AutoUpdateJComboBox</code></li>
 * <li>Subclasses of <code>JToggleButton</code></li>
 * </ul>
 * If subclasses require handling more components, they should override these
 * methods and provide implementation to handle the other component types.
 * </p>
 * <p>
 * This class supports organizing forms into sections and dividing sections into
 * columns. In order to use several columns, all components of a column should
 * be added and then {@link #advanceColumn()} should be called to start adding
 * components to the next column.
 * </p>
 * <p>
 * To use more than one section, the name of the first section should be defined
 * using {@link #renameSection(String)} (before or after adding the first
 * section's components) and then {@link #advanceSection(String)} should be
 * called to start a new section. An example for creating a multi-section,
 * multi-column form:
 * </p>
 * 
 * <pre>
 * renameSection(&quot;Section 1&quot;);
 * addComponent(&quot;C1&quot;, makeDateDatePicker());
 * addComponent(&quot;C2&quot;, makeDateDatePicker());
 * advanceColumn();
 * addComponent(&quot;C3&quot;, makeDateDatePicker());
 * addComponent(&quot;C4&quot;, makeDateDatePicker());
 * advanceSection(&quot;Section 2&quot;);
 * addComponent(&quot;C5&quot;, makeDateDatePicker());
 * addComponent(&quot;C6&quot;, makeDateDatePicker());
 * advanceColumn();
 * addComponent(&quot;C7&quot;, makeDateDatePicker());
 * addComponent(&quot;C8&quot;, makeDateDatePicker());
 * </pre>
 */
public class KoolForm extends JPanel {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * All form components.
	 */
	private final List<Component> components;

	/**
	 * Registered listeners.
	 */
	private final List<KoolFormListener> listeners;

	/**
	 * Does the form has focus?
	 */
	private boolean hasFocus;

	/**
	 * In which row are we currently inserting components?
	 */
	private int currentRow;

	/**
	 * In which column are we currently inserting components?
	 */
	private int currentColumn;

	/**
	 * The current column's panel.
	 */
	private JPanel columnPanel;

	/**
	 * The current section panel.
	 */
	private JPanel sectionPanel;

	/**
	 * Current tabbed pane (if used, <code>null</code> if no tabs are shown).
	 */
	private JTabbedPane tab;

	/**
	 * Insets to use (if any -- may be <code>null</code>).
	 */
	private Insets insets;

	/**
	 * Creates a new, empty form.
	 */
	public KoolForm() {
		setLayout(new BorderLayout());
		components = new ArrayList<>();
		listeners = new ArrayList<>();
		hasFocus = false;
		currentColumn = -1;

		advanceSection(null);
		advanceColumn();

		final KeyboardFocusManager kfm = KeyboardFocusManager
				.getCurrentKeyboardFocusManager();
		kfm.addPropertyChangeListener("focusOwner",
				new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						checkHasFocus();
					}
				});

		checkHasFocus();
	}

	/**
	 * Ensures the currently editing section is extended to use all available
	 * space.
	 */
	public void extendSection() {
		String lastTitle = null;
		if (tab != null) {
			lastTitle = tab.getTitleAt(tab.getTabCount() - 1);
		}

		if (!removeCurrentSectionFromForm()) {
			return;
		}

		if (tab == null) {
			add(sectionPanel, BorderLayout.CENTER);
		} else {
			assert lastTitle != null;
			tab.add(sectionPanel, lastTitle);
		}
	}

	/**
	 * Removes the current section from the form.
	 * 
	 * @return was anything done?
	 */
	private boolean removeCurrentSectionFromForm() {
		if (tab == null) {
			// FIXME: Use method above to remove.

			assert getComponentCount() == 1;
			Component c = getComponent(0);
			if (c == sectionPanel) {
				return false;
			}

			assert c instanceof JPanel;
			JPanel oldContainer = (JPanel) c;
			assert oldContainer.getComponentCount() == 1;
			assert oldContainer.getComponent(0) == sectionPanel;
			remove(c);
			oldContainer.remove(sectionPanel);
			return true;
		} else {
			if (sectionPanel.getParent() == tab) {
				/*
				 * Section is already directly inside tab, so ignore.
				 */
				return false;
			}

			Container c = sectionPanel.getParent();
			assert c.getParent() == tab;
			c.remove(sectionPanel);
			tab.remove(c);
			return true;
		}
	}

	/**
	 * Opens a new empty form section.
	 * 
	 * @param title the new section title
	 */
	public void advanceSection(String title) {
		if (title == null && sectionPanel != null) {
			throw new IllegalArgumentException("title == null");
		}

		sectionPanel = new JPanel();
		sectionPanel.setLayout(new GridBagLayout());

		if (tab == null) {
			add(createWrapperPanel(sectionPanel), BorderLayout.CENTER);
		} else {
			tab.add(createWrapperPanel(sectionPanel), title);
			currentColumn = -1;
			advanceColumn();
		}
	}

	/**
	 * Changes the name of the current section.
	 * 
	 * @param title the section title
	 */
	public void renameSection(String title) {
		if (title == null) {
			throw new IllegalArgumentException("title == null");
		}

		if (tab == null) {
			tab = new JTabbedPane();
			assert getComponentCount() == 1;
			Component c = getComponent(0);
			assert c instanceof JPanel;
			JPanel oldContainer = (JPanel) c;
			assert oldContainer.getComponentCount() == 1;
			assert oldContainer.getComponent(0) == sectionPanel;
			remove(c);
			oldContainer.remove(sectionPanel);
			tab.add(createWrapperPanel(sectionPanel), title);
			add(tab, BorderLayout.CENTER);
		} else {
			int tcount = tab.getTabCount();
			tab.setTitleAt(tcount - 1, title);
		}
	}

	/**
	 * Creates a panel to wrap the section's panel within a tab (because of
	 * section alignment).
	 * 
	 * @param panel the panel to wrap
	 * 
	 * @return the panel
	 */
	private JPanel createWrapperPanel(JPanel panel) {
		assert panel != null;

		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.weightx = 1;
		c.weighty = 1;
		containerPanel.add(panel, c);
		return containerPanel;
	}

	/**
	 * Move component insertion to the next column (next components to be added
	 * will be added to a new column in the form).
	 */
	public void advanceColumn() {
		currentColumn++;
		currentRow = 0;

		GridBagConstraints c = new GridBagConstraints();

		c.gridx = currentColumn;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		columnPanel = new JPanel();
		columnPanel.setLayout(new GridBagLayout());
		sectionPanel.add(columnPanel, c);
	}

	/**
	 * Adds a listener.
	 * 
	 * @param l the listener to add
	 */
	public void addKoolFormListener(KoolFormListener l) {
		if (l == null) {
			throw new IllegalArgumentException("l == null");
		}

		listeners.add(l);
	}

	/**
	 * Removes a listener.
	 * 
	 * @param l the listener to remove
	 */
	public void removeKoolFormListener(KoolFormListener l) {
		if (l == null) {
			throw new IllegalArgumentException("l == null");
		}

		listeners.remove(l);
	}

	/**
	 * Adds a component with a label.
	 * 
	 * @param <T> the component type
	 * @param label the component label
	 * @param component the component to add
	 * 
	 * @return the added component (the same as <code>component</code>)
	 */
	public <T extends Component> T addComponent(String label, T component) {
		return addComponent(label, component, null);
	}

	/**
	 * Adds a component with a label.
	 * 
	 * @param <T> the component type
	 * @param label the component label
	 * @param component the component to add
	 * @param position the label position
	 * 
	 * @return the added component (the same as <code>component</code>)
	 */
	public <T extends Component> T addComponent(String label, T component,
			LabelPosition position) {
		if (label == null && position != LabelPosition.NONE) {
			throw new IllegalArgumentException("label == null");
		}

		if (component == null) {
			throw new IllegalArgumentException("component == null");
		}

		addComponent(label, component, component, position);
		return component;
	}

	/**
	 * Adds a component placing a scrollable pane around it.
	 * 
	 * @param <T> the component type
	 * @param label the component label
	 * @param component the component to add
	 * 
	 * @return the added component (the same as <code>component</code>)
	 */
	public <T extends Component> T addScrollableComponent(String label,
			T component) {
		return addScrollableComponent(label, component, null);
	}

	/**
	 * Adds a component placing a scrollable pane around it.
	 * 
	 * @param <T> the component type
	 * @param label the component label
	 * @param component the component to add
	 * @param position the label position
	 * 
	 * @return the added component (the same as <code>component</code>)
	 */
	public <T extends Component> T addScrollableComponent(String label,
			T component, LabelPosition position) {
		if (label == null && position != LabelPosition.NONE) {
			throw new IllegalArgumentException("label == null");
		}

		if (component == null) {
			throw new IllegalArgumentException("component == null");
		}

		Component scrollable = makeScrollable(component);
		addComponent(label, component, scrollable, position);
		return component;
	}

	/**
	 * Adds a component.
	 * 
	 * @param label the label
	 * @param dataComponent the component with the data itself
	 * @param viewComponent the component to add to the pane (might be a
	 * container with <code>dataComponent</code> inside or it can be the data
	 * component itself)
	 * @param position the label position or <code>null</code> to use the
	 * default
	 */
	private void addComponent(String label, Component dataComponent,
			Component viewComponent, LabelPosition position) {
		assert label != null || position == LabelPosition.NONE;
		assert dataComponent != null;
		assert viewComponent != null;

		if (position == null) {
			position = LabelPosition.ON_SIDE;
		}

		GridBagConstraints c = new GridBagConstraints();

		installListener(dataComponent);

		Component labelComponent = null;
		if (label != null) {
			labelComponent = makeLabelComponent(label);
		}

		c.fill = GridBagConstraints.BOTH;
		if (insets != null) {
			c.insets = insets;
		}

		switch (position) {
		case ON_SIDE:
			c.gridx = currentColumn * 2;
			c.gridy = currentRow;
			c.weightx = 0;
			c.weighty = 0;

			columnPanel.add(labelComponent, c);

			c.gridx = currentColumn * 2 + 1;
			c.gridy = currentRow;
			c.weightx = 0;
			c.weighty = 0;

			if (viewComponent instanceof JScrollPane) {
				c.weightx = 1;
				c.weighty = 1;
			}

			columnPanel.add(viewComponent, c);
			break;
		case ON_TOP:
			c.gridx = currentColumn * 2;
			c.gridy = currentRow;
			c.gridwidth = 2;
			c.weightx = 0;
			c.weighty = 0;

			columnPanel.add(labelComponent, c);

			currentRow++;
			c.gridx = currentColumn * 2;
			c.gridy = currentRow;
			c.gridwidth = 2;
			c.weightx = 0;
			c.weighty = 0;

			if (viewComponent instanceof JScrollPane) {
				c.weightx = 1;
				c.weighty = 1;
			}

			columnPanel.add(viewComponent, c);
			break;
		case NONE:
			c.gridx = currentColumn * 2;
			c.gridy = currentRow;
			c.gridwidth = 2;
			c.weightx = 0;
			c.weighty = 0;

			if (viewComponent instanceof JScrollPane) {
				c.weightx = 1;
				c.weighty = 1;
			}

			columnPanel.add(viewComponent, c);
			break;
		default:
			assert false;
		}

		components.add(dataComponent);
		currentRow++;
	}

	/**
	 * Creates a new combo box.
	 * 
	 * @param <E> the combo box data type
	 * @param data the data for the combo box
	 * @param nullSupport how should <code>null</code> values be supported in
	 * the combo box
	 * @param nullValue how should <code>null</code> values be represented?
	 * (This parameter can be <code>null</code>)
	 * @param converter converter of the objects to string (if <code>null</code>
	 * , the default converter will be used)
	 * 
	 * @return the created combo box
	 */
	protected <E> AutoUpdateJComboBox<E> makeComboBoxField(
			ObservableList<E> data,
			AutoUpdateJComboBox.NullSupport nullSupport, String nullValue,
			StringValue converter) {
		AutoUpdateJComboBox<E> auxComboBox = new AutoUpdateJComboBox<>(data,
				converter);
		auxComboBox.setNullSupport(nullSupport, nullValue);
		return auxComboBox;
	}

	/**
	 * Creates a text field.
	 * 
	 * @param columns the number of visible columns of the text field
	 * @param regex an optional regular expression that the text field is
	 * required to validate (no input is accepted which violates the regular
	 * expression)
	 * 
	 * @return the text field
	 */
	protected JTextField makeTextField(int columns, String regex) {
		JTextField tfield = new JTextField();
		if (columns > 0) {
			tfield.setColumns(columns);
		}

		if (regex != null) {
			tfield.setDocument(new RegexValidationDocument(regex));
		}

		return tfield;
	}

	/**
	 * Creates a text field with a limit of characters.
	 * 
	 * @param columns the number of visible columns of the text field
	 * @param limit the maximum number of characters that can be added to the
	 * text field
	 * 
	 * @return the text field
	 */
	protected JTextField makeTextFieldLimited(int columns, int limit) {
		return makeTextField(columns, makeLimitRegex(limit));
	}

	/**
	 * Creates a password field.
	 * 
	 * @param columns of the with of the field
	 * @param limit the maximum number of characters allowed
	 * 
	 * @return the password field
	 */
	protected JPasswordField makePasswordFieldLimited(int columns, int limit) {
		JPasswordField passField = new JPasswordField();
		if (columns > 0) {
			passField.setColumns(columns);
		}

		if (limit > 0) {
			passField.setDocument(new RegexValidationDocument(
					makeLimitRegex(limit)));
		}

		return passField;
	}

	/**
	 * Creates a date picker.
	 * 
	 * @return the date picker
	 */
	protected JXDatePicker makeDatePicker() {
		JXDatePicker dpicker = new JXDatePicker();
		return dpicker;
	}

	/**
	 * Creates a label.
	 * 
	 * @return the label
	 */
	protected JLabel makeLabel() {
		return new JLabel();
	}

	/**
	 * Creates a text area component.
	 * 
	 * @param rows number of visible rows
	 * @param columns number of visible columns
	 * @param regex regular expression that the contents of the text area are
	 * required to validate
	 * 
	 * @return the created component
	 */
	protected JTextArea makeTextArea(int rows, int columns, String regex) {
		JTextArea textArea = new JTextArea();

		if (rows > 0) {
			textArea.setRows(rows);
		}

		if (columns > 0) {
			textArea.setColumns(columns);
		}

		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		if (regex != null) {
			textArea.setDocument(new RegexValidationDocument(regex));
		}

		modifyTextAreaTabBehavior(textArea);

		return textArea;
	}

	/**
	 * Creates a checkbox.
	 * 
	 * @return the checkbox
	 */
	protected JCheckBox makeCheckBox() {
		JCheckBox cb = new JCheckBox();
		return cb;
	}

	/**
	 * <p>
	 * If the platform's focus cycling uses the tab key, we'll change the
	 * default behvior of the text area to keep the tab key for focus change and
	 * add ctrl+tab to add a tab character.
	 * </p>
	 * <p>
	 * Code was copied and adapted from <a
	 * href="http://www.javalobby.org/java/forums/t20457.html"> here</a>
	 * </p>
	 * 
	 * @param textArea the text area to modify
	 */
	private void modifyTextAreaTabBehavior(final JTextArea textArea) {
		/**
		 * Action used to insert a tab character in a text area.
		 */
		final class TextInserter extends AbstractAction {
			/**
			 * Serial version IUD.
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				/*
				 * Could be improved to overtype selected range.
				 */
				textArea.insert("\t", textArea.getCaretPosition());
			}
		}

		int ftk = KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS;
		int btk = KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS;

		Set<AWTKeyStroke> forwardKeys = textArea.getFocusTraversalKeys(ftk);
		Set<AWTKeyStroke> backwardKeys = textArea.getFocusTraversalKeys(btk);

		/*
		 * Check that we want to modify current focus traversal keystrokes (if
		 * there is no focus cyling or more than way of focus traversing, just
		 * ignore it).
		 */
		if (forwardKeys.size() != 1 || backwardKeys.size() != 1) {
			return;
		}

		final AWTKeyStroke fks = forwardKeys.iterator().next();
		final AWTKeyStroke bks = backwardKeys.iterator().next();
		final int fkm = fks.getModifiers();
		final int bkm = bks.getModifiers();
		final int ctrlMask = InputEvent.CTRL_MASK + InputEvent.CTRL_DOWN_MASK;
		final int ctrlShiftMask = InputEvent.SHIFT_MASK
				+ InputEvent.SHIFT_DOWN_MASK + ctrlMask;
		if (fks.getKeyCode() != KeyEvent.VK_TAB || (fkm & ctrlMask) == 0
				|| (fkm & ctrlMask) != fkm) {
			/*
			 * Not currently CTRL+TAB using for forward focus traversal.
			 */
			return;
		}

		if (bks.getKeyCode() != KeyEvent.VK_TAB || (bkm & ctrlShiftMask) == 0
				|| (bkm & ctrlShiftMask) != bkm) {
			/*
			 * Not currently CTRL+SHIFT+TAB using for backwards focus traversal.
			 */
			return;
		}

		/*
		 * Bind our new forward focus traversal keys.
		 */
		Set<AWTKeyStroke> newForwardKeys = new HashSet<>(1);
		newForwardKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, 0));
		textArea.setFocusTraversalKeys(
				KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections
						.unmodifiableSet(newForwardKeys));

		/*
		 * Bind our new backward focus traversal keys.
		 */
		Set<AWTKeyStroke> newBackwardKeys = new HashSet<>(1);
		newBackwardKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB,
				InputEvent.SHIFT_MASK + InputEvent.SHIFT_DOWN_MASK));
		textArea.setFocusTraversalKeys(
				KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections
						.unmodifiableSet(newBackwardKeys));

		/*
		 * Now, it's still useful to be able to type TABs in some cases. Using
		 * this technique assumes that it's rare however (if the user is
		 * expected to want to type TAB often, consider leaving text area's
		 * behaviour unchanged...). Let's add some key bindings, inspired from a
		 * popular behaviour in instant messaging applications...
		 */
		textArea.getInputMap(JComponent.WHEN_FOCUSED).put(
				KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.CTRL_MASK
						+ InputEvent.CTRL_DOWN_MASK), "tab");
		textArea.getActionMap().put("tab", new TextInserter());
	}

	/**
	 * Creates a text area.
	 * 
	 * @param rows number of visible rows
	 * @param columns number of visible columns
	 * @param limit maximum number of characters that can be written in the text
	 * area
	 * 
	 * @return the text area
	 */
	protected JTextArea makeTextAreaLimited(int rows, int columns, int limit) {
		return makeTextArea(rows, columns, makeLimitRegex(limit));
	}

	/**
	 * Creates a label.
	 * 
	 * @param label the label text
	 * 
	 * @return the label
	 */
	protected Component makeLabelComponent(String label) {
		if (label == null) {
			throw new IllegalArgumentException("label == null");
		}

		JLabel component = new JLabel(label + ":");
		component.setHorizontalAlignment(SwingConstants.LEFT);
		return component;
	}

	/**
	 * Creates a scrollable component around a given component.
	 * 
	 * @param component the component to add the scrollable part around
	 * 
	 * @return the scrollable component
	 */
	protected Component makeScrollable(Component component) {
		if (component == null) {
			throw new IllegalArgumentException("component == null");
		}

		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(component);
		return scroll;
	}

	/**
	 * Enables or disables all components in the form.
	 * 
	 * @param enabled should all components be enabled?
	 */
	protected void setFormEnabled(boolean enabled) {
		for (Component c : components) {
			setComponentEnabled(c, enabled);
		}
	}

	/**
	 * Makes a form read only (or read write)
	 * 
	 * @param editable should the form be editable?
	 */
	public void setFormEditable(boolean editable) {
		for (Component c : components) {
			setComponentEditable(c, editable);
		}
	}

	/**
	 * Enables or disabled a component.
	 * 
	 * @param c the component
	 * @param enabled enable?
	 */
	protected void setComponentEnabled(Component c, boolean enabled) {
		if (c == null) {
			throw new IllegalArgumentException("c == null");
		}

		c.setEnabled(enabled);
	}

	/**
	 * Marks a component as editable or not.
	 * 
	 * @param c the component
	 * @param editable should be editable?
	 */
	protected void setComponentEditable(Component c, boolean editable) {
		if (c == null) {
			throw new IllegalArgumentException("c == null");
		}

		if (c instanceof JTextComponent) {
			((JTextComponent) c).setEditable(editable);
		} else if (c instanceof JXDatePicker) {
			((JXDatePicker) c).setEditable(editable);
		} else if (c instanceof AutoUpdateJComboBox<?>) {
			((AutoUpdateJComboBox<?>) c).setEditable(editable);
		} else {
			// FIXME: Should throw a good exception.
			throw new IllegalArgumentException("Unknown component class '"
					+ c.getClass().getName() + "': cannot set component "
					+ "as editable (or not).");
		}
	}

	/**
	 * Create a regular expression that only accepts a certain number of
	 * characters.
	 * 
	 * @param limit the number of characters
	 * 
	 * @return the regular expression
	 */
	private String makeLimitRegex(int limit) {
		String regex = null;
		if (limit > 0) {
			regex = ".{0," + limit + "}";
		}

		return regex;
	}

	/**
	 * Sets the value of a component.
	 * 
	 * @param c the component
	 * @param value the value to set (can be <code>null</code>)
	 */
	protected void setComponentValue(Component c, Object value) {
		if (c == null) {
			throw new IllegalArgumentException("c == null");
		}

		if (c instanceof JTextComponent) {
			String v = convert(value, String.class);
			((JTextComponent) c).setText(v);
		} else if (c instanceof JXDatePicker) {
			Date v = convert(value, Date.class);
			((JXDatePicker) c).setDate(v);
		} else if (c instanceof AutoUpdateJComboBox<?>) {
			((AutoUpdateJComboBox<?>) c).setSelected(value);
		} else if (c instanceof JLabel) {
			String v = convert(value, String.class);
			((JLabel) c).setText(v);
		} else if (c instanceof JToggleButton) {
			Boolean v = convert(value, Boolean.class);
			((JToggleButton) c).setSelected(v);
		} else {
			// FIXME: Should throw a good exception.
			throw new IllegalArgumentException("Unknown component class '"
					+ c.getClass().getName() + "': cannot set component "
					+ "value.");
		}
	}

	/**
	 * Obtains the value of a component.
	 * 
	 * @param c the component
	 * 
	 * @return the value (<code>null</code> if none)
	 */
	protected Object getComponentValue(Component c) {
		if (c == null) {
			throw new IllegalArgumentException("c == null");
		}

		if (c instanceof JTextComponent) {
			String v = ((JTextComponent) c).getText();
			v = StringUtils.trimToNull(v);
			return v;
		} else if (c instanceof JXDatePicker) {
			Date v = ((JXDatePicker) c).getDate();
			return v;
		} else if (c instanceof AutoUpdateJComboBox<?>) {
			Object v = ((AutoUpdateJComboBox<?>) c).getSelected();
			return v;
		} else if (c instanceof JLabel) {
			String v = ((JLabel) c).getText();
			return v;
		} else if (c instanceof JToggleButton) {
			return ((JToggleButton) c).isSelected();
		} else {
			// FIXME: Should throw a good exception.
			throw new IllegalArgumentException("Unknown component class '"
					+ c.getClass().getName() + "': cannot set component "
					+ "value.");
		}
	}

	/**
	 * Installs a listener on a component that invokes
	 * {@link #componentDataChanged(Component)} whenever the component's value
	 * changes.
	 * 
	 * @param c the component
	 */
	@SuppressWarnings("rawtypes")
	protected void installListener(final Component c) {
		if (c == null) {
			throw new IllegalArgumentException("c == null");
		}

		if (c instanceof JTextComponent) {
			((JTextComponent) c).getDocument().addDocumentListener(
					new DocumentListener() {
						@Override
						public void changedUpdate(DocumentEvent arg0) {
							componentDataChanged(c);
						}

						@Override
						public void insertUpdate(DocumentEvent arg0) {
							componentDataChanged(c);
						}

						@Override
						public void removeUpdate(DocumentEvent arg0) {
							componentDataChanged(c);
						}
					});
		} else if (c instanceof JXDatePicker) {
			c.addPropertyChangeListener ("date",
										 new PropertyChangeListener () {
						@Override
						public void propertyChange (PropertyChangeEvent evt) {
							componentDataChanged (c);
						}
					});
		} else if (c instanceof JComboBox) {
			((JComboBox) c).addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent arg0) {
					componentDataChanged(c);
				}
			});
		} else if (c instanceof JLabel) {
			c.addPropertyChangeListener ("text",
										 new PropertyChangeListener () {
						@Override
						public void propertyChange (PropertyChangeEvent evt) {
							componentDataChanged (c);
						}
					});
		} else if (c instanceof JToggleButton) {
			((JToggleButton) c).addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					componentDataChanged(c);
				}
			});
		}
	}

	/**
	 * Converts a value from one type to another (fails if the value is not
	 * castable).
	 * 
	 * @param <E> the expected value type
	 * @param value the value to convert (can be <code>null</code>)
	 * @param clazz the class to which the value should be converted
	 * 
	 * @return the converted value
	 */
	protected <E> E convert(Object value, Class<E> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz == null");
		}

		if (value == null) {
			return null;
		}

		if (clazz.isInstance(value)) {
			return clazz.cast(value);
		}

		if (value instanceof Integer && clazz == String.class) {
			return clazz.cast (value.toString ());
		}

		if (value instanceof String
				&& (clazz == Integer.class || clazz == int.class)) {
			try {
				return clazz.cast(new Integer((String) value));
			} catch (NumberFormatException e) {
				/*
				 * Ignore and fall to default exception.
				 */
				assert true;
			}
		}

		// FIXME: Should throw a good exception.
		throw new IllegalArgumentException("Cannot convert value {" + value
				+ "} (type '" + value.getClass().getName() + "') to '"
				+ clazz.getName() + "'.");
	}

	/**
	 * Informed when a component has been changed.
	 * 
	 * @param c the component
	 */
	protected void componentDataChanged(Component c) {
		/*
		 * Empty stub method.
		 */
	}

	/**
	 * Determines if a component belongs to this form (it can be one of the
	 * components or a subcomponent of it).
	 * 
	 * @param c the component
	 * 
	 * @return does the component belong to the form?
	 */
	protected boolean isMine(Component c) {
		assert c != null;

		for (; c != null; c = c.getParent()) {
			if (components.contains(c)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks whether focus has been gained or lost on the form (and informs
	 * listeners accordingly).
	 */
	private void checkHasFocus() {
		KeyboardFocusManager kfm = KeyboardFocusManager
				.getCurrentKeyboardFocusManager();
		Component c = kfm.getFocusOwner();
		if (c == null) {
			return;
		}

		Class<?>[] toIgnore = new Class<?>[] { JRootPane.class, Window.class };

		for (Class<?> ic : toIgnore) {
			if (ic.isInstance(c)) {
				return;
			}
		}

		boolean isMine = isMine(c);
		if (hasFocus == isMine) {
			return;
		}

		hasFocus = isMine;

		for (KoolFormListener l : new ArrayList<>(listeners)) {
			l.formFocusChanged(hasFocus);
		}
	}

	@Override
	public void requestFocus() {
		if (components.size() == 0) {
			return;
		}

		Component c1 = components.get(0);
		c1.requestFocus();
	}

	/**
	 * Sets the default insets to use on new components.
	 * 
	 * @param insets the insets (which may be <code>null</code>)
	 */
	public void setDefaultInsets(Insets insets) {
		if (insets == null) {
			this.insets = null;
		} else {
			this.insets = (Insets) insets.clone();
		}
	}

	/**
	 * Where should the component label be placed?
	 */
	public enum LabelPosition {
		/**
		 * Label should be placed on the component side.
		 */
		ON_SIDE,

		/**
		 * Label should be placed on component top.
		 */
		ON_TOP,

		/**
		 * Label should not be placed.
		 */
		NONE
	}
}
