package edu.cmu.cs.able.typelib.scope;

import incubator.pval.Ensure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>Allows indexing an object inside a scope. The difference between using a
 * hierarchical name and just the plain name is that the hierarchical name
 * allows access to names that lay in sub-scopes of a scope. A hierarchical
 * name is a list of names that forms a path to a scoped object in a scope.</p>
 * <p>Hierarchical names can be absolute or hierarchical. The interpretation
 * of the semantics is not done by the hierarchical name class.</p>
 */
public final class HierarchicalName {
	/**
	 * The various names in the path.
	 */
	private List<String> m_names;
	
	/**
	 * Is this an absolute name?
	 */
	private boolean m_is_absolute;
	
	/**
	 * Creates a new hierarchical name.
	 * @param absolute is the name an absolute name?
	 * @param names the list of names that make the hierarchy from the
	 * root up to the leafs
	 */
	public HierarchicalName(boolean absolute, List<String> names) {
		Ensure.not_null(names);
		Ensure.greater(names.size(), 0);
		for (String n : names) {
			Ensure.not_null(n);
		}
		
		m_names = new ArrayList<>(names);
		m_is_absolute = absolute;
	}
	
	/**
	 * Creates a new hierarchical name.
	 * @param absolute is the name an absolute name?
	 * @param names the list of names that make the hierarchy
	 */
	public HierarchicalName(boolean absolute, String... names) {
		this(absolute, Arrays.asList(names));
	}
	
	/**
	 * Is this name an absolute name?
	 * @return is the name an absolute name?
	 */
	public boolean absolute() {
		return m_is_absolute;
	}
	
	/**
	 * Obtains the first name in the hierarchical name (the root).
	 * @return the first name
	 */
	public String peek() {
		return m_names.get(0);
	}
	
	/**
	 * Obtains the hierarchical name that results from removing the first
	 * name.
	 * @return the hierarchical name or <code>null</code> if there are no
	 * more names
	 */
	public HierarchicalName pop_first() {
		if (m_names.size() == 1) {
			return null;
		}
		
		return new HierarchicalName(false, m_names.subList(1, m_names.size()));
	}
	
	/**
	 * Checks if this name is a leaf name. A leaf name is a name which has
	 * no more sub names (it contains no dividers). Leaf names may be
	 * absolute names.
	 * @return is this a leaf name?
	 */
	public boolean leaf() {
		return m_names.size() == 1;
	}
	
	/**
	 * Creates a new hierarchical name by pushing a name to the end of this one.
	 * @param name the name to push
	 * @return the new hierarchical name
	 */
	public HierarchicalName push(String name) {
		Ensure.not_null(name);
		
		List<String> newNames = new ArrayList<>(m_names);
		newNames.add(name);
		return new HierarchicalName(m_is_absolute, newNames);
	}
	
	/**
	 * Creates a new hierarchical name from this one that is an absolute
	 * name. The current name must not be absolute.
	 * @return the new name
	 */
	public HierarchicalName make_absolute() {
		Ensure.is_false(m_is_absolute);
		return new HierarchicalName(true, m_names);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof HierarchicalName)) {
			return false;
		}
		
		HierarchicalName hn = (HierarchicalName) obj;
		return hn.m_is_absolute == m_is_absolute && hn.m_names.equals(m_names);
	}
	
	@Override
	public int hashCode() {
		return (m_is_absolute? 2 : 1) + 3 * m_names.hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (m_is_absolute) {
			sb.append("::");
		}
		
		for (int i = 0; i < m_names.size(); i++) {
			if (i > 0) {
				sb.append("::");
			}
			
			sb.append(m_names.get(i));
		}
		
		return sb.toString();
	}
}
