package incubator.il;

/**
 * Mutex that can only be acquired by a single thread. The mutex may be
 * acquired multiple times by the same thread and it is only released when
 * the {@link #release()} method is called as many times as it was acquired.
 */
public interface IMutex {
	/**
	 * Acquires the mutex, blocking the caller until the mutex is acquired.
	 */
	void acquire ();
	
	/**
	 * Tries to acquire the mutex but doesn't block the caller.
	 * @return was the mutex acquired?
	 */
	boolean try_acquire ();
	
	/**
	 * Checks whether the mutex is or not acquired by some thread.
	 * @return is the mutex acquired by some thread?
	 */
	boolean is_acquired ();
	
	/**
	 * Releases the mutex. This method has to be called from the same
	 * thread that acquired the mutex.
	 * @throws IllegalStateException if this thread does not hold the mutex
	 */
	void release ();
	
	/**
	 * Obtains a snapshot of the current mutex status.
	 * @return the mutex status
	 */
	IMutexStatus status_snapshot ();
	
	/**
	 * Cleans the statistics of this mutex.
	 */
	void reset_statistics ();
}
