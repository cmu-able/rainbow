package incubator.scb.filter;

import incubator.Pair;
import incubator.SetSynchronizer;
import incubator.dispatch.Dispatcher;
import incubator.pval.Ensure;
import incubator.scb.Scb;
import incubator.scb.ScbContainer;
import incubator.scb.ScbContainerListener;
import incubator.scb.ScbEditableContainerImpl;

import java.util.HashSet;
import java.util.Set;

/**
 * Container that keeps a subset of data from another container which passes
 * a filter.
 * @param <T> the type of SCB
 */
public class ScbFilterContainer<T extends Scb<T>> implements ScbContainer<T> {
	/**
	 * The container implementation.
	 */
	private ScbEditableContainerImpl<T> m_impl;
	
	/**
	 * The container to use.
	 */
	private ScbContainer<T> m_container;
	
	/**
	 * The applied filter.
	 */
	private ScbFilter<T> m_filter;
	
	/**
	 * Creates a new filter container.
	 * @param container the container where data comes from
	 * @param filter the filter to use
	 */
	public ScbFilterContainer(ScbContainer<T> container, ScbFilter<T> filter) {
		Ensure.not_null(container);
		Ensure.not_null(filter);
		
		m_container = container;
		m_filter = filter;
		m_impl = new ScbEditableContainerImpl<T>();
		
		synchronized (m_container) {
			resync();
			
			m_container.dispatcher().add(new ScbContainerListener<T>() {
				@Override
				public void scb_added(T t) {
					synchronized (ScbFilterContainer.this) {
						if (m_filter.accepts(t)) {
							m_impl.add_scb(t);
						}
					}
				}

				@Override
				public void scb_removed(T t) {
					synchronized (ScbFilterContainer.this) {
						if (m_filter.accepts(t)) {
							m_impl.remove_scb(t);
						}
					}
				}

				@Override
				public void scb_updated(T t) {
					synchronized (ScbFilterContainer.this) {
						boolean contains = m_impl.all_scbs().contains(t);
						boolean accepts = m_filter.accepts(t);
						
						if (!contains && accepts) {
							m_impl.add_scb(t);
						} else if (contains && !accepts) {
							m_impl.remove_scb(t);
						}
					}
				}
			});
		}
	}
	
	/**
	 * Sets the filter.
	 * @param filter the filter
	 */
	public synchronized void filter(ScbFilter<T> filter) {
		Ensure.not_null(filter);
		m_filter = filter;
		
		resync();
	}

	@Override
	public Dispatcher<ScbContainerListener<T>> dispatcher() {
		return m_impl.dispatcher();
	}

	@Override
	public Set<T> all_scbs() {
		return m_impl.all_scbs();
	}
	
	/**
	 * Rebuilds the list of SCBs that are accepted by the filter.
	 */
	private synchronized void resync() {
		Set<T> all = new HashSet<>();
		
		synchronized (m_container) {
			for (T t : m_container.all_scbs()) {
				if (m_filter.accepts(t)) {
					all.add(t);
				}
			}
		}
		
		Pair<Set<T>, Set<T>> sn = SetSynchronizer.synchronization_changes(
				m_impl.all_scbs(), all);
		for (T t : sn.first()) {
			m_impl.add_scb(t);
		}
		
		for (T t : sn.second()) {
			m_impl.remove_scb(t);
		}
	}
}
