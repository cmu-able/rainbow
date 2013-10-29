package edu.cmu.cs.able.eseb.rpc;

import incubator.pval.Ensure;

import java.util.Map;

import org.apache.commons.lang.math.RandomUtils;

import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Stub for a remote operation. Instances of this class are used to invoke
 * operations remotely.
 */
public class RemoteOperationStub {
	/**
	 * The environment.
	 */
	private RpcEnvironment m_environment;
	
	/**
	 * The destination participant ID.
	 */
	private String m_dst_id;
	
	/**
	 * The remote object ID.
	 */
	private String m_obj_id;
	
	/**
	 * The remote operation.
	 */
	private DataValue m_operation;
	
	/**
	 * Creates a reference to a remote operation. This method does not
	 * execute the operation.
	 * @param environment the RPC environment
	 * @param dst_id the ID of the destination participant
	 * @param operation the operation to execute
	 * @param obj_id the ID of the remote object that will execute the
	 * operation
	 */
	public RemoteOperationStub(RpcEnvironment environment, String dst_id,
			DataValue operation, String obj_id) {
		Ensure.not_null(environment, "environment == null");
		Ensure.not_null(dst_id, "dst_id == null");
		Ensure.not_null(operation, "operation == null");
		Ensure.is_true(environment.operation_information().is_operation(
				operation), "Data value given for operation is not an "
				+ "operation");
		Ensure.not_null(obj_id, "Obj_id == null");
		
		m_dst_id = dst_id;
		m_obj_id = obj_id;
		m_operation = operation;
		m_environment = environment;
	}
	
	/**
	 * Executes the operation with the given arguments which must match the
	 * operation's input parameters.
	 * @param arguments the operation's arguments; if no arguments need to
	 * be provided, <code>null</code> can be used
	 * @return the remote execution object
	 */
	public RemoteExecution execute(Map<String, DataValue> arguments) {
		Ensure.not_null(arguments);
		
		OperationInformation oi = m_environment.operation_information();
		long id = RandomUtils.nextLong();
		DataValue er = oi.create_execution_request(id, m_dst_id, m_obj_id,
				m_operation, arguments);
		
		return m_environment.execute(er, m_operation);
	}
}
