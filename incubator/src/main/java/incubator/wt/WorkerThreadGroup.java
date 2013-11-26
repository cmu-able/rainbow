package incubator.wt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import incubator.pval.Ensure;

/**
 * Implementation of a worker thread group.
 */
public class WorkerThreadGroup implements WorkerThreadGroupCI {
	/**
	 * The thread group name.
	 */
	private String m_name;
	
	/**
	 * The thread group description.
	 */
	private String m_description;
	
	/**
	 * All threads in this work group.
	 */
	private Set<WorkerThread> m_threads;
	
	/**
	 * Direct subgroups of this group.
	 */
	private Set<WorkerThreadGroup> m_subgroups;
	
	/**
	 * Creates a new worker thread group with the given name.
	 * @param name the name
	 */
	public WorkerThreadGroup(String name) {
		Ensure.not_null(name, "name == null");
		m_name = name;
		m_threads = new HashSet<>();
		m_subgroups = new HashSet<>();
	}

	@Override
	public synchronized String name() {
		return m_name;
	}
	
	@Override
	public synchronized String description() {
		return m_description;
	}
	
	/**
	 * Sets the thread group description.
	 * @param d the description
	 */
	public synchronized void description(String d) {
		m_description = d;
	}

	@Override
	public synchronized Set<WorkerThreadCI> threads() {
		return new HashSet<WorkerThreadCI>(m_threads);
	}

	@Override
	public synchronized void start() {
		for (WorkerThread wt : m_threads) {
			synchronized (wt) {
				if (wt.state() == WtState.STOPPED
						|| wt.state() == WtState.ABORTED) {
					wt.start();
				}
			}
		}
	}

	@Override
	public synchronized void stop() {
		for (WorkerThread wt : m_threads) {
			synchronized (wt) {
				if (wt.state() == WtState.RUNNING) {
					wt.stop();
				}
			}
		}
	}
	
	/**
	 * Adds a worker thread to this group.
	 * @param wt the thread
	 */
	public synchronized void add_thread(WorkerThread wt) {
		Ensure.not_null(wt, "wt == null");
		Ensure.is_false(m_threads.contains(wt), "m_threads.contains(wt)");
		m_threads.add(wt);
	}
	
	/**
	 * Removes a worker thread from this group.
	 * @param wt the thread
	 */
	public synchronized void remove_thread(WorkerThread wt) {
		Ensure.not_null(wt, "wt == null");
		Ensure.is_true(m_threads.contains(wt), "m_threads.contains(wt)");
		m_threads.remove(wt);
	}
	
	/**
	 * Adds a worker thread group as subgroup of this group.
	 * @param wtg the group
	 */
	public synchronized void add_subgroup(WorkerThreadGroup wtg) {
		Ensure.not_null(wtg, "wtg == null");
		Ensure.is_true(wtg != this, "wtg == this");
		Ensure.is_false(m_subgroups.contains(wtg), "m_subgroups.contains(wtg)");
		Ensure.is_false(wtg.all_subgroups().contains(this),
				"wtg.all_subgroups().contains(this)");
		m_subgroups.add(wtg);
	}
	
	/**
	 * Removes a worker thread group as subgroup of this group.
	 * @param wtg the group
	 */
	public synchronized void remove_subgroup(WorkerThreadGroup wtg) {
		Ensure.not_null(wtg, "wtg == null");
		Ensure.is_true(wtg != this, "wtg == this");
		Ensure.is_true(m_subgroups.contains(wtg),
				"!m_subgroups.contains(wtg)");
		m_subgroups.remove(wtg);
	}

	@Override
	public synchronized Set<WorkerThreadGroupCI> direct_subgroups() {
		return new HashSet<WorkerThreadGroupCI>(m_subgroups);
	}

	@Override
	public Set<WorkerThreadGroupCI> all_subgroups() {
		List<WorkerThreadGroupCI> pending;
		
		synchronized (this) {
			pending = new ArrayList<WorkerThreadGroupCI>(m_subgroups);
		}
		
		Set<WorkerThreadGroupCI> all = new HashSet<>();
		
		while (pending.size() > 0) {
			WorkerThreadGroupCI c = pending.remove(0);
			
			if (!all.contains(c)) {
				all.add(c);
				pending.addAll(c.all_subgroups());
			}
		}
		
		return all;
	}
	
	@Override
	public void start_all() {
		start();
		
		Set<WorkerThreadGroupCI> all = all_subgroups();
		for (WorkerThreadGroupCI i : all) {
			i.start();
		}
	}
	
	@Override
	public void stop_all() {
		stop();
		
		Set<WorkerThreadGroupCI> all = all_subgroups();
		for (WorkerThreadGroupCI i : all) {
			i.stop();
		}
	}
}
