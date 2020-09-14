package incubator.scb.scbset;

import incubator.scb.CloneableScb;
import incubator.scb.MergeableIdScb;
import incubator.scb.Scb;
import incubator.scb.filter.ScbFilter;

/**
 * Entry in an SCB set change log. Each entry in a change log represents an
 * operation that modifies a set.
 * @param <T> the type of SCB
 */
public interface ChangeLogEntry
		<T extends Scb<T> & MergeableIdScb<T> & CloneableScb<T>> {
	/**
	 * Applies the change log entry to a set, optionally using a filter
	 * that defines the SCBs accepted by the set.
	 * @param set the set
	 * @param filter an optional filter to apply
	 */
	void apply(ScbWritableSet<T> set, ScbFilter<T> filter);
	
	/**
	 * Checks if this entry is a checkpoint.
	 * @return is the entry a checkpoint?
	 */
	boolean checkpoint();
}
