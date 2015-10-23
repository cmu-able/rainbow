package incubator.il.srv;

import incubator.il.IMutexStatus;

import java.util.Map;

/**
 * Remote interface for mutex manager.
 */
public interface IMutexManagerRemoteAccess {
	/**
	 * Obtains the name of the mutex manager name.
	 * @return the name
	 */
	String manager_name ();
	
	/**
	 * Obtains the mutex manager's status report.
	 * @return a mapping from mutex name to mutex status
	 */
	Map<String, IMutexStatus> getStatusReport ();
}
