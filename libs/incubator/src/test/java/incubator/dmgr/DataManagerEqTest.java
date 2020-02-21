package incubator.dmgr;

import java.beans.PropertyVetoException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Equivalence class testing for the dmgr API.
 */
public class DataManagerEqTest extends Assert {
	/**
	 * Normal listener to use in tests (does not veto).
	 */
	private TestEntityListener listener;

	/**
	 * Veto listener to use during the tests.
	 */
	private TestVetoPropertyListener vetoListener;

	/**
	 * Data manager to use in tests.
	 */
	private DataManager dManager;

	/**
	 * Prepares the test fixture.
	 */
	@Before
	public void setUp() {
		listener = new TestEntityListener();
		vetoListener = new TestVetoPropertyListener();
		dManager = new DataManager();
	}

	/**
	 * Cleans up the test fixture.
	 */
	@After
	public void tearDown() {
		listener = null;
		vetoListener = null;
		dManager = null;
	}

	/**
	 * Verifies that all operations occur with success when there are no
	 * listeners.
	 * 
	 * @throws Exception failed
	 */
	@Test
	public void callDataManagerWithoutListeners() throws Exception {
		String test = "Test";

		// Verifies that can add a new object with success
		dManager.canAdd(test);

		// Adds a new object with success
		dManager.added(test);

		// Verify that can remove an object with success
		dManager.canRemove(test);

		// Removes an object with success
		dManager.removed(test);

		// Verify that can update an object with success
		dManager.canChange(test, test);

		// Inform that an object was updated with success
		dManager.changed(test, test);
	}

	/**
	 * Verifies that all operations occur with success when there are at
	 * least one veto listener and one listener.
	 * 
	 * @throws Exception failed
	 */
	@Test
	public void callDataManagerWithListeners() throws Exception {
		String oldTest = "oldTest";
		String newTest = "newTest";

		// Add listeners
		dManager.addListener(listener);
		dManager.addVetoListener(vetoListener);

		// Verifies that can add a new object with success
		dManager.canAdd(newTest);

		// Adds a new object with success
		dManager.added(newTest);

		// Verify that can remove an object with success
		dManager.canRemove(newTest);

		// Removes an object with success
		dManager.removed(newTest);

		// Verify that can update an object with success
		dManager.canChange(oldTest, newTest);

		// Inform that an object was updated with success
		dManager.changed(oldTest, newTest);

		assertEquals(3, vetoListener.numCalls);
		assertEquals(3, listener.numCalls);

		// Verify that it does not call any listener when the object to
		// update is the same
		dManager.canChange(oldTest, oldTest);

		// Does not inform any listener when the object to
		// update is the same
		dManager.changed(oldTest, oldTest);

		assertEquals(3, vetoListener.numCalls);
		assertEquals(3, listener.numCalls);

		// Remove listeners
		dManager.removeListener(listener);
		dManager.removeVetoListener(vetoListener);
	}

	/**
	 * Verifies that with specific logic the operation is veto by the veto
	 * listener.
	 * 
	 * @throws Exception failed
	 */
	@Test
	public void callDataManagerAndOpIsVeto() throws Exception {
		String oldTest = "oldTest";
		String newTest = "newTest";

		// Add veto listener
		dManager.addVetoListener(vetoListener);

		// Verify that the remove is veto by the listener
		try {
			dManager.canRemove(oldTest);
			fail();
		} catch (PropertyVetoException e) {
			/*
			 * Expected.
			 */
		}

		// Verifies that can add a new object with success
		dManager.canAdd(newTest);

		// Verify that the update is veto by the listener
		try {
			dManager.canChange(oldTest, newTest);
			fail();
		} catch (PropertyVetoException e) {
			/*
			 * Expected.
			 */
		}
		assertEquals(3, vetoListener.numCalls);

		// Verify that can update an object with success
		try {
			dManager.canChange(oldTest, newTest);
			fail();
		} catch (PropertyVetoException e) {
			/*
			 * Expected.
			 */
		}
		assertEquals(4, vetoListener.numCalls);
	}
}
