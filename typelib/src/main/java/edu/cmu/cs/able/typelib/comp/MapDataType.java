package edu.cmu.cs.able.typelib.comp;

import incubator.pval.Ensure;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.cmu.cs.able.typelib.prim.AnyType;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.scope.AmbiguousNameException;
import edu.cmu.cs.able.typelib.scope.Scope;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;

/**
 * A tuple data type is a data type containing a map of keys to values.
 */
public class MapDataType extends DataType {
	/**
	 * Prefix for map type name.
	 */
	private static final String MAP_NAME_PREFIX = "map<";
	
	/**
	 * Suffix for map type name.
	 */
	private static final String MAP_NAME_SUFFIX = ">";
	
	/**
	 * Separator used to split types in the map.
	 */
	private static final String MAP_NAME_SEPARATOR = ",";
	
	/**
	 * Data type used for keys.
	 */
	private DataType m_key_type;
	
	/**
	 * Data type used for values.
	 */
	private DataType m_value_type;

	/**
	 * Creates a new map data type.
	 * @param key_type the type of data used for keys
	 * @param value_type the type of data used for values
	 * @param any the any type, super type of the tuple
	 */
	public MapDataType(DataType key_type, DataType value_type, AnyType any) {
		super(build_map_name(Ensure.not_null(key_type),
				Ensure.not_null(value_type)),
				new HashSet<DataType>(Arrays.asList(any)));
		m_key_type = key_type;
		m_value_type = value_type;
	}
	
	/**
	 * Creates the name of the map type from the key and value types
	 * @param key_type the type of data used for keys
	 * @param value_type the type of data used for values
	 * @return the tuple name
	 */
	public static String build_map_name(DataType key_type,
			DataType value_type) {
		Ensure.not_null(key_type);
		Ensure.not_null(value_type);
		String name = MAP_NAME_PREFIX + key_type.name() + MAP_NAME_SEPARATOR
				+ value_type.name() + MAP_NAME_SUFFIX;
		return name;
	}
	
	/**
	 * Obtains (and creates, if necessary) the map data type that contains
	 * the given key and value data types. The map data type is created, if
	 * necessary, in the inner-most scope that contains both the key and
	 * the value types
	 * @param key_type the type of data used for keys
	 * @param value_type the type of data used for values
	 * @param pscope the primitive data type scope which should be the scope
	 * (or a parent of the scope) of all inner types
	 * @return the list data type
	 */
	public static MapDataType map_of(DataType key_type, DataType value_type,
			PrimitiveScope pscope) {
		Ensure.not_null(key_type);
		Ensure.not_null(value_type);
		Ensure.not_null(pscope);
		
		Set<DataTypeScope> scopes = new HashSet<>();
		scopes.add(key_type.parent_dts());
		scopes.add(value_type.parent_dts());
		
		DataTypeScope scope = (DataTypeScope)
				Scope.common_inner_most_parent_scope(scopes);
		Ensure.not_null(scope);
		
		DataType found = null;
		try {
			found = scope.find(build_map_name(key_type, value_type));
		} catch (AmbiguousNameException e) {
			/*
			 * No map found.
			 */
		}
		
		if (found == null) {
			found = new MapDataType(key_type, value_type, pscope.any());
			scope.add(found);
		}
		
		Ensure.not_null(found);
		Ensure.is_instance(found, MapDataType.class);
		return (MapDataType) found;
	}
	
	/**
	 * Makes a new map.
	 * @return the new map
	 */
	public MapDataValue make() {
		return new MapDataValue(this);
	}
	
	/**
	 * Obtains the key type of the map.
	 * @return the key type
	 */
	public DataType key_type() {
		return m_key_type;
	}
	
	/**
	 * Obtains the value type of the map.
	 * @return the value type
	 */
	public DataType value_type() {
		return m_value_type;
	}

	@Override
	public boolean is_abstract() {
		return false;
	}
}
