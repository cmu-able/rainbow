package edu.cmu.cs.able.typelib.parser;

import incubator.pval.Ensure;
import edu.cmu.cs.able.typelib.comp.BagDataType;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;

/**
 * Composite data type name for creating a {@link BagDataType}.
 */
public class BagCompositeDataTypeName extends CompositeDataTypeName {
	/**
	 * Creates a new data type.
	 * @param inner the name of the bag's inner type
	 */
	public BagCompositeDataTypeName(DataTypeName inner) {
		Ensure.not_null(inner);
		add(inner);
	}

	@Override
	protected String composite_name() {
		return BagDataType.build_bag_name(inner_types().get(0));
	}

	@Override
	protected DataTypeScope scope() {
		return inner_types().get(0).parent_dts();
	}
	
	@Override
	protected DataType create_data_type(PrimitiveScope pscope) {
		Ensure.not_null(pscope);
		return BagDataType.bag_of(inner_types().get(0), pscope);
	}
}
