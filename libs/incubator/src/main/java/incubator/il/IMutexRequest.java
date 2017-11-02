package incubator.il;

import java.util.Date;

/**
 * Request for a mutex.
 */
public interface IMutexRequest {
	/**
	 * Obtains the request timestamp.
	 * @return the timestamp
	 */
	Date request_time ();
	
	/**
	 * Obtains the wait time.
	 * @return the wait time; if the mutex has not yet been acquired, returns
	 * the current waiting time
	 */
	long wait_time ();
	
	/**
	 * Obtains the mutex acquisition mutex.
	 * @return the stack trace
	 */
	String acquisition_trace ();
	
	/**
	 * Obtains the name of the thread that acquired the mutex.
	 * @return the thread name
	 */
	String acquisition_thread ();
}
