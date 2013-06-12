package incubator.ctxaction;

import javax.swing.JTable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.ActionContextTableSynchronizer;

/**
 * Equivalence class tests for the {@link ActionContextTableSynchronizer}
 * class.
 */
public class ActionContextTableSynchronizerEqTest extends Assert {
	/**
	 * Table.
	 */
	private JTable table;

	/**
	 * Table model data.
	 */
	private Object data[];

	/**
	 * Action context.
	 */
	private ActionContext context;

	/**
	 * Prepares the test.
	 */
	@Before
	public void setup() {
		ActionContext.disableAwtThreadCheck();
		data = new Object[] {new Object(), new Object(), new Object()};
		table = new JTable(new TestTableModel(data));
		context = new ActionContext();
	}

	/**
	 * If selection changes to single row, multiple rows and then no rows,
	 * the context is updated.
	 * 
	 * @throws Exception test failed
	 */
	@Test
	public void selectionChangesUpdatesContext() throws Exception {
		new ActionContextTableSynchronizer(table, context, "foo");
		table.getSelectionModel().setSelectionInterval(1, 1);
		assertEquals(data[1], context.get("foo"));

		table.getSelectionModel().setSelectionInterval(0, 1);
		Object cv[] = (Object[]) context.get("foo");
		assertEquals(2, cv.length);
		assertEquals(data[0], cv[0]);
		assertEquals(data[1], cv[1]);

		table.getSelectionModel().clearSelection();
		assertNull(context.get("foo"));
	}

	/**
	 * When the synchronizer is initialized, if the table contains a
	 * selection already made, the context is immediately updated.
	 * 
	 * @throws Exception test failed
	 */
	@Test
	public void selectionIsSetAtStart() throws Exception {
		table.getSelectionModel().setSelectionInterval(2, 2);
		assertNull(context.get("foo"));
		new ActionContextTableSynchronizer(table, context, "foo");
		assertEquals(data[2], context.get("foo"));
	}

	/**
	 * The row selection model may return <code>null</code> for some
	 * selected rows. This should be tested with 1 and multiple rows
	 * selected.
	 * 
	 * @throws Exception test failed
	 */
	@Test
	public void someRowsMayReturnNullObject() throws Exception {
		data[0] = null;
		new ActionContextTableSynchronizer(table, context, "foo");
		assertNull(context.get("foo"));

		table.getSelectionModel().setSelectionInterval(1, 1);
		assertEquals(data[1], context.get("foo"));

		table.getSelectionModel().setSelectionInterval(0, 0);
		assertNull(context.get("foo"));
	}
}
