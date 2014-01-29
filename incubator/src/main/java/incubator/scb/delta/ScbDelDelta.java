package incubator.scb.delta;

import incubator.scb.MergeableScb;

/**
 * Change in an SCB that removes a sub value.
 * @param <T> the type of the SCB that receives the new SCB
 * @param <V> the type of the value to remove
 */
public interface ScbDelDelta<T extends MergeableScb<T>, V> extends ScbDelta<T> {
	/**
	 * Obtains the removed value.
	 * @return the value
	 */
	public V deleted();
}
