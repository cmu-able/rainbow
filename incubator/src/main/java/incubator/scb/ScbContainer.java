package incubator.scb;

import java.util.Collection;

/**
 * An interface implemented by objects which can contain SCBs.
 * @param <T> the bean type
 */
public interface ScbContainer<T> {
	/**
	 * Adds a listener to the container.
	 * @param listener the listener to add
	 */
	void add_listener(ScbContainerListener<T> listener);
	
	/**
	 * Removes a previously added listener from the container. 
	 * @param listener the listener to remove
	 */
	void remove_listener(ScbContainerListener<T> listener);
	
	/**
	 * Obtains all SCBs in the container.
	 * @return all SCBs
	 */
	Collection<T> all_scbs();
}
