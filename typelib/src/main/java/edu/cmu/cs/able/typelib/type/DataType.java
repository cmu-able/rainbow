package edu.cmu.cs.able.typelib.type;

import incubator.pval.Ensure;

import java.util.HashSet;
import java.util.Set;

import edu.cmu.cs.able.typelib.scope.Scope;
import edu.cmu.cs.able.typelib.scope.ScopedObject;

/**
 * <p>Superclass for all data types. Data types are represented by Java objects
 * whose type is a subclass of data type. For many standard data types, there
 * is only object of each class and they are implemented using the singleton
 * pattern. Still, it is important to keep clear the difference between a
 * type (an instance of {@link DataType}) and a value (an instance of
 * {@link DataValue}).</p>
 * <p>Data types are scoped objects and the scoped object
 * name is the data type name. Data types must always belong to a scope.</p>
 * <p>Data types can form a hierarchy in which inheritance means specialization,
 * like in normal object-oriented programming. Data types provide ways to test
 * whether data values belong to the type and whether data types are
 * super types of the data type. Data types have knowledge of the hierarchy
 * they are on: they know which types are their parents and which types are
 * their children.</p>
 * <p>Data types are immutable objects: they cannot be deleted once created.</p>
 * <p>All data types should be thread-safe.</p>
 * <p>Data types must be registered in a scope before they can be used.
 * Information provided in the constructor (such as name and super types)
 * may be queried before registration.</p>
 */
public abstract class DataType extends ScopedObject {
	/**
	 * Direct super types.
	 */
	private Set<DataType> m_super;
	
	/**
	 * Direct sub types.
	 */
	private Set<DataType> m_sub;
	
	/**
	 * Set to true when the data type has been registered in a scope.
	 */
	private boolean m_registered;
	
	/**
	 * Creates a new data type.
	 * @param name the data type name
	 * @param super_types parent data types; may be <code>null</code> if there
	 * are no parent data types; all parent data types must share the same
	 * top-level scope, <em>i.e.</em>, they must be in the same type
	 * hierarchy.
	 */
	protected DataType(String name, Set<DataType> super_types) {
		super(name);
		Ensure.notNull(name);
		
		if (super_types != null) {
			for (DataType d : super_types) {
				Ensure.not_null(d);
			}
			
			m_super = new HashSet<>(super_types);
		} else {
			m_super = new HashSet<>();
		}
		
		m_sub = new HashSet<>();
		m_registered = false;
	}
	
	@Override
	protected final void check_register(Scope<?> scope) {
		Ensure.isTrue("Data types can only be added to DataTypeScope.",
				scope instanceof DataTypeScope);
		for (DataType d : m_super) {
			m_super.add(d);
			d.m_sub.add(this);
		}
		
		m_registered = true;
	}
	
	/**
	 * Obtains the parent (as a data type scope).
	 * @return the parent
	 */
	public DataTypeScope parent_dts() {
		return (DataTypeScope) parent();
	}
	
	/**
	 * Obtains the direct super types of this type, if any. Does not include
	 * any of the super types' super types.
	 * @return a set with all super types which will be empty if there are none
	 */
	public Set<DataType> super_types() {
		return new HashSet<>(m_super);
	}
	
	/**
	 * Obtains the direct sub types of this type, if any. Does not include
	 * any of the sub types' sub types.
	 * @return the sub types
	 */
	public Set<DataType> sub_types() {
		Ensure.is_true(m_registered);
		return new HashSet<>(m_sub);
	}
	
	/**
	 * Checks whether this type is a super type of the given type. Super type
	 * works with multiple hierarchy levels: is <code>x</code> is super of
	 * <code>y</code> and <code>y</code> super of <code>z</code>,
	 * <code>x.super_of(z)</code> returns <code>true</code>. Note that
	 * <code>x.super_of(x)</code> is always <code>false</code>.
	 * @param t the type to check
	 * @return is this type a super type of the given type?
	 */
	public boolean super_of(DataType t) {
		Ensure.is_true(m_registered);
		Ensure.notNull(t);
		if (m_sub.contains(t)) {
			return true;
		}
		
		for (DataType s : m_sub) {
			if (s.super_of(t)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks whether this type is a sub type of the given type. Sub types
	 * work with multiple hierarchy. <code>x.sub_of(y)</code> is equivalent to
	 * <code>y != x && !y.super_of(x)</code>.
	 * @param t the type to check
	 * @return is this type a sub type of the given type?
	 */
	public boolean sub_of(DataType t) {
		Ensure.is_true(m_registered);
		Ensure.notNull(t);
		if (m_super.contains(t)) {
			return true;
		}
		
		for (DataType s : m_super) {
			if (s.sub_of(t)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks whether a data value is an instance of this type or one of its
	 * sub types.
	 * @param v the value
	 * @return is it an instance?
	 */
	public boolean is_instance(DataValue v) {
		Ensure.is_true(m_registered);
		Ensure.notNull(v);
		if (v.type() == this) {
			return true;
		}
		
		return v.type().sub_of(this);
	}
	
	/**
	 * Is this data type abstract? Abstract data types cannot be instantiated.
	 * Note that even if the class that represents the data type is not
	 * abstract (in the Java code), the type represented by the
	 * <em>instance</em> of the class may be abstract.
	 * @return is this data type abstract
	 */
	public abstract boolean is_abstract();
}
