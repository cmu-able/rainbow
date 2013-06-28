package incubator.scb;

import incubator.dispatch.DispatcherOp;
import incubator.dispatch.LocalDispatcher;
import incubator.pval.Ensure;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * SCB container that aggregates the SCBs from several containers.
 * @param <T> the bean type
 */
public class ScbAggregateContainer<T extends Scb<T>>
		implements ScbContainer<T> {
	/**
	 * Maps all SCBs to the number of containers that contain them.
	 */
	private Map<T, Integer> m_scbs;
	
	/**
	 * Listeners used to listen to changes in the containers.
	 */
	private Map<ScbContainer<T>, ScbContainerListener<T>> m_listeners;
	
	/**
	 * The dispatcher.
	 */
	private LocalDispatcher<ScbContainerListener<T>> m_dispatcher;
	
	/**
	 * Creates a new container.
	 */
	public ScbAggregateContainer() {
		m_scbs = new HashMap<>();
		m_listeners = new HashMap<>();
		m_dispatcher = new LocalDispatcher<>();
	}
	
	/**
	 * Adds a new container to the aggregate.
	 * @param container the container
	 */
	public synchronized void add_container(final ScbContainer<T> container) {
		Ensure.notNull(container);
		Ensure.isTrue(!m_listeners.containsKey(container));
		
		m_listeners.put(container, new ScbContainerListener<T>() {
			@Override
			public void scb_added(T t) {
				added(container, t);
			}

			@Override
			public void scb_removed(T t) {
				removed(container, t);
			}

			@Override
			public void scb_updated(T t) {
				updated(container, t);
			}
		});
		
		for (T t : container.all_scbs()) {
			added(container, t);
		}
		
		container.add_listener(m_listeners.get(container));
		
		for (T t : container.all_scbs()) {
			added(container, t);
		}
	}
	
	/**
	 * Removes a container from the aggregate.
	 * @param container the container
	 */
	public synchronized void remove_container(ScbContainer<T> container) {
		Ensure.notNull(container);
		Ensure.isTrue(m_listeners.containsKey(container));
		
		container.remove_listener(m_listeners.get(container));
		m_listeners.remove(container);
		
		for (T t : container.all_scbs()) {
			removed(container, t);
		}
	}
	
	/**
	 * Invoked when an SCB has been added to a container. This is also used
	 * when adding a container.
	 * @param container the container
	 * @param t the SCB
	 */
	private void added(ScbContainer<T> container, final T t) {
		Integer cnt = m_scbs.get(t);
		if (cnt == null) {
			m_scbs.put(t, 1);
		} else {
			m_scbs.put(t, cnt + 1);
		}
		
		m_dispatcher.dispatch(new DispatcherOp<ScbContainerListener<T>>() {
			@Override
			public void dispatch(ScbContainerListener<T> l) {
				l.scb_added(t);
			}
		});
	}
	
	/**
	 * Invoked when an SCB has been removed from a container. This is also
	 * used when removing a container.
	 * @param container the container
	 * @param t the SCB
	 */
	private void removed(ScbContainer<T> container, final T t) {
		Integer cnt = m_scbs.get(t);
		if (cnt == 1) {
			m_scbs.remove(t);
		} else {
			m_scbs.put(t, cnt - 1);
		}
		
		m_dispatcher.dispatch(new DispatcherOp<ScbContainerListener<T>>() {
			@Override
			public void dispatch(ScbContainerListener<T> l) {
				l.scb_removed(t);
			}
		});
	}
	
	/**
	 * Invoked when an SCB has been updated in a container.
	 * @param container the container
	 * @param t the SCB
	 */
	private void updated(ScbContainer<T> container, final T t) {
		m_dispatcher.dispatch(new DispatcherOp<ScbContainerListener<T>>() {
			@Override
			public void dispatch(ScbContainerListener<T> l) {
				l.scb_updated(t);
			}
		});
	}

	@Override
	public void add_listener(ScbContainerListener<T> listener) {
		m_dispatcher.add(listener);
	}

	@Override
	public void remove_listener(ScbContainerListener<T> listener) {
		m_dispatcher.remove(listener);
	}

	@Override
	public Collection<T> all_scbs() {
		return new HashSet<>(m_scbs.keySet());
	}
}
