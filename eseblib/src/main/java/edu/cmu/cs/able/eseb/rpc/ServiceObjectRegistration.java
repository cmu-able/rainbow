package edu.cmu.cs.able.eseb.rpc;

import incubator.pval.Ensure;

import java.io.Closeable;
import java.io.IOException;

import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * A service object registration allows an object to be published and be made
 * accessible remotely. When registering an object, an ID is provided which
 * is used to identify the object remotely. When the service object is closed
 * the service is made unavailable for remote invokers.
 */
public class ServiceObjectRegistration implements Closeable {
	/**
	 * The object that will actually do the operations.
	 */
	private ServiceOperationExecuter m_executer;
	
	/**
	 * The operations supported by the executer.
	 */
	private DataValue m_group;
	
	/**
	 * The object ID.
	 */
	private String m_obj_id;
	
	/**
	 * The environment, <code>null</code> if the registration closed.
	 */
	private RpcEnvironment m_environment;
	
	/**
	 * Creates a new registration.
	 * @param group the operations supported by this object
	 * @param executer the object invoked to execute the operations
	 * @param obj_id the ID of the object to invoke
	 * @param environment the environment where the service is available
	 */
	private ServiceObjectRegistration(ServiceOperationExecuter executer,
			DataValue group, String obj_id, RpcEnvironment environment) {
		Ensure.not_null(executer);
		Ensure.not_null(group);
		Ensure.not_null(obj_id);
		Ensure.not_null(environment);
		
		m_executer = executer;
		m_group = group;
		m_obj_id = obj_id;
		m_environment = environment;
	}
	
	/**
	 * Creates a new registration.
	 * @param group the operations supported by this object
	 * @param executer the object invoked to execute the operations
	 * @param obj_id the ID of the object to invoke
	 * @param environment the environment where the service is available
	 * @return the registration
	 */
	public static ServiceObjectRegistration make(
			ServiceOperationExecuter executer, DataValue group, String obj_id,
			RpcEnvironment environment) {
		Ensure.not_null(executer);
		Ensure.not_null(group);
		Ensure.not_null(obj_id);
		Ensure.not_null(environment);
		
		ServiceObjectRegistration s = new ServiceObjectRegistration(executer,
				group, obj_id, environment);
		environment.publish(s);
		return s;
	}
	
	/**
	 * Obtains the object ID.
	 * @return the object ID
	 */
	public String object_id() {
		return m_obj_id;
	}
	
	/**
	 * Obtains the executer that will execute the service.
	 * @return the executer
	 */
	public ServiceOperationExecuter executer() {
		return m_executer;
	}
	
	/**
	 * Obtains the operation group supported by this operation executer.
	 * @return the operation group
	 */
	public DataValue group() {
		return m_group;
	}
	
	@Override
	public synchronized void close() throws IOException {
		if (m_environment == null) {
			return;
		}
		
		m_environment.unpublish(this);
		m_environment = null;
	}
}
