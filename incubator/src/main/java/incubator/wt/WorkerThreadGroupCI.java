package incubator.wt;

import java.util.Set;

/**
 * A worker thread group maintains a list of worker threads and allows
 * starting and stopping all of them. Note that threads in the thread group
 * are independent of each other and they may belong to other groups. Thread
 * groups can also be organized in a somewhat hierarchical way: thread groups
 * may be placed in thread groups also not exclusively. It may be confusing
 * (although not illegal) to have the same thread belonging to several groups
 * in the same hierarchy. The only requirement for the containment of
 * thread groups is that containment must not form cyclic loops. Although in
 * general thread groups are organized hierarchically, strictly speaking they
 * may form any directed acyclic graph.  
 */
public interface WorkerThreadGroupCI {
	/**
	 * Obtains the name of this thread group.
	 * @return the name
	 */
	String name();
	
	/**
	 * Obtains a description of this thread group.
	 * @return the description or <code>null</code> if no description if set
	 */
	String description();
	
	/**
	 * Obtains all worker threads in this group.
	 * @return all threads
	 */
	Set<WorkerThreadCI> threads();
	
	/**
	 * Starts all stopped and aborted threads in this group. This method only
	 * returns after all threads have been started. Threads already running are
	 * not affected.
	 */
	void start();
	
	/**
	 * Stops all running threads in this group. This method only returns after
	 * all threads have been stopped. Thread not running are not affected.
	 */
	void stop();
	
	/**
	 * Obtains all subgroups directly attached to this thread groups. This
	 * method does not return subgroups of the subgroups.
	 * @return all subgroups of this group
	 */
	Set<WorkerThreadGroupCI> direct_subgroups();
	
	/**
	 * Obtains all subgroups directly or indirectly attached to this thread
	 * groups. This method returns all subgroups recursively.
	 * @return all subgroups of this group
	 */
	Set<WorkerThreadGroupCI> all_subgroups();
	
	/**
	 * Starts all stopped and aborted threads in this group and all its
	 * subgroups recursively. This method only returns after all threads have
	 * been started. Threads already running are not affected.
	 */
	void start_all();
	
	/**
	 * Stops all running threads in this group and all its subgroups
	 * recursively. This method only returns after all threads have been
	 * stopped. Thread not running are not affected.
	 */
	void stop_all();
}
