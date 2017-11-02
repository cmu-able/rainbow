package edu.cmu.cs.able.eseb.filter;

import java.io.IOException;

import incubator.pval.Ensure;
import edu.cmu.cs.able.eseb.BusData;
import edu.cmu.cs.able.eseb.DataTypeSocketConnection;

/**
 * Sink that sends all events to a {@link DataTypeSocketConnection}.
 */
public class DataTypeSocketConnectionSink implements EventSink {
	/**
	 * The connection to send data to.
	 */
	private DataTypeSocketConnection m_dts;
	
	/**
	 * Creates a new sink.
	 * @param c the connection to send data to
	 */
	public DataTypeSocketConnectionSink(DataTypeSocketConnection c) {
		Ensure.not_null(c);
		m_dts = c;
	}

	@Override
	public void sink(BusData data) throws IOException {
		Ensure.not_null(data);
		m_dts.write(data);
	}
}
