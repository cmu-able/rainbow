package edu.cmu.cs.able.typelib.comp;

import incubator.pval.Ensure;

import java.util.HashSet;
import java.util.Set;

import edu.cmu.cs.able.typelib.scope.AmbiguousNameException;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataTypeScope;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * The optional data type is a datatype that encapsulates another data type
 * making it <em>nullable</em>. If the inner data type has natural ordering,
 * the encapsulated data type will also have natural ordering and
 * <code>null</code>s will be placed at the end. 
 */
public class OptionalDataType extends SingleCompositeDataType {
	/**
	 * Suffix in data type name indicating it is optional.
	 */
	private static final String OPTIONAL_SUFFIX = "?";
	
	/**
	 * Creates a new optional data type.
	 * @param inner the inner data type
	 * @param super_types the optional data type's super types
	 */
	public OptionalDataType(DataType inner, Set<OptionalDataType> super_types) {
		super(build_optional_name(inner), inner, new HashSet<DataType>(
				Ensure.not_null(super_types)));
	}
	
	/**
	 * Obtains the name of an optional data type over an inner type.
	 * @param inner the inner type
	 * @return the name of the data type
	 */
	public static final String build_optional_name(DataType inner) {
		Ensure.notNull(inner);
		return inner.name() + OPTIONAL_SUFFIX;
	}
	
	/**
	 * Obtains the optional type of a given type. The optional type is created
	 * if it doesn't already exist. If necessary, the type is created in the
	 * same scope as the inner type and its parents are the optional version of
	 * all inner type's parents.
	 * @param inner the inner type of the optional type
	 * @return the existing or created optional type
	 */
	public static OptionalDataType optional_of(DataType inner) {
		Ensure.not_null(inner);
		DataTypeScope inner_scope = inner.parent_dts();
		String optional_name = build_optional_name(inner);
		
		DataType found = null;
		try {
			found = inner_scope.find(optional_name);
		} catch (AmbiguousNameException e) {
			/*
			 * The optional data type was not found.
			 */
		}
		
		if (found == null) {
			Set<OptionalDataType> supers = new HashSet<>();
			for (DataType t : inner.super_types()) {
				supers.add(optional_of(t));
			}
			
			found = new OptionalDataType(inner, supers);
			inner_scope.add(found);
		}
		
		Ensure.not_null(found);
		Ensure.is_instance(found, OptionalDataType.class);
		Ensure.equals(build_optional_name(inner), found.name());
		return (OptionalDataType) found;
	}

	@Override
	public boolean is_abstract() {
		return inner_type().is_abstract();
	}
	
	/**
	 * Makes a new data value.
	 * @param v the value or <code>null</code> if there is none
	 * @return the optional data value whose data type will this data type if
	 * <code>v</code> is <code>null</code> and will be the optional data type
	 * of <code>v</code>'s type if <code>v</code> is not <code>null</code>;
	 * consequently, the resulting value will always be an instance of this
	 * type but it may be an instance of a sub type if <code>v</code> is an
	 * instance of a sub type of this type's inner type  
	 */
	public OptionalDataValue make(DataValue v) {
		if (v != null) {
			Ensure.isTrue(inner_type().is_instance(v));
			if (v.type() != inner_type()) {
				return OptionalDataType.optional_of(v.type()).make(v);
			}
		}
		
		return new OptionalDataValue(this, v);
	}
}
