package edu.cmu.cs.able.typelib;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.cmu.cs.able.typelib.type.DataType;

/**
 * Data type used for testing.
 */
public class TestDataType extends DataType {
	/**
	 * Is the data type abstract?
	 */
	public boolean m_abstract;
	
	/**
	 * Does this data type supports natural ordering?
	 */
	public boolean m_ordering;
	
	/**
	 * Creates a new data type.
	 * @param name the name of the data type
	 * @param types an optional list of parent types
	 */
	public TestDataType(String name, TestDataType...types) {
		super(name, new HashSet<DataType>(Arrays.asList(types)));
		m_abstract = false;
		m_ordering = false;
	}
	
	/**
	 * Creates a new data type.
	 * @param name the name of the data type
	 * @param types an optional set of parent types
	 */
	public TestDataType(String name, Set<DataType> types) {
		super(name, types);
	}
	
	@Override
	public boolean is_abstract() {
		return m_abstract;
	}
}
