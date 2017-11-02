package incubator.scb.scbset;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import incubator.pval.Ensure;
import incubator.scb.CloneableScb;
import incubator.scb.MergeableIdScb;
import incubator.scb.Scb;
import incubator.scb.ScbUpdateListener;

/**
 * Implementation of an SCB set.
 * @param <T> the type of SCB
 */
public class ScbSet<T extends Scb<T> & MergeableIdScb<T> & CloneableScb<T>>
		implements ScbWritableSet<T> {
	/**
	 * Data: maps IDs to objects.
	 */
	private Map<Integer, T> m_data;
	
	/**
	 * The set's changelog.
	 */
	private ChangeLogImpl<T> m_changelog;
	
	/**
	 * Update listener that informs changes in an SCB.
	 */
	private ScbUpdateListener<T> m_update_listener;
	
	/**
	 * Creates a new, empty, SCB set.
	 */
	public ScbSet() {
		m_data = new HashMap<>();
		m_changelog = new ChangeLogImpl<>();
		m_changelog.impl_dispatcher().add(new ChangeLogImplListener() {
			@Override
			public void checkpoint_needed() {
				checkpoint();
			}
		});
		
		m_update_listener = new ScbUpdateListener<T>() {
			@Override
			public void updated(T t) {
				Ensure.not_null(t, "t == null");
				scb_updated(t);
			}
		};
	}
	
	/**
	 * Invoked when an SCB has been updated.
	 * @param t the SCB that has been updated
	 */
	private synchronized void scb_updated(T t) {
		Ensure.not_null(t, "t == null");
		T found = m_data.get(t.id());
		if (found == null) {
			/*
			 * This may happen if an updated is triggered after an SCB has
			 * been removed. We will ignore it.
			 */
		} else {
			found.merge(t);
			m_changelog.change(new UpdateScbChangeLogEntry<>(t));
		}
	}
	
	/**
	 * Performs a checkpoint.
	 */
	private synchronized void checkpoint() {
		m_changelog.change(new CheckpointScbChangeLogEntry<>(all()));
	}
	
	@Override
	public synchronized void add(T scb) {
		Ensure.not_null(scb, "scb == null");
		Ensure.is_false(m_data.containsKey(scb.id()), "SCB with ID "
				+ scb.id() + " already exists.");
		T clone = scb.deep_clone();
		clone.dispatcher().add(m_update_listener);
		m_data.put(scb.id(), clone);
		m_changelog.change(new AddScbChangeLogEntry<>(scb));
	}
	
	@Override
	public synchronized Set<ScbIw<T>> all() {
		Set<ScbIw<T>> data = new HashSet<>();
		for (T t : m_data.values()) {
			data.add(new ScbIw<>(t));
		}
		
		return data;
	}
	
	@Override
	public ChangeLog<T> changelog() {
		return m_changelog;
	}
	
	@Override
	public synchronized T get(int id) {
		return m_data.get(id);
	}
	
	@Override
	public synchronized void remove(T scb) {
		Ensure.not_null(scb, "scb == null");
		Ensure.is_true(m_data.containsKey(scb.id()), "SCB with ID " + scb.id()
				+ " not found.");
		m_data.get(scb.id()).dispatcher().remove(m_update_listener);
		m_data.remove(scb.id());
		m_changelog.change(new RemoveScbChangeLogEntry<>(scb));
	}
	
	@Override
	public synchronized void sync(Set<ScbIw<T>> data) {
		Set<Integer> all_ids = new HashSet<>();
		for (ScbIw<T> t : data) {
			T found = m_data.get(t.scb().id());
			if (found == null) {
				add(found);
			} else {
				found.merge(t.scb());
			}
			
			all_ids.add(t.scb().id());
		}
		
		Set<Integer> to_del = new HashSet<>(m_data.keySet());
		to_del.removeAll(all_ids);
		for (int id : to_del) {
			T found = m_data.get(id);
			remove(found);
		}
	}
}
