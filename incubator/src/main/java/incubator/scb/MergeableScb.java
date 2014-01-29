package incubator.scb;

import incubator.scb.delta.ScbDelta;

/**
 * A mergeable SCB is an SCB which can be merged with another one. When an SCB
 * is merged with another, its data is copied to the destination SCB.
 * @param <T> the SCB type
 */
public interface MergeableScb<T extends MergeableScb<T>> {
	/**
	 * Merges the two SCBs copying data from <code>v</code> into this SCB.
	 * @param v the SCB to copy data from, must have the same ID as this one
	 */
	void merge(T v);
	
	/**
	 * Obtains the set of differences that need to apply to a source SCB
	 * to make it equal to this one.
	 * @param t the source SCB
	 * @return the set of differences
	 */
	ScbDelta<T> diff_from(T t);
}
