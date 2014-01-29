package incubator.scb;

import incubator.ListSet;
import incubator.dispatch.Dispatcher;
import incubator.dispatch.DispatcherOp;
import incubator.dispatch.LocalDispatcher;
import incubator.obscol.ObservableSet;
import incubator.obscol.ObservableSetListener;
import incubator.obscol.WrapperObservableSet;
import incubator.pval.Ensure;

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
	private ListSet<T> m_copy;
	
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
		m_children = new WrapperObservableSet<>(new ListSet<T>());
		m_dispatcher = new LocalDispatcher<>();
		m_copy = new ListSet<>();
		m_update_listener = new ScbUpdateListener<T>() {
			@Override
			public void updated(T t) {
				ScbContainerImpl.this.updated(t);
			}
		};
		
		m_children.addObservableSetListener(
			new ObservableSetListener<T>() {
				@Override
				public void elementAdded(T e) {
					m_dispatcher.dispatch(new Runnable() {
						@Override
						public void run() {
							sync();
						}
					});
				}

				@Override
				public void elementRemoved(final T e) {
					m_dispatcher.dispatch(new Runnable() {
						@Override
						public void run() {
							sync();
						}
					});
				}

				@Override
				public void setCleared() {
					m_dispatcher.dispatch(new Runnable() {
						@Override
						public void run() {
							sync();
						}
					});
				}
		});
	}
	
	@Override
	public Dispatcher<ScbContainerListener<T>> dispatcher() {
		return m_dispatcher;
	}

	@Override
	public synchronized Set<T> all_scbs() {
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
		Ensure.not_null(t, "t == null");
		Ensure.is_true(m_children.contains(t), "!m_children.contains(t)");
		m_dispatcher.dispatch(new DispatcherOp<ScbContainerListener<T>>() {
			@Override
			public void dispatch(ScbContainerListener<T> l) {
				l.scb_updated(t);
			}
		});
	}
	
	/**
	 * Synchronizes the list of known children with the set.
	 */
	private synchronized void sync() {
		for (T t : m_copy) {
			t.dispatcher().remove(m_update_listener);
			t.dispatcher().add(m_update_listener);
		}
		
		ListSet<T> bkp = new ListSet<>(m_copy);
		ListSet<T> new_copy = new ListSet<>(m_children);
		Set<T> new_t = new ListSet<>(new_copy);
		new_t.removeAll(m_copy);
		Set<T> del_t = new ListSet<>(m_copy);
		del_t.removeAll(new_copy);
		m_copy = new_copy;
		
		for (final T t : new_t) {
			t.dispatcher().add(m_update_listener);
			m_dispatcher.dispatch(new DispatcherOp<ScbContainerListener<T>>() {
				@Override
				public void dispatch(ScbContainerListener<T> l) {
					l.scb_added(t);
				}
			});
		}
		
		for (final T t : del_t) {
			t.dispatcher().remove(m_update_listener);
			m_dispatcher.dispatch(new DispatcherOp<ScbContainerListener<T>>() {
				@Override
				public void dispatch(ScbContainerListener<T> l) {
					l.scb_removed(t);
				}
			});
		}
		
		for (T t : m_copy) {
			try {
				t.dispatcher().remove(m_update_listener);
			} catch (AssertionError e) {
				e.printStackTrace();
			}
			t.dispatcher().add(m_update_listener);
		}
	}
}
