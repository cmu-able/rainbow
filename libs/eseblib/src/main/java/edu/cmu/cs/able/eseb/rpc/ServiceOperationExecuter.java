package edu.cmu.cs.able.eseb.rpc;

import edu.cmu.cs.able.typelib.type.DataValue;
import incubator.Pair;

import java.util.Map;

/**
 * Interface implemented by the objects responsible for executing an operation
 * in an {@link ServiceObjectRegistration}.
 */
public interface ServiceOperationExecuter {
	/**
	 * Invoked to execute an operation.
	 * @param operation the meta data of the operation to be executed
	 * @param input_arguments the input arguments to the operation
	 * @return a pair with either a map of the output parameters to their
	 * respective output values or information about the failure; exactly
	 * one the elements of the pair must be <code>null</code>
	 * @throws Exception execution failed; if thrown, a
	 * {@link FailureInformation} object will be created from the exception
	 * and will be used as return value
	 */
	Pair<Map<String, DataValue>, FailureInformation> execute (
			DataValue operation, Map<String, DataValue> input_arguments)
			throws Exception;
}
