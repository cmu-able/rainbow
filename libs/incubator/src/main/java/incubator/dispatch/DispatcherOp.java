package incubator.dispatch;

/**
 * Dispatch operation.
 * @param <L> the type of listener to dispatch
 */
public interface DispatcherOp<L> {
	/**
	 * Performs the dispatching operation on the listener. This generally
	 * means invoking the listener's event method
	 * @param l the listener
	 */
	void dispatch(L l);
}
