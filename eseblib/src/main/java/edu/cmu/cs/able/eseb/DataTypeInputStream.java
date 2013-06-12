package edu.cmu.cs.able.eseb;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;

import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;

/**
 * Input stream that reads data types.
 */
public interface DataTypeInputStream extends Closeable {
	/**
	 * Reads a data type from the stream. This method will block until the
	 * data type has been read.
	 * @return the data type read
	 * @throws EOFException the end of the stream has been reached
	 * @throws IOException I/O error; the stream is probably corrupted and
	 * should be discarded
	 * @throws InvalidEncodingException invalid data was read from the
	 * stream
	 */
	public BusData read() throws EOFException, IOException, InvalidEncodingException;
	
	/**
	 * Closes the input stream and the underlying input stream.
	 * @throws IOException failed to close the input stream 
	 */
	@Override
	public void close() throws IOException;
}
