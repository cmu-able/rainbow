package edu.cmu.cs.able.eseb.rpc;

import incubator.pval.Ensure;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.cs.able.eseb.BusData;
import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.eseb.filter.EventFilter;

/**
 * Event filter that reads execution results from a bus connection. Only
 * one filter exists in each connection. Readers can be added or removed
 * using the {@link #add_request_reader(BusConnection, ExecutionResultReader)}
 * and {@link #remove_request_reader(BusConnection, ExecutionResultReader)}
 * methods.
 */
class ExecutionResultReadFilter extends EventFilter {
	/**
	 * The filter's operation information.
	 */
	private OperationInformation m_information;
	
	/**
	 * Readers registered with this filter.
	 */
	private Set<ExecutionResultReader> m_readers;
	
	/**
	 * Creates a new filter with no readers.
	 * @param information the operation information
	 */
	private ExecutionResultReadFilter(OperationInformation information) {
		Ensure.not_null(information, "information == null");
		
		m_information = information;
		m_readers = new HashSet<>();
	}
	
	/**
	 * Adds a new reader to the filter.
	 * @param reader the reader
	 */
	private synchronized void add_reader(ExecutionResultReader reader) {
		Ensure.not_null(reader, "reader == null");
		Ensure.is_false(m_readers.contains(reader), "reader is already in the "
				+ "filter");
		m_readers.add(reader);
	}
	
	/**
	 * Removes a reader from the filter.
	 * @param reader the reader
	 */
	private synchronized void remove_reader(ExecutionResultReader reader) {
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
		if (!m_information.is_execution_response(data.value())) {
			forward(data);
			return;
		}
		
		/*
		 * Check all readers to see if any handles the data.
		 */
		Set<ExecutionResultReader> rdrs;
		synchronized (this) {
			rdrs = new HashSet<>(m_readers);
		}
		
		for (ExecutionResultReader r : rdrs) {
			if (r.handles(data)) {
				break;
			}
		}
	}
	
	/**
	 * Adds a new result reader to a bus connection. This will create a
	 * filter, if necessary, and will add the reader to the filter.
	 * @param connection the bus connection
	 * @param reader the reader
	 */
	synchronized static void add_request_reader(BusConnection connection,
			ExecutionResultReader reader) {
		Ensure.not_null(connection, "connection == null");
		Ensure.not_null(reader, "reader == null");
		
		/*
		 * Try to find an existing filter to add the reader to.
		 */
		ExecutionResultReadFilter f = null;
		List<EventFilter> filters = connection.incoming_chain().filters();
		for (EventFilter ff : filters) {
			if (ff instanceof ExecutionResultReadFilter) {
				f = (ExecutionResultReadFilter) ff;
				break;
			}
		}
		
		/*
		 * If there is no filter, create one.
		 */
		if (f == null) {
			f = new ExecutionResultReadFilter(new OperationInformation(
					connection.primitive_scope()));
			connection.incoming_chain().add_filter(f);
		}
		
		f.add_reader(reader);
	}
	
	/**
	 * Removes a result reader from a bus connection. This will remove the
	 * filter the reader is registered if there are no more readers in the
	 * filter.
	 * @param connection the connection
	 * @param reader the reader
	 */
	synchronized static void remove_request_reader(BusConnection connection,
			ExecutionResultReader reader) {
		Ensure.not_null(connection, "connection == null");
		Ensure.not_null(reader, "reader == null");
		
		/*
		 * Try to find an existing filter to remove the reader to.
		 */
		ExecutionResultReadFilter f = null;
		List<EventFilter> filters = connection.incoming_chain().filters();
		for (EventFilter ff : filters) {
			if (ff instanceof ExecutionResultReadFilter) {
				f = (ExecutionResultReadFilter) ff;
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
