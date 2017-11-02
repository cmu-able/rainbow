package edu.cmu.cs.able.typelib.comp;

import java.util.Set;

import org.junit.Before;

import auxtestlib.DefaultTCase;
import edu.cmu.cs.able.typelib.TestDataType;
import edu.cmu.cs.able.typelib.TestDataValue;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.type.DataType;

/**
 * This abstract class provides the common infrastructure for all complex data 
 * type tests.
 */
public abstract class ComplexDataTypeTestFixture extends DefaultTCase {
	/**
	 * Primitive data scope.
	 */
	protected PrimitiveScope m_scope;
	
	/**
	 * A data type used for testing. This an inner data type of the
	 * complex data type.
	 */
	protected TestDataType m_type_1;
	
	/**
	 * A data type used for testing. This an inner data type of the
	 * complex data type.
	 */
	protected TestDataType m_type_2;
	
	/**
	 * Super data type of m_type_1.
	 */
	protected TestDataType m_super_type_1;
	
	/**
	 * Sub data type of m_type_1.
	 */
	protected TestDataType m_sub_type_1;
	
	/**
	 * A value of type m_type_1.
	 */
	protected TestDataValue m_v1_t1;
	
	/**
	 * A value of type m_type_1 which is greater than m_v1_t1.
	 */
	protected TestDataValue m_v2_t1;
	
	/**
	 * A value of type m_type_1 whose value equals v2
	 */
	protected TestDataValue m_v2_2_t1;
	
	/**
	 * A value of type m_type_2.
	 */
	protected TestDataValue m_v1_t2;
	
	/**
	 * Another value of type m_type_2.
	 */
	protected TestDataValue m_v2_t2;
	
	/**
	 * A value of the type m_sub_type_1.
	 */
	protected TestDataValue m_vsub;
	
	/**
	 * A value of the type m_super_type_1.
	 */
	protected TestDataValue m_vsuper;
	
	/**
	 * Sets up the test fixture.
	 * @throws Exception test failed
	 */
	@Before
	public void complex_data_type_set_up() throws Exception {
		m_scope = new PrimitiveScope();
		m_super_type_1 = new TestDataType("a", (Set<DataType>) null);
		m_scope.add(m_super_type_1);
		m_type_1 = new TestDataType("b", m_super_type_1);
		m_scope.add(m_type_1);
		m_type_2 = new TestDataType("bb", (Set<DataType>) null);
		m_scope.add(m_type_2);
		m_sub_type_1 = new TestDataType("c", m_type_1);
		m_scope.add(m_sub_type_1);
		m_v1_t1 = new TestDataValue(m_type_1, 7);
		m_v2_t1 = new TestDataValue(m_type_1, 9);
		m_v2_2_t1 = new TestDataValue(m_type_1, 9);
		m_vsub = new TestDataValue(m_sub_type_1, 10);
		m_vsuper = new TestDataValue(m_super_type_1, -5);
		m_v1_t2 = new TestDataValue(m_type_2, -8);
		m_v2_t2 = new TestDataValue(m_type_2, 0);
	}
}
