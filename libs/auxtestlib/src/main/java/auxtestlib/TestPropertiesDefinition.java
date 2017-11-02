package auxtestlib;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * <p>
 * Class that defines (and allows manipulating) test properties. Test properties
 * are loaded from a <code>.properties</code> file. Two files should exist on
 * the project's base directory (or somewhere in the classpath): a file named
 * <code>testdata-project.properties</code> and a file named
 * <code>testdata-local.properties</code>. The former contains all properties
 * that are general in the project (common for all users) and the latter
 * overrides some properties that are specific for each user. This class
 * contains a method to load these properties as system properties. It also
 * provides utility methods to access those properties.
 * </p>
 * <p>
 * This class is not usually accessed directly but through the
 * {@link AbstractTestHelper} class by using any test helpers. However, test
 * classes that do not use helpers may use this class directly. Before being
 * used, the class must be initialized through the
 * {@link #load_global_properties()} method. This method is automatically
 * invoked by the {@link DefaultTCase} class during test fixture preparation.
 * </p>
 * <p>
 * The <code>testdata-local.propertiers</code> file is optional (and, if it
 * exists, should not be added to source control) but the
 * <code>testdata-project.properties</code> is mandatory.
 * </p>
 */
public class TestPropertiesDefinition {
	/**
	 * Marker set to <code>true</code> when the properties have been loaded.
	 */
	private static boolean loaded = false;

	/**
	 * Utility class: no constructor.
	 */
	private TestPropertiesDefinition() {
		/*
		 * Nothing to do.
		 */
	}

	/**
	 * Loads the test properties (if not already done).
	 * @throws Exception failed to load properties
	 */
	public static void load_global_properties() throws Exception {
		if (loaded) {
			return;
		}

		Properties testLocalProperties = new Properties();
		try (InputStream is = loadPropertiesStream(
				"testdata-local.properties")) {
			if (is != null) {
				testLocalProperties.load(is);
			}
		}

		Properties testProjectProperties = new Properties();
		try (InputStream is =
				loadPropertiesStream("testdata-project.properties")) {
			if (is == null) {
				throw new Exception("File 'testdata-project.properties' "
						+ "not found (in classpath or working directory).");
			}
			
			testProjectProperties.load(is);
		}

		testProjectProperties.putAll(testLocalProperties);

		processVariableReplaces(testProjectProperties);

		Properties systemProperties = System.getProperties();
		systemProperties.putAll(testProjectProperties);
		System.setProperties(systemProperties);
		loaded = true;
	}

	/**
	 * Opens a properties file. This can be either a resource or a file in the
	 * current working directory.
	 * @param name the file name
	 * @return the input stream ou <code>null</code> if no file or resource was
	 * found
	 * @throws Exception failed processing the file
	 */
	private static InputStream loadPropertiesStream(String name)
			throws Exception {
		ClassLoader ldr = TestPropertiesDefinition.class.getClassLoader();
		InputStream is = ldr.getResourceAsStream(name);
		if (is != null) {
			return is;
		}

		try {
			is = new FileInputStream(name);
			return is;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Obtains a property as a string.
	 * @param key the property name
	 * @return the property value
	 */
	public static String getMString(String key) {
		assert key != null;
		String value = System.getProperty(key);
		if (value == null) {
			throw new IllegalPropertyException("Property '" + key + "' "
					+ "must be defined.");
		}

		return value.trim();
	}

	/**
	 * Obtains a property which must exist and be an integer number.
	 * @param key the property name
	 * @return the property value
	 */
	public static int getInt(String key) {
		String sval = getMString(key);
		try {
			return Integer.parseInt(sval);
		} catch (NumberFormatException e) {
			throw new IllegalPropertyException("Property '" + key + "' "
					+ "should be a number but is '" + sval + "'", e);
		}
	}

	/**
	 * Obtains a property which must exist and be a double number.
	 * @param key the property name
	 * @return the property value
	 */
	public static double getDouble(String key) {
		String sval = getMString(key);
		try {
			return Double.parseDouble(sval);
		} catch (NumberFormatException e) {
			throw new IllegalPropertyException("Property '" + key + "' "
					+ "should be a double but is '" + sval + "'", e);
		}
	}

	/**
	 * Try to replace all variables defined in 'properties'. Replacements occur
	 * in the property values: where ${X} is written, it is replaced by the
	 * value of property X. The algorithm guarantees that dependency resolution
	 * is made on the correct order and cases in which substitution fails raise
	 * an exception.
	 * @param properties properties to replace
	 * @throws Exception a definition is missing (or there are circular
	 * references)
	 */
	private static void processVariableReplaces(Properties properties)
			throws Exception {
		/*
		 * We'll use the following algorithm: we copy all properties to a map
		 * keeping the key/value map. We go through the whole map and remove all
		 * properties which need no substitutions. For each property removed we
		 * will replace ${<key>} by its value on all other properties. When we
		 * reach the end of the map, we'll do all again until either the map is
		 * empty or no changes could have been made. If, when we end, the map is
		 * not empty, we'll thrown an exception.
		 */
		// Regex: \$\{ ( [^}]+ ) \}
		Pattern varpat = Pattern.compile("\\$\\{[^}]+\\}");

		// Place in 'unreplaced' all variables with unreplaced values..
		Map<Object, Object> unreplaced = new HashMap<>();
		unreplaced.putAll(properties);
		int removed;
		do {
			removed = 0;
			for (Iterator<Map.Entry<Object, Object>> it = unreplaced.entrySet()
					.iterator(); it.hasNext();) {
				Map.Entry<Object, Object> e = it.next();
				String k = (String) e.getKey();
				String v = (String) e.getValue();

				if (!varpat.matcher(v).find()) {
					// Nothing to replace here. This variable can be removed.
					removed++;
					properties.setProperty(k, v);
					it.remove();

					for (Iterator<Map.Entry<Object, Object>> it2 = unreplaced
							.entrySet().iterator(); it2.hasNext();) {
						Map.Entry<Object, Object> ee = it2.next();
						String k2 = (String) ee.getKey();
						String v2 = (String) ee.getValue();

						v2 = v2.replaceAll("\\$\\{" + k + "\\}", v);
						unreplaced.put(k2, v2);
					}
				}
			}
		} while (!unreplaced.isEmpty() && removed > 0);

		// We're done.
		if (!unreplaced.isEmpty()) {
			StringBuffer text = new StringBuffer("Failed to replace value for "
					+ "properties due to undefined properties or circular "
					+ "references: ");
			for (Iterator<Object> it = unreplaced.keySet().iterator(); it
					.hasNext();) {
				String k = (String) it.next();
				text.append("'" + k + "'");
				if (it.hasNext()) {
					text.append(", ");
				}
			}

			throw new Exception(text.toString());
		}
	}

	/**
	 * Exception thrown when a property contains an invalid value.
	 */
	static class IllegalPropertyException extends RuntimeException {
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new exception-
		 * 
		 * @param description error description
		 */
		public IllegalPropertyException(String description) {
			super(description);
		}

		/**
		 * Creates a new exception-
		 * 
		 * @param description error description
		 * @param cause the cause of the exception
		 */
		public IllegalPropertyException(String description, Throwable cause) {
			super(description, cause);
		}
	}
}
