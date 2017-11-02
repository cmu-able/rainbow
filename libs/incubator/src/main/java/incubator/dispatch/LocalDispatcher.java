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
		Ensure.not_null(listener, "listener == null");
		m_listeners.add(listener);
	}
	
	@Override
	public synchronized void remove(L listener) {
		Ensure.not_null(listener, "listener == null");
		Ensure.is_true(m_listeners.remove(listener), "Listener not known");
	}
	
	/**
	 * Informs all listeners of some event.
	 * @param op the event dispatching operation
	 */
	public synchronized void dispatch(DispatcherOp<L> op) {
		Ensure.not_null(op, "op == null");
		
		if (m_listeners.size() == 0) {
			return;
		}
		
		List<L> listeners_copy = new ArrayList<>(m_listeners);
		RuntimeException stack_marker = new RuntimeException("Dispatch was "
				+ "invoked here.");
		m_global.dispatch(new QueuedDispatch<>(listeners_copy, op,
				stack_marker), stack_marker);
	}
	
	/**
	 * Dispatches a runnable.
	 * @param r the runnable to dispatch
	 */
	public synchronized void dispatch(Runnable r) {
		Ensure.not_null(r, "r == null");
		RuntimeException stack_marker = new RuntimeException("Dispatch was "
				+ "invoked here.");
		m_global.dispatch(r, stack_marker);
	}
}
