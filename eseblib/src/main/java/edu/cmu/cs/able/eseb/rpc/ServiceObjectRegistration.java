package edu.cmu.cs.able.eseb.rpc;

import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * A service object registration allows an object to be published and be made
 * accessible remotely. When registering an object, an ID is provided which
 * is used to identify the object remotely.
 */
public class ServiceObjectRegistration {
	/**
	 * Creates a new registration.
	 * @param op_info access to operation data types
	 * @param group the operations supported by this object
	 * @param executer the object invoked to execute the operations
	 * @param obj_id the ID of the object to invoke
	 */
	public ServiceObjectRegistration(OperationInformation op_info,
			DataValue group, ServiceOperationExecuter executer, long obj_id) {
	}
	
	/**
	 * Obtains the object ID.
	 * @return the object ID
	 */
	public long object_id() {
		return 0;
	}
	
	/**
	 * Publishes this object in the given registry, making it available
	 * remotely if the registry is installed.
	 * @param r the registry
	 */
	public void publish(OperationRegistry r) {
	}
	
	/**
	 * Removes this object from the given registry, making it no longer
	 * available remotely if the registry was installed.
	 * @param r the registry
	 */
	public void unpublish(OperationRegistry r) {
	}
	
	/**
	 * Checks if the object is published.
	 * @return is it published
	 */
	public boolean published() {
		return false;
	}
}
