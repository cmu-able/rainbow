package incubator.ctxaction;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.CompositeActionContext;

/**
 * Equivalence class tests for the {@link CompositeActionContext} class.
 */
public class CompositeActionContextEqTest extends Assert {
	/**
	 * Disabled the AWT thread check.
	 */
	@Before
	public void setup() {
		ActionContext.disableAwtThreadCheck();
	}

	/**
	 * After creating a composite action context, creating several contexts
	 * and adding them should make the composite change its context
	 * automatically when action contexts change.
	 * 
	 * @throws Exception test failed
	 */
	@Test
	public void addingActionContextsAndChangingContexts() throws Exception {
		CompositeActionContext cac = new CompositeActionContext();
		assertNull(cac.get("foo"));
		assertNull(cac.get("bar"));

		ActionContext ac1 = new ActionContext();
		Object o1 = new Object();
		ac1.set("foo", o1);
		cac.addActionContext(ac1);
		assertEquals(o1, cac.get("foo"));
		assertNull(cac.get("bar"));

		ActionContext ac2 = new ActionContext();
		Object o2 = new Object();
		ac2.set("bar", o2);
		cac.addActionContext(ac2);
		assertEquals(o1, cac.get("foo"));
		assertEquals(o2, cac.get("bar"));

		Object o3 = new Object();
		ac1.set("foo2", o3);
		assertEquals(o3, cac.get("foo2"));

		Object o4 = new Object();
		ac2.set("bar2", o4);
		assertEquals(o4, cac.get("bar2"));
	}

	/**
	 * Action contexts which are removed from the composite are not used in
	 * the computation of the composite context.
	 * 
	 * @throws Exception test failed
	 */
	@Test
	public void removedContextsAreNotUsed() throws Exception {
		ActionContext ac1 = new ActionContext();
		Object o1 = new Object();
		ac1.set("foo", o1);

		ActionContext ac2 = new ActionContext();
		Object o2 = new Object();
		ac2.set("bar", o2);

		CompositeActionContext cac = new CompositeActionContext();
		cac.addActionContext(ac1);
		cac.addActionContext(ac2);

		assertEquals(o1, cac.get("foo"));

		cac.removeActionContext(ac1);
		assertNull(cac.get("foo"));
		assertEquals(o2, cac.get("bar"));
	}

	/**
	 * Properties can be set directly in the composite context and will be
	 * kept even if other contexts are added or removed.
	 * 
	 * @throws Exception failed
	 */
	@Test
	public void canSetItemsDirectly() throws Exception {
		ActionContext ac1 = new ActionContext();
		Object o1 = new Object();
		ac1.set("foo", o1);

		ActionContext ac2 = new ActionContext();
		Object o2 = new Object();
		ac2.set("bar", o2);

		CompositeActionContext cac = new CompositeActionContext();
		cac.addActionContext(ac1);
		Object o3 = new Object();
		cac.set("xpto", o3);
		cac.addActionContext(ac2);

		assertEquals(o1, cac.get("foo"));
		assertEquals(o2, cac.get("bar"));
		assertEquals(o3, cac.get("xpto"));

		cac.removeActionContext(ac1);
		assertNull(cac.get("foo"));
		assertEquals(o2, cac.get("bar"));
		assertEquals(o3, cac.get("xpto"));

		cac.removeActionContext(ac2);
		assertNull(cac.get("foo"));
		assertNull(cac.get("bar"));
		assertEquals(o3, cac.get("xpto"));
	}
}
