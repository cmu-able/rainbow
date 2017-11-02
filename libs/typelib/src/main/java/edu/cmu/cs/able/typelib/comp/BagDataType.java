package edu.cmu.cs.able.typelib.comp;

import incubator.pval.Ensure;
import edu.cmu.cs.able.typelib.prim.AnyType;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.scope.AmbiguousNameException;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;

/**
 * Data type implementing a bag of another data type.
 */
public class BagDataType extends CollectionDataType {
	/**
	 * Prefix for bag name.
	 */
	private static final String BAG_NAME_PREFIX = "bag<";
	
	/**
	 * Suffix for bag name.
	 */
	private static final String BAG_NAME_SUFFIX = ">";
	
	/**
	 * Creates a new bag.
	 * @param inner_type the bag's inner type
	 * @param any the any type
	 */
	public BagDataType(DataType inner_type, AnyType any) {
		super(build_bag_name(inner_type), inner_type, any);
	}

	/**
	 * Obtains the name of a bag over an inner type.
	 * @param inner the inner type
	 * @return the name of the data type
	 */
	public static final String build_bag_name(DataType inner) {
		Ensure.not_null(inner, "inner == null");
		return BAG_NAME_PREFIX + inner.name() + BAG_NAME_SUFFIX;
	}
	
	/**
	 * Obtains (and creates, if necessary) the bag data type that contains the
	 * given inner data type. The bag data type is created, if necessary, in
	 * the same scope as the inner type.
	 * @param inner the inner data type
	 * @param pscope the primitive data type scope which should be the scope
	 * (or a parent of the scope) of inner
	 * @return the list data type
	 */
	public static BagDataType bag_of(DataType inner, PrimitiveScope pscope) {
		Ensure.not_null(inner);
		Ensure.not_null(pscope);
		Ensure.is_true(inner.in_scope(pscope));
		
		DataTypeScope scope = inner.parent_dts();
		DataType found = null;
		try {
			found = scope.find(build_bag_name(inner));
		} catch (AmbiguousNameException e) {
			/*
			 * No set found.
			 */
		}
		
		if (found == null) {
			found = new BagDataType(inner, pscope.any());
			scope.add(found);
		}
		
		Ensure.not_null(found);
		Ensure.is_instance(found, BagDataType.class);
		return (BagDataType) found;
	}
	
	@Override
	public BagDataValue make() {
		return new BagDataValue(this);
	}

	@Override
	public boolean is_abstract() {
		return false;
	}
}
