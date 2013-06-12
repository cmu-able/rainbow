package edu.cmu.cs.able.typelib.txtenc;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Interface for encodings that convert between data values and their textual
 * representation. This interface is used by {@link TextEncoding} to provide
 * support for data type encoding.
 */
public interface DelegateTextEncoding {
	/**
	 * Checks whether the text encoding supports the given data type.
	 * @param t the data type
	 * @return is the data type supported by this encoding?
	 */
	boolean supports(DataType t);
	
	/**
	 * Encodes a data value.
	 * @param v the value
	 * @param w the writer where to write the encoded text
	 * @param enc the text encoding being used
	 * @throws IOException failed to write the value
	 */
	void encode(DataValue v, Writer w, TextEncoding enc) throws IOException;
	
	/**
	 * Decodes a data value from a text stream.
	 * @param r the reader to read characters from; note that more characters
	 * may exist after decoding in the reader as multiple values may be
	 * encoded in the same reader
	 * @param type the data type to decode
	 * @param dts the data type scope
	 * @param enc the text encoding being used 
	 * @return the decoded data value
	 * @throws IOException failed to read from the reader
	 * @throws InvalidEncodingException failed to decode the character stream
	 */
	DataValue decode(Reader r, DataType type, DataTypeScope dts,
			TextEncoding enc) throws IOException, InvalidEncodingException;
}
