package edu.cmu.cs.able.eseb.rpc;

import incubator.pval.Ensure;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Class representing an asynchronous execution of a remote object.
 */
public class RemoteExecution implements Future<RemoteExecutionResult> {
	/**
	 * The execution result, <code>null</code> if not done yet.
	 */
	private RemoteExecutionResult m_result;
	
	/**
	 * Creates a remote execution which is started but not finished.
	 */
	public RemoteExecution() {
		m_result = null;
	}
	
	/**
	 * Finishes the remote execution with the given result.
	 * @param r the result
	 */
	synchronized void done(RemoteExecutionResult r) {
		Ensure.not_null(r);
		Ensure.is_null(m_result);
		m_result = r;
		notifyAll();
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public synchronized boolean isDone() {
		return (m_result != null);
	}

	@Override
	public synchronized RemoteExecutionResult get()
			throws InterruptedException, ExecutionException {
		while (m_result == null) {
			wait();
		}
		
		return m_result;
	}

	@Override
	public synchronized RemoteExecutionResult get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		Ensure.greater_equal(timeout, 0);
		Ensure.not_null(unit);
		
		if (timeout == 0) {
			return get();
		}
		
		long millis = TimeUnit.MILLISECONDS.convert(timeout, unit);
		long now = System.currentTimeMillis();
		long end = now + millis;
		
		while (m_result == null
				&& ((now = System.currentTimeMillis()) < end)) {
			wait(end - now);
		}
		
		if (m_result == null) {
			throw new TimeoutException();
		}
		
		return m_result;
	}
}
