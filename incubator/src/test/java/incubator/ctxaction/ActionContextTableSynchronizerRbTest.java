package incubator.ctxaction;

import javax.swing.JTable;

import org.junit.Assert;
import org.junit.Test;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.ActionContextTableSynchronizer;
import incubator.ctxaction.RowContextTableModel;

/**
 * Robustness tests for the {@link ActionContextTableSynchronizer} class.
 */
public class ActionContextTableSynchronizerRbTest extends Assert {
	/**
	 * Cannot create a synchronizer with a <code>null</code> table.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void cannotCreateWithNullTable() throws Exception {
		new ActionContextTableSynchronizer(null, new ActionContext(), "foo");
	}
	
	/**
	 * Cannot create a synchronizer with a <code>null</code> context.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void cannotCreateWithNullContext() throws Exception {
		new ActionContextTableSynchronizer(new JTable(new TestTableModel(null)),
				null, "foo");
	}
	
	/**
	 * Cannot create a synchronizer with a <code>null</code> key.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void cannotCreateWithNullKey() throws Exception {
		new ActionContextTableSynchronizer(new JTable(new TestTableModel(null)),
				new ActionContext(), null);
	}
	
	/**
	 * Cannot create a synchronizer with a table whose model does not
	 * implement {@link RowContextTableModel}.
	 * 
	 * @throws IllegalArgumentException expected
	 * @throws Exception test failed
	 */
	@Test(expected = IllegalArgumentException.class)
	public void cannotCreateWithModelNotImplementingInterface()
			throws Exception {
		new ActionContextTableSynchronizer(new JTable(), new ActionContext(),
				"foo");
	}
}
