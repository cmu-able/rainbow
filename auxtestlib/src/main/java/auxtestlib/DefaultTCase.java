package auxtestlib;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Super class for all test cases. Test helpers (see
 * {@link AbstractTestHelper}) may be added as private variables with the
 * {@link TestHelper} annotation. They will be automatically initialized and
 * destroyed. Note that helpers will be initialized by alphabetical order and
 * will be destroyed in reverse order. This class will save system properties
 * on set up and will restore them at tear down.
 */
public class DefaultTCase extends Assert {
	/**
	 * All helpers.
	 */
	private List<Field> m_helpers;
	
	/**
	 * Saved system properties.
	 */
	private Properties m_system_properties;
	
	/**
	 * Runs before any preparation. Ensures there are no helpers pending from
	 * previous test cases. It will also initialize any helpers marked with the
	 * {@link TestHelper} annotation.
	 * @throws Exception failed to initialize helpers.
	 */
	@Before
	public void pre_set_up() throws Exception {
		/*
		 * We're in trouble if this is not true...
		 */
		assert m_helpers == null;
		
		/*
		 * Ensure global properties are loaded.
		 */
		TestPropertiesDefinition.load_global_properties();
		
		/*
		 * Save all system properties with the modifications from the test
		 * properties but before test code has been run.
		 */
		m_system_properties = (Properties) System.getProperties().clone();

		/*
		 * Check that there are no helpers pending from previous runs.
		 */
		assertEquals(0, AbstractTestHelper.getTotalHelperCount());

		/*
		 * Initialize helpers.
		 */
		m_helpers = new ArrayList<>();

		for (Class<?> cls = getClass(); cls != null;
				cls = cls.getSuperclass()) {
			Field[] fields = cls.getDeclaredFields();
			Arrays.sort(fields, new Comparator<Field>() {
				@Override
				public int compare(Field o1, Field o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			for (Field f : fields) {
				if (f.getAnnotation(TestHelper.class) != null) {
					if (!f.isAccessible()) {
						f.setAccessible(true);
					}

					Class<?> ftype = f.getType();
					if (!AbstractTestHelper.class.isAssignableFrom(ftype)) {
						throw new TestCaseConfigurationException("Field '"
								+ f.toString() + "' " + "of type '"
								+ cls.getCanonicalName() + "' has type '"
								+ ftype.getCanonicalName()
								+ "' which is not a subclass of "
								+ "AbstractTestHelper.");
					}

					m_helpers.add(f);
					f.set(this, f.getType().newInstance());
				}
			}
		}
	}

	/**
	 * Runs after tear down. Ensures all helpers have been destroyed.
	 * @throws Exception failed to tear down the test case
	 */
	@After
	public void post_tear_down() throws Exception {
		/*
		 * Not sure what means if this doesn't hold.
		 */
		assert m_helpers != null;

		/*
		 * Dispose of all helpers.
		 */
		Field[] fr = new Field[m_helpers.size()];
		ArrayUtils.reverse(m_helpers.toArray(fr));
		for (Field f : fr) {
			AbstractTestHelper ath = (AbstractTestHelper) f.get(this);
			ath.tearDown();
		}

		m_helpers = null;

		/*
		 * At the end there should be no more helpers.
		 */
		assertEquals(0, AbstractTestHelper.getTotalHelperCount());

		/*
		 * Sets all system properties back to their original values.
		 */
		System.setProperties(m_system_properties);
	}
}
