package edu.cmu.cs.able.eseb.rpc;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.type.DataValue;
import auxtestlib.DefaultTCase;

/**
 * Tests working with the operation information data types.
 */
@SuppressWarnings("javadoc")
public class OperationInformationTest extends DefaultTCase {
	/**
	 * The primitive type scope.
	 */
	private PrimitiveScope m_pscope;
	
	/**
	 * Operation information.
	 */
	private OperationInformation m_oi;
	
	@Before
	public void set_up() throws Exception {
		m_pscope = new PrimitiveScope();
		m_oi = new OperationInformation(m_pscope);
	}
	
	@Test
	public void create_operation() throws Exception {
		DataValue v = m_oi.create_operation("test");
		assertEquals("test", m_oi.operation_name(v));
		assertEquals(0, m_oi.parameters(v).size());
	}
	
	@Test
	public void check_is_operation_on_operation() throws Exception {
		DataValue v = m_oi.create_operation("test");
		assertTrue(m_oi.is_operation(v));
	}
	
	@Test
	public void check_is_operation_on_non_operation() throws Exception {
		assertFalse(m_oi.is_operation(m_pscope.int32().make(2)));
	}
	
	@Test
	public void adding_paramters_to_operation() throws Exception {
		DataValue v = m_oi.create_operation("test");
		m_oi.add_parameter(v, m_pscope.int32(), "x", ParameterDirection.INPUT);
		assertEquals(1, m_oi.parameters(v).size());
		assertTrue(m_oi.parameters(v).contains("x"));
		assertSame(m_pscope.int32(), m_oi.parameter_type(v, "x"));
		assertEquals(ParameterDirection.INPUT, m_oi.parameter_direction(
				v, "x"));
	}
	
	@Test
	public void create_operation_group() throws Exception {
 		DataValue v = m_oi.create_group();
 		assertNotNull(v);
 		assertEquals(0, m_oi.group_operation_names(v).size());
	}
	
	@Test
	public void check_is_group_on_group() throws Exception {
		assertTrue(m_oi.is_group(m_oi.create_group()));
	}
	
	@Test
	public void check_is_group_on_non_group() throws Exception {
		assertFalse(m_oi.is_group(m_oi.create_operation("x")));
	}
	
	@Test
	public void add_operation_to_group() throws Exception {
		DataValue g = m_oi.create_group();
		DataValue op = m_oi.create_operation("foo");
		m_oi.add_operation_to_group(g, op);
		assertEquals(1, m_oi.group_operation_names(g).size());
		assertTrue(m_oi.group_operation_names(g).contains("foo"));
		DataValue op_2 = m_oi.group_operation(g, "foo");
		assertEquals("foo", m_oi.operation_name(op_2));
	}
	
	@Test
	public void create_execution_request() throws Exception {
		DataValue op = m_oi.create_operation("foo");
		m_oi.add_parameter(op, m_pscope.int32(), "foo",
		ParameterDirection.INPUT);
		
		Map<String, DataValue> iargs = new HashMap<>();
		iargs.put("foo", m_pscope.int32().make(-6));
		DataValue er = m_oi.create_execution_request(3, -4, "5", op, iargs);
		
		assertEquals(3, m_oi.execution_request_id(er));
		assertEquals(-4, m_oi.execution_request_dst(er));
		assertEquals("5", m_oi.execution_request_obj_id(er));
		assertEquals("foo", m_oi.execution_request_operation(er));
		assertEquals(iargs, m_oi.execution_request_input_arguments(er));
	}
	
	@Test
	public void check_is_execution_request_on_request() throws Exception {
		DataValue op = m_oi.create_operation("foo");
		m_oi.add_parameter(op, m_pscope.int32(), "foo",
					ParameterDirection.INPUT);
		
		Map<String, DataValue> iargs = new HashMap<>();
		iargs.put("foo", m_pscope.int32().make(-6));
		DataValue er = m_oi.create_execution_request(3, -4, "5", op, iargs);
		
		assertTrue(m_oi.is_execution_request(er));
	}
	
	@Test
	public void check_is_execution_request_on_non_request() throws Exception {
		assertFalse(m_oi.is_execution_request(m_oi.create_group()));
	}
	
	@Test
	public void create_successful_execution_response() throws Exception {
		DataValue op = m_oi.create_operation("foo");
		m_oi.add_parameter(op, m_pscope.int32(), "foo",
					ParameterDirection.INPUT);
		m_oi.add_parameter(op, m_pscope.int32(), "bar",
					ParameterDirection.OUTPUT);
		
		Map<String, DataValue> iargs = new HashMap<>();
		iargs.put("foo", m_pscope.int32().make(-6));
		DataValue er = m_oi.create_execution_request(3, -4, "5", op, iargs);
		
		Map<String, DataValue> oargs = new HashMap<>();
		oargs.put("bar", m_pscope.int32().make(7));
		
		DataValue res = m_oi.create_execution_response(er, op, oargs);
		assertTrue(m_oi.is_successful_execution(res));
		assertEquals(3, m_oi.execution_response_id(res));
		assertEquals(oargs, m_oi.execution_response_output_arguments(res));
	}
	
	@Test
	public void create_failure_execution_response() throws Exception {
		DataValue op = m_oi.create_operation("foo");
		m_oi.add_parameter(op, m_pscope.int32(), "foo",
					ParameterDirection.INPUT);
		m_oi.add_parameter(op, m_pscope.int32(), "bar",
					ParameterDirection.OUTPUT);
		
		Map<String, DataValue> iargs = new HashMap<>();
		iargs.put("foo", m_pscope.int32().make(-6));
		DataValue er = m_oi.create_execution_request(3, -4, "5", op, iargs);
		
		Map<String, DataValue> oargs = new HashMap<>();
		oargs.put("bar", m_pscope.int32().make(7));
		
		DataValue res = m_oi.create_execution_failure(er, "a", "b","c");
		assertFalse(m_oi.is_successful_execution(res));
		assertEquals("c", m_oi.execution_response_failure_data(res));
		assertEquals("b", m_oi.execution_response_failure_description(res));
		assertEquals("a", m_oi.execution_response_failure_type(res));
		assertEquals(3, m_oi.execution_response_id(res));
	}
	
	@Test
	public void check_is_response_on_response() throws Exception {
		DataValue op = m_oi.create_operation("foo");
		m_oi.add_parameter(op, m_pscope.int32(), "foo",
					ParameterDirection.INPUT);
		m_oi.add_parameter(op, m_pscope.int32(), "bar",
					ParameterDirection.OUTPUT);
		
		Map<String, DataValue> iargs = new HashMap<>();
		iargs.put("foo", m_pscope.int32().make(-6));
		DataValue er = m_oi.create_execution_request(3, -4, "5", op, iargs);
		
		Map<String, DataValue> oargs = new HashMap<>();
		oargs.put("bar", m_pscope.int32().make(7));
		
		DataValue res = m_oi.create_execution_failure(er, "a", "b","c");
		
		assertTrue(m_oi.is_execution_response(res));
	}
	
	@Test
	public void check_is_response_on_non_response() throws Exception {
		assertFalse(m_oi.is_execution_response(m_oi.create_group()));
	}
}
