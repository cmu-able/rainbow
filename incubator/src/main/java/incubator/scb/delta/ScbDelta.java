package incubator.scb.delta;

import incubator.scb.MergeableScb;


/**
 * Interface of an object that can apply deltas to an SCB. The general
 * contract of {@link ScbDelta} is that {@link #apply()} followed by
 * {@link #revert()} should yield an SCB which is equal to the original
 * one.
 * @param <T> the type of SCB
 */
public interface ScbDelta<T extends MergeableScb<T>> {
	/**
	 * Obtains the target.
	 * @return the target
	 */
	public T target();
	
	/**
	 * Obtains the source.
	 * @return the source
	 */
	public T source();
	
	/**
	 * Applies the change to the SCB
	 */
	void apply();
	
	/**
	 * Reverts the applied changes.
	 */
	void revert();
}
