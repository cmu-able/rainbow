package incubator.ctxaction;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * Class which is able to keep an action context synchronized with selected
 * nodes on a tree. The class will keep a value on the context defined if
 * any node is selected. The key is defined in this class and the value is
 * the object provided by the model. If several nodes are selected, an array
 * is defined in the context (instead of a single value).
 */
public class ActionContextTreeSynchronizer {
	/**
	 * The tree model.
	 */
	private final NodeContextTreeModel model;

	/**
	 * The tree.
	 */
	private final JTree tree;

	/**
	 * The context to synchronize.
	 */
	private final ActionContext context;

	/**
	 * Context key to keep updated.
	 */
	private final String key;

	/**
	 * Creates a new synchronizer.
	 * 
	 * @param tree the tree. The tree model must implement the
	 * {@link NodeContextTreeModel} interface
	 * @param context the action context to keep synchronized
	 * @param key the context key to use
	 */
	public ActionContextTreeSynchronizer(JTree tree, ActionContext context,
			String key) {
		if (tree == null) {
			throw new IllegalArgumentException("tree == null");
		}

		if (context == null) {
			throw new IllegalArgumentException("context == null");
		}

		if (key == null) {
			throw new IllegalArgumentException("key == null");
		}

		this.tree = tree;
		this.context = context;
		this.key = key;

		TreeModel model = tree.getModel();
		if (model == null || !(model instanceof NodeContextTreeModel)) {
			throw new IllegalArgumentException(
					"Tree model must implement the "
							+ "NodeContextTreeModel interface.");
		}

		this.model = (NodeContextTreeModel) model;

		TreeSelectionListener listener = new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				synchronize();
			}
		};

		this.tree.getSelectionModel().addTreeSelectionListener(listener);
		synchronize();
	}

	/**
	 * Synchronizes the context with the table selection.
	 */
	private void synchronize() {
		TreePath[] paths = tree.getSelectionPaths();
		if (paths == null || paths.length == 0) {
			context.clear(key);
			return;
		}

		Object[] data = new Object[paths.length];
		for (int i = 0; i < data.length; i++) {
			data[i] = model.getTreeContextObject(paths[i]);
		}

		if (data.length == 1) {
			context.set(key, data[0]);
			return;
		}

		context.set(key, data);
	}

	/**
	 * This method does nothing but prevents checkstyle from complaining.
	 */
	public void dummy() {
		/*
		 * No code here. Dummy method.
		 */
	}
}
