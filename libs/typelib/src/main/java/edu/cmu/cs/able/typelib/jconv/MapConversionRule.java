package edu.cmu.cs.able.typelib.jconv;

import incubator.pval.Ensure;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import edu.cmu.cs.able.typelib.comp.MapDataType;
import edu.cmu.cs.able.typelib.comp.MapDataValue;
import edu.cmu.cs.able.typelib.type.DataType;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Conversion rules for map data types.
 */
public class MapConversionRule
		implements TypelibJavaConversionRule {
	/**
	 * Creates a new rule.
	 */
	public MapConversionRule() {
		/*
		 * Nothing to do.
		 */
	}

	@Override
	public boolean handles_java(Object value, DataType dst) {
		if (value == null || dst == null) {
			return false;
		}
		
		return dst instanceof MapDataType && value instanceof Map;
	}

	@Override
	public boolean handles_typelib(DataValue value, Class<?> cls) {
		Ensure.not_null(value);
		
		if (make_instance(cls) == null) {
			return false;
		}
		
		return value instanceof MapDataValue;
	}

	@Override
	public DataValue from_java(Object value, DataType dst,
			TypelibJavaConverter converter) throws ValueConversionException {
		Ensure.not_null(dst);
		Ensure.is_instance(dst, MapDataType.class);
		Ensure.not_null(value);
		Ensure.is_instance(value, Map.class);
		
		MapDataType mdt = (MapDataType) dst;
		MapDataValue mdv = mdt.make();
		for (Object o : ((Map<?, ?>) value).keySet()) {
			mdv.put(converter.from_java(o, mdt.key_type()),
					converter.from_java(((Map<?, ?>) value).get(o),
					mdt.value_type()));
		}
		
		return mdv;
	}

	@Override
	public <T> T to_java(DataValue value, Class<T> cls,
			TypelibJavaConverter converter) throws ValueConversionException {
		Ensure.not_null(value);
		Ensure.is_instance(value, MapDataValue.class);
		
		Map<Object, Object> map = make_instance(cls);
		Ensure.not_null(map);
		
		Map<DataValue, DataValue> all = ((MapDataValue) value).all();
		for (DataValue v : all.keySet()) {
			map.put(converter.to_java(v, null),
					converter.to_java(all.get(v), null));
		}
		
		@SuppressWarnings("unchecked")
		T t = (T) map;
		return t;
	}
	
	/**
	 * Creates an instance of the given class, which must be a subclass of
	 * Map.
	 * @param cls the class; if <code>null</code> a default map will be created
	 * @return the instance
	 */
	private Map<Object, Object> make_instance(Class<?> cls) {
		if (cls == null || cls == Map.class) {
			return new HashMap<>();
		}
		
		if (!Map.class.isAssignableFrom(cls)) {
			return null;
		}
		
		try {
			Map<?, ?> m = Map.class.cast(cls.getConstructor().newInstance());
			@SuppressWarnings("unchecked")
			Map<Object, Object> mo = (Map<Object, Object>) m;
			return mo;
		} catch (NoSuchMethodException | SecurityException
				| InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| ClassCastException e) {
			return null;
		}
	}
}
