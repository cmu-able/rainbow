package edu.cmu.cs.able.typelib.comp;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;

/**
 * Test fixture for the optional data type.
 */
public class OptionalDataTypeTestFixture extends ComplexDataTypeTestFixture {
	/**
	 * The optional any type.
	 */
	protected OptionalDataType m_opt_any;
	
	/**
	 * Optional data type over type m_type_1.
	 */
	protected OptionalDataType m_opt_1;
	
	/**
	 * Optional data type over type m_type_2.
	 */
	protected OptionalDataType m_opt_2;
	
	/**
	 * A <code>null</code> value for the optional type over m_type_1.
	 */
	protected OptionalDataValue m_null_1;
	
	/**
	 * A <code>null</code> value for the optional type over m_type_2.
	 */
	protected OptionalDataValue m_null_2;
	
	/**
	 * Sets up the test fixture.
	 * @throws Exception set up failed
	 */
	@Before
	public void setup_optional_data_type_fixture() throws Exception {
		m_opt_any = new OptionalDataType(m_scope.any(),
				new HashSet<OptionalDataType>());
		m_scope.add(m_opt_any);
		
		m_opt_1 = new OptionalDataType(m_type_1, new HashSet<>(
				Arrays.asList(m_opt_any)));
		m_scope.add(m_opt_1);
		m_null_1 = m_opt_1.make(null);
		
		m_opt_2 = new OptionalDataType(m_type_2, new HashSet<>(
				Arrays.asList(m_opt_any)));
		m_scope.add(m_opt_2);
		m_null_2 = m_opt_2.make(null);
	}
}
