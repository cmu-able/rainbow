package incubator.ctxaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class capable of organizing lists of actions reading the configuration
 * from a properties file.
 */
class ActionOrganizer {
	/**
	 * Configuration object.
	 */
	private final ConfigurationAccess config;

	/**
	 * Creates a organizer. The bundle where the bar configuration is read
	 * from is based on the same rules as the bundles for the contextual
	 * action.
	 * 
	 * @param clazz the class name used to determine the resource bundle
	 * (not used if <code>bundle</code> is not <code>null</code>
	 * @param bundle an optional bundle name
	 */
	public ActionOrganizer(Class<?> clazz, String bundle) {
		if (clazz == null) {
			throw new IllegalArgumentException("clazz == null");
		}

		config = new ConfigurationAccess(clazz, bundle);
	}

	/**
	 * Reads a list of configuration values. The values read are the ones
	 * starting with the prefix and having appended <code>.1</code>,
	 * <code>.2</code>, and so on. If there are values missing in the
	 * sequence, this method will stop reading at the first missing number
	 * ignoring all following items.
	 * 
	 * @param prefix the list prefix
	 * 
	 * @return a list with the values of all items read, in the order by
	 * which they were read
	 */
	protected List<String> readList(String prefix) {
		assert prefix != null;

		List<String> result = new ArrayList<>();
		for (int i = 1;; i++) {
			String value = config.getOptionalConfig(prefix + "." + i);
			if (value != null) {
				result.add(value);
			} else {
				return result;
			}
		}
	}

	/**
	 * Obtains a configuration value.
	 * 
	 * @param key the configuration key
	 * @param defaultValue the default value
	 * 
	 * @return the configuration value
	 */
	protected String getConfig(String key, String defaultValue) {
		return config.getConfig(key, defaultValue);
	}

	/**
	 * Obtains an optional configuration value.
	 * 
	 * @param key the configuration key
	 * 
	 * @return the configuration value or <code>null</code> if not defined
	 */
	protected String getOptionalConfig(String key) {
		return config.getOptionalConfig(key);
	}

	/**
	 * Finds an action with a specific ID.
	 * 
	 * @param id the action ID
	 * @param actions the collections of actions to be searched
	 * 
	 * @return the action found or <code>null</code> if none
	 */
	protected ContextualAction findAction(String id,
			Collection<ContextualAction> actions) {
		for (ContextualAction ca : actions) {
			if (id.equals(ca.getId())) {
				return ca;
			}
		}

		return null;
	}
}
