package edu.cmu.cs.able.eseb.rpc;

import incubator.pval.Ensure;

import java.util.HashMap;
import java.util.Map;

import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Result of executing a remote operation. A remote operation can result in
 * two possible outcomes: a successful result, in which case a map with the
 * output arguments is available, or in an unsuccessful result, in which case
 * a {@link FailureInformation} object is available.
 */
public class RemoteExecutionResult {
	/**
	 * Result of execution if the execution was successful, <code>null</code>
	 * otherwise.
	 */
	private Map<String, DataValue> m_output_arguments;
	
	/**
	 * Result of execution if the execution failed, <code>null</code> if
	 * execution was successful.
	 */
	private FailureInformation m_failure_information;
	
	/**
	 * Creates a representation of a successful remote execution result.
	 * @param output_arguments the output arguments of the execution
	 */
	public RemoteExecutionResult(Map<String, DataValue> output_arguments) {
		Ensure.not_null(output_arguments);
		m_output_arguments = output_arguments;
		m_failure_information = null;
	}
	
	/**
	 * Creates a representation of a failed remote execution result.
	 * @param fi information about the failure
	 */
	public RemoteExecutionResult(FailureInformation fi) {
		Ensure.not_null(fi);
		m_output_arguments = null;
		m_failure_information = fi;
	}
	
	/**
	 * Was this execution successful?
	 * @return was successful?
	 */
	public boolean successful() {
		return m_output_arguments != null;
	}
	
	/**
	 * Obtains the output arguments of this execution. Can only be invoked
	 * if this execution finished successfully.
	 * @return the output arguments
	 */
	public Map<String, DataValue> output_arguments() {
		Ensure.is_true(successful());
		return new HashMap<>(m_output_arguments);
	}
	
	/**
	 * Obtains the failure information of this execution. Can only be invoked
	 * if the execution is a failed execution.
	 * @return the type of failure
	 */
	public FailureInformation failure_information() {
		Ensure.is_false(successful());
		return m_failure_information;
	}
}
