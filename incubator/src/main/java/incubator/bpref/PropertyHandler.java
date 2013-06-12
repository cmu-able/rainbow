package incubator.bpref;

import java.util.prefs.Preferences;

/**
 * Interface allowing access to a reading and writing a property in the
 * preferences. This is a generic interface. Specific implementations should
 * be able to read and write specific data types.
 *
 * @param <T> the type of data to read/write
 */
interface PropertyHandler<T> {
	/**
	 * Saves a property in the preferences.
	 * 
	 * @param prefs the preferences node
	 * @param key the key to save the data with
	 * @param t the value to save (can be <code>null</code>)
	 * 
	 * @throws Exception failed to save
	 */
	void save(Preferences prefs, String key, Object t) throws Exception;
	
	/**
	 * Reads a property from a preference node.
	 * 
	 * @param prefs the preferences node
	 * @param key the key to read the data from
	 * 
	 * @return the data read (<code>null</code> if none)
	 * 
	 * @throws Exception failed to read or convert the data type
	 */
	T read(Preferences prefs, String key) throws Exception;
}
