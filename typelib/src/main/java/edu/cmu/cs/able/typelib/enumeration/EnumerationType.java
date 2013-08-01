package edu.cmu.cs.able.typelib.enumeration;

import incubator.pval.Ensure;

import java.util.HashSet;
import java.util.Set;

import edu.cmu.cs.able.typelib.prim.AnyType;
import edu.cmu.cs.able.typelib.type.DataType;

/**
 * Enumeration data type. An enumeration has several predefined values which
 * are identified with a name.
 */
public class EnumerationType extends DataType {
	/**
	 * All enumeration values.
	 */
	private Set<EnumerationValue> m_values;
	
	/**
	 * Creates a new enumeration data type.
	 * @param name the name of the enumeration
	 * @param super_types the enumeration super types
	 */
	private EnumerationType(String name, Set<DataType> super_types) {
		super(name, super_types);
	}
	
	/**
	 * Creates a new enumeration data type.
	 * @param name the name of the enumeration
	 * @param values the possible values for the enumeration
	 * @param any the <code>any</code> data type which will be the super
	 * type for the enumeration
	 * @return the created enumeration type 
	 */
	public static EnumerationType make(String name, Set<String> values,
			AnyType any) {
		Ensure.not_null(name);
		Ensure.not_null(values);
		Ensure.not_null(any);
		
		Set<DataType> super_types = new HashSet<>();
		super_types.add(any);
		
		EnumerationType e = new EnumerationType(name, super_types);
		e.m_values = new HashSet<>();
		for (String v : values) {
			e.m_values.add(new EnumerationValue(e, v));
		}
		
		return e;
	}

	@Override
	public boolean is_abstract() {
		return false;
	}
	
	/**
	 * Checks whether this enumeration has a value with the given name.
	 * @param name the enumeration name
	 * @return does the enumeration have the given value
	 */
	public boolean has_value(String name) {
		Ensure.not_null(name);
		for (EnumerationValue v : m_values) {
			if (v.name().equals(name)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Obtains an enumeration value given its name. An enumeration value must
	 * exist with this name. Use {@link #has_value(String)} to check.
	 * @param name the enumeration value name
	 * @return the enumeration value
	 */
	public EnumerationValue value(String name) {
		Ensure.not_null(name);
		
		EnumerationValue r = null;
		for (EnumerationValue v : m_values) {
			if (v.name().equals(name)) {
				r = v;
			}
		}
		
		Ensure.not_null(r);
		return r;
	}
	
	/**
	 * Obtains all enumeration values.
	 * @return all values
	 */
	public Set<EnumerationValue> values() {
		return new HashSet<>(m_values);
	}
}
