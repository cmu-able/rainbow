package incubator.ctxaction;

import javax.swing.JTree;

import org.junit.Assert;
import org.junit.Test;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.ActionContextTreeSynchronizer;
import incubator.ctxaction.NodeContextTreeModel;

/**
 * Robustness tests for the {@link ActionContextTreeSynchronizer} class.
 */
public class ActionContextTreeSynchronizerRbTest extends Assert {
	/**
	 * Cannot create a synchronizer with a <code>null</code> tree.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception test failed
	 */
	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void cannotCreateWithNullTree() throws Exception {
		new ActionContextTreeSynchronizer(null, new ActionContext(), "foo");
	}
	
	/**
	 * Cannot create a synchronizer with a <code>null</code> context.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception test failed
	 */
	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void cannotCreateWithNullContext() throws Exception {
		new ActionContextTreeSynchronizer(new JTree(new TestTreeModel(
				new Object[] { "root" } )), null, "foo");
	}
	
	/**
	 * Cannot create a synchronizer with a <code>null</code> key.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception test failed
	 */
	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void cannotCreateWithNullKey() throws Exception {
		new ActionContextTreeSynchronizer(new JTree(new TestTreeModel(
				new Object[] { "root" } )), new ActionContext(), null);
	}
	
	/**
	 * Cannot create a synchronizer with a tree whose model does not
	 * implement {@link NodeContextTreeModel}.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception test failed
	 */
	@SuppressWarnings("unused")
	@Test(expected = IllegalArgumentException.class)
	public void cannotCreateWithModelNotImplementingInterface()
			throws Exception {
		new ActionContextTreeSynchronizer(new JTree(), new ActionContext(),
				"foo");
	}
}
