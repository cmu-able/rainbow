package edu.cmu.cs.able.eseb.rpc;

import incubator.Pair;

/**
 * Interface for a test service to be invoked remotely.
 */
public interface TestService {
	/**
	 * Executes the service.
	 * @param x the input value
	 * @return the output value or failure information; exactly one must be
	 * <code>null</code>
	 */
	Pair<Integer, FailureInformation> execute(int x);
}
