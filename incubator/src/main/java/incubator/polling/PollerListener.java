package incubator.polling;

/**
 * <p>
 * The interface that must be implemented by the objects that want to be
 * listener of the {@link Poller} when a change is detected in the data that
 * is being polled through the {@link PollingDataSource}.
 * </p>
 * 
 * @param <T> the polling object type
 */
public interface PollerListener<T> {
	/**
	 * This method is called when an object was added.
	 * 
	 * @param object that was added
	 * @param idx index of the object that is going to be added
	 */
	void objectAdded(T object, int idx);

	/**
	 * This method is called when an object was removed.
	 * 
	 * @param object that was removed
	 * @param idx index of the object that is going to be removed
	 */
	void objectRemoved(T object, int idx);
}
