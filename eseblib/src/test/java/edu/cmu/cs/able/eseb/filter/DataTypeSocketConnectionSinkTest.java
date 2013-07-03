package edu.cmu.cs.able.eseb.filter;

import incubator.dispatch.Dispatcher;
import incubator.wt.CloseableListener;
import incubator.wt.WorkerThreadGroupCI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.cmu.cs.able.eseb.BusData;
import edu.cmu.cs.able.eseb.BusDataQueueGroup;
import edu.cmu.cs.able.eseb.DataTypeSocketConnection;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Test case that tests the data type socket connection.
 */
@SuppressWarnings("javadoc")
public class DataTypeSocketConnectionSinkTest extends FilterTestCase {
	/**
	 * The data type connection.
	 */
	private DataTypeSocketConnection m_dts;
	
	/**
	 * Data written to the socket connection.
	 */
	private List<BusData> m_written;
	
	/**
	 * The sink.
	 */
	private DataTypeSocketConnectionSink m_sink;
	
	@Before
	public void set_up() throws Exception {
		m_written = new ArrayList<>();
		m_dts = new DataTypeSocketConnection() {
			@Override
			public void write(BusData bd) throws IOException {
				m_written.add(bd);
			}
			
			@Override
			public void write(DataValue dt) throws IOException {
				fail();
			}
			
			@Override
			public WorkerThreadGroupCI thread_group() {
				fail();
				return null;
			}
			
			@Override
			public void stop() {
				fail();
			}
			
			@Override
			public void start() {
				fail();
			}
			
			@Override
			public BusDataQueueGroup queue_group() {
				fail();
				return null;
			}
			
			@Override
			public Dispatcher<CloseableListener> closeable_dispatcher() {
				fail();
				return null;
			}
			
			@Override
			public void close() throws IOException {
				fail();
			}
		};
		
		m_sink = new DataTypeSocketConnectionSink(m_dts);
	}
	
	@Test
	public void sink_data_is_written_to_connection() throws Exception {
		m_sink.sink(bus_data());
		assertEquals(1, m_written.size());
	}
	
	@Test(expected = AssertionError.class)
	public void cannot_sink_null_data() throws Exception {
		m_sink.sink(null);
	}
	
	@Test(expected = AssertionError.class)
	@SuppressWarnings("unused")
	public void cannot_create_with_null_connection() throws Exception {
		new DataTypeSocketConnectionSink(null);
	}
}
