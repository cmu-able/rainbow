/**
 * Created March 15, 2006
 */
package org.sa.rainbow.stitch.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sa.rainbow.stitch.util.Tool;
import org.sa.rainbow.stitch.visitor.Stitch;

/**
 * Implements a namespace scope to contain references.  Construction of a scope
 * object requires a parent scope, where a null indicates a root scope.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class ScopedEntity implements IScope {

	/** Expression access index that reflects the currently active expression index */
	public int curExprIdx = 0;

	protected String m_name = null;
	protected int m_level = 0;
	protected Stitch m_stitch = null;
	protected IScope m_parent = null;
	protected boolean m_distinctScope = true;  // true means this is a distinctive scope
	protected boolean m_hasError = false;
	protected boolean m_hasErrorHandler = false;

	protected List<IScope> m_children = null;
	// all vars in scope
	protected Map<String,Var> m_vars = null;
	// all expressions in scope
	protected List<Expression> m_expressions = null;
	// for compound statements, must be stored in sequence
	protected List<Statement> m_statements = null;

	/**
	 * Constructor of a scope, requiring a parent scope.
	 * 
	 * @param parent  the parent scope, or <code>null</code> if this is to be the root scope
	 * @param name    the name, if any, of this scope
	 */
	public ScopedEntity (IScope parent, String name, Stitch stitch) {
		m_name = name;
		m_stitch = stitch;
		m_parent = parent;
		m_children = new ArrayList<IScope>();
		m_vars = new HashMap<String,Var>();
		m_expressions = new ArrayList<Expression>();
		m_statements = new ArrayList<Statement>();
		
		if (parent == null) {
			m_level = 1;  // null is the 0-level scope, so this is 1
		} else {
			parent.addChildScope(this);
			m_level = parent.getLevel() + 1;
		}
	}

	/**
	 * Clones a ScopedEntity object, but without deep-copying collections.
	 */
	public ScopedEntity clone () {
		ScopedEntity newObj = new ScopedEntity(m_parent, m_name, m_stitch);
		copyState(newObj);
		return newObj;
	}

	/**
	 * Copy the states of this object into target object.
	 * @param target
	 */
	protected void copyState (ScopedEntity target) {
		target.m_name = m_name;
		target.m_stitch = m_stitch;
		target.m_parent = m_parent;
		target.m_level = m_level;
		target.m_distinctScope = m_distinctScope;
		target.m_hasErrorHandler = m_hasErrorHandler;
		target.m_hasError = m_hasError;
		target.curExprIdx = curExprIdx;
		target.m_children = new ArrayList<IScope> (m_children);
		target.m_vars = new HashMap<String,Var> (m_vars);
		target.m_expressions = new ArrayList<Expression> (m_expressions);
		target.m_statements = new ArrayList<Statement> (m_statements);
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#getName()
	 */
	public String getName () {
		return m_name;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#getQualifiedName()
	 */
	public String getQualifiedName () {
		if (stitch().script != null && stitch().script != this) {
			return stitch().script.getName() + "." + getName();
		} else {
			return getName();
		}
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#setName(java.lang.String)
	 */
	public void setName (String name) {
		m_name = name;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#getLevel()
	 */
	public int getLevel () {
		return m_level;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#setLevel(int)
	 */
	public void setLevel (int l) {
		m_level = l;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#isRoot()
	 */
	public boolean isRoot () {
		return m_parent == null;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#isDistinctScope()
	 */
	public boolean isDistinctScope () {
		return m_distinctScope;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#setDistinctScope(boolean)
	 */
	public void setDistinctScope (boolean b) {
		m_distinctScope = b;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#hasError()
	 */
	public boolean hasError () {
		return m_hasError;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#setError(boolean)
	 */
	public void setError (boolean b) {
		// sets failure flag on all enclosing scopes until one with failure
		// handler is found, or no more parent
		m_hasError = b;
		if (b && !(hasErrorHandler())) {
			if (parent() != null) {
				parent().setError(b);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#hasErrorHandler()
	 */
	public boolean hasErrorHandler () {
		return m_hasErrorHandler;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#setHasErrorHandler(boolean)
	 */
	public void setHasErrorHandler (boolean b) {
		m_hasErrorHandler = b;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#stitch()
	 */
	public Stitch stitch () {
		return m_stitch;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#setStitch(org.sa.rainbow.stitch.visitor.Stitch)
	 */
	public void setStitch (Stitch s) {
		m_stitch = s;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#moveUp()
	 */
	public IScope parent () {
		return m_parent;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#vars()
	 */
	public Map<String, Var> vars () {
		return isDistinctScope() ? m_vars : parent().vars();
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#expressions()
	 */
	public List<Expression> expressions () {
		// no checking for distinct scope when expressions
		return m_expressions;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#statements()
	 */
	public List<Statement> statements () {
		return m_statements;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#addChildScope(org.sa.rainbow.stitch.core.IScope)
	 */
	public void addChildScope (IScope child) {
		m_children.add(child);
		
		if (child.parent() != null && child.parent() != this) {
			Tool.logger().error("addChildScope: Did you confuse the scope or forget to set the proper parent?");
		}
	}

	/**
	 * Returns the children scope objects.
	 * This method is meant to be used internally, thus not exposed via IScope interface.
	 * @return
	 */
	public List<IScope> getChildren () {
		return m_children;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#addVar(antlr.collections.AST, org.sa.rainbow.stitch.core.Var)
	 */
	public boolean addVar (String id, Var var) {
		boolean rv = true;
		if (isDistinctScope()) {
			if (m_vars.containsKey(id)) {
				rv = false;  // cannot add variable
			} else {
				m_vars.put(id, var);
			}
		} else {
			if (parent() != null) {
				rv = parent().addVar(id, var);
			}
		}

		return rv;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#addExpression(org.sa.rainbow.stitch.core.Expression)
	 */
	public void addExpression (Expression expr) {
		// no checking for distinct scope when adding expression
		m_expressions.add(expr);
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#addStatement(org.sa.rainbow.stitch.core.Statement)
	 */
	public void addStatement (Statement stmt) {
		// no checking for distinct scope when adding expression
		m_statements.add(stmt);
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#lookup(java.lang.String)
	 */
	public Object lookup (String name) {
		if (name == null) return null;

		// by default, we search for variable declarations of this name
		Object v = vars().get(name);
		if (v == null && parent() != null) {
			// move out the scope
			v = parent().lookup(name);
		}
		return v;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString () {
		String parentName = null;
		if (m_parent != null) parentName = m_parent.getName();
		return "ScopedEntity \"" + m_name + "\", parent " + parentName;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#toStringTree()
	 */
	public String toStringTree () {
		String str = leadPadding("  ");
		str += (isDistinctScope() ? "*" : "") + getName() + " [e# ";
		str += m_expressions.size() + ", s# " + m_statements.size() + "] (";
		str += m_vars.toString() + ") {";
		for (IScope c : m_children) {
			str += "\n" + c.toStringTree();
		}
		str += "\n" + leadPadding("  ") + "}";
		return str;
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.IScope#leadPadding(java.lang.String)
	 */
	public String leadPadding (String padder) {
		if (padder == null) padder = " ";  // just use a single space by default
		StringBuffer spBuf = new StringBuffer();
		for (int i=0; i < getLevel()-1; i++) {
			spBuf.append(padder);
		}
		// last seg has a space
		if (getLevel() > 0) spBuf.append(padder.substring(0, padder.length()-1) + " ");
		return spBuf.toString();
	}

}
