package edu.cmu.cs.able.eseb.rpc;

import java.util.Map;

import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * The operation registry represents a "server". An operation registry
 * receives incoming requests to execute operations and forwards them to
 * {@link ServiceObjectRegistration} objects which handle registered objects.
 */
public class OperationRegistry {
	/**
	 * Creates a new registry associated with the given connection.
	 * @param connection the connection
	 * @param op_info access to operation data types
	 * @param group the operations supported by this registry
	 * @param executer the object invoked to perform the execution of requested
	 * operations
	 * @param obj_id the ID of the object to invoke
	 */
	public OperationRegistry(BusConnection connection,
			OperationInformation op_info, DataValue group,
			ServiceOperationExecuter executer, long obj_id) {
	}
	
	/**
	 * Installs the registry, making published objects available remotely.
	 */
	public void install() {
	}
	
	/**
	 * Uninstalls the registry, making objects no longer available.
	 * The result of executions that are running will not be sent to the
	 * invokers. 
	 */
	public void uninstall() {
	}
	
	/**
	 * Checks whether the registry is installed.
	 * @return is installed?
	 */
	public boolean installed() {
		return false;
	}
	
	/**
	 * Invoked by the operation executer when an operation execution has
	 * finished successfully.
	 * @param id the operation ID (given to the executer)
	 * @param outputs the result of the execution which are the values to the
	 * output parameters 
	 */
	public void execution_succeeded(long id, Map<String, DataValue> outputs) {
	}
	
	/**
	 * Invoked when execution of an operation has failed.
	 * @param id the operation ID (given to the executer) 
	 * @param type the type of failure
	 * @param description the description of the failure
	 * @param data failure data
	 */
	public void execution_failed(long id, String type, String description,
			String data) {
	}
}
