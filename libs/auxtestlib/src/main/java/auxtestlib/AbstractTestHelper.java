package auxtestlib;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;

/**
 * <p>
 * Super class for all test helpers.
 * </p>
 * <p>
 * In general, subclasses should override {@link #mySetUp()} and
 * {@link #myTearDown()} to create and destroy (using the {@link #tearDown()}
 * method) helpers they depend on. All test preparation should be done in the
 * {@link #myPrepareFixture()} and all clean up done in {@link #myCleanUp()}.
 * </p>
 * <p>
 * Clean up should be an idempotent operation (meaning it should be able to run
 * several times and have the same effect as running a single one). This is
 * important because test case execution may be aborted and disposal of
 * resources may not be done. {@link #myPrepareFixture()} is always called after
 * clean up so it can assume the test fixture has been previously cleaned.
 * </p>
 * <p>
 * As a general example, temporary tables should be dropped in the clean up
 * method and created in the prepare fixture. However, the clean up method
 * should be prepared to cope with the error arising from the table not
 * existing. The prepare fixture method, on the other hand, should not catch
 * errors because it is guaranteed that the table has been previously removed.
 * </p>
 * <p>
 * Note that <em>all</em> helper variables should be static as the
 * {@link #mySetUp()} method is called when the first helper (of a specific
 * class) is created and the {@link #myTearDown()} is called when the last
 * helper (of a specific class) is destroyed.
 * </p>
 */
public abstract class AbstractTestHelper extends Assert {
	/**
	 * Total number of helpers created.
	 */
	private static int helperCount;

	/**
	 * System properties that existed before the test started.
	 */
	private static Properties systemProperties;

	/**
	 * Number of helpers of each type created.
	 */
	private static Map<Class<?>, Integer> helperCounter;

	/**
	 * Creates a new test helper.
	 * 
	 * @throws Exception failed
	 */
	public AbstractTestHelper() throws Exception {
		/**
		 * Check if we're initializing the first helper. If so, call globalInit.
		 */
		helperCount++;
		if (helperCount == 1) {
			globalInit();
		}

		/**
		 * Check if we're initializing the first helper of this type. If so,
		 * call helperInit.
		 */
		Class<?> myClass = getClass();
		Integer cnt = helperCounter.get(myClass);
		if (cnt == null) {
			helperCounter.put(myClass, 0);
			helperInit();
		}

		helperCounter.put(myClass, helperCounter.get(myClass) + 1);
	}

	/**
	 * Destroys a test helper.
	 * 
	 * @throws Exception failed
	 */
	public final void tearDown() throws Exception {
		/**
		 * Check if we're destroying the last helper, call helperTearDown.
		 */
		Class<?> myClass = getClass();
		int hcnt = helperCounter.get(myClass) - 1;
		helperCounter.put(myClass, hcnt);
		if (hcnt == 0) {
			helperCounter.remove(myClass);
			helperTearDown();
		}

		/**
		 * Check if we're destroying the last helper. If so, globalTearDown.
		 */
		helperCount--;
		if (helperCount == 0) {
			globalTearDown();
		}
	}

	/**
	 * Initializes the helpers (called when the first helper is initialized).
	 * 
	 * @throws Exception failed
	 */
	private static void globalInit() throws Exception {
		helperCounter = new HashMap<>();
		TestPropertiesDefinition.load_global_properties();
		systemProperties = new Properties();
		systemProperties.putAll(System.getProperties());
	}

	/**
	 * Destroys the helpers (called when the last helper is destroyed).
	 */
	private static void globalTearDown() {
		helperCounter = null;
		Properties p = new Properties();
		p.putAll(systemProperties);
		System.setProperties(p);
	}

	/**
	 * Initializes a helper.
	 * 
	 * @throws Exception failed
	 */
	private void helperInit() throws Exception {
		mySetUp();
		myCleanUp();
		myPrepareFixture();
	}

	/**
	 * Destroys a helper.
	 * 
	 * @throws Exception failed
	 */
	private void helperTearDown() throws Exception {
		myCleanUp();
		myTearDown();
	}

	/**
	 * Gets a test property which has to be defined. The property is obtained as
	 * a string.
	 * 
	 * @param key the property key (the package name will be automatically
	 * prepended)
	 * 
	 * @return the property value
	 */
	public final String getPropMString(String key) {
		return TestPropertiesDefinition.getMString(prependPackage(key));
	}

	/**
	 * Gets a test property which has to be defined. The property is obtained as
	 * an integer.
	 * 
	 * @param key the property key (the package name will be automatically
	 * prepended)
	 * 
	 * @return the property value
	 */
	public final int getPropInt(String key) {
		return TestPropertiesDefinition.getInt(prependPackage(key));
	}

	/**
	 * Gets a test property which has to be defined. The property is obtained as
	 * a double.
	 * 
	 * @param key the property key (the package name will be automatically
	 * prepended)
	 * 
	 * @return the property value
	 */
	public final double getPropDouble(String key) {
		return TestPropertiesDefinition.getDouble(prependPackage(key));
	}

	/**
	 * Prepends the package name to a key.
	 * 
	 * @param key the key
	 * 
	 * @return the key with the package name prepended
	 */
	private String prependPackage(String key) {
		String pkg;

		pkg = this.getClass().getCanonicalName();
		int idx = pkg.lastIndexOf('.');
		return pkg.substring(0, idx + 1) + key;
	}

	/**
	 * Obtains the total number of helpers created. This may be useful to check
	 * if we leak helpers.
	 * 
	 * @return the total number of helpers created
	 */
	public static int getTotalHelperCount() {
		return helperCount;
	}

	/**
	 * Prepares the helper for execution. This method is invoked when the first
	 * helper of this class is created.
	 * 
	 * @throws Exception failed
	 */
	protected abstract void mySetUp() throws Exception;

	/**
	 * Destroys the helper. This method is invoked when the last helper of this
	 * class is destroyed.
	 * 
	 * @throws Exception failed
	 */
	protected abstract void myTearDown() throws Exception;

	/**
	 * Cleans all data that may have been generated by this run or previous run
	 * of the helper.
	 * 
	 * @throws Exception failed
	 */
	protected abstract void myCleanUp() throws Exception;

	/**
	 * Prepares data for the test.
	 * 
	 * @throws Exception failed
	 */
	protected abstract void myPrepareFixture() throws Exception;
}
