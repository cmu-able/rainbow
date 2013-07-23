package edu.cmu.cs.able.typelib.jconv;

import incubator.pval.Ensure;

import java.util.ArrayList;
import java.util.List;

import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * A rule-based converter of values between Java and typelib.
 */
public class TypelibJavaConverter {
	/**
	 * Rules to apply, ordered by precedence.
	 */
	private List<TypelibJavaConversionRule> m_rules;
	
	/**
	 * Creates a new converter with no rules. The
	 * {@link #add(TypelibJavaConversionRule)} method can be used to add
	 * rules to a converter. Use a {@link DefaultTypelibJavaConverter} to
	 * create a converter with all default typelib conversion rules.
	 */
	public TypelibJavaConverter() {
		m_rules = new ArrayList<>();
	}
	
	/**
	 * Adds a conversion rule to the converter. Rules are attempted in the
	 * order they are added: rules added first have higher priority over
	 * rules added later.
	 * @param rule the rule to add
	 */
	public synchronized void add(TypelibJavaConversionRule rule) {
		Ensure.not_null(rule);
		m_rules.add(rule);
	}
	
	/**
	 * Converts a value from Java to a typelib value.
	 * @param obj the object to convert
	 * @param type an optional destination data type
	 * @return the converted value
	 * @throws ValueConversionException failed to convert
	 */
	public synchronized DataValue from_java(Object obj, DataType type)
			throws ValueConversionException {
		for (TypelibJavaConversionRule r : m_rules)  {
			if (r.handles_java(obj, type)) {
				DataValue v = r.from_java(obj, type, this);
				Ensure.not_null(v);
				return v;
			}
		}
		
		String type_name;
		if (type == null) {
			type_name = "null";
		} else {
			type_name = "'" + type.name() + "'";
		}
		
		throw new ValueConversionException("Cannot convert {" + obj
				+ "} to typelib with data type " + type_name + ".");
	}
	
	/**
	 * Converts a value from a typelib value to a Java value.
	 * @param value the value to convert
	 * @param type an optional destination data type
	 * @return the converted value
	 * @param <T> the Java data type
	 * @throws ValueConversionException failed to convert
	 */
	public <T> T to_java(DataValue value, Class<T> type)
			throws ValueConversionException {
		Ensure.not_null(value);
		
		for (TypelibJavaConversionRule r : m_rules)  {
			if (r.handles_typelib(value, type)) {
				return r.to_java(value, type, this);
			}
		}
		
		String type_name;
		if (type == null) {
			type_name = "null";
		} else {
			type_name = "'" + type + "'";
		}
		
		throw new ValueConversionException("Cannot convert {" + value
				+ "} to Java with data type " + type_name + ".");
	}
}
