package incubator.ctxaction;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import incubator.ctxaction.NodeContextTreeModel;

/**
 * Tree model used for tests.
 */
class TestTreeModel extends DefaultTreeModel
		implements NodeContextTreeModel {
	private static final long serialVersionUID = 1L;

	public TestTreeModel(Object data[]) {
		super(buildRoot(data));
	}
	
	private static DefaultMutableTreeNode buildRoot(Object data[]) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(data[0]);
		buildChildren(root, data);
		return root;
	}
	
	private static void buildChildren(DefaultMutableTreeNode parent,
			Object data[]) {
		for (int i = 1; i < data.length; i++) {
			Object d = data[i];
			DefaultMutableTreeNode node;
			if (d instanceof Object []) {
				node = new DefaultMutableTreeNode(((Object []) d)[0]);
			} else {
				node = new DefaultMutableTreeNode(d);
			}
			
			parent.add(node);
			
			if (d instanceof Object []) {
				buildChildren(node, (Object []) d);
			}
		}
	}
	
	TreePath getPath(Object ...path) {
		DefaultMutableTreeNode curr = null;
		for (Object p : path) {
			if (curr == null) {
				curr = (DefaultMutableTreeNode) getRoot();
				continue;
			}
			
			for (int i = 0; i < curr.getChildCount(); i++) {
				DefaultMutableTreeNode chld =
						(DefaultMutableTreeNode) curr.getChildAt(i);
				boolean ok = false;
				if (chld.getUserObject() == null && p == null) {
					ok = true;
				}
				
				if (chld.getUserObject() != null
						&& chld.getUserObject().equals(p)) {
					ok = true;
				}
				
				if (ok) {
					curr = chld;
				}
			}
		}
		
		/*
		 * We know curr != null but we have to test because otherwise the
		 * compiler will complain.
		 */
		assert curr != null;
		if (curr == null) {
			throw new AssertionError("curr == null");
		}
		
		return new TreePath(curr.getPath());
	}

	@Override
	public Object getTreeContextObject(TreePath path) {
		return ((DefaultMutableTreeNode)
				path.getLastPathComponent()).getUserObject();
	}
}
