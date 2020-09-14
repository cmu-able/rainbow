package edu.cmu.cs.able.eseb.rpc;

import incubator.pval.Ensure;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.cmu.cs.able.eseb.BusData;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Reader that reads execution results from a bus connection. When a request
 * to execute a remote RPC is sent, an instance of this class is informed
 * that a result is being waited for (see 
 * {@link #add_wait(RemoteExecution, long)}).
 */
class ExecutionResultReader {
	/**
	 * Milliseconds between clearing the {@link #m_pending} map of
	 * garbage-collected references.
	 */
	static final long MINIMUM_CLEAR_TIME_MS = 1000;
	
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
	 * Creates a new reader.
	 * @param oinf the operation information
	 */
	ExecutionResultReader(OperationInformation oinf) {
		Ensure.not_null(oinf, "oinf == null");
		m_information = oinf;
		m_pending = new HashMap<>();
		m_last_cleared = System.currentTimeMillis();
	}
	
	/**
	 * Handles data from the bus which is known to be an execution response.
	 * @param data the data
	 * @return has this data been handled by the reader?
	 * @throws IOException failed to process the data
	 */
	boolean handles(BusData data) throws IOException {
		Ensure.not_null(data, "data == null");
		Ensure.is_true(m_information.is_execution_response(data.value()),
				"data is not an execution response");
		
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
			return false;
		}
		
		/*
		 * Inform the remote execution.
		 */
		if (m_information.is_successful_execution(data.value())) {
			Map<String, DataValue> output =
					m_information.execution_response_output_arguments(
					data.value());
			
			/*
			 * Check that all return parameters are correct and that they have
			 * the right type.
			 */
			DataValue operation = re.operation();
			Ensure.is_true(m_information.is_operation(operation));
			
			FailureInformation fi = null;
			
			Set<String> out_params = m_information.parameters(operation);
			for (Iterator<String> it = out_params.iterator(); it.hasNext(); ) {
				String p = it.next();
				if (m_information.parameter_direction(operation, p)
						!= ParameterDirection.OUTPUT) {
					it.remove();
					continue;
				}
				
				if (!output.containsKey(p)) {
					fi = new FailureInformation("Missing output parameter",
							"Output parameter '" + p + "' was not "
							+ "provided by remote service.", "");
					break;
				}
				
				DataType type = m_information.parameter_type(operation, p);
				if (!type.is_instance(output.get(p))) {
					fi = new FailureInformation("Invalid output parameter type",
							"Output parameter '" + p + "' has type '"
							+ type.name() + "' but server sent value with "
							+ "type '" + output.get(p).type().name()
							+ "'.", "");
					break;
				}
			}
			
			if (out_params.size() != output.size()) {
				fi = new FailureInformation("Extra output parameters",
						"Unexpected output parameters received from service.",
						"");
			}
			
			if (fi == null) {
				re.done(new RemoteExecutionResult(output));
			} else {
				re.done(new RemoteExecutionResult(fi));
			}
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
				
				m_last_cleared = now;
			}
		}
		
		return true;
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
