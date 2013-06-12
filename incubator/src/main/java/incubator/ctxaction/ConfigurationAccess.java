package incubator.ctxaction;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

/**
 * Class allowing access to the configuration file (usually named
 * <code>action-config.properties</code>.
 */
class ConfigurationAccess {
	/**
	 * Logger used.
	 */
	private static final Logger LOGGER = Logger
			.getLogger(ConfigurationAccess.class);

	/**
	 * The resource bundle or <code>null</code> if it failed to load.
	 */
	private ResourceBundle bundle;

	/**
	 * The bundle name.
	 */
	private final String bname;

	/**
	 * <p>
	 * Creates a new configuration access object that reads a configuration
	 * file. The configuration file is read from the given resource bundle (
	 * <code>bundleName</code> parameter). If <code>bundleName</code> is
	 * <code>null</code> a predefined bundle is used which is the class'
	 * package name with <code>.action-config</code> at the end.
	 * </p>
	 * 
	 * <p>
	 * This method will not fail even if the bundle can't be read.
	 * </p>
	 * 
	 * @param clazz the class used to access the configuration. Cannot be
	 * <code>null</code> and it will be used to find the properties file if
	 * <code>bundleNull</code> is <code>null</code>
	 * @param bundleName the name of the resource bundle (can be
	 * <code>null</code>)
	 */
	ConfigurationAccess(Class<?> clazz, String bundleName) {
		assert clazz != null;

		if (bundleName == null) {
			String fullClassName = clazz.getName();
			int fdotidx = fullClassName.lastIndexOf('.');
			bundleName = fullClassName.substring(0, fdotidx)
					+ ".action-config";
		}

		bundle = null;
		bname = bundleName;

		try {
			bundle = ResourceBundle.getBundle(bundleName);
		} catch (MissingResourceException e) {
			/*
			 * We'll ignore if we can't find the bundle. We'll use default
			 * values which should be horrible enough so the user will notice.
			 */
			LOGGER.error(e);
		}

	}

	/**
	 * Obtains a configuration from the resource bundle.
	 * 
	 * @param name the configuration name
	 * 
	 * @return the configuration value (<code>null</code> if none found)
	 */
	protected String getOptionalConfig(String name) {
		if (bundle == null) {
			return null;
		}

		String v;
		try {
			v = bundle.getString(name);
		} catch (MissingResourceException e) {
			// OK, we'll just return null.
			v = null;
		}

		return v;
	}

	/**
	 * Obtains a configuration from the resource bundle.
	 * 
	 * @param name the configuration name
	 * @param def the default value. If <code>null</code> we'll return an
	 * error message as configuration value
	 * 
	 * @return the value of the configuration
	 */
	protected String getConfig(String name, String def) {
		String missing = "**" + name + "(" + bundle + "/" + bname + ")**";

		String v = getOptionalConfig(name);
		if (v == null) {
			v = def;
		}

		if (v == null) {
			return missing;
		}

		return v;
	}
}
