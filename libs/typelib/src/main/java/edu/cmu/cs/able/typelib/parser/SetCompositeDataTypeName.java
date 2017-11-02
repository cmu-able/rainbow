package edu.cmu.cs.able.typelib.parser;

import incubator.pval.Ensure;
import edu.cmu.cs.able.typelib.comp.SetDataType;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;

/**
 * Composite data type name for creating a {@link SetDataType}.
 */
public class SetCompositeDataTypeName extends CompositeDataTypeName {
	/**
	 * Creates a new data type.
	 * @param inner the name of the set's inner type
	 */
	public SetCompositeDataTypeName(DataTypeName inner) {
		Ensure.not_null(inner);
		add(inner);
	}

	@Override
	protected String composite_name() {
		return SetDataType.build_set_name(inner_types().get(0));
	}

	@Override
	protected DataTypeScope scope() {
		return inner_types().get(0).parent_dts();
	}
	
	@Override
	protected DataType create_data_type(PrimitiveScope pscope) {
		Ensure.not_null(pscope);
		return SetDataType.set_of(inner_types().get(0), pscope);
	}
}
