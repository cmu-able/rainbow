package edu.cmu.cs.able.typelib.parser;

import incubator.pval.Ensure;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cmu.cs.able.typelib.comp.SetDataType;
import edu.cmu.cs.able.typelib.comp.TupleDataType;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.scope.Scope;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;

/**
 * Composite data type name for creating a {@link TupleDataType}.
 */
public class TupleCompositeDataTypeName extends CompositeDataTypeName {
	/**
	 * Creates a new data type.
	 * @param inner the names of the tuple's inner types; at least one name
	 * must be provided
	 */
	public TupleCompositeDataTypeName(List<DataTypeName> inner) {
		Ensure.not_null(inner);
		Ensure.greater(inner.size(), 0);
		
		for (DataTypeName n : inner) {
			Ensure.not_null(n);
			add(n);
		}
	}

	@Override
	protected String composite_name() {
		return SetDataType.build_set_name(inner_types().get(0));
	}

	@Override
	protected DataTypeScope scope() {
		Ensure.greater(inner_types().size(), 0);
		
		Set<DataTypeScope> scopes = new HashSet<>();
		for (DataType i : inner_types()) {
			scopes.add(i.parent_dts());
		}
		
		DataTypeScope common = (DataTypeScope)
				Scope.common_inner_most_parent_scope(scopes);
		Ensure.not_null(common);
		
		return common;
	}
	
	@Override
	protected DataType create_data_type(PrimitiveScope pscope) {
		Ensure.not_null(pscope);
		return TupleDataType.tuple_of(inner_types(), pscope);
	}
}
