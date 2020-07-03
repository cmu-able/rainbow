package edu.cmu.cs.able.eseb.rpc;

import incubator.pval.Ensure;

import java.io.Closeable;
import java.io.IOException;

import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.eseb.participant.ParticipantIdentifier;
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
	private ExecutionResultReader m_exec_reader;
	
	/**
	 * Request filter.
	 */
	private ExecutionRequestReader m_req_reader;
	
	/**
	 * The typelib/java converter.
	 */
	private TypelibJavaConverter m_jconv;
	
	/**
	 * The participant ID.
	 */
	private String m_participant_id;
	
	/**
	 * Creates a new environment using the given connection.
	 * @param connection the connection
	 * @param participant_id the ID of this participant; this value is
	 * usually obtained using a {@link ParticipantIdentifier} but this
	 * is not strictly necessary
	 * @throws OperationException failed to create the environment
	 */
	public RpcEnvironment(BusConnection connection, String participant_id)
			throws OperationException {
		Ensure.not_null(connection, "connection == null");
		Ensure.not_null(participant_id, "participant_id == null");
		
		m_connection = connection;
		m_participant_id = participant_id;
		m_information = new OperationInformation(connection.primitive_scope());
		m_exec_reader = new ExecutionResultReader(m_information);
		ExecutionResultReadFilter.add_request_reader(m_connection,
				m_exec_reader);
		m_req_reader = new ExecutionRequestReader(m_information,
				m_participant_id);
		ExecutionRequestReadFilter.add_request_reader(m_connection,
				m_req_reader);
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
		if (m_exec_reader == null) {
			return;
		}
		
		ExecutionResultReadFilter.remove_request_reader(m_connection,
				m_exec_reader);
		ExecutionRequestReadFilter.remove_request_reader(m_connection,
				m_req_reader);
		m_exec_reader = null;
		m_req_reader = null;
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
			Ensure.not_null(m_exec_reader);
			re = new RemoteExecution(operation);
			m_exec_reader.add_wait(re, id);
		}
		
		m_connection.send(request);
		return re;
	}
	
	/**
	 * Publishes a service.
	 * @param sor the service to publish
	 */
	synchronized void publish(ServiceObjectRegistration sor) {
		Ensure.not_null(sor, "sor == null");
		Ensure.not_null(m_req_reader, "m_req_reader == null");
		m_req_reader.publish(sor);
	}
	
	/**
	 * Unpublishes a service.
	 * @param sor the service to unpublish
	 */
	synchronized void unpublish(ServiceObjectRegistration sor) {
		Ensure.not_null(sor, "sor == null");
		/*
		 * If m_req_filter is null then we're already closed. The SOR should
		 * not be unpublishing if the RPC environment is already closed. This
		 * probably means someone is trying to close stuff in the wrong
		 * sequence.
		 */
		Ensure.not_null(m_req_reader, "m_req_reader == null");
		m_req_reader.unpublish(sor);
	}
	
	/**
	 * Obtains the converter between <em>typelib</em> data types and Java data
	 * types. This converter is used for Java RPC calls.
	 * @return the converter
	 */
	public TypelibJavaConverter converter() {
		return m_jconv;
	}
	
	/**
	 * Obtains the participant ID that was used to create this environment.
	 * @return the participant ID
	 */
	public String participant_id() {
		return m_participant_id;
	}
}
