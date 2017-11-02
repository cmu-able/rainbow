package edu.cmu.cs.able.eseb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.junit.Before;
import org.junit.Test;

import auxtestlib.AbstractControlledThread;
import auxtestlib.RandomGenerator;
import edu.cmu.cs.able.typelib.enc.DataValueEncoding;
import edu.cmu.cs.able.typelib.prim.Int32Value;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.prim.StringValue;
import edu.cmu.cs.able.typelib.txtenc.typelib.DefaultTextEncoding;

/**
 * Test suite that verifies reading and writing to/from data streams.
 */
public class DataStreamTest extends EsebTestCase {
	/**
	 * The primitive data type scope.
	 */
	private PrimitiveScope m_scope;
	
	/**
	 * Encoding to use.
	 */
	private DataValueEncoding m_enc;
	
	/**
	 * Prepares the test fixture.
	 */
	@Before
	public void set_up() {
		m_scope = new PrimitiveScope();
		m_enc = new DefaultTextEncoding(m_scope);
	}
	
	@SuppressWarnings("javadoc")
	@Test
	public void read_write_data_values() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (DataTypeOutputStream dtos =
				new DataTypeOutputStreamImpl(os, m_enc)) {
			dtos.write(m_scope.int32().make(17));
			dtos.write(m_scope.string().make("foo"));
			
			ByteArrayInputStream is =
					new ByteArrayInputStream(os.toByteArray());
			try (DataTypeInputStream dtis =
					new DataTypeInputStreamImpl(is, m_enc, m_scope)) {
				Thread.sleep(100);
				Int32Value i32 = (Int32Value) dtis.read().value();
				assertNotNull(i32);
				assertEquals(17, i32.value().intValue());
				StringValue foo = (StringValue) dtis.read().value();
				assertNotNull(foo);
				assertEquals("foo", foo.value());
			}
		}
	}
	
	@SuppressWarnings("javadoc")
	@Test
	public void read_write_data_slowly() throws Exception {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (DataTypeOutputStream dtos =
				new DataTypeOutputStreamImpl(os, m_enc)) {
			dtos.write(m_scope.int32().make(17));
			
			try (PipedInputStream pis = new PipedInputStream();
					final PipedOutputStream pos = new PipedOutputStream(pis)) {
				AbstractControlledThread ct = new AbstractControlledThread() {
					@Override
					public Object myRun() throws Exception {
						byte[] all = os.toByteArray();
						for (int i = 0; i < all.length; i++) {
							pos.write(all[i]);
							pos.flush();
							Thread.sleep(10);
						}
						
						pos.close();
						return null;
					}
				};
				
				try (DataTypeInputStream dtis = new DataTypeInputStreamImpl(pis,
						m_enc, m_scope)) {
					ct.start();
					
					Int32Value i32 = (Int32Value) dtis.read().value();
					assertNotNull(i32);
					assertEquals(17, i32.value().intValue());
					
					try {
						dtis.read();
						fail();
					} catch (EOFException e) {
						/*
						 * Expected.
						 */
					}
					
					dtos.close();
					dtis.close();
					ct.waitForEnd();
					ct.getResult();
				}
			}
		}
	}
	
	@SuppressWarnings("javadoc")
	@Test(expected = AssertionError.class)
	public void create_input_with_null_stream() throws Exception {
		try (DataTypeInputStreamImpl i = new DataTypeInputStreamImpl(null,
				m_enc, m_scope)) {
			/*
			 * We never get here.
			 */
		}
	}
	
	@SuppressWarnings("javadoc")
	@Test(expected = AssertionError.class)
	public void create_input_with_null_encoding() throws Exception {
		try (DataTypeInputStreamImpl i = new DataTypeInputStreamImpl(
				new ByteArrayInputStream(new byte[0]), null, m_scope)) {
			/*
			 * We never get here.
			 */
		}
	}
	
	@SuppressWarnings("javadoc")
	@Test(expected = IllegalStateException.class)
	public void operating_on_closed_input_stream() throws Exception {
		try (DataTypeInputStreamImpl dtis = new DataTypeInputStreamImpl(
				new ByteArrayInputStream(new byte[0]), m_enc, m_scope)) {
			dtis.close();
			dtis.read();
		}
	}
	
	@SuppressWarnings("javadoc")
	@Test()
	public void eof_support_on_input_stream() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (DataTypeOutputStreamImpl dtos = new DataTypeOutputStreamImpl(os,
				m_enc)) {
			dtos.write(m_scope.int32().make(45));
			
			byte[] data = os.toByteArray();
			
			/*
			 * Try reading with all sizes.
			 */
			for (int i = 0; i < data.length; i++) {
				try (ByteArrayInputStream is = new ByteArrayInputStream(data,
						0, i);
						DataTypeInputStreamImpl dtis =
						new DataTypeInputStreamImpl(is, m_enc, m_scope)) {
					try {
						dtis.read();
						fail();
					} catch (EOFException e) {
						/*
						 * Expected.
						 */
					}
					
					dtos.close();
					dtis.close();
				}
			}
		}
	}
	
	@SuppressWarnings("javadoc")
	@Test(expected = IOException.class)
	public void read_negative_packet_size() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (DataOutputStream dos = new DataOutputStream(os)) {
			dos.writeInt(-7);
			dos.write(RandomGenerator.randBytes(500));
		}
			
		ByteArrayInputStream is = new ByteArrayInputStream(
				os.toByteArray());
		try (DataTypeInputStreamImpl dtis = new DataTypeInputStreamImpl(is,
				m_enc, m_scope)) {
			dtis.read();
		}
	}
			
	@SuppressWarnings("javadoc")
	@Test(expected = IOException.class)
	public void read_very_big_packet_size() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (DataOutputStream dos = new DataOutputStream(os)) {
			int big_size = 2*1024*1024;
			dos.writeInt(big_size);
			dos.write(RandomGenerator.randBytes(big_size));
		}
			
		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		try (DataTypeInputStreamImpl dtis =
				new DataTypeInputStreamImpl(is, m_enc, m_scope)) {
			dtis.read();
		}
	}
	
	@SuppressWarnings("javadoc")
	@Test(expected = IllegalArgumentException.class)
	public void create_output_with_null_stream() throws Exception {
		try (DataTypeOutputStreamImpl i =
				new DataTypeOutputStreamImpl(null, m_enc)) {
			/*
			 * We should never get here.
			 */
		}
	}
	
	
	@SuppressWarnings("javadoc")
	@Test(expected = IllegalArgumentException.class)
	public void create_output_with_null_encoding() throws Exception {
		try (DataTypeOutputStreamImpl i =
				new DataTypeOutputStreamImpl(new ByteArrayOutputStream(),
				null)) {
			/*
			 * We should never get here.
			 */
		}
	}
	
	@SuppressWarnings("javadoc")
	@Test(expected = IllegalStateException.class)
	public void write_on_closed_output_stream() throws Exception {
		try (DataTypeOutputStream os = new DataTypeOutputStreamImpl(
				new ByteArrayOutputStream(), m_enc)) {
			os.close();
			os.write(m_scope.int32().make(0));
		}
	}
	
	@SuppressWarnings("javadoc")
	@Test(expected = IllegalArgumentException.class)
	public void write_null_to_output_stream() throws Exception {
		try (DataTypeOutputStream d = new DataTypeOutputStreamImpl(
				new ByteArrayOutputStream(), m_enc)) {
			d.write((BusData) null);
		}
	}
}
