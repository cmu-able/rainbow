package edu.cmu.cs.able.eseb.rpc;

import edu.cmu.cs.able.eseb.conn.BusConnection;

/**
 * Class that provides support for a remote RPC interface. This class provides
 * a way to make a Java interface available remotely and to create a stub to
 * invoke it remotely.
 */
public class RemoteRpcFactory {
	/**
	 * Creates a stub to execute remote operations.
	 * @param t_class the interface used to execute the remote operations;
	 * this interface must match the one used to create the registry wrapper
	 * with the
	 * {@link #create_registry_wrapper(Class, Object, BusConnection, OperationInformation, long)}
	 * method; all methods in this interface must throw
	 * {@link OperationException} or some super class
	 * @param connection the bus connection
	 * @param op_inf information on operation data types
	 * @param dst_id the ID of the participant that will be invoked
	 * @param time_out_ms timeout, in milliseconds, for operation executions;
	 * <code>0</code> means no timeout
	 * @param obj_id the ID of the remote object to invoke the operation on
	 * @return the remote execution stub
	 */
	public <T> T create_remote_stub(Class<T> t_class, BusConnection connection,
			OperationInformation op_inf, long dst_id, long time_out_ms,
			long obj_id) {
		return null;
	}
	
	/**
	 * Registers an interface as a registry, effectively allowing it to be
	 * invoked remotely.
	 * @param t_class the type of the interface
	 * @param t the interface itself which will be invoked
	 * @param connection the event bus connection where execution requests
	 * will be come from
	 * @param op_inf information on the operation types
	 * @param obj_id the ID of the object to register
	 */
	public <T> void create_registry_wrapper(Class<T> t_class, T t,
			BusConnection connection, OperationInformation op_inf,
			long obj_id) {
	}
}
