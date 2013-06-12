package edu.cmu.cs.able.typelib.parser;

import incubator.pval.Ensure;
import edu.cmu.cs.able.typelib.comp.OptionalDataType;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;

/**
 * Composite data type name for creating an {@link OptionalDataType}.
 */
public class OptionalCompositeDataTypeName extends CompositeDataTypeName {
	/**
	 * Creates a new data type.
	 * @param inner the name of the optional inner type
	 */
	public OptionalCompositeDataTypeName(DataTypeName inner) {
		Ensure.not_null(inner);
		add(inner);
	}

	@Override
	protected String composite_name() {
		return OptionalDataType.build_optional_name(inner_types().get(0));
	}

	@Override
	protected DataTypeScope scope() {
		return inner_types().get(0).parent_dts();
	}
	
	@Override
	protected DataType create_data_type(PrimitiveScope pscope) {
		Ensure.not_null(pscope);
		return OptionalDataType.optional_of(inner_types().get(0));
	}
}
