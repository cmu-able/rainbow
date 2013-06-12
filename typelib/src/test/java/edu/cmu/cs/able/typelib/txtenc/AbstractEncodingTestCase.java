package edu.cmu.cs.able.typelib.txtenc;

import incubator.pval.Ensure;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;

import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.txtenc.typelib.DefaultTextEncoding;
import edu.cmu.cs.able.typelib.type.DataValue;
import auxtestlib.DefaultTCase;

/**
 * Abstract test case that provides common structure for all encoding test
 * cases.
 */
public class AbstractEncodingTestCase extends DefaultTCase {
	/**
	 * Number of invalid decoding random tests to perform.
	 */
	protected static final int RANDOM_TESTS = 1000;
	
	/**
	 * The primitive type scope.
	 */
	protected PrimitiveScope m_pscope;
	
	/**
	 * The encoding to test.
	 */
	protected DefaultTextEncoding m_enc;
	
	/**
	 * Output stream where encoding is done.
	 */
	protected ByteArrayOutputStream m_output;
	
	/**
	 * Output data stream to write to.
	 */
	protected DataOutputStream m_doutput;

	/**
	 * Prepares the text fixture.
	 * @throws Exception preparation failed
	 */
	@Before
	public void set_up() throws Exception {
		m_pscope = new PrimitiveScope();
		m_enc = new DefaultTextEncoding();
		m_output = new ByteArrayOutputStream();
		m_doutput = new DataOutputStream(m_output);
	}
	
	/**
	 * Makes a data input stream that reads the output written.
	 * @return the data input stream
	 */
	protected DataInputStream make_din() {
		return new DataInputStream(new ByteArrayInputStream(
				m_output.toByteArray()));
	}
	
	/**
	 * Makes a data input stream which is corrupt by either (1) changing a
	 * random byte in the input, (2) adding a random byte, (3) removing a
	 * random byte.
	 * @return the data input stream
	 */
	protected DataInputStream make_corrupt_din() {
		byte[] b = m_output.toByteArray();
		
		/*
		 * Select a random byte.
		 */
		int idx = RandomUtils.nextInt(b.length);
		
		switch (RandomUtils.nextInt(3)) {
		case 0:
			/*
			 * Switch a random byte.
			 */
			byte repl;
			do {
				repl = (byte)(RandomUtils.nextInt(256) + Byte.MIN_VALUE);
			} while (repl == b[idx]);
			
			b[idx] = repl;
			break;
		case 1:
			/*
			 * Remove a random byte.
			 */
			byte[] bb_r = new byte[b.length - 1];
			System.arraycopy(b, 0, bb_r, 0, idx);
			System.arraycopy(b, idx + 1, bb_r, idx, bb_r.length - idx);
			b = bb_r;
			break;
		case 2:
			/*
			 * Add a random byte.
			 */
			byte[] bb_a = new byte[b.length + 1];
			System.arraycopy(b, 0, bb_a, 0, idx);
			System.arraycopy(b, idx, bb_a, idx + 1, b.length - idx);
			bb_a[idx] = (byte)(RandomUtils.nextInt(256) + Byte.MIN_VALUE);
			b = bb_a;
			break;
		default:
			fail();
		}
		
		return new DataInputStream(new ByteArrayInputStream(b));
	}
	
	/**
	 * Encodes values in a stream and then decodes them.
	 * @param v the values to encode and decode
	 * @param rounds how many times to encode the values
	 * @return how much time (in milliseconds) did encoding/decoding take
	 * individually
	 */
	protected double encode_decode(DataValue[] v, int rounds) {
		Ensure.not_null(v);
		Ensure.greater(rounds, 0);
		
		DataValue[] d = new DataValue[v.length];
		
		long start = System.currentTimeMillis();
		
		/*
		 * First encode all.
		 */
		ByteArrayOutputStream output = new ByteArrayOutputStream(
				10 * 1024 * 1024);
		try (DataOutputStream data_out = new DataOutputStream(output)) {
			for (int j = 0; j < rounds; j++) {
				for (int i = 0; i < v.length; i++) {
					try {
						m_enc.encode(v[i], data_out);
					} catch (IOException e) {
						Ensure.never_thrown(e);
					}
				}
			}
		} catch (IOException e) {
			Ensure.never_thrown(e);
		}
		
		ByteArrayInputStream input = new ByteArrayInputStream(
				output.toByteArray());
		DataInputStream data_in = new DataInputStream(input);
		for (int j = 0; j < rounds; j++) {
			for (int i = 0; i < v.length; i++) {
				try {
					d[i] = m_enc.decode(data_in, m_pscope);
				} catch (IOException | InvalidEncodingException e) {
					Ensure.never_thrown(e);
				}
			}
		}
		
		long end = System.currentTimeMillis();
		
		try {
			m_enc.decode(data_in, m_pscope);
			fail();
		} catch (EOFException e) {
			/*
			 * Expected.
			 */
		} catch (IOException | InvalidEncodingException e) {
			Ensure.never_thrown(e);
		}
		
		for (int i = 0; i < v.length; i++) {
			assertEquals(v[i], d[i]);
		}
		
		return (end - start) / ((double) v.length * rounds);
	}
}
