package edu.cmu.cs.able.typelib.alg;

import incubator.pval.Ensure;
import edu.cmu.cs.able.typelib.comp.OptionalDataType;
import edu.cmu.cs.able.typelib.comp.OptionalDataValue;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Class containing several algorithms simplifying the work with optional
 * data types.
 */
public class Optionality {
	/**
	 * Utility class: no constructor.
	 */
	private Optionality() {
		/*
		 * No constructor.
		 */
	}
	
	/**
	 * Checks if a data type is optional.
	 * @param dt the data type to check
	 * @return is the data type optional?
	 */
	public static boolean is_optional(DataType dt) {
		Ensure.not_null(dt);
		return (dt instanceof OptionalDataType);
	}
	
	/**
	 * Checks if a data value is optional.
	 * @param dv the data value to check
	 * @return is the data value optional?
	 */
	public static boolean is_optional(DataValue dv) {
		Ensure.not_null(dv);
		return (dv instanceof OptionalDataValue);
	}
	
	/**
	 * Obtains the non-optional version of a data type. This method returns the
	 * inner type of the optional type (which may be an optional data type
	 * itself).
	 * @param dt the data type
	 * @return if the data type is not an optional data type, then the data
	 * type is returned, if it is optional, then the inner data type is
	 * returned
	 */
	public static DataType unoptionalize(DataType dt) {
		Ensure.not_null(dt);
		if (is_optional(dt)) {
			return ((OptionalDataType) dt).inner_type();
		}
		
		return dt;
	}
	
	/**
	 * Obtains the non-optional version of a data type. This method extracts
	 * the inner type recursively while the inner type is an optional type.
	 * @param dt the data type
	 * @return the non-optional inner type in <code>dt</code>
	 */
	public static DataType deep_unoptionalize(DataType dt) {
		Ensure.not_null(dt);
		if (is_optional(dt)) {
			return deep_unoptionalize(((OptionalDataType) dt).inner_type());
		}
		
		return dt;
	}
	
	/**
	 * Obtains the inner value of a data value.
	 * @param dv the data value
	 * @return if the data value is not an optional data value, then the
	 * data value is returned, otherwise, the value encapsulated within the
	 * optional data value is returned
	 */
	public static DataValue unoptionalize(DataValue dv) {
		Ensure.not_null(dv);
		if (is_optional(dv)) {
			return ((OptionalDataValue) dv).value();
		}
		
		return dv;
	}
	
	/**
	 * Obtains the inner value of a data value, operating recursively until
	 * a non-optional value is found or <code>null</code> is found.
	 * @param dv the data value
	 * @return the non-optional inner value in <code>dv</code>
	 */
	public static DataValue deep_unoptionalize(DataValue dv) {
		Ensure.not_null(dv);
		if (is_optional(dv)) {
			dv = ((OptionalDataValue) dv).value();
			if (dv != null) {
				dv = deep_unoptionalize(dv);
			}
		}
		
		return dv;
	}
	
	/**
	 * Checks if a data value needs to be enclosed in an optional data type.
	 * @param v the value; <code>null</code> is allowed if <code>dt</code> is
	 * an optional data type. If <code>dt</code> is an optional data type with
	 * optional data types inside, the value will be placed in multiple
	 * optional values recursively to fit the data type. If <code>v</code> is
	 * an optional value itself, it will be unwrapped first.
	 * @param dt the data type
	 * @return if the data type is an optional data type, returns <em>v</em>
	 * enclosed within an optional value (or multiple optional values
	 * recursively, if needed); if the data type is not an optional
	 * data type, return <em>v</em>
	 */
	public static DataValue optionalize(DataValue v, DataType dt) {
		Ensure.not_null(dt);
		
		if (!(dt instanceof OptionalDataType)) {
			Ensure.not_null(v);
			return v;
		}
		
		if (v != null) {
			v = unoptionalize(v);
		}
		
		OptionalDataType odt = (OptionalDataType) dt;
		DataType inner_type = odt.inner_type();
		if (v != null && inner_type instanceof OptionalDataType) {
			v = optionalize(v, inner_type);
		}
		
		return odt.make(v);
	}
	
	/**
	 * Checks whether a data type is an optional data type whose inner type
	 * is the one given.
	 * @param opt the maybe optional data type
	 * @param inner the type to check if it is the inner type
	 * @return whether the type is an optional data type with <em>inner</em>
	 * as inner type
	 */
	public static boolean is_optional_of(DataType opt, DataType inner) {
		Ensure.not_null(opt);
		Ensure.not_null(inner);
		
		if (!(opt instanceof OptionalDataType)) {
			return false;
		}
		
		OptionalDataType odt = (OptionalDataType) opt;
		return (odt.inner_type() == inner);
	}
	
	/**
	 * Checks whether a data type is an optional data type whose inner type
	 * is the one given. If <code>opt</code> contains an optional data type
	 * inside which is not <code>inner</code>, this process will check it
	 * recursively
	 * @param opt the maybe optional data type
	 * @param inner the type to check if it is the inner type
	 * @return whether the type is an optional data type with <em>inner</em>
	 * as inner type or as an inner type of an inner type recursively
	 */
	public static boolean is_deep_optional_of(DataType opt, DataType inner) {
		Ensure.not_null(opt);
		Ensure.not_null(inner);
		
		if (!(opt instanceof OptionalDataType)) {
			return false;
		}
		
		OptionalDataType odt = (OptionalDataType) opt;
		if (odt.inner_type() == inner) {
			return true;
		}
		
		return is_deep_optional_of(odt.inner_type(), inner);
	}
}
