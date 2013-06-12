package edu.cmu.cs.able.typelib.comp;

import incubator.pval.Ensure;

import java.util.Arrays;
import java.util.HashSet;

import edu.cmu.cs.able.typelib.prim.AnyType;
import edu.cmu.cs.able.typelib.type.DataType;

/**
 * Abstract data type which is a specialization of a single composite data
 * type which contains one or more data types inside which can be obtained with
 * a snapshot. All data values instances of {@link CollectionDataType} are
 * instances of {@link CollectionDataValue}.
 */
public abstract class CollectionDataType extends SingleCompositeDataType {
	/**
	 * Creates a new composite data type.
	 * @param name the data type name
	 * @param inner_type the inner data type
	 * @param any the any type
	 */
	protected CollectionDataType(String name, DataType inner_type,
			final AnyType any) {
		super(name, inner_type, new HashSet<>(Arrays.asList(
				(DataType) Ensure.not_null(any))));
	}
	
	/**
	 * Creates a new collection data value.
	 * @return a new, empty, collection
	 */
	public abstract CollectionDataValue make();
}
