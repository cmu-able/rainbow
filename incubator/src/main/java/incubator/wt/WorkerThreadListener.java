package incubator.wt;

/**
 * Interface implemented by listeners that want to be informed of changes
 * in worker threads.
 */
public interface WorkerThreadListener {
	/**
	 * The worker thread state has changed.
	 */
	void state_changed();
}
