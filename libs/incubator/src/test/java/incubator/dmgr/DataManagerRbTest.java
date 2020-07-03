package incubator.dmgr;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Robustness tests for the dmgr API.
 */
public class DataManagerRbTest extends Assert {
	/**
	 * Data manager to be used in the tests.
	 */
	private DataManager dManager;

	/**
	 * Prepares the test.
	 */
	@Before
	public void setUp() {
		dManager = new DataManager();
	}

	/**
	 * Cleans up the test.
	 */
	@After
	public void tearDown() {
		dManager = null;
	}

	/**
	 * Cannot notify listeners of an add object with <code>null</code> source.
	 * 
	 * @throws Exception failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void cannotCanAddWithNullSource() throws Exception {
		dManager.canAdd(null);
	}

	/**
	 * Cannot notify listeners of an add object with <code>null</code> source.
	 * 
	 * @throws Exception failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void cannotAddWithNullSource() throws Exception {
		dManager.added(null);
	}

	/**
	 * Cannot notify listeners of remove object with <code>null</code> source.
	 * 
	 * @throws Exception failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void cannotCanRemoveWithNullSource() throws Exception {
		dManager.canRemove(null);
	}

	/**
	 * Cannot remove object with <code>null</code> source.
	 * 
	 * @throws Exception failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void cannotRemovedWithNullSource() throws Exception {
		dManager.removed(null);
	}

	/**
	 * Cannot notify listeners of change object with <code>null</code> source.
	 * 
	 * @throws Exception failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void cannotCanChangeWithNullOldObject() throws Exception {
		dManager.canChange(null, null);
	}

	/**
	 * Cannot notify listeners of change object with <code>null</code> source.
	 * 
	 * @throws Exception failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void cannotCanChangeWithNullNewObject() throws Exception {
		dManager.canChange(this, null);
	}

	/**
	 * Cannot notify listeners of change with object with <code>null</code>
	 * source.
	 * 
	 * @throws Exception failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void cannotChangedWithNullOldObject() throws Exception {
		dManager.changed(null, null);
	}

	/**
	 * Cannot notify listeners of change with object with <code>null</code>
	 * source.
	 * 
	 * @throws Exception failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void cannotChangedWithNullNewObject() throws Exception {
		dManager.changed(this, null);
	}

	/**
	 * Cannot add veto listener with <code>null</code> source.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void cannotAddNullVetoListener() {
		dManager.addVetoListener(null);
	}

	/**
	 * Cannot add listener with <code>null</code> source.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void cannotAddNullListener() {
		dManager.addListener(null);
	}

	/**
	 * Cannot remove veto listener with <code>null</code> source.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void cannotRemoveNullVetoListener() {
		dManager.removeVetoListener(null);
	}

	/**
	 * Cannot remove listener with <code>null</code> source.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void cannotRemoveNullListener() {
		dManager.removeListener(null);
	}
}
