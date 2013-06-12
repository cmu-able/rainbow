package incubator.dispatch;

import incubator.pval.Ensure;

import java.util.ArrayList;
import java.util.List;

/**
 * Local dispatcher that informs listeners of events.
 * @param <L> the interface implemented by the listeners
 */
public class LocalDispatcher<L> implements Dispatcher<L> {
	/**
	 * The global dispatcher.
	 */
	private GlobalDispatcher m_global;
	
	/**
	 * The listeners currently registered.
	 */
	private List<L> m_listeners;
	
	/**
	 * Creates a new local dispatcher.
	 */
	public LocalDispatcher() {
		m_global = GlobalDispatcher.instance();
		m_listeners = new ArrayList<>();
	}
	
	@Override
	public synchronized void add(L listener) {
		Ensure.notNull(listener);
		m_listeners.add(listener);
	}
	
	@Override
	public synchronized void remove(L listener) {
		Ensure.notNull(listener);
		Ensure.stateCondition(m_listeners.remove(listener) == true);
	}
	
	/**
	 * Informs all listeners of some event.
	 * @param op the event dispatching operation
	 */
	public synchronized void dispatch(DispatcherOp<L> op) {
		Ensure.notNull(op);
		
		if (m_listeners.size() == 0) {
			return;
		}
		
		List<L> listeners_copy = new ArrayList<>(m_listeners);
		m_global.dispatch(new QueuedDispatch<>(listeners_copy, op));
	}
	
	/**
	 * Dispatches a runnable.
	 * @param r the runnable to dispatch
	 */
	public synchronized void dispatch(Runnable r) {
		Ensure.notNull(r);
		m_global.dispatch(r);
	}
}
