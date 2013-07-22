package incubator.scb;

import incubator.dispatch.Dispatcher;

import java.util.Collection;

/**
 * An interface implemented by objects which can contain SCBs.
 * @param <T> the bean type
 */
public interface ScbContainer<T extends Scb<T>> {
	/**
	 * Obtains the event dispatcher where we can register to be notified of
	 * events.
	 * @return the dispatcher
	 */
	Dispatcher<ScbContainerListener<T>> dispatcher();
	
	/**
	 * Obtains all SCBs in the container.
	 * @return all SCBs
	 */
	Collection<T> all_scbs();
}
