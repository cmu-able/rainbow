package edu.cmu.cs.able.eseb.rpc;

import incubator.pval.Ensure;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.cmu.cs.able.parsec.LocalizedParseException;
import edu.cmu.cs.able.parsec.ParsecFileReader;
import edu.cmu.cs.able.typelib.comp.MapDataType;
import edu.cmu.cs.able.typelib.comp.MapDataValue;
import edu.cmu.cs.able.typelib.comp.SetDataType;
import edu.cmu.cs.able.typelib.comp.SetDataValue;
import edu.cmu.cs.able.typelib.enumeration.EnumerationType;
import edu.cmu.cs.able.typelib.enumeration.EnumerationValue;
import edu.cmu.cs.able.typelib.parser.DefaultTypelibParser;
import edu.cmu.cs.able.typelib.parser.TypelibParsingContext;
import edu.cmu.cs.able.typelib.prim.Int64Value;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.prim.StringValue;
import edu.cmu.cs.able.typelib.prim.TypeValue;
import edu.cmu.cs.able.typelib.scope.AmbiguousNameException;
import edu.cmu.cs.able.typelib.scope.HierarchicalName;
import edu.cmu.cs.able.typelib.struct.Field;
import edu.cmu.cs.able.typelib.struct.StructureDataType;
import edu.cmu.cs.able.typelib.struct.StructureDataValue;
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
	 * Name of resource with TDL of that defines the participant types.
	 */
	public static final String TDL_RESOURCE = "operation_types.tdl";
	
	/**
	 * Namespace of eseb data types.
	 */
	public static final String ESEB_NAMESPACE = "eseb";
	
	/**
	 * Namespace of operation data types
	 */
	public static final String OPERATION_NAMESPACE = "rpc";
	
	/**
	 * Name of enumeration with parameter direction.
	 */
	public static final String PARAMETER_DIRECTION_ENUM_NAME
			= "parameter_direction";
	
	/**
	 * Name of enumeration value representing an input parameter.
	 */
	public static final String PARAMETER_DIRECTION_INPUT_ENUM_VALUE = "input";
	
	/**
	 * Name of enumeration value representing an output parameter.
	 */
	public static final String PARAMETER_DIRECTION_OUTPUT_ENUM_VALUE
			= "output";
	
	/**
	 * Name of parameter structure.
	 */
	public static final String PARAMETER_STRUCTURE_NAME = "parameter";
	
	/**
	 * Name of field with the parameter name.
	 */
	public static final String PARAMETER_NAME_FIELD_NAME = "name";
	
	/**
	 * Name of field with the parameter direction.
	 */
	public static final String PARAMETER_DIRECTION_FIELD_NAME = "direction";
	
	/**
	 * Name of field with parameter data type.
	 */
	public static final String PARAMETER_TYPE_FIELD_NAME = "data_type";
	
	/**
	 * Name of operation structure.
	 */
	public static final String OPERATION_STRUCTURE_NAME = "operation";
	
	/**
	 * Field with the name of an operation.
	 */
	public static final String OPERATION_NAME_FIELD_NAME = "name";
	
	/**
	 * Field with the parameters of an operation.
	 */
	public static final String OPERATION_PARAMETERS_FIELD_NAME = "parameters";
	
	/**
	 * Name of the operation group structure.
	 */
	public static final String GROUP_STRUCTURE_NAME = "group";
	
	/**
	 * Field with operations of a group.
	 */
	public static final String GROUP_OPERATIONS_FIELD_NAME = "operations";
	
	/**
	 * Name of the structure with an execution request.
	 */
	public static final String REQUEST_STRUCTURE_NAME = "execution_request";
	
	/**
	 * Field with the execution ID in an execution request.
	 */
	public static final String REQUEST_EXEC_ID_FIELD_NAME = "exec_id";
	
	/**
	 * Field with the destination ID in an execution request.
	 */
	public static final String REQUEST_DST_ID_FIELD_NAME = "dst_id";
	
	/**
	 * Field with the object ID in an execution request.
	 */
	public static final String REQUEST_OBJ_ID_FIELD_NAME = "obj_id";
	
	/**
	 * Field with the operation name in an execution request.
	 */
	public static final String REQUEST_OPERATION_FIELD_NAME = "operation_name";
	
	/**
	 * Field with the input parameters of the request.
	 */
	public static final String REQUEST_INPUTS_FIELD_NAME = "inputs";
	
	/**
	 * Name of the structure with the execution response.
	 */
	public static final String RESPONSE_STRUCTURE_NAME = "execution_response";
	
	/**
	 * Name of the field with the execution ID in the execution response.
	 */
	public static final String RESPONSE_EXEC_ID_FIELD_NAME = "exec_id";
	
	/**
	 * Name of the structure used when the execution of an operation has
	 * succeeded.
	 */
	public static final String RESPONSE_SUCCESS_STRUCTURE_NAME
			= "execution_response_success";
	
	/**
	 * Name of the field with the output parameters of a successful response.
	 */
	public static final String RESPONSE_OUTPUT_FIELD_NAME = "outputs";
	
	/**
	 * Name of structure used when the execution of an operation has failed.
	 */
	public static final String RESPONSE_FAILURE_STRUCTURE_NAME
			= "execution_response_failure";
	
	/**
	 * Name of field with the type of failure.
	 */
	public static final String RESPONSE_FAILURE_TYPE_FIELD_NAME = "type";
	
	/**
	 * Name of field with the description of a failure.
	 */
	public static final String RESPONSE_FAILURE_DESCRIPTION_FIELD_NAME
			= "description";
	
	/**
	 * Name of field with failure data.
	 */
	public static final String RESPONSE_FAILURE_DATA_FIELD_NAME = "data";
	
	/**
	 * The primitive type scope.
	 */
	private PrimitiveScope m_pscope;
	
	/**
	 * Enumeration with parameter direction.
	 */
	private EnumerationType m_parameter_direction_type;
	
	/**
	 * Enumeration value representing an input parameter.
	 */
	private EnumerationValue m_parameter_direction_input;
	
	/**
	 * Enumeration value representing an output parameter.
	 */
	private EnumerationValue m_parameter_direction_output;
	
	/**
	 * Data type of a parameter.
	 */
	private StructureDataType m_parameter_type;
	
	/**
	 * Field with the parameter name.
	 */
	private Field m_parameter_name_field;
	
	/**
	 * Field with parameter direction.
	 */
	private Field m_parameter_direction_field;
	
	/**
	 * Field with parameter data type.
	 */
	private Field m_parameter_type_field;
	
	/**
	 * Data type of an operation.
	 */
	private StructureDataType m_operation_type;
	
	/**
	 * Field with the operation name.
	 */
	private Field m_operation_name_field;
	
	/**
	 * Field with operation parameters.
	 */
	private Field m_operation_parameters_field;
	
	/**
	 * Data type of a group.
	 */
	private StructureDataType m_group_type;
	
	/**
	 * Field with operations in a group.
	 */
	private Field m_group_operations_field;
	
	/**
	 * Structure with an execution request.
	 */
	private StructureDataType m_request_type;
	
	/**
	 * Field with the execution ID in an execution request.
	 */
	private Field m_request_exec_id_field;
	
	/**
	 * Field with the destination ID in an execution request.
	 */
	private Field m_request_dst_id_field;
	
	/**
	 * Field with the object ID in an execution request.
	 */
	private Field m_request_obj_id_field;
	
	/**
	 * Field with the operation name in an execution request.
	 */
	private Field m_request_operation_field;
	
	/**
	 * Field with the input parameters of the request.
	 */
	private Field m_request_inputs_field;
	
	/**
	 * Structure with a response to an execution request.
	 */
	private StructureDataType m_response_type;
	
	/**
	 * Field with the execution ID of the response.
	 */
	private Field m_response_exec_id_field;
	
	/**
	 * Structure with a successful response to an execution request.
	 */
	private StructureDataType m_response_success_type;
	
	/**
	 * Field with the output of the response.
	 */
	private Field m_response_output_field;
	
	/**
	 * Structure with a failure response to an execution request.
	 */
	private StructureDataType m_response_failure_type;
	
	/**
	 * Field with the failure type.
	 */
	private Field m_response_failure_type_field;
	
	/**
	 * Field with the failure description.
	 */
	private Field m_response_failure_description_field;
	
	/**
	 * Field with the failure data.
	 */
	private Field m_response_failure_data_field;
	
	/**
	 * Creates a new operation information.
	 * @param pscope the primitive scope this operation is based on
	 * @throws OperationException failed to parse the operation data types
	 */
	public OperationInformation(PrimitiveScope pscope)
			throws OperationException {
		Ensure.not_null(pscope);
		
		m_pscope = pscope;
		
		if (!read_types(pscope)) {
			load_types(pscope);
			boolean read = read_types(pscope);
			Ensure.is_true(read);
		}
	}
	
	/**
	 * Reads the data types from the scope filling in the object's instance
	 * variables.
	 * @param pscope the primitive scope
	 * @return have the data types been read?
	 * @throws OperationException some data already exists in the scope but
	 * is invalid
	 */
	private boolean read_types(PrimitiveScope pscope)
			throws OperationException {
		try {
			HierarchicalName parameter_direction_enum_name
					= new HierarchicalName(true, ESEB_NAMESPACE,
					OPERATION_NAMESPACE, PARAMETER_DIRECTION_ENUM_NAME);
			
			DataType direction_enum_type = pscope.find(
					parameter_direction_enum_name);
			if (direction_enum_type == null) {
				return false;
			}
			
			Ensure.is_instance(direction_enum_type, EnumerationType.class);
			m_parameter_direction_type = (EnumerationType) direction_enum_type;
			
			m_parameter_direction_input = m_parameter_direction_type.value(
					PARAMETER_DIRECTION_INPUT_ENUM_VALUE);
			m_parameter_direction_output = m_parameter_direction_type.value(
					PARAMETER_DIRECTION_OUTPUT_ENUM_VALUE);
			
			HierarchicalName parameter_structure_name = new HierarchicalName(true,
					ESEB_NAMESPACE, OPERATION_NAMESPACE,
					PARAMETER_STRUCTURE_NAME);
			
			DataType parameter_type = pscope.find(parameter_structure_name);
			if (parameter_type == null) {
				return false;
			}
			
			Ensure.is_instance(parameter_type, StructureDataType.class);
			m_parameter_type = (StructureDataType) parameter_type;
			
			Set<Field> fields = m_parameter_type.fields();
			Ensure.equals(3, fields.size());
			
			m_parameter_name_field = m_parameter_type.field(
					PARAMETER_NAME_FIELD_NAME);
			Ensure.not_null(m_parameter_name_field);
			Ensure.equals(pscope.string(),
					m_parameter_name_field.description().type());
			
			m_parameter_direction_field = m_parameter_type.field(
					PARAMETER_DIRECTION_FIELD_NAME);
			Ensure.not_null(m_parameter_direction_field);
			Ensure.equals(m_parameter_direction_type,
					m_parameter_direction_field.description().type());
			
			m_parameter_type_field = m_parameter_type.field(
					PARAMETER_TYPE_FIELD_NAME);
			Ensure.not_null(m_parameter_type_field);
			Ensure.equals(pscope.type(),
					m_parameter_type_field.description().type());
			
			HierarchicalName operation_structure_name = new HierarchicalName(
					true, ESEB_NAMESPACE, OPERATION_NAMESPACE,
					OPERATION_STRUCTURE_NAME);
			
			DataType operation_type = pscope.find(operation_structure_name);
			if (operation_type == null) {
				return false;
			}
			
			Ensure.is_instance(operation_type, StructureDataType.class);
			m_operation_type = (StructureDataType) operation_type;
			
			fields = m_operation_type.fields();
			Ensure.equals(2, fields.size());
			
			m_operation_name_field = m_operation_type.field(
					OPERATION_NAME_FIELD_NAME);
			Ensure.not_null(m_operation_name_field);
			Ensure.equals(pscope.string(),
					m_operation_name_field.description().type());
			
			m_operation_parameters_field = m_operation_type.field(
					OPERATION_PARAMETERS_FIELD_NAME);
			Ensure.not_null(m_operation_name_field);
			Ensure.equals(SetDataType.set_of(m_parameter_type, m_pscope),
					m_operation_parameters_field.description().type());
			
			HierarchicalName group_structure_name = new HierarchicalName(true,
					ESEB_NAMESPACE, OPERATION_NAMESPACE, GROUP_STRUCTURE_NAME);
			
			DataType group_type = pscope.find(group_structure_name);
			Ensure.not_null(group_type);
			if (group_type == null) {
				return false;
			}
			
			Ensure.is_instance(group_type, StructureDataType.class);
			m_group_type = (StructureDataType) group_type;
			
			fields = m_group_type.fields();
			Ensure.equals(1, fields.size());
			
			m_group_operations_field = m_group_type.field(
					GROUP_OPERATIONS_FIELD_NAME);
			Ensure.not_null(m_group_operations_field);
			Ensure.equals(MapDataType.map_of(pscope.string(), m_operation_type,
					m_pscope), m_group_operations_field.description().type());
			
			HierarchicalName request_structure_name = new HierarchicalName(
					true, ESEB_NAMESPACE, OPERATION_NAMESPACE,
					REQUEST_STRUCTURE_NAME);
			
			DataType request_type = pscope.find(request_structure_name);
			if (request_type == null) {
				return false;
			}
			
			Ensure.is_instance(request_type, StructureDataType.class);
			m_request_type = (StructureDataType) request_type;
			
			fields = m_request_type.fields();
			Ensure.equals(5, fields.size());
			
			m_request_dst_id_field = m_request_type.field(
					REQUEST_DST_ID_FIELD_NAME);
			Ensure.not_null(m_request_dst_id_field);
			Ensure.equals(m_pscope.string(),
					m_request_dst_id_field.description().type());
			m_request_exec_id_field = m_request_type.field(
					REQUEST_EXEC_ID_FIELD_NAME);
			Ensure.not_null(m_request_exec_id_field);
			Ensure.equals(m_pscope.int64(),
					m_request_exec_id_field.description().type());
			m_request_inputs_field = m_request_type.field(
					REQUEST_INPUTS_FIELD_NAME);
			Ensure.not_null(m_request_inputs_field);
			Ensure.equals(MapDataType.map_of(m_pscope.string(), m_pscope.any(),
					m_pscope), m_request_inputs_field.description().type());
			m_request_obj_id_field = m_request_type.field(
					REQUEST_OBJ_ID_FIELD_NAME);
			Ensure.not_null(m_request_obj_id_field);
			Ensure.equals(m_pscope.string(),
					m_request_obj_id_field.description().type());
			m_request_operation_field = m_request_type.field(
					REQUEST_OPERATION_FIELD_NAME);
			Ensure.not_null(m_request_operation_field);
			Ensure.equals(m_pscope.string(),
					m_request_operation_field.description().type());
			
			HierarchicalName response_structure_name = new HierarchicalName(
					true, ESEB_NAMESPACE, OPERATION_NAMESPACE,
					RESPONSE_STRUCTURE_NAME);
			
			DataType response_type = pscope.find(response_structure_name);
			if (response_type == null) {
				return false;
			}
			
			Ensure.is_instance(response_type, StructureDataType.class);
			m_response_type = (StructureDataType) response_type;
			
			fields = m_response_type.fields();
			Ensure.equals(1, fields.size());
			
			m_response_exec_id_field = m_response_type.field(
					RESPONSE_EXEC_ID_FIELD_NAME);
			Ensure.not_null(m_response_exec_id_field);
			Ensure.equals(m_pscope.int64(),
					m_response_exec_id_field.description().type());
			
			HierarchicalName response_success_structure_name
					= new HierarchicalName(true, ESEB_NAMESPACE,
					OPERATION_NAMESPACE, RESPONSE_SUCCESS_STRUCTURE_NAME);
			
			DataType success_type = pscope.find(
					response_success_structure_name);
			if (success_type == null) {
				return false;
			}
			
			Ensure.is_instance(success_type, StructureDataType.class);
			m_response_success_type = (StructureDataType) success_type;
			Ensure.is_true(m_response_success_type.sub_of(m_response_type));
			
			fields = m_response_success_type.fields();
			Ensure.equals(2, fields.size());
			
			m_response_output_field = m_response_success_type.field(
					RESPONSE_OUTPUT_FIELD_NAME);
			Ensure.not_null(m_response_output_field);
			Ensure.equals(MapDataType.map_of(m_pscope.string(), m_pscope.any(),
					m_pscope), m_response_output_field.description().type());
			
			HierarchicalName response_failure_structure_name =
					new HierarchicalName(true, ESEB_NAMESPACE,
					OPERATION_NAMESPACE, RESPONSE_FAILURE_STRUCTURE_NAME);
			
			DataType failure_type = pscope.find(
					response_failure_structure_name);
			if (failure_type == null) {
				return false;
			}
			
			Ensure.is_instance(failure_type, StructureDataType.class);
			m_response_failure_type = (StructureDataType) failure_type;
			Ensure.is_true(m_response_failure_type.sub_of(m_response_type));
			
			fields = m_response_failure_type.fields();
			Ensure.equals(4, fields.size());
			
			m_response_failure_data_field = m_response_failure_type.field(
					RESPONSE_FAILURE_DATA_FIELD_NAME);
			Ensure.not_null(m_response_failure_data_field);
			Ensure.equals(pscope.string(),
					m_response_failure_data_field.description().type());
			m_response_failure_description_field =
					m_response_failure_type.field(
					RESPONSE_FAILURE_DESCRIPTION_FIELD_NAME);
			Ensure.not_null(m_response_failure_description_field);
			Ensure.equals(pscope.string(),
					m_response_failure_description_field.description().type());
			m_response_failure_type_field = m_response_failure_type.field(
					RESPONSE_FAILURE_TYPE_FIELD_NAME);
			Ensure.not_null(m_response_failure_type_field);
			Ensure.equals(pscope.string(),
					m_response_failure_type_field.description().type());
		} catch (AmbiguousNameException e) {
			Ensure.never_thrown(e);
		}
		
		return true;
	}
	
	/**
	 * Loads the data types into the scope reading them from the definition
	 * in the resources.
	 * @param pscope the primitive scope
	 * @throws OperationException failed to read the data types
	 */
	private void load_types(PrimitiveScope pscope)
			throws OperationException {
		Ensure.not_null(pscope);
		
		String types_text;
		
		InputStream r = getClass().getResourceAsStream(TDL_RESOURCE);
		if (r == null) {
			throw new OperationException("Resource '" + TDL_RESOURCE + "' "
					+ "not found.");
		}
		
		/*
		 * The local resource should be encoded with UNIX conventions.
		 */
		
		try (InputStream is = r;
				InputStreamReader isr = new InputStreamReader(is);
				StringWriter sw = new StringWriter()) {
			
			int ch;
			while ((ch = isr.read()) != -1) {
				Ensure.not_same(0x000d, ch);
				if (ch == 0x000a) {
					sw.write(0x000d);
				}
				
				sw.write(ch);
			}
			
			types_text = sw.toString();
		} catch (IOException e) {
			throw new OperationException("Failed to load resource '"
					+ TDL_RESOURCE + "'.", e);
		}
		
		TypelibParsingContext context = new TypelibParsingContext(pscope,
				pscope);
		DefaultTypelibParser dtp = DefaultTypelibParser.make();
		
		try {
			dtp.parse(new ParsecFileReader().read_memory(types_text), context);
		} catch (LocalizedParseException e) {
			throw new OperationException("Failed to parse '" + TDL_RESOURCE
					+ "' resource.", e);
		}
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
		Ensure.not_null(name);
		
		Map<Field, DataValue> values = new HashMap<>();
		values.put(m_operation_name_field, m_pscope.string().make(name));
		values.put(m_operation_parameters_field,
				SetDataType.set_of(m_parameter_type, m_pscope).make());
		return m_operation_type.make(values);
	}
	
	/**
	 * Checks whether a data value represents an operation.
	 * @param op_v the data value
	 * @return is it an operation?
	 */
	public boolean is_operation(DataValue op_v) {
		Ensure.not_null(op_v);
		return m_operation_type.is_instance(op_v);
	}
	
	/**
	 * Obtains the name of an operation.
	 * @param op_v the operation, which must be a valid operation
	 * (see {@link #is_operation(DataValue)})
	 * @return the operation's name
	 */
	public String operation_name(DataValue op_v) {
		Ensure.is_true(is_operation(op_v));
		DataValue n = ((StructureDataValue) op_v).value(m_operation_name_field);
		return ((StringValue) n).value();
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
		Ensure.is_true(is_operation(op_v));
		Ensure.not_null(type);
		Ensure.not_null(name);
		Ensure.not_null(direction);
		Ensure.is_false(parameters(op_v).contains(name));
		
		Map<Field, DataValue> pfields = new HashMap<>();
		if (direction == ParameterDirection.INPUT) {
			pfields.put(m_parameter_direction_field,
					m_parameter_direction_input);
		} else {
			Ensure.equals(ParameterDirection.OUTPUT, direction);
			pfields.put(m_parameter_direction_field,
					m_parameter_direction_output);
		}
		
		pfields.put(m_parameter_name_field, m_pscope.string().make(name));
		pfields.put(m_parameter_type_field, m_pscope.type().make(type));
		
		StructureDataValue op_p = m_parameter_type.make(pfields);
		StructureDataValue op_s = (StructureDataValue) op_v;
		SetDataValue params = (SetDataValue) op_s.value(
				m_operation_parameters_field);
		params.add(op_p);
	}
	
	/**
	 * Obtains the names of all parameters in an operation.
	 * @param op_v the operation, which must be a valid operation
	 * (see {@link #is_operation(DataValue)}
	 * @return the names of all parameters
	 */
	public Set<String> parameters(DataValue op_v) {
		Ensure.is_true(is_operation(op_v));
		DataValue p = ((StructureDataValue) op_v).value(
				m_operation_parameters_field);
		SetDataValue ps = (SetDataValue) p;
		Set<String> names = new HashSet<>();
		for (DataValue dv : ps.all()) {
			DataValue pn = ((StructureDataValue) dv).value(
					m_parameter_name_field);
			names.add(((StringValue) pn).value());
		}
		
		return names;
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
		Ensure.is_true(is_operation(op_v));
		Ensure.not_null(name);
		
		DataType type = null;
		StructureDataValue op_s = (StructureDataValue) op_v;
		SetDataValue params = (SetDataValue) op_s.value(
				m_operation_parameters_field);
		for (DataValue dv : params.all()) {
			DataValue pn = ((StructureDataValue) dv).value(
					m_parameter_name_field);
			if (((StringValue) pn).value().equals(name)) {
				DataValue type_ref = ((StructureDataValue) dv).value(
						m_parameter_type_field);
				type = ((TypeValue) type_ref).value();
			}
		}
		
		Ensure.not_null(type);
		return type;
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
		Ensure.is_true(is_operation(op_v));
		Ensure.not_null(name);
		
		ParameterDirection dir = null;
		StructureDataValue op_s = (StructureDataValue) op_v;
		SetDataValue params = (SetDataValue) op_s.value(
				m_operation_parameters_field);
		for (DataValue dv : params.all()) {
			DataValue pn = ((StructureDataValue) dv).value(
					m_parameter_name_field);
			if (((StringValue) pn).value().equals(name)) {
				DataValue dir_v = ((StructureDataValue) dv).value(
						m_parameter_direction_field);
				if (dir_v == m_parameter_direction_input) {
					dir = ParameterDirection.INPUT;
				} else {
					Ensure.equals(m_parameter_direction_output, dir_v);
					dir = ParameterDirection.OUTPUT;
				}
			}
		}
		
		Ensure.not_null(dir);
		return dir;
	}
	
	/**
	 * Creates an operation group. Operations can be added to the group
	 * using the {@link #add_operation_to_group(DataValue, DataValue)}
	 * method.
	 * @return an operation group
	 */
	public DataValue create_group() {
		MapDataType op_map_type =
				(MapDataType) m_group_operations_field.description().type();
		Map<Field, DataValue> values = new HashMap<>();
		values.put(m_group_operations_field, op_map_type.make());
		return m_group_type.make(values);
	}
	
	/**
	 * Checks whether a data value is an operation group.
	 * @param group_v the data value to check
	 * @return is it an operation group?
	 */
	public boolean is_group(DataValue group_v) {
		Ensure.not_null(group_v);
		return m_group_type.is_instance(group_v);
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
		Ensure.is_true(is_group(group_v));
		Ensure.is_true(is_operation(op_v));
		Ensure.is_false(group_has_operation(group_v, operation_name(op_v)));
		
		StructureDataValue group = (StructureDataValue) group_v;
		MapDataValue ops = (MapDataValue) group.value(
				m_group_operations_field);
		StringValue sv = m_pscope.string().make(operation_name(op_v));
		ops.put(sv, op_v);
	}
	
	/**
	 * Obtains the names of all operations in a group.
	 * @param group_v the operation group, which must be a valid operation
	 * group (see {@link #is_group(DataValue)})
	 * @return the names of all operations
	 */
	public Set<String> group_operation_names(DataValue group_v) {
		Ensure.is_true(is_group(group_v));
		StructureDataValue group = (StructureDataValue) group_v;
		MapDataValue ops = (MapDataValue) group.value(
				m_group_operations_field);
		Set<String> names = new HashSet<>();
		for (DataValue kv : ops.all().keySet()) {
			names.add(((StringValue) kv).value());
		}
		
		return names;
	}
	
	/**
	 * Checks whether an operation group has an operation in it.
	 * @param group_v the operation group, which must be a valid operation
	 * group (see {@link #is_group(DataValue)})
	 * @param name the name of the operation to check
	 * @return does the group has an operation with the given name?
	 */
	public boolean group_has_operation(DataValue group_v, String name) {
		Ensure.is_true(is_group(group_v));
		Ensure.not_null(name);
		return group_operation_names(group_v).contains(name);
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
		Ensure.is_true(is_group(group_v));
		StructureDataValue group = (StructureDataValue) group_v;
		MapDataValue ops = (MapDataValue) group.value(
				m_group_operations_field);

		DataValue r = null;
		for (DataValue kv : ops.all().keySet()) {
			if (((StringValue) kv).value().equals(name)) {
				r = ops.get(kv);
			}
		}
		
		return r;
	}
	
	/**
	 * Creates a request to execute an operation. Note that this method
	 * only creates the request data type. The
	 * {@link RemoteOperationStub} classe should be used to create the
	 * executions.
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
	public DataValue create_execution_request(long exec_id, String dst_id,
			String obj_id, DataValue op_v, Map<String,
			DataValue> input_arguments) {
		Ensure.not_null(dst_id, "dst_id == null");
		Ensure.not_null(obj_id, "obj_id == null");
		Ensure.is_true(is_operation(op_v), "op_v is not an operation");
		Ensure.not_null(input_arguments, "input_arguments == null");
		
		Set<String> all_params = parameters(op_v);
		Set<String> params = new HashSet<>();
		for (String p : all_params) {
			if (parameter_direction(op_v, p) == ParameterDirection.INPUT) {
				params.add(p);
			}
		}
		
		Ensure.equals(params.size(), input_arguments.size());
		
		MapDataValue mdv = MapDataType.map_of(m_pscope.string(),
				m_pscope.any(), m_pscope).make();
		for (String p : params) {
			DataValue v = input_arguments.get(p);
			Ensure.not_null(v);
			Ensure.is_true(parameter_type(op_v, p).is_instance(v));
			mdv.put(m_pscope.string().make(p), v);
		}
		
		Map<Field, DataValue> sfields = new HashMap<>();
		sfields.put(m_request_dst_id_field, m_pscope.string().make(dst_id));
		sfields.put(m_request_exec_id_field, m_pscope.int64().make(exec_id));
		sfields.put(m_request_obj_id_field, m_pscope.string().make(obj_id));
		sfields.put(m_request_operation_field, m_pscope.string().make(
				operation_name(op_v)));
		sfields.put(m_request_inputs_field, mdv);
		
		return m_request_type.make(sfields);
	}
	
	/**
	 * Checks whether a data value is an execution request.
	 * @param ereq_v the data value to check
	 * @return is it an execution request?
	 */
	public boolean is_execution_request(DataValue ereq_v) {
		Ensure.not_null(ereq_v);
		return m_request_type.is_instance(ereq_v);
	}
	
	/**
	 * Obtains the ID of the execution request.
	 * @param ereq_v the execution request
	 * @return the ID of the execution request
	 */
	public long execution_request_id(DataValue ereq_v) {
		Ensure.is_true(is_execution_request(ereq_v));
		
		StructureDataValue r = (StructureDataValue) ereq_v;
		Int64Value id = (Int64Value) r.value(m_request_exec_id_field);
		return id.value();
	}
	
	/**
	 * Obtains the ID of the destination participant in the execution request.
	 * @param ereq_v the execution request
	 * @return the ID of the destination participant
	 */
	public String execution_request_dst(DataValue ereq_v)  {
		Ensure.is_true(is_execution_request(ereq_v));
		
		StructureDataValue r = (StructureDataValue) ereq_v;
		StringValue dst = (StringValue) r.value(m_request_dst_id_field);
		return dst.value();
	}
	
	/**
	 * Obtains the ID of object object that should execute the operation.
	 * @param ereq_v the execution request
	 * @return the object ID
	 */
	public String execution_request_obj_id(DataValue ereq_v) {
		Ensure.is_true(is_execution_request(ereq_v));
		
		StructureDataValue r = (StructureDataValue) ereq_v;
		StringValue id = (StringValue) r.value(m_request_obj_id_field);
		return id.value();
	}
	
	/**
	 * Obtains the name of the operation requested in an execution request. 
	 * @param ereq_v the execution request
	 * @return the operation name
	 */
	public String execution_request_operation(DataValue ereq_v) {
		Ensure.is_true(is_execution_request(ereq_v));
		
		StructureDataValue r = (StructureDataValue) ereq_v;
		StringValue op = (StringValue) r.value(m_request_operation_field);
		return op.value();
	}
	
	/**
	 * Obtains the values of the input parameters in an execution request.
	 * @param ereq_v the execution request
	 * @return map that maps parameters names to the argument values
	 */
	public Map<String, DataValue> execution_request_input_arguments(
			DataValue ereq_v) {
		Ensure.is_true(is_execution_request(ereq_v));
		
		StructureDataValue r = (StructureDataValue) ereq_v;
		MapDataValue op = (MapDataValue) r.value(m_request_inputs_field);
		
		Map<String, DataValue> m = new HashMap<>();
		for (Map.Entry<DataValue, DataValue> p : op.all().entrySet()) {
			m.put(((StringValue) p.getKey()).value(), p.getValue());
		}
		
		return m;
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
		Ensure.is_true(is_execution_request(ereq_v));
		Ensure.is_true(is_operation(op_v));
		Ensure.equals(execution_request_operation(ereq_v),
				operation_name(op_v));
		Ensure.not_null(output_arguments);
		
		Set<String> all_params = parameters(op_v);
		Set<String> params = new HashSet<>();
		for (String p : all_params) {
			if (parameter_direction(op_v, p) == ParameterDirection.OUTPUT) {
				params.add(p);
			}
		}
		
		Ensure.equals(params.size(), output_arguments.size());
		
		MapDataValue mdv = MapDataType.map_of(m_pscope.string(),
				m_pscope.any(), m_pscope).make();
		for (String p : params) {
			DataValue v = output_arguments.get(p);
			Ensure.not_null(v);
			Ensure.is_true(parameter_type(op_v, p).is_instance(v));
			mdv.put(m_pscope.string().make(p), v);
		}
		
		Map<Field, DataValue> sfields = new HashMap<>();
		sfields.put(m_response_exec_id_field, m_pscope.int64().make(
				execution_request_id(ereq_v)));
		sfields.put(m_response_output_field, mdv);
		
		return m_response_success_type.make(sfields);
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
		Ensure.is_true(is_execution_request(ereq_v));
		Ensure.not_null(failure_type);
		Ensure.not_null(failure_description);
		Ensure.not_null(failure_data);
		
		Map<Field, DataValue> sfields = new HashMap<>();
		sfields.put(m_response_exec_id_field, m_pscope.int64().make(
				execution_request_id(ereq_v)));
		sfields.put(m_response_failure_data_field, m_pscope.string().make(
				failure_data));
		sfields.put(m_response_failure_description_field,
				m_pscope.string().make(failure_description));
		sfields.put(m_response_failure_type_field, m_pscope.string().make(
				failure_type));
		
		return m_response_failure_type.make(sfields);
	}
	
	/**
	 * Checks whether a data value is an execution response.
	 * @param eres_v the data value
	 * @return is it an execution response?
	 */
	public boolean is_execution_response(DataValue eres_v) {
		Ensure.not_null(eres_v);
		if (m_response_success_type.is_instance(eres_v)) {
			return true;
		} else {
			return m_response_failure_type.is_instance(eres_v);
		}
	}
	
	/**
	 * Checks whether a data value corresponds to a successful execution of
	 * an operation.
	 * @param eres_v the execution response
	 * @return is it a successful execution? If <code>false</code> then it is
	 * a failed execution
	 */
	public boolean is_successful_execution(DataValue eres_v) {
		Ensure.is_true(is_execution_response(eres_v));
		return m_response_success_type.is_instance(eres_v);
	}
	
	/**
	 * Obtains the ID of an execution response.
	 * @param eres_v the execution response
	 * @return the ID
	 */
	public long execution_response_id(DataValue eres_v) {
		Ensure.is_true(is_execution_response(eres_v));
		
		StructureDataValue s = (StructureDataValue) eres_v;
		Int64Value v = (Int64Value) s.value(m_response_exec_id_field);
		return v.value();
	}
	
	/**
	 * Obtains the data values for the output parameters in an execution
	 * response.
	 * @param eres_v the execution response
	 * @return the data values
	 */
	public Map<String, DataValue> execution_response_output_arguments(
			DataValue eres_v) {
		Ensure.is_true(is_successful_execution(eres_v));
		
		StructureDataValue r = (StructureDataValue) eres_v;
		MapDataValue op = (MapDataValue) r.value(m_response_output_field);
		
		Map<String, DataValue> m = new HashMap<>();
		for (Map.Entry<DataValue, DataValue> p : op.all().entrySet()) {
			m.put(((StringValue) p.getKey()).value(), p.getValue());
		}
		
		return m;
	}
	
	/**
	 * Obtains the failure type in a failed execution response. 
	 * @param eres_v the execution response
	 * @return the failure type
	 */
	public String execution_response_failure_type(DataValue eres_v) {
		Ensure.is_true(is_execution_response(eres_v));
		Ensure.is_false(is_successful_execution(eres_v));
		
		StructureDataValue s = (StructureDataValue) eres_v;
		StringValue v = (StringValue) s.value(m_response_failure_type_field);
		return v.value();
	}
	
	/**
	 * Obtains the failure description in a failed execution response. 
	 * @param eres_v the execution response
	 * @return the failure description
	 */
	public String execution_response_failure_description(DataValue eres_v) {
		Ensure.is_true(is_execution_response(eres_v));
		Ensure.is_false(is_successful_execution(eres_v));
		
		StructureDataValue s = (StructureDataValue) eres_v;
		StringValue v = (StringValue) s.value(
				m_response_failure_description_field);
		return v.value();
	}
	
	/**
	 * Obtains the failure data in a failed execution response. 
	 * @param eres_v the execution response
	 * @return the failure data
	 */
	public String execution_response_failure_data(DataValue eres_v) {
		Ensure.is_true(is_execution_response(eres_v));
		Ensure.is_false(is_successful_execution(eres_v));
		
		StructureDataValue s = (StructureDataValue) eres_v;
		StringValue v = (StringValue) s.value(m_response_failure_data_field);
		return v.value();
	}
}
