package edu.cmu.cs.able.eseb;

import incubator.pval.Ensure;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import edu.cmu.cs.able.typelib.enc.DataValueEncoding;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Implementation of a data type output stream.
 */
public class DataTypeOutputStreamImpl implements DataTypeOutputStream {
	/**
	 * The stream to write types to.
	 */
	private DataOutputStream m_dos;
	
	/**
	 * Encoding to use.
	 */
	private DataValueEncoding m_encoding;
	
	/**
	 * Creates a new output stream.
	 * @param os the output stream to write data
	 * @param enc the encoding to use
	 */
	public DataTypeOutputStreamImpl(OutputStream os, DataValueEncoding enc) {
		Ensure.not_null(os, "os == null");
		Ensure.not_null(enc, "enc == null");
		
		m_dos = new DataOutputStream(os);
		m_encoding = enc;
	}
	
	@Override
	public void write(DataValue dt) throws IOException {
		Ensure.not_null(dt, "dt == null");
		write(new BusData(dt));
	}
	
	@Override
	public void write(BusData bd) throws IOException {
		Ensure.not_null(bd, "bd == null");
		Ensure.not_null(m_dos, "Stream is already closed");
		
		byte[] bytes = bd.encoding();
		if (bytes == null) {
			ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
			try (DataOutputStream dataOut = new DataOutputStream(bytesOut)) {
				m_encoding.encode(bd.value(), dataOut);
			}
			
			bytes = bytesOut.toByteArray();
		}

		m_dos.writeInt(bytes.length);
		m_dos.write(bytes);
	}

	@Override
	public void close() throws IOException {
		if (m_dos == null) {
			return;
		}
		
		DataOutputStream dos = m_dos;
		m_dos = null;
		dos.close();
	}
}
