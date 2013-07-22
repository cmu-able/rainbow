package edu.cmu.cs.able.eseb.rpc;

import java.util.Map;
import java.util.Set;

import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * <p>Class that provides an easy access to operation information data types.
 * Since all data and meta data regarding RPCs is built on top of data types,
 * this class provides an easy API to work with the data types instead of
 * using all the meta APIs provided by <code>typelib</code>.</p>
 * 
 * <p>There are a few basic concepts used. The first is the concept of an
 * <em>operation</em>. An operation represents a <em>signature</em>, a type
 * of execution that can be performed. Each operation is kept in a data
 * type. An operation can be created by using the
 * {@link #create_operation(String)} method.</p>
 * 
 * <p>Operation can have both input and output parameters. These are added
 * to an operation using the
 * {@link #add_parameter(DataValue, DataType, String, ParameterDirection)}
 * method.</p>
 * 
 * <p>There are several utility methods to obtain information about an
 * operation: the {@link #is_operation(DataValue)} checks whether a data
 * value is an operation, the {@link #operation_name(DataValue)} method obtains
 * the name of an operation, the {@link #parameters(DataValue)} method
 * obtains the names of all parameters in an operation. The
 * {@link #parameter_type(DataValue, String)} and
 * {@link #parameter_direction(DataValue, String)} methods obtain information
 * about a parameter in an operation.</p>
 * 
 * <p>Operations belong to <em>groups</em>. A group is a set of operations
 * whose names must be unique. A group is similar to an interface declaration.
 * The {@link #create_group()} method is used to create an operation
 * group. The {@link #is_group(DataValue)} can be used to check if a data
 * type is an operation group. The 
 * {@link #add_operation_to_group(DataValue, DataValue)} method is used to 
 * add operations to groups. The {@link #group_operation_names(DataValue)},
 * {@link #group_operation(DataValue, String)} and
 * {@link #group_has_operation(DataValue, String)} methods can be used to
 * obtain information about operations in a group.</p>
 * 
 * <p>Operations and groups define interfaces but not individual invocations
 * of an operation. A request to execute an operation is a <em>execution
 * request</em> and a response to an operation execution request is a
 * <em>execution response</em>. Each execution has a unique ID which is used
 * to match the requests and the responses. The execution request has
 * additionally the destination ID, the ID of the participant that will
 * execute the operation, and also values for all input parameters. There are
 * two types of responses to a request: a successful response and an
 * unsuccessful response. A successful response contains values for all
 * output parameters. An unsuccessful response contains information about
 * a failure. This class provides methods to create requests and responses
 * as well as query information on them.</p> 
 */
public class OperationInformation {
	/**
	 * Creates a new operation information.
	 * @param pscope the primitive scope this operation is based on
	 * @throws OperationException failed to parse the operation data types
	 */
	public OperationInformation(PrimitiveScope pscope)
			throws OperationException {
	}
	
	/**
	 * Creates a new operation with the given name. The created operation has
	 * no parameters. Parameters can be added using the
	 * {@link #add_parameter(DataValue, DataType, String, ParameterDirection)}
	 * method. The operation may be added to a group using the
	 * {@link #add_operation_to_group(DataValue, DataValue)} method.
	 * @param name the operation name
	 * @return the created operation
	 */
	public DataValue create_operation(String name) {
		return null;
	}
	
	/**
	 * Checks whether a data value represents an operation.
	 * @param op_v the data value
	 * @return is it an operation?
	 */
	public boolean is_operation(DataValue op_v) {
		return false;
	}
	
	/**
	 * Obtains the name of an operation.
	 * @param op_v the operation, which must be a valid operation
	 * (see {@link #is_operation(DataValue)})
	 * @return the operation's name
	 */
	public String operation_name(DataValue op_v) {
		return null;
	}
	
	/**
	 * Adds a parameter to an operation.
	 * @param op_v the operation, which must be a valid operation
	 * (see {@link #is_operation(DataValue)}
	 * @param type the parameter data type
	 * @param name the parameter name; no parameter may exist in the operation
	 * with this name (see {@link #parameters(DataValue)})
	 * @param direction what is the parameter direction?
	 */
	public void add_parameter(DataValue op_v, DataType type, String name,
			ParameterDirection direction) {
	}
	
	/**
	 * Obtains the names of all parameters in an operation.
	 * @param op_v the operation, which must be a valid operation
	 * (see {@link #is_operation(DataValue)}
	 * @return the names of all parameters
	 */
	public Set<String> parameters(DataValue op_v) {
		return null;
	}
	
	/**
	 * Obtains the data type of a parameter in an operation.
	 * @param op_v the operation, which must be a valid operation
	 * (see {@link #is_operation(DataValue)}
	 * @param name the parameter name, which must be a valid parameters name
	 * in the operation (see {@link #parameters(DataValue)}
	 * @return the data type
	 */
	public DataType parameter_type(DataValue op_v, String name) {
		return null;
	}
	
	/**
	 * Obtains the direction of a parameter in an operation.
	 * @param op_v the operation, which must be a valid operation
	 * (see {@link #is_operation(DataValue)}
	 * @param name the parameter name, which must be a valid parameters name
	 * in the operation (see {@link #parameters(DataValue)}
	 * @return the parameter direction
	 */
	public ParameterDirection parameter_direction(DataValue op_v, String name) {
		return null;
	}
	
	/**
	 * Creates an operation group. Operations can be added to the group
	 * using the {@link #add_operation_to_group(DataValue, DataValue)}
	 * method.
	 * @return an operation group
	 */
	public DataValue create_group() {
		return null;
	}
	
	/**
	 * Checks whether a data value is an operation group.
	 * @param group_v the data value to check
	 * @return is it an operation group?
	 */
	public boolean is_group(DataValue group_v) {
		return false;
	}
	
	/**
	 * Adds an operation to an operation group. The names of all operations
	 * in the group must be unique so no other operation may be in the
	 * group with this operation name (see
	 * {@link #group_operation_names(DataValue)}). The same operation can
	 * be added to multiple groups.
	 * @param group_v the operation group, which must be a valid operation
	 * group (see {@link #is_group(DataValue)})
	 * @param op_v the operation, which must be a valid operation
	 * (see {@link #is_operation(DataValue)}
	 */
	public void add_operation_to_group(DataValue group_v, DataValue op_v) {
	}
	
	/**
	 * Obtains the names of all operations in a group.
	 * @param group_v the operation group, which must be a valid operation
	 * group (see {@link #is_group(DataValue)})
	 * @return the names of all operations
	 */
	public Set<String> group_operation_names(DataValue group_v) {
		return null;
	}
	
	/**
	 * Checks whether an operation group has an operation in it.
	 * @param group_v the operation group, which must be a valid operation
	 * group (see {@link #is_group(DataValue)})
	 * @param name the name of the operation to check
	 * @return does the group has an operation with the given name?
	 */
	public boolean group_has_operation(DataValue group_v, String name) {
		return false;
	}
	
	/**
	 * Obtains the operation in a group given its name.
	 * @param group_v the operation group, which must be a valid operation
	 * group (see {@link #is_group(DataValue)})
	 * @param name the operation name; there must be an operation in the
	 * group with the given name (see
	 * {@link #group_has_operation(DataValue, String)})
	 * @return the operation in the group with the given name
	 */
	public DataValue group_operation(DataValue group_v, String name) {
		return null;
	}
	
	/**
	 * Creates a request to execute an operation. Note that this method
	 * only creates the request data type. The
	 * {@link OperationExecution} or the {@link SynchronousOperationExecution}
	 * classes should be used to create the executions.
	 * @param exec_id a execution identifier used to map the response to
	 * the request
	 * @param dst_id the ID of the destination participant (the participant
	 * that will execute the operation)
	 * @param obj_id the ID of the destination object that will be invoked
	 * @param op_v the operation, which must be a valid operation
	 * (see {@link #is_operation(DataValue)}
	 * @param input_arguments maps names to the values of all input arguments
	 * @return the execution request
	 */
	public DataValue create_execution_request(long exec_id, long dst_id,
			long obj_id, DataValue op_v, Map<String,
			DataValue> input_arguments) {
		return null;
	}
	
	/**
	 * Checks whether a data value is an execution request.
	 * @param ereq_v the data value to check
	 * @return is it an execution request?
	 */
	public boolean is_execution_request(DataValue ereq_v) {
		return false;
	}
	
	/**
	 * Obtains the ID of the execution request.
	 * @param ereq_v the execution request
	 * @return the ID of the execution request
	 */
	public long execution_request_id(DataValue ereq_v) {
		return 0;
	}
	
	/**
	 * Obtains the ID of the destination participant in the execution request.
	 * @param ereq_v the execution request
	 * @return the ID of the destination participant
	 */
	public long execution_request_dst(DataValue ereq_v)  {
		return 0;
	}
	
	/**
	 * Obtains the ID of object object that should execute the operation.
	 * @param ereq_v the execution request
	 * @return the object ID
	 */
	public long execution_request_obj_id(DataValue ereq_v) {
		return 0;
	}
	
	/**
	 * Obtains the name of the operation requested in an execution request. 
	 * @param ereq_v the execution request
	 * @return the operation name
	 */
	public String execution_request_operation(DataValue ereq_v) {
		return null;
	}
	
	/**
	 * Obtains the values of the input parameters in an execution request.
	 * @param ereq_v the execution request
	 * @return map that maps parameters names to the argument values
	 */
	public Map<String, DataValue> execution_request_input_arguments(
			DataValue ereq_v) {
		return null;
	}
	
	/**
	 * Creates an execution response for an execution request that represents
	 * a successful execution.
	 * @param ereq_v the execution request
	 * @param op_v the operation, which must be a valid operation
	 * (see {@link #is_operation(DataValue)}
	 * @param output_arguments the values for the output parameters of the
	 * request
	 * @return the execution response
	 */
	public DataValue create_execution_response(DataValue ereq_v,
			DataValue op_v, Map<String, DataValue> output_arguments) {
		return null;
	}
	
	/**
	 * Creates an execution response for an execution request that represents
	 * a failure.
	 * @param ereq_v the execution request
	 * @param failure_type the type of failure (e.g., the name of the Java
	 * exception class)
	 * @param failure_description a description of the failure (e.g. the
	 * Java exception message)
	 * @param failure_data detailed failure data (e.g., the Java exception
	 * stack trace)
	 * @return the execution response
	 */
	public DataValue create_execution_failure(DataValue ereq_v,
			String failure_type, String failure_description,
			String failure_data) {
		return null;
	}
	
	/**
	 * Checks whether a data value is an execution response.
	 * @param eres_v the data value
	 * @return is it an execution response?
	 */
	public boolean is_execution_response(DataValue eres_v) {
		return false;
	}
	
	/**
	 * Checks whether a data value corresponds to a successful execution of
	 * an operation.
	 * @param eres_v the execution response
	 * @return is it a successful execution? If <code>false</code> then it is
	 * a failed execution
	 */
	public boolean is_successful_execution(DataValue eres_v) {
		return false;
	}
	
	/**
	 * Obtains the ID of an execution response.
	 * @param eres_v the execution response
	 * @return the ID
	 */
	public long execution_response_id(DataValue eres_v) {
		return 0;
	}
	
	/**
	 * Obtains the data values for the output parameters in an execution
	 * response.
	 * @param eres_v the execution response
	 * @return the data values
	 */
	public Map<String, DataValue> execution_response_output_arguments(
			DataValue eres_v) {
		return null;
	}
	
	/**
	 * Obtains the failure type in a failed execution response. 
	 * @param eres_v the execution response
	 * @return the failure type
	 */
	public String execution_response_failure_type(DataValue eres_v) {
		return null;
	}
	
	/**
	 * Obtains the failure description in a failed execution response. 
	 * @param eres_v the execution response
	 * @return the failure description
	 */
	public String execution_response_failure_description(DataValue eres_v) {
		return null;
	}
	
	/**
	 * Obtains the failure data in a failed execution response. 
	 * @param eres_v the execution response
	 * @return the failure data
	 */
	public String execution_response_failure_data(DataValue eres_v) {
		return null;
	}
}
