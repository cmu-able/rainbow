package edu.cmu.cs.able.typelib.comp;

import java.util.Arrays;

import org.junit.Test;

import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Robustness tests for the tuple data type.
 */
@SuppressWarnings("javadoc")
public class TupleDataTypeRbTest extends TupleDataTypeTestFixture
		implements ComplexDataTypeAbstractRbTest {
	@Test(expected = IllegalArgumentException.class)
	@Override
	public void create_with_null_inner_type() throws Exception {
		m_test_tuple_type.make(Arrays.asList((DataValue) null));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void create_with_null_type_list() throws Exception {
		m_test_tuple_type.make(null);
	}

	@Test(expected = AssertionError.class)
	@Override
	@SuppressWarnings("unused")
	public void create_with_null_super_type() throws Exception {
		new TupleDataType(Arrays.asList((DataType) m_type_1), null);
	}

	@Test(expected = IllegalArgumentException.class)
	@Override
	public void create_instance_with_nonconforming_inner_type()
			throws Exception {
		m_test_tuple_type.make(Arrays.asList((DataValue) m_v1_t2));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void create_instance_with_incorrect_number_of_inner_types()
			throws Exception {
		m_test_112_tuple_type.make(Arrays.asList((DataValue) m_v1_t1,
				m_v1_t1));
	}
}
