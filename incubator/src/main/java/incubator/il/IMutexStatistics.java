package incubator.il;

/**
 * Interface that represents mutex statistics.
 */
public interface IMutexStatistics {
	/**
	 * Obtains the number of times the mutex has been acquired.
	 * @return the number of acquisitions
	 */
	int total_acquisition_count ();
	
	/**
	 * Obtains the average waiting time for the mutex, when it was
	 * necessary to wait.
	 * @return the average waiting time in milliseconds
	 */
	long average_wait_time ();
	
	/**
	 * Obtains the average mutex acquisition time, with or without waiting.
	 * @return the average time in milliseconds
	 */
	long average_acquire_time ();
	
	/**
	 * The average mutex use (holding) time.
	 * @return average time in milliseconds
	 */
	long average_usage_time ();
	
	/**
	 * Obtains the number of acquisitions that didn't need to wait.
	 * @return the number of acquisitions
	 */
	int counts_with_no_wait ();
}
