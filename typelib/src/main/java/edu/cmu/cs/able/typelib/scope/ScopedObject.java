package edu.cmu.cs.able.typelib.scope;

import incubator.pval.Ensure;

/**
 * <p>A scoped object is a named object that can be placed inside a scope. A
 * scoped object can only belong to one scope. Scoped objects are placed on a
 * scope at construction and cannot change their scope during their lifetime.
 * Their name is also immutable.</p>
 * <p>Scoped object do not give access to the scope they belong to. This
 * would make preventing modification of scopes.</p>
 * <p>Scoped objects can be inserted in a scope and will live in that scope
 * until garbage collected.</p>
 */
public class ScopedObject {
	/**
	 * The name of the object inside the scope.
	 */
	private String m_name;
	
	/**
	 * The scope this object is registered in, <code>null</code> if none.
	 */
	private Scope<?> m_scope;
	
	/**
	 * Creates a new scoped object that is not registered in any scope.
	 * @param name the scoped object's name
	 */
	public ScopedObject(String name) {
		Ensure.not_null(name);
		
		m_name = name;
	}
	
	/**
	 * Registers this object as belonging to the given scope. This method is
	 * invoked by the scope when objects are added.
	 * @param scope the scope
	 */
	final void register(Scope<?> scope) {
		Ensure.not_null(scope);
		
		// If this is false, object already belongs in a scope.
		Ensure.is_null(m_scope);
		
		check_register(scope);
		m_scope = scope;
	}
	
	/**
	 * Performs some extra checks before registering the object in a scope.
	 * The <code>ScopedObject</code>'s checks have already been performed.
	 * @param scope the scope
	 */
	protected void check_register(Scope<?> scope) {
		/*
		 * Hook method.
		 */
	}
	
	/**
	 * Obtains the name of the object.
	 * @return the name of the object
	 */
	public final String name() {
		return m_name;
	}
	
	/**
	 * Obtains an absolute hierarchical name for this object. The name uniquely
	 * identifies this object in the hierarchy. This method may not be invoked
	 * in objects which do not belong to a scope, that is to objects whose
	 * {@link #parent()} method returns <code>null</code>.
	 * @return the returned hierarchical name which is an absolute hierarchical
	 * name
	 */
	public HierarchicalName absolute_hname() {
		Ensure.not_null(m_scope);
		HierarchicalName h = m_scope.absolute_hname();
		
		if (h == null) {
			h = new HierarchicalName(true, m_name);
		} else {
			h = h.push(m_name);
		}
		
		return h;
	}
	
	/**
	 * Obtains the scope that contains this object.
	 * @return the scope or <code>null</code> if none
	 */
	public final Scope<?> parent() {
		return m_scope;
	}
	
	/**
	 * Checks whether this object is inside the given scope, either directly
	 * or indirectly.
	 * @param scope the scope
	 * @return <code>true</code> if either <code>scope</code> or one of its
	 * parent scopes is the scope containing this object
	 */
	public final boolean in_scope(Scope<?> scope) {
		if (m_scope == null) {
			return false;
		}
		
		Ensure.not_null(scope);
		for (Scope<?> c = m_scope; c != null; c = c.parent()) {
			if (c == scope) {
				return true;
			}
		}
		
		return false;
	}
}
