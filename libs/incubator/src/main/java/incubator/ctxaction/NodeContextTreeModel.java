package incubator.ctxaction;

import javax.swing.tree.TreePath;

/**
 * Interface implemented by tree models that provide a mapping between nodes
 * and objects.
 */
public interface NodeContextTreeModel {
	/**
	 * Obtains the context object of a tree node.
	 * 
	 * @param path the path to the tree node
	 * 
	 * @return the object associated with the path (can return
	 * <code>null</code>)
	 */
	Object getTreeContextObject(TreePath path);
}
