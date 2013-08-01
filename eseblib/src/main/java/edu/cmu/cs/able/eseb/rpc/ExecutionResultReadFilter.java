package edu.cmu.cs.able.eseb.rpc;

import incubator.pval.Ensure;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.cmu.cs.able.eseb.BusData;
import edu.cmu.cs.able.eseb.filter.EventFilter;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Event filter that reads execution results from a bus connection. In
 * general, only one event filter exists per connection.
 */
class ExecutionResultReadFilter extends EventFilter {
	/**
	 * Milliseconds between clearing the {@link #m_pending} map of
	 * garbage-collected references.
	 */
	private static final long MINIMUM_CLEAR_TIME_MS = 1000;
	
	/**
	 * The operation information.
	 */
	private OperationInformation m_information;
	
	/**
	 * Pending objects.
	 */
	private Map<Long, WeakReference<RemoteExecution>> m_pending;
	
	/**
	 * When, in system milliseconds, was the map last cleared.
	 */
	private long m_last_cleared;
	
	/**
	 * Creates a new filter.
	 * @param oinf the operation information
	 */
	ExecutionResultReadFilter(OperationInformation oinf) {
		Ensure.not_null(oinf);
		m_information = oinf;
		m_pending = new HashMap<>();
		m_last_cleared = System.currentTimeMillis();
	}
	
	
	@Override
	public void sink(BusData data) throws IOException {
		Ensure.not_null(data);
		if (!m_information.is_execution_response(data.value())) {
			forward(data);
			return;
		}
		
		long id = m_information.execution_response_id(data.value());
		RemoteExecution re = null;
		synchronized (this) {
			WeakReference<RemoteExecution> wr = m_pending.get(id);
			if (wr != null) {
				re = wr.get();
			}
			
			if (re != null) {
				m_pending.remove(id);
			}
		}
		
		if (re == null) {
			/*
			 * We don't know what execution the received value refers to. It
			 * maybe directed at some other participant or the request may
			 * have been garbage collected already.
			 */
			return;
		}
		
		/*
		 * Inform the remote execution.
		 */
		if (m_information.is_successful_execution(data.value())) {
			Map<String, DataValue> output =
					m_information.execution_response_output_arguments(
					data.value());
			re.done(new RemoteExecutionResult(output));
		} else {
			String type = m_information.execution_response_failure_type(
					data.value());
			String description =
					m_information.execution_response_failure_description(
					data.value());
			String fdata = m_information.execution_response_failure_data(
					data.value());
			re.done(new RemoteExecutionResult(new FailureInformation(type,
					description, fdata)));
		}
		
		/*
		 * Clear the map if it is time to do so.
		 */
		synchronized (this) {
			long now = System.currentTimeMillis();
			if (m_last_cleared < now - MINIMUM_CLEAR_TIME_MS) {
				for (Iterator<Map.Entry<Long,
						WeakReference<RemoteExecution>>> it =
						m_pending.entrySet().iterator(); it.hasNext(); ) {
					if (it.next().getValue().get() == null) {
						it.remove();
					}
				}
			}
			
			m_last_cleared = now;
		}
	}
	
	/**
	 * Adds a new remote execution whose response we want to wait for.
	 * @param re the remote execution 
	 * @param id the participant ID
	 */
	synchronized void add_wait(RemoteExecution re, long id) {
		Ensure.not_null(re);
		Ensure.is_false(m_pending.containsKey(id));
		m_pending.put(id, new WeakReference<>(re));
	}
}
