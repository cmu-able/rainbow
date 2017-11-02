package incubator.dispatch;

import incubator.pval.Ensure;

import java.util.List;

/**
 * Class representing a queued dispatch request.
	 * @param <L> the type of listener
 */
class QueuedDispatch<L> implements Runnable {
	/**
	 * Listeners to be informed.
	 */
	private List<L> m_listeners;
	
	/**
	 * The dispatch operation.
	 */
	private DispatcherOp<L> m_dispatch;
	
	/**
	 * Marker with stack trace when dispatch was originated.
	 */
	private RuntimeException m_dispatch_marker;
	
	/**
	 * Creates the dispatch request.
	 * @param listeners the listeners that will be informed of the request
	 * @param op the dispatch operation
	 * @param dispatch_marker the dispatch marker
	 */
	QueuedDispatch(List<L> listeners, DispatcherOp<L> op,
			RuntimeException dispatch_marker) {
		Ensure.not_null(listeners, "listeners == null");
		Ensure.not_null(op, "op == null");
		Ensure.not_null(dispatch_marker, "dispatch_marker == null");
		
		m_listeners = listeners;
		m_dispatch = op;
		m_dispatch_marker = dispatch_marker;
	}
	
	@Override
	public void run() {
		for (L l : m_listeners) {
			try {
				m_dispatch.dispatch(l);
			} catch (Throwable t) {
				t.addSuppressed(m_dispatch_marker);
				GlobalDispatcher.instance().handle_exception(this, l, t);
			}
		}
	}
}
