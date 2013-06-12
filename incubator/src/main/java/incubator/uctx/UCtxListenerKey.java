package incubator.uctx;

/**
 * Class used to identify a dependency of a listener on a specific context key.
 * The listener will be informed if there are changes under the key. The type of
 * changes are defined in {@link ListeningType}. A specific type (class) may be
 * defined in which case the context key's value is considered not defined (
 * <code>null</code>) if the value registered with the type is not an instance
 * of the type.
 */
public class UCtxListenerKey {
	/**
	 * The context key.
	 */
	private String key;

	/**
	 * The key object type.
	 */
	private Class<?> type;

	/**
	 * The listening type.
	 */
	private ListeningType listeningType;

	/**
	 * Creates a new listening key.
	 * 
	 * @param key the key to listen to
	 * @param type an optional type constraining visible key values
	 * @param listeningType the type of listening
	 */
	public UCtxListenerKey(String key, Class<?> type,
			ListeningType listeningType) {
		if (key == null) {
			throw new IllegalArgumentException("key == null");
		}
		
		if (listeningType == null) {
			throw new IllegalArgumentException("listeningType == null");
		}
		
		this.key = key;
		this.type = type;
		this.listeningType = listeningType;
	}
	
	/**
	 * Obtains the key to listen to.
	 * 
	 * @return the key to listen to
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * Obtains the type of the value.
	 * 
	 * @return the type or <code>null</code> if unconstrained
	 */
	public Class<?> getType() {
		return type;
	}
	
	/**
	 * Obtains the listening type on the key.
	 * 
	 * @return the listening type
	 */
	public ListeningType getListeningType() {
		return listeningType;
	}
}
