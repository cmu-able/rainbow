package edu.cmu.cs.able.typelib.parser;

import incubator.pval.Ensure;
import edu.cmu.cs.able.typelib.comp.ListDataType;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;

/**
 * Composite data type name for creating a {@link ListDataType}.
 */
public class ListCompositeDataTypeName extends CompositeDataTypeName {
	/**
	 * Creates a new data type name.
	 * @param inner the name of the list's inner type
	 */
	public ListCompositeDataTypeName(DataTypeName inner) {
		Ensure.not_null(inner);
		add(inner);
	}

	@Override
	protected String composite_name() {
		return ListDataType.build_list_name(inner_types().get(0));
	}

	@Override
	protected DataTypeScope scope() {
		return inner_types().get(0).parent_dts();
	}
	
	@Override
	protected DataType create_data_type(PrimitiveScope pscope) {
		Ensure.not_null(pscope);
		return ListDataType.list_of(inner_types().get(0), pscope);
	}
}
