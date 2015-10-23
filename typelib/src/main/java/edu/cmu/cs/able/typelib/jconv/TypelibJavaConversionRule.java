package edu.cmu.cs.able.typelib.jconv;

import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * <p>Interface of a conversion rule that can convert between Java and typelib
 * data types. Because values may have types different that what they appear
 * due to subclasses and to handle <code>null</code>, besides providing the
 * object to convert, it is possible to provide the destination data type.</p>
 * <p>Conversion should be done in two steps: one should first check if it
 * is possible to convert using the <code>handles_xxx</code> method and, if
 * the method return <code>true</code>, convert using either the
 * {@link #from_java(Object, DataType, TypelibJavaConverter)} or
 * {@link #to_java(DataValue, Class, TypelibJavaConverter)} methods.</p>
 * <p>Conversion may proceed recursively for complex data structures. If
 * conversion fails for some reason, {@link ValueConversionException} may
 * be thrown.</p>
 */
public interface TypelibJavaConversionRule {
	/**
	 * Can the given Java value be handled by this rule?
	 * @param value the Java value
	 * @param dst optionally, the data type to convert to
	 * @return can it be converted?
	 */
	boolean handles_java (Object value, DataType dst);
	
	/**
	 * Can the given typelib value be handled by this rule?
	 * @param value the typelib value
	 * @param cls optionally, the data type to convert to
	 * @return can it be converted?
	 */
	boolean handles_typelib (DataValue value, Class<?> cls);
	
	/**
	 * Converts a value from Java into typelib.
	 * @param value the Java value, which cannot be <code>null</code>
	 * @param dst optionally, the data type to convert to
	 * @param converter the converter that may be used to convert values
	 * recursively
	 * @return the typelib value, which cannot be <code>null</code>
	 * @throws ValueConversionException failed to convert
	 */
	DataValue from_java (Object value, DataType dst,
						 TypelibJavaConverter converter) throws ValueConversionException;
	
	/**
	 * Converts a typelib value to a Java value.
	 * @param value the typelib value
	 * @param cls optionally, the data type to convert to
	 * @param converter the converter that may be used to convert values
	 * recursively
	 * @return the Java value, which may be <code>null</code>
	 * @param <T> the type of the Java value
	 * @throws ValueConversionException failed to convert
	 */
	<T> T to_java (DataValue value, Class<T> cls,
				   TypelibJavaConverter converter) throws ValueConversionException;
}
