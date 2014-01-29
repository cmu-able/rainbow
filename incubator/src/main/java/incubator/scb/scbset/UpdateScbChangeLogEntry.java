package incubator.scb.scbset;

import incubator.pval.Ensure;
import incubator.scb.CloneableScb;
import incubator.scb.MergeableIdScb;
import incubator.scb.Scb;
import incubator.scb.filter.ScbFilter;

/**
 * Change log entry that updates an SCB in a set. If a filter is used, the
 * update may end up adding or removing the SCB from the set.
 * @param <T> the type of SCB
 */
public class UpdateScbChangeLogEntry
		<T extends Scb<T> & MergeableIdScb<T> & CloneableScb<T>>
		implements ChangeLogEntry<T> {
	/**
	 * The SCB to update.
	 */
	private T m_scb;
	
	/**
	 * Creates a new changelog entry.
	 * @param scb the SCB to update
	 */
	public UpdateScbChangeLogEntry(T scb) {
		Ensure.not_null(scb, "scb == null");
		m_scb = scb.deep_clone();
	}
	
	@Override
	public void apply(ScbWritableSet<T> set, ScbFilter<T> filter) {
		Ensure.not_null(set, "set == null");
		boolean accepts = (filter == null || filter.accepts(m_scb));
		
		synchronized (set) {
			T found = set.get(m_scb.id());
			if (found == null) {
				if (filter == null) {
					Ensure.not_null(found, "No SCB found with ID " + m_scb.id()
							+ " but one was expected.");
				} else {
					/*
					 * We assume the filter was not accepting the SCB as it was
					 * previously defined.
					 */
					if (accepts) {
						set.add(m_scb);
					}
				}
			} else {
				if (accepts) {
					/*
					 * SCB was previously accepted and is now also accepted.
					 */
					found.merge(m_scb);
				} else {
					/*
					 * SCB was previously accepted but not it isn't.
					 */
					set.remove(m_scb);
				}
			}
		}
		
	}
	
	@Override
	public boolean checkpoint() {
		return false;
	}
}
