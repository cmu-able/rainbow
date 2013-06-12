package edu.cmu.cs.able.typelib.comp;

import incubator.pval.Ensure;

import java.util.Set;

import edu.cmu.cs.able.typelib.type.DataType;

/**
 * Superclass for composite data types which have a single inner type.
 */
public abstract class SingleCompositeDataType extends DataType {
	/**
	 * The inner data type.
	 */
	private DataType m_inner_type;
	
	/**
	 * Creates a new composite data type.
	 * @param name the data type name
	 * @param inner_type the inner data type
	 * @param super_types the data type's super types
	 */
	protected SingleCompositeDataType(String name, DataType inner_type,
			Set<DataType> super_types) {
		super(name, Ensure.not_null(super_types));
		
		Ensure.notNull(inner_type);
		m_inner_type = inner_type;
	}
	
	/**
	 * Obtains the inner type of this type.
	 * @return the inner type
	 */
	public DataType inner_type() {
		return m_inner_type;
	}
}
