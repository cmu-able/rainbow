package incubator.ctxaction;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.ClassUtils;

/**
 * Utility class that can be used to bind fields of objects to context values.
 * Processing is generally done in two stages: preprocessing and processing.
 * The first is optional but may be used to detect some configuration problems.
 * 
 * FIXME: Need to detect (and add test) for non public class.
 */
public class KeyFieldProcessor {
	/**
	 * Cache of field data by class. Maps each processed class to a map of all
	 * fields by their respective key annotations.
	 */
	private static Map<Class<?>, Map<Field, Key>> classCache;
	
	static {
		classCache = new HashMap<>();
	}
	
	/**
	 * Obtains all fields used as keys from the given object.
	 * 
	 * @param obj the object
	 * 
	 * @return all fields used as keys
	 */
	public static Set<Field> getKeyFields(Object obj) {
		if (obj == null) {
			throw new IllegalArgumentException("obj == null");
		}
		
		return new HashSet<>(getKeys(obj).keySet());
	}

	/**
	 * Preprocesses an object. This method has the advantage over
	 * {@link #preprocess(Object)} that an exception is thrown if a
	 * configuration error exists.
	 * 
	 * @param obj the object to process
	 * 
	 * @throws KeyConfigurationException object configuration is invalid
	 */
	public static void preprocess(Object obj)
			throws KeyConfigurationException {
		if (obj == null) {
			throw new IllegalArgumentException("obj == null");
		}
		
		getKeys(obj);
	}
	
	/**
	 * Obtains the mapping between fields and keys for a given object. Returns
	 * the previously cached value if any.
	 * 
	 * @param obj the object to obtain the map
	 * 
	 * @return the map
	 * 
	 * @throws KeyConfigurationException the object configuration is invalid
	 */
	private static synchronized Map<Field,Key> getKeys(Object obj)
			throws KeyConfigurationException {
		assert obj != null;
		
		Class<?> cls = obj.getClass();
		Map<Field, Key> keys = classCache.get(cls);
		if (keys != null) {
			return keys;
		}
		
		keys = new HashMap<>();
		
		/*
		 * Start by following all classes up in the hierarchy.
		 */
		for (; cls != null; cls = cls.getSuperclass()) {
			/*
			 * For each class, and process each one separately.
			 */
			Field[] fields = cls.getDeclaredFields();
			for (Field field : fields) {
				/*
				 * Check we the field has the @Key annotation. If it doesn't,
				 * ignore it. 
				 */
				Key key = field.getAnnotation(Key.class);
				if (key == null) {
					continue;
				}
				
				/*
				 * This must not happen because it should not be possible :)
				 */
				assert key.contextKey() != null;
				
				/*
				 * If we have the Key, we need ensure the field is public.
				 */
				if ((field.getModifiers() & Modifier.PUBLIC) == 0) {
					throw new KeyConfigurationException("Field '"
							+ field.getName() + "' in class '"
							+ cls.getCanonicalName() + "' is not public.");
				}
				
				/*
				 * Save the field in the list.
				 */
				keys.put(field, key);
			}
		}
		
		/*
		 * Cache and return the data.
		 */
		classCache.put(cls, keys);
		return keys;
	}
	
	/**
	 * Processes an object filling all its fields marking with the
	 * {@link Key} annotation with the respective context values. Values which
	 * cannot be coerced (due to incompatible types) are set to
	 * <code>null</code> or the primitive equivalent (<code>0</code> or
	 * <code>false</code>).
	 * 
	 * @param obj the object to process
	 * @param ctx the action context
	 * 
	 * @return a structure with some information on the process
	 */
	public static KeyFieldProcessResults process(Object obj,
			ActionContext ctx) {
		if (obj == null) {
			throw new IllegalArgumentException("obj == null");
		}
		
		if (ctx == null) {
			throw new IllegalArgumentException("ctx == null");
		}
		
		Map<Field, Key> keys = null;
		try {
			keys = getKeys(obj);
		} catch (KeyConfigurationException e) {
			throw new RuntimeException(e);
		}
		
		KeyFieldProcessResults results = new KeyFieldProcessResults();
		
		for (Entry<Field, Key> e : keys.entrySet()) {
			Field f = e.getKey();
			Key k = e.getValue();
			
			String ck = k.contextKey();
			Object v = ctx.get(ck);
			Object current = null;
			try {
				current = f.get(obj);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			
			if (current != v) {
				results.addChanged(f);
				if (v == null) {
					if (!coerceEmpty(f, obj)) {
						results.addPrimitiveNull(f);
					} else {
						results.addSuccessNull(f);
					}
				} else {
					if (!coerce(v, ck, f, obj)) {
						coerceEmpty(f, obj);
						results.addCoersionFailed(f);
					} else {
						results.addSuccessNonNull(f);
					}
				}
			} else {
				if (current == null) {
					results.addSuccessNull(f);
				} else {
					results.addSuccessNonNull(f);
				}
			}
		}
		
		return results;
	}
	
	/**
	 * Tries to coerce a non-null context value into a field.
	 * 
	 * @param value the value to coerce
	 * @param ck the context key
	 * @param f the field whose value should be set
	 * @param obj the object whose field we want to set
	 * 
	 * @return has a value been coerced?
	 */
	private static boolean coerce(Object value, String ck, Field f,
			Object obj) {
		assert value != null;
		assert ck != null;
		assert f != null;
		assert obj != null;
		
		Class<?> type = f.getType();
		/*
		 * Since the field may be a primitive type, we'll convert it to the
		 * wrapper type.
		 */
		type = ClassUtils.primitiveToWrapper(type);
		
		try {
			f.set(obj, value);
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Coerce an empty value into an object field.
	 * 
	 * @param f the field whose value should be set
	 * @param obj the object whose field we want to set
	 * 
	 * @return has a <code>null</code> value been set on the object? (Returns
	 * <code>false</code> if the type is primitive but sets it to <code>0</code>
	 * or <code>false</code>)
	 */
	private static boolean coerceEmpty(Field f, Object obj) {
		assert f != null;
		assert obj != null;
		
		try {
			Class<?> type = f.getType();
			if (!type.isPrimitive()) {
				f.set(obj, null);
				return true;
			}
			
			/*
			 * Primitive numeric types are set to 0 / false.
			 */
			if (type == int.class) {
				f.set(obj, new Integer(0));
			} else if (type == short.class) {
				f.set(obj, new Short((short) 0));
			} else if (type == long.class) {
				f.set(obj, new Long(0));
			} else if (type == char.class) {
				f.set(obj, new Character((char) 0));
			} else if (type == float.class) {
				f.set(obj, new Float(0));
			} else if (type == double.class) {
				f.set(obj, new Double(0));
			} else if (type == boolean.class) {
				f.set(obj, new Boolean(false));
			}
		} catch (Exception e) {
			return false;
		}
		
		return false;
	}
}
