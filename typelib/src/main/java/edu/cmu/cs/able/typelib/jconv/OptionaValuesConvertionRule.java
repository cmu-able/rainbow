package edu.cmu.cs.able.typelib.jconv;

import incubator.pval.Ensure;
import edu.cmu.cs.able.typelib.comp.OptionalDataType;
import edu.cmu.cs.able.typelib.comp.OptionalDataValue;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Conversion rule that converts between optional types.
 */
public class OptionaValuesConvertionRule implements TypelibJavaConversionRule {
	/**
	 * Creates a new conversion rule.
	 */
	public OptionaValuesConvertionRule() {
		/*
		 * Nothing to do.
		 */
	}

	@Override
	public boolean handles_java(Object value, DataType dst) {
		return dst != null && dst instanceof OptionalDataType;
	}

	@Override
	public boolean handles_typelib(DataValue value, Class<?> cls) {
		Ensure.not_null(value);
		return value instanceof OptionalDataValue;
	}

	@Override
	public DataValue from_java(Object value, DataType dst,
			TypelibJavaConverter converter) throws ValueConversionException {
		Ensure.not_null(dst);
		OptionalDataType odt = (OptionalDataType) dst;
		if (value == null) {
			return odt.make(null);
		} else {
			return odt.make(converter.from_java(value, odt.inner_type()));
		}
	}

	@Override
	public <T> T to_java(DataValue value, Class<T> cls,
			TypelibJavaConverter converter) throws ValueConversionException {
		Ensure.not_null(value);
		OptionalDataValue odv = (OptionalDataValue) value;
		if (odv.value() == null) {
			return null;
		} else {
			return converter.to_java(odv.value(), cls);
		}
	}

}
