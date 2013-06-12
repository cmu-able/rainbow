package edu.cmu.cs.able.typelib.scope;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import incubator.Pair;
import incubator.pval.Ensure;

/**
 * <p>A scope contains several objects with distinct names. Scopes can be used
 * to implement structures, families and other entities that map names to
 * entities.</p>
 * 
 * <p>Scopes can be arranged hierarchically. In that case, each scope has a
 * name within the parent scope. The name space is shared between scopes and
 * objects so names must be distinct. Scopes cannot form cycles and each scope
 * must be placed at most inside another scope.</p>
 * 
 * <p>Scopes can have linked scopes. A linked scope will be searched after the
 * scope if no object is found. If an object is not found then at maximum
 * one linked scope may find it. Linked scopes can be used to implement imports
 * of name spaces (like field inheritance in structures). Finding an object in
 * two linked scopes reveals ambiguity (specifying it in the current scope is
 * a way to resolve the ambiguity). Linked scopes must form an acyclic
 * graph.</p>
 * 
 * <p>It is not considered ambiguous if several several linked scopes find
 * the <em>same</em> object. It is only considered ambiguous if they find
 * <em>different</em> objects under the same name.</p>
 * 
 * @param <T> the type of objects in the scope
 */
public class Scope<T extends ScopedObject> {
	/**
	 * The scope's name.
	 */
	private String m_name;
	
	/**
	 * The scope's parent.
	 */
	private Scope<T> m_parent;
	
	/**
	 * All objects in the scope mapped by their names.
	 */
	private Map<String, T> m_objects;
	
	/**
	 * Sub-scopes of this scope.
	 */
	private Map<String, Scope<T>> m_sub_scopes;
	
	/**
	 * Linked scopes.
	 */
	private Set<Scope<T>> m_linked;
	
	/**
	 * Creates a new scope.
	 * @param name the name of the scope; this can be <code>null</code> if
	 * this scope will never be added to another scope; the empty string
	 * is not allowed as a scope name
	 */
	public Scope(String name) {
		Ensure.is_true(name == null || name.length() > 0);
		
		m_name = name;
		m_objects = new HashMap<>();
		m_sub_scopes = new HashMap<>();
		m_linked = new HashSet<>();
		m_parent = null;
	}
	
	/**
	 * Obtains the name of the scope.
	 * @return the name or <code>null</code> if this scope doesn't have one
	 */
	public final String name() {
		return m_name;
	}
	
	/**
	 * Obtains the parent of the scope.
	 * @return the parent or <code>null</code> if there is none
	 */
	public final Scope<T> parent() {
		return m_parent;
	}
	
	/**
	 * Obtains the absolute hierarchical name of a scope.
	 * @return the absolute hierarchical name or <code>null</code> if this
	 * scope doesn't have a hierarchical name (it is the root)
	 */
	public final HierarchicalName absolute_hname() {
		if (m_parent == null) {
			return null;
		}
		
		HierarchicalName hn = m_parent.absolute_hname();
		if (hn == null) {
			return new HierarchicalName(true, m_name);
		} else {
			hn = hn.push(m_name);
			return hn;
		}
	}
	
	/**
	 * Adds a scope as a child of another scope. Sub classes may place extra
	 * requirements on which scopes can be added as sub scopes.
	 * @param scope the child to add; note that scopes that further refine
	 * the child type are not allowed (<code>&lt;? extends T&gt;</code>
	 * instead of <code>&lt;T&gt;</code>) or otherwise finding names
	 * above in the hierarchy (which is possible using absolute hierarchical
	 * names) would break the type system
	 */
	public final void add(Scope<T> scope) {
		Ensure.not_null(scope);
		Ensure.is_null(scope.m_parent);
		
		// Unnamed scopes cannot be placed inside other scopes.
		Ensure.not_null(scope.m_name);
		
		if (m_objects.get(scope.m_name) != null) {
			throw new IllegalStateException("Scope already has an object "
					+ "named '" + scope.m_name + "'.");
		}
		
		if (m_sub_scopes.get(scope.m_name) != null) {
			throw new IllegalStateException("Scope already has a sub scope "
					+ "named '" + scope.m_name + "'.");
		}
		
		check_add(scope);
		
		m_sub_scopes.put(scope.m_name, scope);
		scope.m_parent = this;
	}
	
	/**
	 * Invoked when {@link #add(Scope)} is called to check whether the scope
	 * can be added. <code>Scope</code>'s validations have already passed and
	 * this method does nothing. It may be overridden by subclasses to perform
	 * their own extra validation. 
	 * @param scope the scope to add
	 */
	protected void check_add(Scope<T> scope) {
		/*
		 * Hook method.
		 */
	}
	
	/**
	 * Adds an object to the scope. The object must not belong to any other
	 * scope and no other object in this scope can have the same name. Sub
	 * classes may place requirements on which objects can be added to the
	 * scope.
	 * @param obj the object
	 */
	public final void add(T obj) {
		Ensure.not_null(obj);
		Ensure.is_true(obj.name().length() > 0);
		
		if (m_objects.get(obj.name()) != null) {
			throw new IllegalStateException("Scope already has an object "
					+ "named '" + obj.name() + "'.");
		}
		
		if (m_sub_scopes.get(obj.name()) != null) {
			throw new IllegalStateException("Scope already has a sub scope "
					+ "named '" + obj.name() + "'.");
		}
		
		check_add(obj);
		
		obj.register(this);
		m_objects.put(obj.name(), obj);
	}
	
	/**
	 * Invoked when {@link #add(ScopedObject)} is called to check whether the
	 * object can be added. <code>Scope</code>'s validations have already
	 * passed and this method does nothing. It may be overridden by subclasses
	 * to perform their own extra validation.
	 * @param obj the object to add
	 */
	protected void check_add(T obj) {
		/*
		 * Hook method.
		 */
	}
	
	/**
	 * Finds an object in this scope, given its name.
	 * @param name the object's name
	 * @return the object found or <code>null</code> if no object was found
	 * @throws AmbiguousNameException if there is no object in this scope that
	 * matches this name but there are more than one in the linked scopes
	 */
	public T find(String name) throws AmbiguousNameException {
		return find(new HierarchicalName(false, name));
	}
	
	/**
	 * Finds a scope in this scope, given its name.
	 * @param name the scope's name
	 * @return the scope found or <code>null</code> if no scope was found
	 * @throws AmbiguousNameException if there is no scope in this scope that
	 * matches this name but there are more than one in the linked scopes
	 */
	public Scope<T> find_scope(String name) throws AmbiguousNameException {
		return find_scope(new HierarchicalName(false, name));
	}
	
	/**
	 * Finds an object or sub-scope in this scope or sub-scopes given a name.
	 * It also searches linked scopes.
	 * @param hn the object's name
	 * @return a pair containing either the object found or the scope found
	 * or <code>null</code> if none was found;
	 * <code>null</code> is also returned if the hierarchical name refers to
	 * a scope which is not found
	 * @throws AmbiguousNameException if there is no object in this scope that
	 * matches this name but there are more than one in the linked scopes
	 */
	private Pair<T, Scope<T>> search(HierarchicalName hn)
			throws AmbiguousNameException {
		Ensure.not_null(hn);
		
		/*
		 * Search parents first if they exist and the name is absolute.
		 */
		if (hn.absolute() && m_parent != null) {
			return m_parent.search(hn);
		}
		
		Pair<T, Scope<T>> r = null;
		if (hn.leaf()) {
			T so = m_objects.get(hn.peek());
			if (so != null) {
				r = new Pair<>(so, null);
			} else {
				Scope<T> s = m_sub_scopes.get(hn.peek());
				if (s != null) {
					r = new Pair<>(null, s);
				}
			}
		} else {
			Scope<T> sub = m_sub_scopes.get(hn.peek());
			if (sub != null) {
				r = sub.search(hn.pop_first());
			}
		}
		
		/*
		 * Object was not found. Search sub-scopes but make sure it only
		 * exists in one of them, at most.
		 */
		if (r == null) {
			for (Scope<T> ls : m_linked) {
				Pair<T, Scope<T>> found = ls.search(hn);
				if (found != null) {
					if (r != null && !r.equals(found)) {
						throw new AmbiguousNameException("Name '" + hn
								+ "' is ambiguous.");
					} else {
						r = found;
					}
				}
			}
		}
		
		return r;
	}
	
	/**
	 * Finds an object in this scope or sub-scopes given a name. It also
	 * searches linked scopes.
	 * @param hn the object's name
	 * @return the object found or <code>null</code> if no object was found;
	 * <code>null</code> is also returned if the hierarchical name refers to
	 * a scope which is not found or uses a scoped object which is not a scope
	 * as a scope 
	 * @throws AmbiguousNameException if there is no object in this scope that
	 * matches this name but there are more than one in the linked scopes
	 */
	public T find(HierarchicalName hn) throws AmbiguousNameException {
		Ensure.not_null(hn);
		
		Pair<T, Scope<T>> r = search(hn);
		if (r == null) {
			return null;
		}
		
		return r.first();
	}
	
	/**
	 * Finds an scope in this scope or sub-scopes given a name. It also
	 * searches linked scopes.
	 * @param hn the scope's name
	 * @return the scope found or <code>null</code> if no object was found;
	 * <code>null</code> is also returned if the hierarchical name refers to
	 * a scope which is not found or uses a scoped object which is not a scope
	 * as a scope 
	 * @throws AmbiguousNameException if there is no object in this scope that
	 * matches this name but there are more than one in the linked scopes
	 */
	public Scope<T> find_scope(HierarchicalName hn)
			throws AmbiguousNameException {
		Ensure.not_null(hn);
		
		Pair<T, Scope<T>> r = search(hn);
		if (r == null) {
			return null;
		}
		
		return r.second();
	}
	
	/**
	 * Obtains all objects in this scope.
	 * @return all objects
	 */
	public Set<T> all() {
		return new HashSet<>(m_objects.values());
	}
	
	/**
	 * Links this scope to the given scope.
	 * @param scope the scope to link to
	 * @throws CyclicScopeLinkageException if linking this scope would produce
	 * a cyclic graph
	 */
	public final void link(Scope<T> scope) throws CyclicScopeLinkageException {
		Ensure.not_null(scope);
		Ensure.is_false(m_linked.contains(scope));
		
		/*
		 * Make sure there are no cyclic dependencies.
		 */
		Set<Scope<T>> current = new HashSet<>();
		current.addAll(m_linked);
		current.add(scope);
		Set<Scope<T>> seen = new HashSet<>(current);
		
		do {
			current = follow_set(current);
			if (current.contains(this)) {
				throw new CyclicScopeLinkageException("Scope '" + m_name
						+ "' would be in a cyclic linkage graph if linked "
						+ "with scope '" + scope.m_name + "'.");
			}
			
			current.removeAll(seen);
			seen.addAll(current);
		} while (current.size() > 0);
		
		/*
		 * If we've reached here then there is no problem in linking the
		 * given scope.
		 */
		check_link(scope);
		
		m_linked.add(scope);
	}
	
	/**
	 * Invoked when {@link #link(Scope)} is called to check whether the scope
	 * can be linked. <code>Scope</code>'s validations have already passed and
	 * this method does nothing. It may be overridden by subclasses to perform
	 * their own extra validation. 
	 * @param scope the scope to link to
	 */
	protected void check_link(Scope<T> scope) {
		/*
		 * Hook method.
		 */
	}
	
	/**
	 * Creates a set of all scopes that are directly linked from the given
	 * set of scopes. 
	 * @param test the set of scopes to test
	 * @return all scopes which are linked from scopes within
	 * <code>test</code>; note that scopes within <code>test</code> may be
	 * placed in the result set if they are linked from another scope in
	 * <code>test</code>
	 */
	private Set<Scope<T>> follow_set(Set<Scope<T>> test) {
		Ensure.notNull(test);
		Set<Scope<T>> l = new HashSet<>();
		for (Scope<T> s : test) {
			l.addAll(s.m_linked);
		}
		
		return l;
	}
	
	/**
	 * Obtains all objects in this scope, sub scopes and linked scopes,
	 * recursively.
	 * @return all objects
	 */
	public Set<T> all_recursive() {
		Set<T> set = all();
		set.addAll(all());
		
		for (Scope<T> ss : m_sub_scopes.values()) {
			set.addAll(ss.all_recursive());
		}
		
		for (Scope<T> ls : m_linked) {
			set.addAll(ls.all_recursive());
		}
		
		return set;
	}
	
	/**
	 * Tests whether this scope is a child scope of another scope. This method
	 * will return <code>true</code> even if the scope is not a direct
	 * child of the given scope
	 * @param scope the scope
	 * @return is this scope a direct or indirect child of <code>scope</code>?
	 */
	public boolean child_scope_of(Scope<?> scope) {
		Ensure.not_null(scope);
		
		for (Scope<?> p = m_parent; p != null; p = p.m_parent) {
			if (p == scope) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Finds the scope that is a common parent for all given scopes. If there
	 * are multiple scopes that are common parents, returns the inner-most
	 * scope.
	 * @param scopes the scopes, must have at least one scope
	 * @return the common parent or <code>null</code> if non exists
	 */
	public static Scope<?> common_inner_most_parent_scope(
			Set<? extends Scope<?>> scopes) {
		Ensure.not_null(scopes);
		Ensure.greater(scopes.size(), 0);
		
		Scope<?> found = null;
		for (Scope<?> s : scopes) {
			if (found == null) {
				found = s;
			} else if (found != s) {
				for (; found != null && s != found && !s.child_scope_of(found);
						found = found.m_parent) ;
				if (found == null) {
					break;
				}
			}
		}
		
		return found;
	}
}
