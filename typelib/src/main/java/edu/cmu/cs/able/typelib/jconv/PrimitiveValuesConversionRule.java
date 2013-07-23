package edu.cmu.cs.able.typelib.jconv;

import java.util.Date;

import incubator.pval.Ensure;
import edu.cmu.cs.able.typelib.prim.AsciiValue;
import edu.cmu.cs.able.typelib.prim.BooleanValue;
import edu.cmu.cs.able.typelib.prim.DoubleValue;
import edu.cmu.cs.able.typelib.prim.FloatValue;
import edu.cmu.cs.able.typelib.prim.Int16Value;
import edu.cmu.cs.able.typelib.prim.Int32Value;
import edu.cmu.cs.able.typelib.prim.Int64Value;
import edu.cmu.cs.able.typelib.prim.Int8Value;
import edu.cmu.cs.able.typelib.prim.PeriodValue;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.prim.StringValue;
import edu.cmu.cs.able.typelib.prim.TimeValue;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Conversion rule that converts between Java and typelib primitive types.
 */
public class PrimitiveValuesConversionRule
		implements TypelibJavaConversionRule {
	/**
	 * The primitive type scope.
	 */
	private PrimitiveScope m_pscope;
	
	/**
	 * Creates a new rule.
	 * @param pscope the primitive type scope
	 */
	public PrimitiveValuesConversionRule(PrimitiveScope pscope) {
		Ensure.not_null(pscope);
		m_pscope = pscope;
	}

	@Override
	public boolean handles_java(Object value, DataType dst) {
		return from_java_fail_null(value, dst) != null;
	}

	@Override
	public boolean handles_typelib(DataValue value, Class<?> cls) {
		return to_java_fail_null(value, cls) != null;
	}

	@Override
	public DataValue from_java(Object value, DataType dst,
			TypelibJavaConverter converter) throws ValueConversionException {
		Ensure.not_null(converter);
		
		DataValue v = from_java_fail_null(value, dst);
		Ensure.not_null(v);
		return v;
	}
	
	/**
	 * Converts a value from Java into typelib.
	 * @param value the value to convert
	 * @param dst an optional destination data type
	 * @return the converted value or <code>null</code> if cannot convert
	 */
	private DataValue from_java_fail_null(Object value, DataType dst) {
		if (value == null) {
			return null;
		}
		
		if (value instanceof Byte) {
			if (dst == null || dst == m_pscope.int8()
					|| dst.super_of(m_pscope.int8())) {
				return m_pscope.int8().make((byte) value);
			}
		}
		
		if (value instanceof Short) {
			if (dst == null || dst == m_pscope.int16()
					|| dst.super_of(m_pscope.int16())) {
				return m_pscope.int16().make((short) value);
			}
		}
		
		if (value instanceof Integer) {
			if (dst == null || dst == m_pscope.int32()
					|| dst.super_of(m_pscope.int32())) {
				return m_pscope.int32().make((int) value);
			}
		}
		
		if (value instanceof Long) {
			if (dst == null || dst == m_pscope.int64()
					|| dst.super_of(m_pscope.int64())) {
				return m_pscope.int64().make((long) value);
			}
			
			if (dst == m_pscope.period()) {
				return m_pscope.period().make((long) value);
			}
		}
		
		if (value instanceof String) {
			if (dst == null || dst == m_pscope.string()
					|| dst.super_of(m_pscope.string())) {
				return m_pscope.string().make((String) value);
			}
			
			if (dst == m_pscope.ascii()) {
				return m_pscope.ascii().make((String) value);
			}
		}
		
		if (value instanceof Boolean) {
			if (dst == null || dst == m_pscope.bool()
					|| dst.super_of(m_pscope.bool())) {
				return m_pscope.bool().make((boolean) value);
			}
		}
		
		if (value instanceof Float) {
			if (dst == null || dst == m_pscope.float_type()
					|| dst.super_of(m_pscope.float_type())) {
				return m_pscope.float_type().make((float) value);
			}
		}
		
		if (value instanceof Double) {
			if (dst == null || dst == m_pscope.double_type()
					|| dst.super_of(m_pscope.double_type())) {
				return m_pscope.double_type().make((double) value);
			}
		}
		
		if (value instanceof Date) {
			if (dst == null || dst == m_pscope.time()
					|| dst.super_of(m_pscope.time())) {
				return m_pscope.time().make(((Date) value).getTime());
			}
		}
		
		return null;
	}

	@Override
	public <T> T to_java(DataValue value, Class<T> cls,
			TypelibJavaConverter converter) throws ValueConversionException {
		Ensure.not_null(value);
		Ensure.not_null(converter);
		
		T t = to_java_fail_null(value, cls);
		Ensure.not_null(t);
		return t;
	}
	
	/**
	 * Converts a value from typelib into Java, returning <code>null</code>
	 * if conversion failed
	 * @param value the value to convert
	 * @param cls an optional destination data type
	 * @return the converted value or <code>null</code> if cannot convert
	 */
	private <T> T to_java_fail_null(DataValue value, Class<T> cls) {
		Ensure.not_null(value);
		
		if (m_pscope.int8().is_instance(value)) {
			if (cls == null || cls.isAssignableFrom(Byte.class)) {
				@SuppressWarnings("unchecked")
				T t = (T) ((Int8Value) value).value();
				return t;
			}
		}
		
		if (m_pscope.int16().is_instance(value)) {
			if (cls == null || cls.isAssignableFrom(Short.class)) {
				@SuppressWarnings("unchecked")
				T t = (T) ((Int16Value) value).value();
				return t;
			}
		}
		
		if (m_pscope.int32().is_instance(value)) {
			if (cls == null || cls.isAssignableFrom(Integer.class)) {
				@SuppressWarnings("unchecked")
				T t = (T) ((Int32Value) value).value();
				return t;
			}
		}
		
		if (m_pscope.int64().is_instance(value)) {
			if (cls == null || cls.isAssignableFrom(Long.class)) {
				@SuppressWarnings("unchecked")
				T t = (T) ((Int64Value) value).value();
				return t;
			}
		}
		
		if (m_pscope.ascii().is_instance(value)) {
			if (cls == null || cls.isAssignableFrom(String.class)) {
				@SuppressWarnings("unchecked")
				T t = (T) ((AsciiValue) value).value();
				return t;
			}
		}
		
		if (m_pscope.string().is_instance(value)) {
			if (cls == null || cls.isAssignableFrom(String.class)) {
				@SuppressWarnings("unchecked")
				T t = (T) ((StringValue) value).value();
				return t;
			}
		}
		
		if (m_pscope.float_type().is_instance(value)) {
			if (cls == null || cls.isAssignableFrom(Float.class)) {
				@SuppressWarnings("unchecked")
				T t = (T) ((FloatValue) value).value();
				return t;
			}
		}
		
		if (m_pscope.double_type().is_instance(value)) {
			if (cls == null || cls.isAssignableFrom(Double.class)) {
				@SuppressWarnings("unchecked")
				T t = (T) ((DoubleValue) value).value();
				return t;
			}
		}
		
		if (m_pscope.period().is_instance(value)) {
			if (cls == null || cls.isAssignableFrom(Long.class)) {
				@SuppressWarnings("unchecked")
				T t = (T) ((PeriodValue) value).value();
				return t;
			}
		}
		
		if (m_pscope.time().is_instance(value)) {
			if (cls == null || cls.isAssignableFrom(Date.class)) {
				@SuppressWarnings("unchecked")
				T t = (T) new Date(((TimeValue) value).value());
				return t;
			}
		}
		
		if (m_pscope.bool().is_instance(value)) {
			if (cls == null || cls.isAssignableFrom(Boolean.class)) {
				@SuppressWarnings("unchecked")
				T t = (T) ((BooleanValue) value).value();
				return t;
			}
		}
		
		return null;
	}
}
