package incubator.dispatch;

import java.util.List;

/**
 * Class representing a queued dispatch request.
	 * @param <L> the type of listener
 */
class QueuedDispatch<L>  implements Runnable {
	/**
	 * Listeners to be informed.
	 */
	private List<L> m_listeners;
	
	/**
	 * The dispatch operation.
	 */
	private DispatcherOp<L> m_dispatch;
	
	/**
	 * Creates the dispatch request.
	 * @param listeners the listeners that will be informed of the request
	 * @param op the dispatch operation
	 */
	QueuedDispatch(List<L> listeners, DispatcherOp<L> op) {
		m_listeners = listeners;
		m_dispatch = op;
	}
	
	@Override
	public void run() {
		for (L l : m_listeners) {
			try {
				m_dispatch.dispatch(l);
			} catch (Throwable t) {
				GlobalDispatcher.instance().handle_exception(this, l, t);
			}
		}
	}
}
