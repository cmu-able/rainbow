package edu.cmu.cs.able.eseb.rpc;

import java.util.Map;

import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Interface implemented by the objects responsible for executing an operation
 * in an {@link OperationRegistry}.
 */
public interface OperationExecuter {
	/**
	 * Invoked to execute an operation. When finished, the executer should
	 * inform the registry of the result of executing the operation.
	 * @param id the operation ID
	 * @param operation the operation to be executed
	 * @param input_arguments the input arguments to the operation
	 * @param reg the registry which should be informed of the operation
	 * execution result
	 */
	public void execute(long id, DataValue operation,
			Map<String, DataValue> input_arguments, OperationRegistry reg);
}
