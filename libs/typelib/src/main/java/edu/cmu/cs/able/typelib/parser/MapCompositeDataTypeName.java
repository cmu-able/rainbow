package edu.cmu.cs.able.typelib.parser;

import incubator.pval.Ensure;

import java.util.HashSet;
import java.util.Set;

import edu.cmu.cs.able.typelib.comp.MapDataType;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.scope.Scope;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;

/**
 * Composite data type name for creating a {@link MapDataType}.
 */
public class MapCompositeDataTypeName extends CompositeDataTypeName {
	/**
	 * Creates a new data type.
	 * @param key_type the type of the map's keys
	 * @param value_type the type of the map's values
	 */
	public MapCompositeDataTypeName(DataTypeName key_type,
			DataTypeName value_type) {
		Ensure.not_null(key_type);
		Ensure.not_null(value_type);
		add(key_type);
		add(value_type);
	}

	@Override
	protected String composite_name() {
		return MapDataType.build_map_name(inner_types().get(0),
				inner_types().get(1));
	}

	@Override
	protected DataTypeScope scope() {
		Ensure.greater(inner_types().size(), 0);
		
		Set<DataTypeScope> scopes = new HashSet<>();
		scopes.add(inner_types().get(0).parent_dts());
		scopes.add(inner_types().get(1).parent_dts());
		
		DataTypeScope common = (DataTypeScope)
				Scope.common_inner_most_parent_scope(scopes);
		Ensure.not_null(common);
		
		return common;
	}
	
	@Override
	protected DataType create_data_type(PrimitiveScope pscope) {
		Ensure.not_null(pscope);
		return MapDataType.map_of(inner_types().get(0), inner_types().get(1),
				pscope);
	}
}
