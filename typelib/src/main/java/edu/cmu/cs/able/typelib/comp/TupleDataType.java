package edu.cmu.cs.able.typelib.comp;

import incubator.pval.Ensure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.cs.able.typelib.prim.AnyType;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.scope.AmbiguousNameException;
import edu.cmu.cs.able.typelib.scope.Scope;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * A tuple data type is a data type containing a ordered list of types.
 */
public class TupleDataType extends DataType {
	/**
	 * Prefix for tuple type name.
	 */
	private static final String TUPLE_NAME_PREFIX = "tuple<";
	
	/**
	 * Suffix for tuple type name.
	 */
	private static final String TUPLE_NAME_SUFFIX = ">";
	
	/**
	 * Separator used to split types in the tuple.
	 */
	private static final String TUPLE_NAME_SEPARATOR = ",";
	
	/**
	 * The inner types.
	 */
	private List<DataType> m_inner;

	/**
	 * Creates a new tuple data type.
	 * @param inner_types the types in the tuple which may be zero
	 * @param any the any type, super type of the tuple
	 */
	public TupleDataType(List<DataType> inner_types, AnyType any) {
		super(build_tuple_name(inner_types),
				new HashSet<DataType>(Arrays.asList(any)));
		Ensure.not_null(inner_types, "inner_types == null");
		m_inner = new ArrayList<>(inner_types);
	}
	
	/**
	 * Creates the tuple from the list of data types.
	 * @param types the data types
	 * @return the tuple name
	 */
	public static String build_tuple_name(List<DataType> types) {
		Ensure.not_null(types, "types == null");
		String name = TUPLE_NAME_PREFIX;
		for (int i = 0; i < types.size(); i++) {
			if (i > 0) {
				name += TUPLE_NAME_SEPARATOR;
			}
			
			name += types.get(i).name();
		}
		
		name += TUPLE_NAME_SUFFIX;
		return name;
	}
	
	/**
	 * Obtains (and creates, if necessary) the tuple data type that contains
	 * the given inner data types. The tuple data type is created, if
	 * necessary, in inner-most scope that contains all its inner types
	 * @param inner the inner data types (cannot be empty)
	 * @param pscope the primitive data type scope which should be the scope
	 * (or a parent of the scope) of all inner types
	 * @return the list data type
	 */
	public static TupleDataType tuple_of(List<DataType> inner,
			PrimitiveScope pscope) {
		Ensure.not_null(inner);
		Ensure.not_null(pscope);
		Ensure.greater(inner.size(), 0);
		
		Set<DataTypeScope> scopes = new HashSet<>();
		for (DataType d : inner) {
			Ensure.is_true(d.in_scope(pscope));
			scopes.add(d.parent_dts());
		}
		
		
		DataTypeScope scope = (DataTypeScope)
				Scope.common_inner_most_parent_scope(scopes);
		Ensure.not_null(scope);
		
		DataType found = null;
		try {
			found = scope.find(build_tuple_name(inner));
		} catch (AmbiguousNameException e) {
			/*
			 * No set found.
			 */
		}
		
		if (found == null) {
			found = new TupleDataType(inner, pscope.any());
			scope.add(found);
		}
		
		Ensure.not_null(found);
		Ensure.is_instance(found, TupleDataType.class);
		return (TupleDataType) found;
	}
	
	/**
	 * Makes a new tuple.
	 * @param values the tuple values which must match the type's values
	 * @return the new tuple
	 */
	public TupleDataValue make(List<DataValue> values) {
		Ensure.not_null(values, "values == null");
		Ensure.is_true(m_inner.size() == values.size(), "Incorrect size "
				+ "of values list");
		for (int i = 0; i < m_inner.size(); i++) {
			Ensure.is_true(m_inner.get(i).is_instance(values.get(i)),
					"Value in value list does not match type");
		}
		
		return new TupleDataValue(this, values);
	}
	
	/**
	 * Obtains the inner types of the tuple.
	 * @return the inner types
	 */
	public List<DataType> inner_types() {
		return new ArrayList<>(m_inner);
	}

	@Override
	public boolean is_abstract() {
		return false;
	}
}
