package incubator.bpref;

import java.util.prefs.Preferences;

/**
 * Abstract property handler that handles all values as strings. Subclasses
 * must only provide a conversion to and from string.
 *
 * @param <T> the data type
 */
abstract class AsStringPropertyHandler<T> implements PropertyHandler<T> {
	/**
	 * The data type.
	 */
	private Class<T> type;
	
	/**
	 * Creates a new property handler.
	 * 
	 * @param clazz the property type
	 */
	AsStringPropertyHandler(Class<T> clazz) {
		this.type = clazz;
	}
	
	@Override
	public T read(Preferences prefs, String key) throws Exception {
		String v = prefs.get(key, null);
		if (v == null) {
			return null;
		}
		
		return convertFromString(v);
	}

	@Override
	public void save(Preferences prefs, String key, Object t) throws Exception {
		if (t == null) {
			prefs.remove(key);
		} else {
			prefs.put(key, convertToString(type.cast(t)));
		}
	}

	/**
	 * Converts a string to the handler type.
	 * 
	 * @param s the string (never <code>null</code>)
	 * 
	 * @return the converted value
	 * 
	 * @throws Exception conversion failed
	 */
	protected abstract T convertFromString(String s) throws Exception;
	
	/**
	 * Converts a value to a string.
	 * 
	 * @param t the value to convert (never <code>null</code>)
	 * 
	 * @return the string value
	 * 
	 * @throws Exception conversion failed
	 */
	protected abstract String convertToString(T t) throws Exception;
}
