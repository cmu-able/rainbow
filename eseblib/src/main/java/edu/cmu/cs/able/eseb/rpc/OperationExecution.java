package edu.cmu.cs.able.eseb.rpc;

import incubator.dispatch.Dispatcher;

import java.util.Map;

import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Operation that represents a request to execute an operation. This class
 * will silently add a filter to the connection to 
 */
public class OperationExecution {
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
	public OperationExecution(BusConnection connection,
			OperationInformation op_inf, long dst_id, DataValue operation,
			Map<String, DataValue> arguments, long time_out_ms, long obj_id) {
	}
	
	/**
	 * Obtains the execution listener dispatcher.
	 * @return the dispatcher
	 */
	public Dispatcher<OperationExecutionListener> dispatcher() {
		return null;
	}
	
	/**
	 * Executes the operation. The operation must not have been started before
	 * (see {@link #state()}). This method returns immediately. The
	 * {@link #state()} method can be used to poll for completion.
	 * Additionally, a {@link OperationExecutionListener} can be added
	 * using the {@link #dispatcher()} method. If adding a listener, then the
	 * listener should be added <em>before</em> execution to avoid a race
	 * condition.
	 */
	public void execute() {
	}
	
	/**
	 * Obtains the state of the execution.
	 * @return the state
	 */
	public ExecutionState state() {
		return null;
	}
	
	/**
	 * If the execution has completed (see {@link #state()}), checks how it
	 * completed.
	 * @return the type of operation completion
	 */
	public OperationCompletion execution_result() {
		return null;
	}
	
	/**
	 * Obtains the result of evaluating the execution. This method may only
	 * be invoked if execution was successful (see
	 * {@link #execution_result()}).
	 * @return evaluation result
	 */
	public Map<String, DataValue> result() {
		return null;
	}
	
	/**
	 * Obtains the execution failure type. This method may only be called if
	 * the execution was unsuccessful (see {@link #execution_result()}).
	 * @return the failure type
	 */
	public String failure_type() {
		return null;
	}
	
	/**
	 * Obtains the execution failure description. This method may only be
	 * called if the execution was unsuccessful
	 * {@link #execution_result()}).
	 * @return the failure description
	 */
	public String failure_description() {
		return null;
	}
	
	/**
	 * Obtains the execution failure data. This method may only be
	 * called if the execution was unsuccessful (see
	 * {@link #execution_result()}).
	 * @return the failure data
	 */
	public String failure_data() {
		return null;
	}
}
