package incubator.scb.scbset;

import incubator.pval.Ensure;
import incubator.scb.CloneableScb;
import incubator.scb.MergeableIdScb;
import incubator.scb.Scb;
import incubator.scb.filter.ScbFilter;

/**
 * Change log entry that removes an SCB from the set.
 * @param <T> the SCB type
 */
public class RemoveScbChangeLogEntry
		<T extends Scb<T> & MergeableIdScb<T> & CloneableScb<T>>
		implements ChangeLogEntry<T> {
	/**
	 * The SCB to remove.
	 */
	private T m_scb;
	
	/**
	 * Creates a new changelog entry.
	 * @param scb the SCB to remove.
	 */
	public RemoveScbChangeLogEntry(T scb) {
		Ensure.not_null(scb, "scb == null");
		m_scb = scb.deep_clone();
	}

	@Override
	public void apply(ScbWritableSet<T> set, ScbFilter<T> filter) {
		Ensure.not_null(set, "set == null");
		if (filter == null || filter.accepts(m_scb)) {
			set.remove(m_scb);
		}
	}
	
	@Override
	public boolean checkpoint() {
		return false;
	}
}
