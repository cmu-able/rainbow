package edu.cmu.cs.able.typelib.jconv;

import incubator.pval.Ensure;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.cs.able.typelib.comp.ListDataType;
import edu.cmu.cs.able.typelib.comp.ListDataValue;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Conversion rules for list data types.
 */
public class ListConversionRule
		implements TypelibJavaConversionRule {
	/**
	 * Creates a new rule.
	 */
	public ListConversionRule() {
		/*
		 * Nothing to do.
		 */
	}

	@Override
	public boolean handles_java(Object value, DataType dst) {
		if (value == null || dst == null) {
			return false;
		}
		
		return dst instanceof ListDataType && value instanceof List;
	}

	@Override
	public boolean handles_typelib(DataValue value, Class<?> cls) {
		Ensure.not_null(value);
		
		if (make_instance(cls) == null) {
			return false;
		}
		
		return value instanceof ListDataValue;
	}

	@Override
	public DataValue from_java(Object value, DataType dst,
			TypelibJavaConverter converter) throws ValueConversionException {
		Ensure.not_null(dst);
		Ensure.is_instance(dst, ListDataType.class);
		Ensure.not_null(value);
		Ensure.is_instance(value, List.class);
		
		ListDataType ldt = (ListDataType) dst;
		ListDataValue ldv = ldt.make();
		for (Object o : (List<?>) value) {
			ldv.add(converter.from_java(o, ldt.inner_type()));
		}
		
		return ldv;
	}

	@Override
	public <T> T to_java(DataValue value, Class<T> cls,
			TypelibJavaConverter converter) throws ValueConversionException {
		Ensure.not_null(value);
		Ensure.is_instance(value, ListDataValue.class);
		
		List<Object> list = make_instance(cls);
		Ensure.not_null(list);
		
		for (DataValue v : ((ListDataValue) value).all()) {
			list.add(converter.to_java(v, null));
		}
		
		@SuppressWarnings("unchecked")
		T t = (T) list;
		return t;
	}
	
	/**
	 * Creates an instance of the given class, which must be a subclass of
	 * List.
	 * @param cls the class; if <code>null</code> a default set will be created
	 * @return the instance
	 */
	private List<Object> make_instance(Class<?> cls) {
		if (cls == null || cls == List.class) {
			return new ArrayList<>();
		}
		
		if (!List.class.isAssignableFrom(cls)) {
			return null;
		}
		
		try {
			List<?> l = List.class.cast(cls.getConstructor().newInstance());
			@SuppressWarnings("unchecked")
			List<Object> lo = (List<Object>) l;
			return lo;
		} catch (NoSuchMethodException | SecurityException
				| InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| ClassCastException e) {
			return null;
		}
	}
}
