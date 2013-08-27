package edu.cmu.cs.able.eseb.rpc;

import incubator.pval.Ensure;

import java.io.Closeable;
import java.io.IOException;

import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.typelib.jconv.DefaultTypelibJavaConverter;
import edu.cmu.cs.able.typelib.jconv.TypelibJavaConverter;
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
	 * Execution filter.
	 */
	private ExecutionResultReadFilter m_exec_filter;
	
	/**
	 * Request filter.
	 */
	private ExecutionRequestReadFilter m_req_filter;
	
	/**
	 * The typelib/java converter.
	 */
	private TypelibJavaConverter m_jconv;
	
	/**
	 * Creates a new environment using the given connection.
	 * @param connection the connection
	 * @param participant_id the ID of this participant
	 * @throws OperationException failed to create the environment
	 */
	public RpcEnvironment(BusConnection connection, long participant_id)
			throws OperationException {
		Ensure.not_null(connection);
		m_connection = connection;
		m_information = new OperationInformation(connection.primitive_scope());
		m_exec_filter = new ExecutionResultReadFilter(m_information);
		m_req_filter = new ExecutionRequestReadFilter(m_information,
				participant_id, m_connection.outgoing_chain());
		m_connection.incoming_chain().add_filter(m_exec_filter);
		m_connection.incoming_chain().add_filter(m_req_filter);
		m_jconv = DefaultTypelibJavaConverter.make(
				m_connection.primitive_scope());
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
		m_connection.incoming_chain().remove_filter(m_req_filter);
		m_exec_filter = null;
		m_req_filter = null;
	}
	
	/**
	 * Sends a request to execute an operation. The environment must not have
	 * been closed.
	 * @param request the request
	 * @param operation the operation that the request refers to
	 * @return the object from where the execution result can be obtained
	 */
	RemoteExecution execute(DataValue request, DataValue operation) {
		Ensure.not_null(request);
		Ensure.not_null(operation);
		Ensure.not_null(m_connection);
		Ensure.is_true(m_information.is_execution_request(request));
		Ensure.is_true(m_information.is_operation(operation));
		long id = m_information.execution_request_id(request);
		
		RemoteExecution re = null;
		synchronized (this) {
			Ensure.not_null(m_exec_filter);
			re = new RemoteExecution(operation);
			m_exec_filter.add_wait(re, id);
		}
		
		m_connection.send(request);
		return re;
	}
	
	/**
	 * Publishes a service.
	 * @param sor the service to publish
	 */
	synchronized void publish(ServiceObjectRegistration sor) {
		Ensure.not_null(sor);
		Ensure.not_null(m_req_filter);
		m_req_filter.publish(sor);
	}
	
	/**
	 * Unpublishes a service.
	 * @param sor the service to unpublish
	 */
	synchronized void unpublish(ServiceObjectRegistration sor) {
		Ensure.not_null(sor);
		/*
		 * If m_req_filter is null then we're already closed. The SOR should
		 * not be unpublishing if the RPC environment is already closed. This
		 * probably means someone is trying to close stuff in the wrong
		 * sequence.
		 */
		Ensure.not_null(m_req_filter);
		m_req_filter.unpublish(sor);
	}
	
	/**
	 * Obtains the converter between <em>typelib</em> data types and Java data
	 * types. This converter is used for Java RPC calls.
	 * @return the converter
	 */
	public TypelibJavaConverter converter() {
		return m_jconv;
	}
}
