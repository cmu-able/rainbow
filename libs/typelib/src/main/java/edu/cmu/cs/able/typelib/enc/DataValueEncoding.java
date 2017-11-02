package edu.cmu.cs.able.typelib.enc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import edu.cmu.cs.able.typelib.type.DataTypeScope;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * A data value encoding is a class capable of encoding and decoding data
 * values into binary arrays of data. Data values are decoded in the context
 * of a data type scope which is used to find the types to create the instances
 * required for instantiation.
 */
public interface DataValueEncoding {
	/**
	 * Encodes a data value.
	 * @param os the stream to encode data into
	 * @param value the data value to encode (cannot be <code>null</code>)
	 * @throws IOException failed to encode write in the stream
	 */
	void encode(DataValue value, DataOutputStream os) throws IOException;
	
	/**
	 * Decodes a data value.
	 * @param is the stream to read data from
	 * @param scope the scope used to find data types
	 * @return the decoded data value
	 * @throws IOException failed to read data from the stream;
	 * <code>EOFException</code> indicates the end of file was reached and
	 * no data was read (if data has been partially read, then
	 * <code>InvalidEncodingException</code> is thrown
	 * @throws InvalidEncodingException the data cannot be decoded 
	 */
	DataValue decode(DataInputStream is, DataTypeScope scope)
			throws IOException, InvalidEncodingException;
}
