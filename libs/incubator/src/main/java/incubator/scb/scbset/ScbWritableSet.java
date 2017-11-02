package incubator.scb.scbset;

import incubator.scb.CloneableScb;
import incubator.scb.MergeableIdScb;
import incubator.scb.Scb;

import java.util.Set;

/**
 * SCB set that can be changed (SCBs can be added and removed).
 * @param <T> the type of SCB
 */
public interface ScbWritableSet
		<T extends Scb<T> & MergeableIdScb<T> & CloneableScb<T>>
		extends ScbReadableSet<T> {
	/**
	 * Adds an SCB to the set.
	 * @param scb the SCB
	 */
	void add(T scb);
	
	/**
	 * Removes an SCB from the set.
	 * @param scb the SCB to remove; this exact object may not be in the set
	 * but as long as an SCB with the same ID exists, it will be removed.
	 */
	void remove(T scb);
	
	/**
	 * Synchronizes all SCBs with the given data. All SCBs not in the given
	 * set are removed, all that exist in the set but not in this are added
	 * and all other SCBs are updated.
	 * @param data the set
	 */
	void sync(Set<ScbIw<T>> data);
}
