package incubator.ctxaction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Class representing an action whose execution depends on a context. The
 * action configuration is defined in a properties file which is read as a
 * resource bundle (to support internationalization).
 * </p>
 * <p>
 * Each action has a name and an associated resource bundle name. The
 * <code>ContextualAction</code> class will read all entries in the format
 * <code>action.&lt;action name&gt;.&lt;xxx&gt;</code> where
 * <code>&lt;action name&gt;</code> is the action name and
 * <code>&lt;xxx&gt;</code> is:
 * </p>
 * <p>
 * <ul>
 * <li>id: an (optional) identifier for the action.</li>
 * <li>name: The name of the action as it should be presented to the user
 * (this property should always be defined);</li>
 * <li>image: The name of a resource which contains the icon for the action;
 * </li>
 * <li>description: A description for the action (used for tool tips);</li>
 * <li>accelerator: The accelerator key (syntax as described by
 * {@link javax.swing.KeyStroke});</li>
 * <li>mnemonic: Mnemonic key;</li>
 * </ul>
 */
public abstract class ContextualAction extends AbstractAction {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1;

	/**
	 * Property with action ID.
	 */
	static final String ID_PROPERTY = ContextualAction.class.getName()
			+ ":id";

	/**
	 * The action context. Is <code>null</code> while the action is not
	 * initialized.
	 */
	private ActionContext context;

	/**
	 * Access to the configuration object.
	 */
	private final ConfigurationAccess config;

	/**
	 * The name of the resource bundle used.
	 */
	private final String bundleName;

	/**
	 * The action name.
	 */
	private final String name;
	
	/**
	 * Listeners of the contextual action.
	 */
	private List<ContextualActionListener> listeners;

	/**
	 * Creates a new action. The bundle name is the classe's package
	 * followed by ".action-config". The action name is the class's name (no
	 * qualification).
	 */
	public ContextualAction() {
		this (null, null);
	}

	/**
	 * Creates a new contextual action copying the configuration from a
	 * template action.
	 * 
	 * @param template the action to copy data from
	 */
	public ContextualAction(ContextualAction template) {
		if (template == null) {
			throw new IllegalArgumentException("template == null");
		}

		this.config = template.config;
		this.bundleName = template.bundleName;
		this.name = template.name;
		
		listeners = new ArrayList<>();
	}

	/**
	 * Creates a new action.
	 * 
	 * @param bundle the name of the resource bundle from which action data
	 * will be read. If <code>null</code>, a default bundle name (which is
	 * the class's package followed by ".action-config") is used
	 * @param name the action name. If <code>null</code>, the class name
	 * (without qualification) is used
	 */
	public ContextualAction(String bundle, String name) {
		if (name == null) {
			name = getDefaultKeyName();
		}

		this.config = new ConfigurationAccess(getClass(), bundle);
		this.bundleName = bundle;
		this.name = name;
		
		listeners = new ArrayList<>();
	}

	/**
	 * Creates a new action.
	 * 
	 * @param classForBundle a class used to determine the bundle name
	 * (using the same rules as in the previous constructor)
	 * @param name the action name. If <code>null</code>, the class name
	 * (without qualification) is used
	 * @param context the execution context
	 */
	public ContextualAction(Class<?> classForBundle, String name,
			ActionContext context) {
		if (classForBundle == null) {
			throw new IllegalArgumentException("classForBundle == null");
		}

		if (context == null) {
			throw new IllegalArgumentException("context == null");
		}

		if (name == null) {
			name = getDefaultKeyName();
		}

		this.config = new ConfigurationAccess(classForBundle, null);
		this.bundleName = null;
		this.name = name;
		
		listeners = new ArrayList<>();
		bind(context);
	}
	
	/**
	 * Binds the action to an action context. This method must be invoked
	 * before invoking the action. It can only be called once.
	 * @param context the context to bind the action to.
	 * @return the same action (<code>this</code>)
	 */
	public ContextualAction bind(ActionContext context) {
		if (context == null) {
			throw new IllegalArgumentException("context == null");
		}
		
		if (this.context != null) {
			throw new IllegalStateException("Action already bound to a "
					+ "context.");
		}
		
		this.context = context;
		init();
		
		return this;
	}

	/**
	 * Initializes the contextual action registering listeners on the
	 * context.
	 */
	private void init() {
		ActionContextListener listener = new ActionContextListener() {
			@Override
			public void contextChanged(ActionContext context) {
				validate();
			}
		};

		context.addActionContextListener(listener);

		configure();
		validate();
	}

	/**
	 * Determines whether or not the action can be executed in a specific
	 * context.
	 * 
	 * @param context the context
	 * 
	 * @return can the action be executed?
	 */
	protected abstract boolean isValid(ActionContext context);

	/**
	 * Executes the action.
	 * 
	 * @param context the execution context
	 */
	protected abstract void perform(ActionContext context);

	/**
	 * Method used to execute the action. The default implementation invokes
	 * the <code>perform</code> method. This method also informs all registered
	 * listeners after execution.
	 * 
	 * @param e the action event
	 */
	@Override
	public final void actionPerformed(ActionEvent e) {
		if (context == null) {
			throw new IllegalStateException(
					"Action not yet bound to a context.");
		}
		
		perform(context);
		
		for (ContextualActionListener l : new ArrayList<>(listeners)) {
			l.actionPerformed();
		}
	}

	/**
	 * Validates if the action can or not be executed and enables or
	 * disables it.
	 */
	protected void validate() {
		if (context == null) {
			throw new IllegalStateException(
					"Action not yet bound to a context.");
		}
		
		if (isValid(context)) {
			setEnabled(true);
		} else {
			setEnabled(false);
		}
	}

	/**
	 * Configures the action's properties.
	 */
	private void configure() {
		assert name != null;

		putValue(Action.NAME, getConfig("name", null));

		String imageName = getOptionalConfig("image");
		if (imageName != null) {
			/*
			 * These lines could be url = getClass().getResource(...) but
			 * findbugs complains otherwise.
			 */
			Class<?> resourceClass = getClass();
			Object resourceClassObj = resourceClass;
			resourceClass = (Class<?>) resourceClassObj;
			URL url = resourceClass.getResource(imageName);
			if (url != null) {
				putValue(Action.SMALL_ICON, new ImageIcon(url));
			}
		}

		String description = getOptionalConfig("description");
		if (description != null) {
			putValue(Action.SHORT_DESCRIPTION, description);
			putValue(Action.LONG_DESCRIPTION, description);
		}

		String accel = getOptionalConfig("accelerator");
		if (accel != null) {
			KeyStroke ks = KeyStroke.getKeyStroke(accel);
			if (ks != null) {
				putValue(Action.ACCELERATOR_KEY, ks);
			}
		}

		String mnemonic = getOptionalConfig("mnemonic");
		if (mnemonic != null) {
			int mnemonicCode = readMnemonicCode(mnemonic);
			putValue(Action.MNEMONIC_KEY, mnemonicCode);
		}

		String id = getOptionalConfig("id");
		if (id != null) {
			putValue(ID_PROPERTY, id);
		}
	}

	/**
	 * Builds the action's configuration parameter name in the configuration
	 * bundle.
	 * 
	 * @param config the configuration parameter (<code>name</code>, for
	 * example)
	 * 
	 * @return the configuration parameter name in the bundle (in the form
	 * <code>action.&lt;action name&gt;.&lt;config parameter&gt;</code>)
	 */
	private String buildActionConfigName(String config) {
		assert config != null;

		return "action." + name + "." + config;
	}

	/**
	 * Builds the name of a global configuration parameter.
	 * 
	 * @param config the configuration parameter name
	 * 
	 * @return the configuration parameter name in the bundle
	 */
	private String buildGlobalConfigName(String config) {
		assert config != null;

		return "action." + config;
	}

	/**
	 * Obtains a configuration from the resource bundle.
	 * 
	 * @param name the action property name
	 * @param def the default value. If <code>null</code> we'll return an
	 * error message as configuration value
	 * 
	 * @return the value of the configuration
	 */
	protected String getConfig(String name, String def) {
		return config.getConfig(buildActionConfigName(name), def);
	}

	/**
	 * Obtains a configuration from the resource bundle.
	 * 
	 * @param name the action property name
	 * 
	 * @return the configuration value (<code>null</code> if none found)
	 */
	protected String getOptionalConfig(String name) {
		return config.getOptionalConfig(buildActionConfigName(name));
	}

	/**
	 * Obtains a global configuration parameter from the resource bundle.
	 * 
	 * @param name the action property name
	 * 
	 * @return the configuration value (<code>null</code> if none found)
	 */
	protected String getOptionalGlobalConfig(String name) {
		return config.getOptionalConfig(buildGlobalConfigName(name));
	}

	/**
	 * Determines the default action name.
	 * 
	 * @return the action name
	 */
	private String getDefaultKeyName() {
		String fullClassName = getClass().getName();
		return fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
	}

	/**
	 * Creates a menu item associated with this action.
	 * 
	 * @param icon create a menu item with icon (if icon is available?)
	 * 
	 * @return the menu item
	 */
	public JMenuItem createJMenuItem(boolean icon) {
		if (context == null) {
			throw new IllegalStateException(
					"Action not yet bound to a context.");
		}
		
		JMenuItem mi = new JMenuItem(this);
		if (!icon) {
			mi.setIcon(null);
		}

		return mi;
	}

	/**
	 * Creates a button associated with this action.
	 * 
	 * @param icon create a button with icon (if icon is available?)
	 * 
	 * @return the button
	 */
	public JButton createJButton(boolean icon) {
		if (context == null) {
			throw new IllegalStateException(
					"Action not yet bound to a context.");
		}
		
		JButton b = new JButton(this);
		if (!icon) {
			b.setIcon(null);
		}

		return b;
	}

	/**
	 * Obtains the value for the {@link KeyEvent} class field with the given
	 * name.
	 * 
	 * @param code the name of the field
	 * 
	 * @return the code value or <code>0</code> if not found
	 */
	private int readMnemonicCode(String code) {
		assert code != null;

		Class<KeyEvent> keyClass = KeyEvent.class;
		try {
			Field f = keyClass.getField(code);
			if (f.getType() != int.class) {
				return 0;
			}

			return (Integer) f.get(null);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Obtains the ID of the action. The ID is defined by the 'id' field in
	 * the action configuration file.
	 * 
	 * @return the ID or <code>null</code> if no ID was defined
	 */
	public String getId() {
		if (context == null) {
			throw new IllegalStateException(
					"Action not yet bound to a context.");
		}
		
		return (String) getValue(ID_PROPERTY);
	}

	/**
	 * Obtains the action context used to create this action.
	 * 
	 * @return the action context
	 */
	protected ActionContext getActionContext() {
		if (context == null) {
			throw new IllegalStateException(
					"Action not yet bound to a context.");
		}
		
		return context;
	}

	/**
	 * Programatically for the action to be executed (if it is valid).
	 * 
	 * @throws Throwable execution error (only thrown if
	 * <code>handleException</code> is <code>false</code>
	 */
	public void programaticExecute() throws Throwable {
		if (context == null) {
			throw new IllegalStateException(
					"Action not yet bound to a context.");
		}
		
		if (isValid(getActionContext())) {
			perform(getActionContext());
		}
	}
	
	/**
	 * Adds a listener to the contextual action.
	 * 
	 * @param listener the listener to add
	 */
	public void addContextualActionListener(ContextualActionListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("listener == null");
		}
		
		listeners.add(listener);
	}
	
	/**
	 * Removes a listener from the contextual action.
	 * 
	 * @param listener the listener to remove
	 */
	public void removeContextualActionListener(
			ContextualActionListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException("listener == null");
		}
		
		if (!listeners.remove(listener)) {
			throw new IllegalStateException("listener not registered.");
		}
	}
}
