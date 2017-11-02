package edu.cmu.cs.able.eseb;

import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;

/**
 * Input stream that reads data values.
 */
public interface DataTypeInputStream extends Closeable {
	/**
	 * Reads a data value from the stream. This method will block until the
	 * data value has been read.
	 * @return the data type read
	 * @throws EOFException the end of the stream has been reached
	 * @throws IOException I/O error; the stream is probably corrupted and
	 * should be discarded
	 * @throws InvalidEncodingException invalid data was read from the
	 * stream
	 */
	BusData read () throws IOException,
		InvalidEncodingException;
	
	/**
	 * Closes the input stream and the underlying input stream.
	 * @throws IOException failed to close the input stream 
	 */
	@Override
	void close () throws IOException;
}
