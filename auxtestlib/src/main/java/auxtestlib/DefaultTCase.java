package auxtestlib;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Super class for all test cases. Test helpers (see
 * {@link AbstractTestHelper}) may be added as private variables with the
 * {@link TestHelper} annotation. They will be automatically initialized and
 * destroyed. Note that helpers will be initialized by alphabetical order and
 * will be destroyed in reverse order.
 */
public class DefaultTCase extends Assert {
	/**
	 * All helpers.
	 */
	private List<Field> helpers;

	/**
	 * Runs before any preparation. Ensures there are no helpers pending from
	 * previous test cases. It will also initialize any helpers marked with the
	 * {@link TestHelper} annotation.
	 * 
	 * @throws Exception failed to initialize helpers.
	 */
	@Before
	public void preSetup() throws Exception {
		/*
		 * We're in trouble if this is not true...
		 */
		assert helpers == null;
		
		/*
		 * Ensure global properties are loaded.
		 */
		TestPropertiesDefinition.loadGlobalProperties();

		/*
		 * Check that there are no helpers pending from previous runs.
		 */
		assertEquals(0, AbstractTestHelper.getTotalHelperCount());

		/*
		 * Initialize helpers.
		 */
		helpers = new ArrayList<>();

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

					helpers.add(f);
					f.set(this, f.getType().newInstance());
				}
			}
		}
	}

	/**
	 * Runs after tear down. Ensures all helpers have been destroyed.
	 * 
	 * @throws Exception failed to tear down the test case
	 */
	@After
	public void postTearDown() throws Exception {
		/*
		 * Not sure what means if this doesn't hold.
		 */
		assert helpers != null;

		/*
		 * Dispose of all helpers.
		 */
		Field[] fr = new Field[helpers.size()];
		ArrayUtils.reverse(helpers.toArray(fr));
		for (Field f : fr) {
			AbstractTestHelper ath = (AbstractTestHelper) f.get(this);
			ath.tearDown();
		}

		helpers = null;

		/*
		 * At the end there should be no more helpers.
		 */
		assertEquals(0, AbstractTestHelper.getTotalHelperCount());

	}
}
