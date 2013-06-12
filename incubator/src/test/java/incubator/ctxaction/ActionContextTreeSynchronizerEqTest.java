package incubator.ctxaction;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.ActionContextTreeSynchronizer;

/**
 * Equivalence class tests for the {@link ActionContextTreeSynchronizer}
 * class.
 */
public class ActionContextTreeSynchronizerEqTest extends Assert {
	/**
	 * Tree.
	 */
	private JTree tree;

	/**
	 * Action context.
	 */
	private ActionContext context;

	/**
	 * Test path 1.
	 */
	private TreePath fooPath;

	/**
	 * Test path 2.
	 */
	private TreePath baryPath;

	/**
	 * Test path 3.
	 */
	private TreePath nullPath;

	/**
	 * Prepares the test.
	 */
	@Before
	public void setup() {
		ActionContext.disableAwtThreadCheck();
		Object data[] = new Object[] {"root", "foo",
				new Object[] {"barr", "bary", "goo"}, "fuy", null};
		TestTreeModel ttm = new TestTreeModel(data);
		tree = new JTree(ttm);
		context = new ActionContext();

		fooPath = ttm.getPath("root", "foo");
		baryPath = ttm.getPath("root", "barr", "bary");
		nullPath = ttm.getPath("root", null);
	}

	/**
	 * If selection changes to single node, multiple nodes and then no
	 * nodes, the context is updated.
	 * 
	 * @throws Exception test failed
	 */
	@Test
	public void selectionChangesUpdatesContext() throws Exception {
		new ActionContextTreeSynchronizer(tree, context, "foo");
		tree.getSelectionModel().setSelectionPath(fooPath);
		assertEquals("foo", context.get("foo"));

		tree.getSelectionModel().setSelectionPaths(
				new TreePath[] {fooPath, baryPath});

		Object cv[] = (Object[]) context.get("foo");
		assertEquals(2, cv.length);
		assertEquals("foo", cv[0]);
		assertEquals("bary", cv[1]);

		tree.getSelectionModel().clearSelection();
		assertNull(context.get("foo"));
	}

	/**
	 * When the synchronizer is initialized, if the tree contains a
	 * selection already made, the context is immediately updated.
	 * 
	 * @throws Exception test failed
	 */
	@Test
	public void selectionIsSetAtStart() throws Exception {
		tree.getSelectionModel().setSelectionPath(fooPath);
		assertNull(context.get("foo"));
		new ActionContextTreeSynchronizer(tree, context, "foo");
		assertEquals("foo", context.get("foo"));
	}

	/**
	 * The tree selection model may return <code>null</code> for some
	 * selected nodes. This should be tested with 1 and multiple rows
	 * selected.
	 * 
	 * @throws Exception test failed
	 */
	@Test
	public void someRowsMayReturnNullObject() throws Exception {
		new ActionContextTreeSynchronizer(tree, context, "foo");
		assertNull(context.get("foo"));

		tree.getSelectionModel().setSelectionPath(fooPath);
		assertEquals("foo", context.get("foo"));

		tree.getSelectionModel().setSelectionPath(nullPath);
		assertNull(context.get("foo"));
	}
}
