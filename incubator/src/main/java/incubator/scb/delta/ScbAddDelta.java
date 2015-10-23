package incubator.scb.delta;

import incubator.scb.MergeableScb;

/**
 * Change in an SCB that adds a sub value.
 * @param <T> the type of the SCB that receives the new value
 * @param <V> the type of the value to add
 */
public interface ScbAddDelta<T extends MergeableScb<T>, V> extends ScbDelta<T> {
	/**
	 * Obtains the added value.
	 * @return the value
	 */
	V added ();
}
