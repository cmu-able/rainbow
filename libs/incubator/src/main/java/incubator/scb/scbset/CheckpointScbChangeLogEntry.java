package incubator.scb.scbset;

import java.util.HashSet;
import java.util.Set;

import incubator.pval.Ensure;
import incubator.scb.CloneableScb;
import incubator.scb.MergeableIdScb;
import incubator.scb.Scb;
import incubator.scb.filter.ScbFilter;

/**
 * A checkpoint changelog entry is a special entry which re-syncs the whole
 * SCB set. It is only sent once to a change log consumer (see
 * {@link ChangeLog} for details).
 * @param <T> the type of SCB
 */
public class CheckpointScbChangeLogEntry
		<T extends Scb<T> & MergeableIdScb<T> & CloneableScb<T>>
		implements ChangeLogEntry<T> {
	/**
	 * The whole set data.
	 */
	private Set<ScbIw<T>> m_data;
	
	/**
	 * Creates a new checkpoint change log entry.
	 * @param data all SCBs in the set
	 */
	public CheckpointScbChangeLogEntry(Set<ScbIw<T>> data) {
		Ensure.not_null(data, "data == null");
		m_data = new HashSet<>();
		for (ScbIw<T> t : data) {
			m_data.add(new ScbIw<>(t.scb()));
		}
	}
	
	@Override
	public void apply(ScbWritableSet<T> set, ScbFilter<T> filter) {
		Ensure.not_null(set, "set == null");
		
		Set<ScbIw<T>> filtered;
		if (filter == null) {
			filtered = m_data;
		} else {
			filtered = new HashSet<>();
			for (ScbIw<T> t : m_data) {
				if (filter.accepts(t.scb())) {
					filtered.add(t);
				}
			}
		}
		
		set.sync(filtered);
	}
	
	@Override
	public boolean checkpoint() {
		return true;
	}
}
