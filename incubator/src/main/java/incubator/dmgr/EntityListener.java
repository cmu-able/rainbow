package incubator.dmgr;

/**
 * The interface that all object must implement to be listeners of
 * the DataManager. The listeners that implement this interface 
 * receive the notification of updates (added, removed and changed).
 */
public interface EntityListener {
	/**
	 * This method is called when an object was changed.
	 * 
	 * @param changes changes performed on the object (old and new)
	 */
	void propertiesChanged(BeanPropertyChange changes);
	/**
	 * This method is called when an object was added.
	 * 
	 * @param object that was added
	 */
	void objectAdded(Object object);
	/**
	 * This method is called when an object was removed.
	 * 
	 * @param object that was removed
	 */
	void objectRemoved(Object object);
}
