package edu.cmu.cs.able.eseb.rpc;

import incubator.pval.Ensure;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.cs.able.eseb.BusData;
import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.eseb.filter.EventFilter;
import edu.cmu.cs.able.eseb.filter.EventSink;

/**
 * Filter that reads requests to execute operations and removes them from
 * the event chain. A filter instance of this class is placed in every
 * connection that is used by an {@link RpcEnvironment}.
 */
class ExecutionRequestReadFilter extends EventFilter {
	/**
	 * The filter's operation information.
	 */
	private OperationInformation m_information;
	
	/**
	 * Where to send the responses to requests.
	 */
	private EventSink m_response_sink;
	
	/**
	 * Readers registered with this filter.
	 */
	private Set<ExecutionRequestReader> m_readers;
	
	/**
	 * Creates a new filter with no readers.
	 * @param information the operation information
	 * @param sink the sink where the filter should send outgoing data
	 */
	private ExecutionRequestReadFilter(OperationInformation information,
			EventSink sink) {
		Ensure.not_null(information, "information == null");
		Ensure.not_null(sink, "sink == null");
		
		m_information = information;
		m_response_sink = sink;
		m_readers = new HashSet<>();
	}
	
	/**
	 * Adds a new reader to the filter.
	 * @param reader the reader
	 */
	private synchronized void add_reader(ExecutionRequestReader reader) {
		Ensure.not_null(reader, "reader == null");
		Ensure.is_false(m_readers.contains(reader), "reader is already in the "
				+ "filter");
		reader.response_sink(m_response_sink);
		m_readers.add(reader);
	}
	
	/**
	 * Removes a reader from the filter.
	 * @param reader the reader
	 */
	private synchronized void remove_reader(ExecutionRequestReader reader) {
		Ensure.not_null(reader, "reader == null");
		Ensure.is_true(m_readers.contains(reader), "reader is not in the "
				+ "filter");
		m_readers.remove(reader);
	}
	
	/**
	 * Obtains the number of readers in the filter.
	 * @return the number of readers
	 */
	private synchronized int reader_count() {
		return m_readers.size();
	}
	
	@Override
	public void sink(final BusData data) throws IOException {
		Ensure.not_null(data, "data == null");
		
		/*
		 * If the data is not an execution request, ignore.
		 */
		if (!m_information.is_execution_request(data.value())) {
			forward(data);
			return;
		}
		
		/*
		 * Check all readers to see if any handles the data.
		 */
		Set<ExecutionRequestReader> rdrs;
		synchronized (this) {
			rdrs = new HashSet<>(m_readers);
		}
		
		for (ExecutionRequestReader r : rdrs) {
			if (r.handles(data)) {
				break;
			}
		}
	}
	
	/**
	 * Adds a new request reader to a bus connection. This will create a
	 * filter, if necessary, and will add the reader to the filter.
	 * @param connection the bus connection
	 * @param reader the reader
	 */
	synchronized static void add_request_reader(BusConnection connection,
			ExecutionRequestReader reader) {
		Ensure.not_null(connection, "connection == null");
		Ensure.not_null(reader, "reader == null");
		
		/*
		 * Try to find an existing filter to add the reader to.
		 */
		ExecutionRequestReadFilter f = null;
		List<EventFilter> filters = connection.incoming_chain().filters();
		for (EventFilter ff : filters) {
			if (ff instanceof ExecutionRequestReadFilter) {
				f = (ExecutionRequestReadFilter) ff;
				break;
			}
		}
		
		/*
		 * If there is no filter, create one.
		 */
		if (f == null) {
			f = new ExecutionRequestReadFilter(new OperationInformation(
					connection.primitive_scope()), connection.outgoing_chain());
			connection.incoming_chain().add_filter(f);
		}
		
		f.add_reader(reader);
	}
	
	/**
	 * Removes a request reader from a bus connection. This will remove the
	 * filter the reader is registered if there are no more readers in the
	 * filter.
	 * @param connection the connection
	 * @param reader the reader
	 */
	synchronized static void remove_request_reader(BusConnection connection,
			ExecutionRequestReader reader) {
		Ensure.not_null(connection, "connection == null");
		Ensure.not_null(reader, "reader == null");
		
		/*
		 * Try to find an existing filter to remove the reader to.
		 */
		ExecutionRequestReadFilter f = null;
		List<EventFilter> filters = connection.incoming_chain().filters();
		for (EventFilter ff : filters) {
			if (ff instanceof ExecutionRequestReadFilter) {
				f = (ExecutionRequestReadFilter) ff;
				break;
			}
		}
		
		if (f == null) {
			Ensure.unreachable("No filter found meaning no reader had "
					+ "been added");
			return;
		}
		
		f.remove_reader(reader);
		
		if (f.reader_count() == 0) {
			connection.incoming_chain().remove_filter(f);
		}
	}
}
