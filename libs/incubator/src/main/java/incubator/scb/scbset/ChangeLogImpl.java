package incubator.scb.scbset;

import incubator.dispatch.Dispatcher;
import incubator.dispatch.DispatcherOp;
import incubator.dispatch.LocalDispatcher;
import incubator.pval.Ensure;
import incubator.scb.CloneableScb;
import incubator.scb.MergeableIdScb;
import incubator.scb.Scb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of a change log.
 * @param <T> the type of SCB
 */
class ChangeLogImpl<T extends Scb<T> & MergeableIdScb<T> & CloneableScb<T>>
		implements ChangeLog<T> {
	/**
	 * Maps all consumers to their change log entries.
	 */
	private Map<ClConsumer<T>, List<ChangeLogEntry<T>>> m_entries;
	
	/**
	 * Consumers waiting for a checkpoint.
	 */
	private Set<ClConsumer<T>> m_pending_cp;
	
	/**
	 * Event dispatcher.
	 */
	private LocalDispatcher<ChangeLogListener> m_dispatcher;
	
	/**
	 * Impl event dispatcher.
	 */
	private LocalDispatcher<ChangeLogImplListener> m_impl_dispatcher;
	
	/**
	 * Creates a new change log implementation.
	 */
	ChangeLogImpl() {
		m_entries = new HashMap<>();
		m_pending_cp = new HashSet<>();
		m_dispatcher = new LocalDispatcher<>();
		m_impl_dispatcher = new LocalDispatcher<>();
	}
	
	/**
	 * Obtains the change log dispatcher.
	 * @return the dispatcher
	 */
	Dispatcher<ChangeLogImplListener> impl_dispatcher() {
		return m_impl_dispatcher;
	}
	
	@Override
	public Dispatcher<ChangeLogListener> dispatcher() {
		return m_dispatcher;
	}
	
	@Override
	public synchronized void add_consumer(ClConsumer<T> cp) {
		Ensure.not_null(cp, "cp == null");
		Ensure.is_false(m_entries.containsKey(cp), "Consumer already "
				+ "registered.");
		m_pending_cp.add(cp);
		m_entries.put(cp, new LinkedList<ChangeLogEntry<T>>());
		m_impl_dispatcher.dispatch(new DispatcherOp<ChangeLogImplListener>() {
			@Override
			public void dispatch(ChangeLogImplListener l) {
				Ensure.not_null(l, "l == null");
				l.checkpoint_needed();
			}
		});
	}
	
	@Override
	public synchronized void remove_consumer(ClConsumer<T> cp) {
		Ensure.not_null(cp, "cp == null");
		Ensure.is_true(m_entries.containsKey(cp), "Consumer not registered.");
		m_entries.remove(cp);
		m_pending_cp.remove(cp);
	}
	
	@Override
	public synchronized List<ChangeLogEntry<T>> consume(ClConsumer<T> cp) {
		Ensure.not_null(cp, "cp == null");
		Ensure.is_true(m_entries.containsKey(cp), "Consumer not registered.");
		List<ChangeLogEntry<T>> r = new ArrayList<>(m_entries.get(cp));
		if (r.size() > 0) {
			m_entries.put(cp, new LinkedList<ChangeLogEntry<T>>());
		}
		
		return r;
	}
	
	/**
	 * Adds a change to the change log.
	 * @param ce the change log entry
	 */
	synchronized void change(ChangeLogEntry<T> ce) {
		Ensure.not_null(ce, "ce == null");
		if (ce.checkpoint()) {
			for (ClConsumer<T> cl : m_pending_cp) {
				m_entries.get(cl).add(ce);
			}
		} else {
			for (Map.Entry<ClConsumer<T>, List<ChangeLogEntry<T>>> e
					: m_entries.entrySet()) {
				if (!m_pending_cp.contains(e.getKey())) {
					e.getValue().add(ce);
				}
			}
		}
		
		m_dispatcher.dispatch(new DispatcherOp<ChangeLogListener>() {
			@Override
			public void dispatch(ChangeLogListener l) {
				l.changed();
			}
		});
	}
}
