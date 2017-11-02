package edu.cmu.cs.able.typelib.comp;

import org.junit.Before;

/**
 * Test fixture for the map data type.
 */
public class MapDataTypeTestFixture extends ComplexDataTypeTestFixture {
	/**
	 * Map data type that maps an int32 to a string.
	 */
	protected MapDataType m_int32_to_string_type;
	
	/**
	 * Map data type that maps test data type
	 * {@link ComplexDataTypeTestFixture#m_type_1} to itself.
	 */
	protected MapDataType m_1_to_1_type;
	
	/**
	 * Map value that maps type 1 to type values.
	 */
	protected MapDataValue m_1_to_1;
	
	/**
	 * Prepares the test fixture.
	 * @throws Exception preparation fails
	 */
	@Before
	public void map_set_up() throws Exception {
		m_int32_to_string_type = MapDataType.map_of(m_scope.int32(),
				m_scope.string(), m_scope);
		m_1_to_1_type = MapDataType.map_of(m_type_1, m_type_1, m_scope);
		m_1_to_1 = m_1_to_1_type.make();
	}
}
