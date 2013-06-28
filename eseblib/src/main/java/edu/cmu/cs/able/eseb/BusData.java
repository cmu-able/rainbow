package edu.cmu.cs.able.eseb;

import incubator.pval.Ensure;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Class representing a piece of data in the event bus. It contains a data
 * value and, possibly, its byte encoding.
 */
public class BusData {
	/**
	 * The data value.
	 */
	private DataValue m_value;
	
	/**
	 * The encoding.
	 */
	private byte[] m_encoding;
	
	/**
	 * Creates a new datum.
	 * @param value the value
	 */
	public BusData(DataValue value) {
		Ensure.not_null(value);
		m_value = value;
		m_encoding = null;
	}
	
	/**
	 * Creates a new bus datum with an associated encoding.
	 * @param value the value
	 * @param encoding the encoding
	 * @param size the number of bytes of <em>encoding</em> to consider
	 */
	public BusData(DataValue value, byte[] encoding, int size) {
		Ensure.not_null(value);
		Ensure.not_null(encoding);
		Ensure.greater_equal(size, 0);
		m_value = value;
		
		m_encoding = new byte[size];
		System.arraycopy(encoding, 0, m_encoding, 0, size);
	}
	

	/**
	 * Obtains the data value in this datum.
	 * @return the data value
	 */
	public DataValue value() {
		return m_value;
	}
	
	/**
	 * Obtains the encoding in this datum, if any.
	 * @return the encoding which may be <code>null</code>
	 */
	public byte []encoding() {
		return m_encoding;
	}
}
