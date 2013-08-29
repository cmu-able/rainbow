package edu.cmu.cs.able.eseb.rpc;

/**
 * Example, used in tests, of a remote Java RPC service.
 */
public interface RemoteJavaRpcTestService {
	/**
	 * Adds one to a value.
	 * @param value the value to add one to.
	 * @return <code>value</code> plus one
	 */
	@ParametersTypeMapping({"int32"})
	@ReturnTypeMapping("int32")
	int returns_number_plus_one(int value);
	
	/**
	 * Does nothing we could possibly see :)
	 * @param value1 a value
	 * @param value2 another value
	 */
	@ParametersTypeMapping({"int32", "int32"})
	void no_return(int value1, int value2);
	
	/**
	 * Does even less.
	 */
	void no_arguments();
}
