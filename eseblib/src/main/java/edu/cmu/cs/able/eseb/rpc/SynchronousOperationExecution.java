package edu.cmu.cs.able.eseb.rpc;

import java.util.Map;

import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Class that supports synchronous operation execution.
 */
public class SynchronousOperationExecution extends OperationExecution {
	/**
	 * Creates a request to execute an operation. This method does not
	 * execute the operation.
	 * @param connection the bus connection
	 * @param op_inf access to operation data types
	 * @param dst_id the ID of the destination participant
	 * @param operation the operation to execute
	 * @param arguments the input arguments for the operation; these must
	 * match the operation's definition
	 * @param time_out_ms the operation timeout in milliseconds;
	 * <code>0</code> means no timeout
	 * @param obj_id the ID of the remote object that will execute the
	 * operation
	 */
	public SynchronousOperationExecution(BusConnection connection,
			OperationInformation op_inf, long dst_id, DataValue operation,
			Map<String, DataValue> arguments, long time_out_ms, long obj_id) {
		super(connection, op_inf, dst_id, operation, arguments, time_out_ms,
				obj_id);
	}
	
	/**
	 * Executes this operation and waits for completion or timeout (if a
	 * timeout was defined during construction).
	 * @return the result of execution, maps output parameters names to their
	 * values
	 * @throws OperationTimedOutException execution timed out
	 * @throws OperationFailureException execution has failed
	 */
	public Map<String, DataValue> execute_synchronous()
			throws OperationTimedOutException, OperationFailureException {
		return null;
	}
}
