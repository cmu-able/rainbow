package incubator.qxt;

import java.util.LinkedList;
import java.util.List;

import incubator.obscol.ObservableList;
import incubator.obscol.ObservableListListener;

/**
 * List synchronizer that can be suspended. This is used to prevent changes
 * to the underlying qxt model while a cell is being edited. This
 * synchronizer keeps two lists in sync (copyind data from a source to a
 * sink) but can be suspended. When suspended, all changes are recorded and
 * flushed to the sink as soon as it is re-enabled.
 * 
 * @param <T> the list object type
 */
class SuspendableListSynchronizer<T> {
	/**
	 * The sink.
	 */
	private final ObservableList<T> sink;

	/**
	 * Are we suspended?
	 */
	private boolean suspended;

	/**
	 * Queue with update commands.
	 */
	private final List<Runnable> queue;

	/**
	 * Creates a new synchronzier.
	 * 
	 * @param source the source list
	 * @param sink the sink list
	 */
	SuspendableListSynchronizer(ObservableList<T> source,
			ObservableList<T> sink) {
		assert source != null;
		assert sink != null;

		this.sink = sink;
		suspended = false;
		queue = new LinkedList<>();

		sink.addAll(source);

		source.addObservableListListener(new ObservableListListener<T>() {
			@Override
			public void elementAdded(final T e, final int idx) {
				queue.add(new Runnable() {
					@Override
					public void run() {
						SuspendableListSynchronizer.this.sink.add(idx, e);
					}
				});

				processQueueIfNotSuspended();
			}

			@Override
			public void elementChanged(T oldE, final T newE, final int idx) {
				queue.add(new Runnable() {
					@Override
					public void run() {
						SuspendableListSynchronizer.this.sink.set(idx, newE);
					}
				});

				processQueueIfNotSuspended();
			}

			@Override
			public void elementRemoved(T e, final int idx) {
				queue.add(new Runnable() {
					@Override
					public void run() {
						SuspendableListSynchronizer.this.sink.remove(idx);
					}
				});

				processQueueIfNotSuspended();
			}

			@Override
			public void listCleared() {
				queue.add(new Runnable() {
					@Override
					public void run() {
						SuspendableListSynchronizer.this.sink.clear();
					}
				});

				processQueueIfNotSuspended();
			}
		});
	}

	/**
	 * Processes all pending items if the synchronizer is not currently
	 * suspended.
	 */
	private void processQueueIfNotSuspended() {
		if (suspended) {
			return;
		}

		processQueue();
	}

	/**
	 * Processes all pending items.
	 */
	private void processQueue() {
		for (Runnable r : queue) {
			r.run();
		}

		queue.clear();
	}

	/**
	 * Obtains information on whether the synchronizer is suspended or not.
	 * 
	 * @return is the synchronizer suspended?
	 */
	boolean isSuspended() {
		return suspended;
	}

	/**
	 * Suspends or re-enables the synchronizer.
	 * 
	 * @param suspended should be suspended?
	 */
	void setSuspended(boolean suspended) {
		this.suspended = suspended;

		if (!suspended) {
			processQueue();
		}
	}
}
