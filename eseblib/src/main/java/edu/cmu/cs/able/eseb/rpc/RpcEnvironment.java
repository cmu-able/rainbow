package edu.cmu.cs.able.eseb.rpc;

import incubator.pval.Ensure;

import java.io.Closeable;
import java.io.IOException;

import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Environment used to execute RPC calls. Only one environment needs to be
 * created for each bus connection. The environment is a closeable but it
 * closes independently of the underlying connection.
 */
public class RpcEnvironment implements Closeable {
	/**
	 * The bus connection.
	 */
	private BusConnection m_connection;
	
	/**
	 * Operation information.
	 */
	private OperationInformation m_information;
	
	/**
	 * Execution filter, <code>null</code> if not installed in the connection.
	 */
	private ExecutionResultReadFilter m_exec_filter;
	
	/**
	 * Creates a new environment using the given connection.
	 * @param connection the connection
	 * @throws OperationException failed to create the environment
	 */
	public RpcEnvironment(BusConnection connection) throws OperationException {
		Ensure.not_null(connection);
		m_connection = connection;
		m_information = new OperationInformation(connection.primitive_scope());
		m_exec_filter = new ExecutionResultReadFilter(m_information);
		m_connection.incoming_chain().add_filter(m_exec_filter);
	}
	
	/**
	 * Obtains the operation information used in this environment.
	 * @return the operation information
	 */
	OperationInformation operation_information() {
		return m_information;
	}
	
	/**
	 * Obtains the connection used in this environment.
	 * @return the connection
	 */
	BusConnection connection() {
		return m_connection;
	}

	@Override
	public synchronized void close() throws IOException {
		if (m_exec_filter == null) {
			return;
		}
		
		m_connection.incoming_chain().remove_filter(m_exec_filter);
		m_exec_filter = null;
	}
	
	/**
	 * Sends a request to execute an operation. The environment must not have
	 * been closed.
	 * @param request the request
	 * @return the object from where the execution result can be obtained
	 */
	RemoteExecution execute(DataValue request) {
		Ensure.not_null(request);
		Ensure.is_true(m_information.is_execution_request(request));
		long id = m_information.execution_request_id(request);
		
		synchronized (this) {
			Ensure.not_null(m_exec_filter);
			RemoteExecution re = new RemoteExecution();
			m_exec_filter.add_wait(re, id);
			return re;
		}
	}
}
