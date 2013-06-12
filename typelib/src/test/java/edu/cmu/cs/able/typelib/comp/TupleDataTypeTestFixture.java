package edu.cmu.cs.able.typelib.comp;

import java.util.Arrays;

import org.junit.Before;

import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Test fixture for tests of the tuple data type.
 */
public class TupleDataTypeTestFixture extends ComplexDataTypeTestFixture {
	/**
	 * Tuple data type with the test data type in it.
	 */
	protected TupleDataType m_test_tuple_type;
	
	/**
	 * Tuple data type with two test data types 1 and one test data type 2 in
	 * it.
	 */
	protected TupleDataType m_test_112_tuple_type;
	
	/**
	 * A tuple data value with {@link ComplexDataTypeTestFixture#m_v1_t1}
	 * inside.
	 */
	protected TupleDataValue m_tv1;
	
	/**
	 * Sets up the text fixture.
	 */
	@Before
	public void tuple_set_up() {
		m_test_tuple_type = new TupleDataType(Arrays.asList(
				(DataType) m_type_1), m_scope.any());
		m_scope.add(m_test_tuple_type);
		m_test_112_tuple_type = new TupleDataType(Arrays.asList(
				(DataType) m_type_1, m_type_1, m_type_2), m_scope.any());
		m_scope.add(m_test_112_tuple_type);
		m_tv1 = m_test_tuple_type.make(Arrays.asList((DataValue) m_v1_t1));
		assertNotNull(m_tv1);
	}
}
