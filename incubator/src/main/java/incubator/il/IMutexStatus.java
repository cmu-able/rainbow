package incubator.il;

import java.util.List;

/**
 * Status of a mutex.
 */
public interface IMutexStatus {
	/**
	 * Obtains the mutex request.
	 * @return o request used to acquire the mutex
	 */
	IMutexRequest lock_request ();
	
	/**
	 * Obtains the (sorted) list of all requests waiting for the mutex.
	 * @return the list which is empty if there is no mutex in the waiting
	 * list
	 */
	List<IMutexRequest> wait_list ();
	
	/**
	 * Obtains the mutex statistics.
	 * @return the statistics
	 */
	IMutexStatistics statistics ();
}
