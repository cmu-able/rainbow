package incubator.ui;

import incubator.dispatch.Dispatcher;

/**
 * Task that can report progress.
 */
public interface ProgressTask {
	/**
	 * Obtains the dispatcher to register listeners to make progress.
	 * @return the dispatcher
	 */
	Dispatcher<ProgressListener> progress_disptacher ();
	
	/**
	 * Executes the task.
	 * @throws Exception failed to execute the task
	 */
	void execute () throws Exception;
	
	/**
	 * Obtains the task state.
	 * @return the task state
	 */
	ProgressTaskState state ();
}
