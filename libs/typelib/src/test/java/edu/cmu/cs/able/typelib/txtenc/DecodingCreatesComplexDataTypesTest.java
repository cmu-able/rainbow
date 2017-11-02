package edu.cmu.cs.able.typelib.txtenc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import edu.cmu.cs.able.typelib.comp.BagDataType;
import edu.cmu.cs.able.typelib.comp.ListDataType;
import edu.cmu.cs.able.typelib.comp.MapDataType;
import edu.cmu.cs.able.typelib.comp.OptionalDataType;
import edu.cmu.cs.able.typelib.comp.SetDataType;
import edu.cmu.cs.able.typelib.comp.TupleDataType;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.txtenc.typelib.DefaultTextEncoding;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;
import auxtestlib.DefaultTCase;

/**
 * While decoding data types, complex data types are created on-the-fly if
 * the need to.
 */
@SuppressWarnings("javadoc")
public class DecodingCreatesComplexDataTypesTest extends DefaultTCase {
	/**
	 * Primitive scope used for encoding.
	 */
	private PrimitiveScope m_encode_scope;
	
	/**
	 * Primitive scope used for decoding.
	 */
	private PrimitiveScope m_decode_scope;
	
	/**
	 * Encoding to use.
	 */
	private TextEncoding m_encoding;
	
	/**
	 * Decoding to use.
	 */
	private TextEncoding m_decoding;
	
	/**
	 * Output stream for encoding.
	 */
	private ByteArrayOutputStream m_out;
	
	/**
	 * Data output stream for encoding.
	 */
	private DataOutputStream m_dout;
	
	@Before
	public void set_up() throws Exception {
		m_encode_scope = new PrimitiveScope();
		m_decode_scope = new PrimitiveScope();
		m_encoding = new DefaultTextEncoding(m_encode_scope);
		m_decoding = new DefaultTextEncoding(m_decode_scope);
		m_out = new ByteArrayOutputStream();
		m_dout = new DataOutputStream(m_out);
	}
	
	/**
	 * Creates a data input stream to read data written to the output stream.
	 * @return the input stream
	 */
	private DataInputStream make_in() {
		return new DataInputStream(new ByteArrayInputStream(
				m_out.toByteArray()));
	}
	
	@Test
	public void encode_decode_set() throws Exception {
		SetDataType send_dt = SetDataType.set_of(m_encode_scope.int32(),
				m_encode_scope);
		m_encoding.encode(send_dt.make(), m_dout);
		
		DataValue d = m_decoding.decode(make_in(), m_decode_scope);
		assertEquals(SetDataType.set_of(m_decode_scope.int32(),
				m_decode_scope).make(), d);
	}
	
	@Test
	public void encode_decode_list() throws Exception {
		ListDataType send_dt = ListDataType.list_of(m_encode_scope.int32(),
				m_encode_scope);
		m_encoding.encode(send_dt.make(), m_dout);
		
		DataValue d = m_decoding.decode(make_in(), m_decode_scope);
		assertEquals(ListDataType.list_of(m_decode_scope.int32(),
				m_decode_scope).make(), d);
	}
	
	@Test
	public void encode_decode_bag() throws Exception {
		BagDataType send_dt = BagDataType.bag_of(m_encode_scope.int32(),
				m_encode_scope);
		m_encoding.encode(send_dt.make(), m_dout);
		
		DataValue d = m_decoding.decode(make_in(), m_decode_scope);
		assertEquals(BagDataType.bag_of(m_decode_scope.int32(),
				m_decode_scope).make(), d);
	}
	
	@Test
	public void encode_decode_tuple() throws Exception {
		TupleDataType send_dt = TupleDataType.tuple_of(Arrays.asList(
				(DataType) m_encode_scope.int32()), m_encode_scope);
		m_encoding.encode(send_dt.make(Arrays.asList((DataValue)
				m_encode_scope.int32().make(4))), m_dout);
		
		DataValue d = m_decoding.decode(make_in(), m_decode_scope);
		assertEquals(TupleDataType.tuple_of(Arrays.asList(
				(DataType) m_decode_scope.int32()), m_decode_scope).make(
				Arrays.asList((DataValue) m_decode_scope.int32().make(4))), d);
	}
	
	@Test
	public void encode_decode_map() throws Exception {
		MapDataType send_dt = MapDataType.map_of(m_encode_scope.int32(),
				m_encode_scope.string(), m_encode_scope);
		m_encoding.encode(send_dt.make(), m_dout);
		
		DataValue d = m_decoding.decode(make_in(), m_decode_scope);
		assertEquals(MapDataType.map_of(m_decode_scope.int32(),
				m_decode_scope.string(), m_decode_scope).make(), d);
	}
	
	@Test
	public void encode_decode_optional() throws Exception {
		OptionalDataType send_dt = OptionalDataType.optional_of(
				m_encode_scope.int32());
		m_encoding.encode(send_dt.make(m_encode_scope.int32().make(4)), m_dout);
		
		DataValue d = m_decoding.decode(make_in(), m_decode_scope);
		assertEquals(OptionalDataType.optional_of(m_decode_scope.int32()).make(
				m_decode_scope.int32().make(4)), d);
	}
}
