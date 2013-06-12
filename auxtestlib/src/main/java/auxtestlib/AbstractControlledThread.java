package auxtestlib;

/**
 * Class implementing a thread with a <code>myRun</code> method which can throw
 * exceptions. The thread will keep any exception thrown and provides some
 * additional features.
 */
public abstract class AbstractControlledThread extends Thread {
	/**
	 * Thrown exception (if any).
	 */
	private Exception deathException;

	/**
	 * Is the thread running?
	 */
	private boolean running;

	/**
	 * Has the thread been started?
	 */
	private boolean started;

	/**
	 * The thread's return value.
	 */
	private Object returnValue;

	/**
	 * Creates a new thread.
	 */
	public AbstractControlledThread() {
		running = false;
		started = false;
		deathException = null;
	}

	/**
	 * <code>run</code> method: just wraps <code>myRun</code>.
	 */
	@Override
	public final void run() {
		synchronized (this) {
			started = true;
			running = true;
		}

		try {
			returnValue = myRun();
		} catch (Exception e) {
			deathException = e;
		}

		doDie();
	}

	/**
	 * Method that will perform the thread's work.
	 * 
	 * @return Object any return value that may be useful
	 * 
	 * @throws Exception execution failed
	 */
	public abstract Object myRun() throws Exception;

	/**
	 * The thread has died. We'll update the current status and awake all
	 * threads waiting for the notification.
	 */
	private synchronized void doDie() {
		running = false;
		notifyAll();
	}

	/**
	 * Obtains the throwable that killed the thread.
	 * 
	 * @return the throwable or <code>null</code> if none
	 */
	public Throwable getDeathException() {
		return deathException;
	}

	/**
	 * Obtains the execution result (the value returned by <code>myRun</code>)
	 * 
	 * @return the execution result or <code>null</code> if the thread failed
	 * with an exception
	 */
	public Object getResult() {
		return returnValue;
	}

	/**
	 * Waits for the thread to die (or for an interrupt). If the thread hasn't
	 * yet started, it will wait for the thread to start and then for it to die.
	 */
	public void waitForEnd() {
		synchronized (this) {
			while (running || !started) {
				try {
					wait();
				} catch (InterruptedException e) {
					return;
				}
			}
		}
		
		/*
		 * Wait for final die.
		 */
		while (isAlive()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				/*
				 * Ignored.
				 */
			}
		}
	}

	/**
	 * Simulates a thread "invocation" starting the thread, if it has not yet
	 * started and waiting for the thread to die, returning the thread's return
	 * value. If the thread dies with an exception, the exception is thrown by
	 * this method too.
	 * 
	 * @return the thread's return value
	 * 
	 * @throws Exception the exception thrown by the thread
	 */
	public Object invoke() throws Exception {
		/*
		 * We don't know whether the thread has already started or not (the
		 * thread may be running already but the 'run' method has not yet
		 * started). So we'll try this 3 times before raising an error.
		 */
		for (int i = 0; i < 3; i++) {
			try {
				synchronized (this) {
					if (!started) {
						start();
					}

					break;
				}
			} catch (IllegalThreadStateException e) {
				Thread.sleep(50);
				// Ignored: thread is already starting.
			}
		}

		waitForEnd();

		if (deathException != null) {
			throw deathException;
		}

		return returnValue;
	}

	/**
	 * Starts all threads in an array.
	 * 
	 * @param threads the array of threads to start
	 */
	public static void startAll(AbstractControlledThread threads[]) {
		for (int i = 0; i < threads.length; i++) {
			threads[i].start();
		}
	}

	/**
	 * Calls the {@link #invoke()} method on all threads in the array. If some
	 * of the methods fails with an exception, the exception is thrown and the
	 * {@link #invoke()} method is not called on subsequent threads.
	 * 
	 * @param threads the thread array
	 * 
	 * @throws Exception at least one threads failed. This is the exception
	 * thrown by the thread
	 */
	public static void invokeAll(AbstractControlledThread threads[])
			throws Exception {
		for (int i = 0; i < threads.length; i++) {
			threads[i].invoke();
		}
	}
}
