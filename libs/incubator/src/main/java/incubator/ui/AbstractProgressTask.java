package incubator.ui;

import incubator.dispatch.Dispatcher;
import incubator.dispatch.DispatcherOp;
import incubator.dispatch.LocalDispatcher;
import incubator.pval.Ensure;

/**
 * Abstract implementation of a progress task.
 */
public abstract class AbstractProgressTask implements ProgressTask {
	/**
	 * The current task state.
	 */
	private ProgressTaskState m_state;
	
	/**
	 * The progress listener.
	 */
	private LocalDispatcher<ProgressListener> m_dispatcher;
	
	/**
	 * Creates a new task.
	 */
	public AbstractProgressTask() {
		m_dispatcher = new LocalDispatcher<>();
		m_state = ProgressTaskState.NOT_STARTED;
	}
	
	@Override
	public Dispatcher<ProgressListener> progress_disptacher() {
		return m_dispatcher;
	}
	
	/**
	 * Marks the task as undefined.
	 */
	protected synchronized void undefined() {
		Ensure.equals(m_state, ProgressTaskState.RUNNING,
				"m_state != RUNNING");
		m_dispatcher.dispatch(new DispatcherOp<ProgressListener>() {
			@Override
			public void dispatch(ProgressListener l) {
				l.undefined();
			}
		});
	}
	
	/**
	 * Marks the task as defined.
	 * @param current the current work
	 * @param total the total work
	 */
	protected synchronized void defined(final int current, final int total) {
		Ensure.greater_equal(current, 0, "current < 0");
		Ensure.greater_equal(total, current, "total < current");
		Ensure.equals(m_state, ProgressTaskState.RUNNING,
				"m_state != RUNNING");
		
		m_dispatcher.dispatch(new DispatcherOp<ProgressListener>() {
			@Override
			public void dispatch(ProgressListener l) {
				l.defined(current, total);
			}
		});
	}
	
	/**
	 * Defines the current text.
	 * @param text the current text
	 */
	protected synchronized void text(final String text) {
		Ensure.not_null(text, "text == null");
		Ensure.equals(m_state, ProgressTaskState.RUNNING,
				"m_state != RUNNING");
		
		m_dispatcher.dispatch(new DispatcherOp<ProgressListener>() {
			@Override
			public void dispatch(ProgressListener l) {
				l.text(text);
			}
		});
	}
	
	/**
	 * Marks the text as done.
	 */
	protected synchronized void done() {
		Ensure.equals(m_state, ProgressTaskState.RUNNING,
				"m_state != RUNNING");
		m_state = ProgressTaskState.FINISHED;
		
		m_dispatcher.dispatch(new DispatcherOp<ProgressListener>() {
			@Override
			public void dispatch(ProgressListener l) {
				l.done();
			}
		});
	}
	
	@Override
	public ProgressTaskState state() {
		return null;
	}
	
	@Override
	public void execute() throws Exception {
		synchronized (this) {
			Ensure.equals(m_state, ProgressTaskState.NOT_STARTED);
			m_state = ProgressTaskState.RUNNING;
		}
		
		run();
	}
	
	/**
	 * Executes the task.
	 * @throws Exception failed to execute the task
	 */
	protected abstract void run() throws Exception;
}
