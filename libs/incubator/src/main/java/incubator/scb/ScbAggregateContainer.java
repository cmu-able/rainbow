package incubator.scb;

import incubator.dispatch.Dispatcher;
import incubator.dispatch.DispatcherOp;
import incubator.dispatch.LocalDispatcher;
import incubator.pval.Ensure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
		Ensure.not_null(container, "container == null");
		Ensure.is_false(m_listeners.containsKey(container),
				"m_listeners.containsKey(container)");
		
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
		
		container.dispatcher().add(m_listeners.get(container));
		
		for (T t : container.all_scbs()) {
			added(container, t);
		}
	}
	
	/**
	 * Removes a container from the aggregate.
	 * @param container the container
	 */
	public synchronized void remove_container(ScbContainer<T> container) {
		Ensure.not_null(container, "container == null");
		Ensure.is_true(m_listeners.containsKey(container),
				"!m_listeners.containsKey(container)");
		
		container.dispatcher().remove(m_listeners.get(container));
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
	public Dispatcher<ScbContainerListener<T>> dispatcher() {
		return m_dispatcher;
	}

	@Override
	public Set<T> all_scbs() {
		return new HashSet<>(m_scbs.keySet());
	}
}
