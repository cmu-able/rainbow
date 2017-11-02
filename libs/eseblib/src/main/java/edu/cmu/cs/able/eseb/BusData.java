package edu.cmu.cs.able.eseb;

import org.apache.commons.lang.ArrayUtils;

import incubator.pval.Ensure;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Class representing a piece of data in the event bus. It contains a data
 * value and its byte encoding. Either the data value or the byte encoding may
 * be <code>null</code> but not both. A <code>null</code> byte encoding means
 * that the data value has not been encoded yet and its byte representation
 * is not known. A <code>null</code> data value means that the byte
 * representation could not be decoded successfully. In the case the
 * representation failed to be decoded, an exception describing the failure
 * is provided.
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
	 * Failure to decode the data.
	 */
	private Exception m_decoding_failure;
	
	/**
	 * Creates a new datum.
	 * @param value the value
	 */
	public BusData(DataValue value) {
		Ensure.not_null(value);
		m_value = value;
		m_encoding = null;
		m_decoding_failure = null;
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
		m_encoding = ArrayUtils.subarray(encoding, 0, size);
		m_decoding_failure = null;
	}
	
	/**
	 * Creates a new datum with a byte representation which could not be
	 * decoded.
	 * @param encoding the byte representation
	 * @param size the number of bytes of <em>encoding</em> to consider
	 * @param decoding_failure why has decoding failed
	 */
	public BusData(byte[] encoding, int size, Exception decoding_failure) {
		Ensure.not_null(encoding);
		Ensure.greater_equal(size, 0);
		Ensure.not_null(decoding_failure);
		m_value = null;
		m_encoding = ArrayUtils.subarray(encoding, 0, size);
		m_decoding_failure = decoding_failure;
	}

	/**
	 * Obtains the data value in this datum.
	 * @return the data value, which may be <code>null</code>
	 */
	public DataValue value() {
		return m_value;
	}
	
	/**
	 * Obtains the encoding in this datum, if any.
	 * @return the encoding, which may be <code>null</code>
	 */
	public byte []encoding() {
		return m_encoding;
	}
	
	/**
	 * Obtains the reason decoding has failed.
	 * @return the exception or <code>null</code> if decoding hasn't failed;
	 * <code>null</code> is also returned if no decoding has been done
	 */
	public Exception decoding_failure() {
		return m_decoding_failure;
	}
}
