package incubator.scb.scbset;

import incubator.pval.Ensure;
import incubator.scb.CloneableScb;
import incubator.scb.MergeableIdScb;
import incubator.scb.Scb;
import incubator.scb.filter.ScbFilter;

/**
 * Change log entry that adds an SCB to the set.
 * @param <T> the SCB type
 */
public class AddScbChangeLogEntry
		<T extends Scb<T> & MergeableIdScb<T> & CloneableScb<T>>
		implements ChangeLogEntry<T> {
	/**
	 * The SCB to add.
	 */
	private T m_scb;
	
	/**
	 * Create a new entry.
	 * @param scb the SCB
	 */
	public AddScbChangeLogEntry(T scb) {
		Ensure.not_null(scb, "scb == null");
		m_scb = scb.deep_clone();
	}
	
	@Override
	public void apply(ScbWritableSet<T> set, ScbFilter<T> filter) {
		Ensure.not_null(set, "set == null");
		if (filter == null || filter.accepts(m_scb)) {
			set.add(m_scb);
		}
	}
	
	@Override
	public boolean checkpoint() {
		return false;
	}
}
