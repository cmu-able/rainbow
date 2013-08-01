package incubator.scb;

import incubator.dispatch.Dispatcher;
import incubator.dispatch.DispatcherOp;
import incubator.dispatch.LocalDispatcher;
import incubator.obscol.ObservableSet;
import incubator.obscol.ObservableSetListener;
import incubator.obscol.WrapperObservableSet;
import incubator.pval.Ensure;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of a SCB container.
 * @param <T> the type of SCB
 */
public class ScbContainerImpl<T extends Scb<T>> implements ScbContainer<T> {
	/**
	 * The children.
	 */
	private ObservableSet<T> m_children;
	
	/**
	 * Copy of the children set in case it is cleared: we need to inform of
	 * all removals one by one.
	 */
	private Set<T> m_copy;
	
	/**
	 * The dispatcher.
	 */
	private LocalDispatcher<ScbContainerListener<T>> m_dispatcher;
	
	/**
	 * Update listener that will be registered in SCBs.
	 */
	private ScbUpdateListener<T> m_update_listener;
	
	/**
	 * Creates a new container.
	 */
	public ScbContainerImpl() {
		m_children = new WrapperObservableSet<>(new HashSet<T>());
		m_dispatcher = new LocalDispatcher<>();
		m_copy = new HashSet<>();
		m_update_listener = new ScbUpdateListener<T>() {
			@Override
			public void updated(T t) {
				ScbContainerImpl.this.updated(t);
			}
		};
		
		m_children.addObservableSetListener(
			new ObservableSetListener<T>() {
				@Override
				public void elementAdded(final T e) {
					synchronized (ScbContainerImpl.this) {
						m_copy.add(e);
						e.dispatcher().add(m_update_listener);
						m_dispatcher.dispatch(
								new DispatcherOp<ScbContainerListener<T>>() {
							@Override
							public void dispatch(ScbContainerListener<T> l) {
								l.scb_added(e);
							}
						});
					}
				}

				@Override
				public void elementRemoved(final T e) {
					synchronized (ScbContainerImpl.this) {
						m_copy.remove(e);
						e.dispatcher().remove(m_update_listener);
						m_dispatcher.dispatch(
								new DispatcherOp<ScbContainerListener<T>>() {
							@Override
							public void dispatch(ScbContainerListener<T> l) {
								l.scb_removed(e);
							}
						});
					}
				}

				@Override
				public void setCleared() {
					synchronized (ScbContainerImpl.this) {
						final Set<T> cp = new HashSet<>(m_copy);
						m_copy.clear();
						for (final T t : cp) {
							t.dispatcher().remove(m_update_listener);
							m_dispatcher.dispatch(
									new DispatcherOp<
									ScbContainerListener<T>>() {
								@Override
								public void dispatch(
										ScbContainerListener<T> l) {
									l.scb_removed(t);
								}
							});
						}
					}
				}
		});
	}
	
	@Override
	public Dispatcher<ScbContainerListener<T>> dispatcher() {
		return m_dispatcher;
	}

	@Override
	public synchronized Collection<T> all_scbs() {
		return new HashSet<>(m_children);
	}
	
	/**
	 * Obtains a reference to the set with the children. This set can be
	 * modified freely and will change the internal set of children.
	 * @return the set of children
	 */
	protected synchronized ObservableSet<T> inner_set() {
		return m_children;
	}
	
	/**
	 * Invoked to inform an object has been updated.
	 * @param t the object
	 */
	private synchronized void updated(final T t) {
		Ensure.notNull(t);
		Ensure.isTrue(m_children.contains(t));
		m_dispatcher.dispatch(new DispatcherOp<ScbContainerListener<T>>() {
			@Override
			public void dispatch(ScbContainerListener<T> l) {
				l.scb_updated(t);
			}
		});
	}
}