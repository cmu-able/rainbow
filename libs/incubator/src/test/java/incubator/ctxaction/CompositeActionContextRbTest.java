package incubator.ctxaction;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.CompositeActionContext;

/**
 * Robustness tests for the composite action class.
 */
public class CompositeActionContextRbTest extends Assert {
	/**
	 * Disables the AWT test check.
	 */
	@Before
	public void setup() {
		ActionContext.disableAwtThreadCheck();
	}

	/**
	 * Cannot add a <code>null</code> context.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void addNullContext() throws Exception {
		CompositeActionContext cac = new CompositeActionContext();
		cac.addActionContext(null);
	}

	/**
	 * Cannot remove a <code>null</code> context.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void removeNullContext() throws Exception {
		CompositeActionContext cac = new CompositeActionContext();
		cac.removeActionContext(null);
	}

	/**
	 * Cannot remove a context which has not been added.
	 * 
	 * @throws IllegalStateException expected
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalStateException.class)
	public void removeNotAddedContext() throws Exception {
		CompositeActionContext cac = new CompositeActionContext();
		ActionContext ac1 = new ActionContext();
		ActionContext ac2 = new ActionContext();
		cac.addActionContext(ac1);
		cac.removeActionContext(ac2);
	}
}
