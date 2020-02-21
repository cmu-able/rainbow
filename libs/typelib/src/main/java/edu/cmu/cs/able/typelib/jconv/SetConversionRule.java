package edu.cmu.cs.able.typelib.jconv;

import incubator.pval.Ensure;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import edu.cmu.cs.able.typelib.comp.SetDataType;
import edu.cmu.cs.able.typelib.comp.SetDataValue;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Conversion rules for set data types.
 */
public class SetConversionRule
		implements TypelibJavaConversionRule {
	/**
	 * Creates a new rule.
	 */
	public SetConversionRule() {
		/*
		 * Nothing to do.
		 */
	}

	@Override
	public boolean handles_java(Object value, DataType dst) {
		if (value == null || dst == null) {
			return false;
		}
		
		return dst instanceof SetDataType && value instanceof Set;
	}

	@Override
	public boolean handles_typelib(DataValue value, Class<?> cls) {
		Ensure.not_null(value);
		
		if (make_instance(cls) == null) {
			return false;
		}
		
		return value instanceof SetDataValue;
	}

	@Override
	public DataValue from_java(Object value, DataType dst,
			TypelibJavaConverter converter) throws ValueConversionException {
		Ensure.not_null(dst);
		Ensure.is_instance(dst, SetDataType.class);
		Ensure.not_null(value);
		Ensure.is_instance(value, Set.class);
		
		SetDataType sdt = (SetDataType) dst;
		SetDataValue sdv = sdt.make();
		for (Object o : (Set<?>) value) {
			sdv.add(converter.from_java(o, sdt.inner_type()));
		}
		
		return sdv;
	}

	@Override
	public <T> T to_java(DataValue value, Class<T> cls,
			TypelibJavaConverter converter) throws ValueConversionException {
		Ensure.not_null(value);
		Ensure.is_instance(value, SetDataValue.class);
		
		Set<Object> set = make_instance(cls);
		Ensure.not_null(set);
		
		for (DataValue v : ((SetDataValue) value).all()) {
			set.add(converter.to_java(v, null));
		}
		
		@SuppressWarnings("unchecked")
		T t = (T) set;
		return t;
	}
	
	/**
	 * Creates an instance of the given class, which must be a subclass of
	 * Set.
	 * @param cls the class; if <code>null</code> a default set will be created
	 * @return the instance
	 */
	private Set<Object> make_instance(Class<?> cls) {
		if (cls == null || cls == Set.class) {
			return new HashSet<>();
		}
		
		if (!Set.class.isAssignableFrom(cls)) {
			return null;
		}
		
		try {
			Set<?> s = Set.class.cast(cls.getConstructor().newInstance());
			@SuppressWarnings("unchecked")
			Set<Object> so = (Set<Object>) s;
			return so;
		} catch (NoSuchMethodException | SecurityException
				| InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| ClassCastException e) {
			return null;
		}
	}
}
