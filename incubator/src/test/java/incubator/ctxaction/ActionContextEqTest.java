package incubator.ctxaction;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import incubator.ctxaction.ActionContext;

/**
 * Equivalent class tests for the action context class.
 */
public class ActionContextEqTest extends Assert {
	/**
	 * Prepares the tests for execution (disables the AWT thread check).
	 */
	@Before
	public void setup() {
		ActionContext.disableAwtThreadCheck();
	}

	/**
	 * Adding and getting values can be done (the correct values are
	 * received) and listeners are informed of the changes.
	 * 
	 * @throws Exception test failed
	 */
	@Test
	public void addingGettingValuesWithListeners() throws Exception {
		ActionContext ac = new ActionContext();
		TestActionContextListener l = new TestActionContextListener();
		ac.addActionContextListener(l);
		ac.set("foo", "bar");
		assertEquals(1, l.invocations.size());
		assertEquals(ac, l.invocations.get(0));
		assertEquals("bar", ac.get("foo"));
	}

	/**
	 * Several values can be set at the same time and listeners are only
	 * informed once.
	 * 
	 * @throws Exception test failed
	 */
	@Test
	public void settingMultipleAtOnce() throws Exception {
		ActionContext ac = new ActionContext();
		TestActionContextListener l = new TestActionContextListener();
		ac.addActionContextListener(l);
		ac.redefine(new String[] {"foo", "bar"}, new Object[] {"x1", "x2"});
		assertEquals(1, l.invocations.size());
		assertEquals(ac, l.invocations.get(0));
		assertEquals("x1", ac.get("foo"));
		assertEquals("x2", ac.get("bar"));
	}

	/**
	 * Values of the context can be redefined and listeners are informed.
	 * 
	 * @throws Exception test failed
	 */
	@Test
	public void redefiningValues() throws Exception {
		ActionContext ac = new ActionContext();
		TestActionContextListener l = new TestActionContextListener();
		ac.addActionContextListener(l);
		ac.set("foo", "bar");
		ac.set("foo", "foobar");
		assertEquals(2, l.invocations.size());
		assertEquals(ac, l.invocations.get(1));
		assertEquals("foobar", ac.get("foo"));
	}

	/**
	 * The whole context can be cleared and listeners are informed.
	 * 
	 * @throws Exception test failed
	 */
	@Test
	public void clearingTheContext() throws Exception {
		ActionContext ac = new ActionContext();
		ac.set("foo", "bar");
		TestActionContextListener l = new TestActionContextListener();
		ac.addActionContextListener(l);
		assertEquals(0, l.invocations.size());
		ac.clear();
		assertEquals(1, l.invocations.size());
		assertEquals(ac, l.invocations.get(0));
	}

	/**
	 * Context can be copied from one context to another but changes made
	 * afterwards are independent. Listeners are informed of the copy.
	 * 
	 * @throws Exception test failed
	 */
	@Test
	public void copyingContext() throws Exception {
		ActionContext ac = new ActionContext();
		ActionContext accopy = new ActionContext();
		ac.set("foo", "bar");
		TestActionContextListener l = new TestActionContextListener();
		accopy.addActionContextListener(l);
		accopy.redefine(ac);
		assertEquals(1, l.invocations.size());
		assertEquals(accopy, l.invocations.get(0));
		ac.set("xxx", "yyy");
		assertNull(accopy.get("xxx"));
		assertEquals("yyy", ac.get("xxx"));
		assertEquals(1, l.invocations.size());
		assertEquals(accopy, l.invocations.get(0));
	}
}
