package incubator.dispatch;

/**
 * Dispatcher that informs listeners of events. It can be used to register and
 * unregister listener.
 * @param <L> the interface implemented by the listeners
 */
public interface Dispatcher<L> {
	/**
	 * Adds a new listener to the dispatcher.
	 * @param listener the listener
	 */
	public abstract void add(L listener);

	/**
	 * Removes a previously registered listener from the dispatcher.
	 * @param listener the listener to remove
	 */
	public abstract void remove(L listener);
}