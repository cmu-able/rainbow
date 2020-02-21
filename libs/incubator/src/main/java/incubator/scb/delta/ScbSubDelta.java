package incubator.scb.delta;

import incubator.scb.MergeableScb;

/**
 * SCB delta that performs an update in a sub-SCB, the <em>sub-target</em>
 * of the <em>target</em> SCB.
 * @param <T> the target type
 * @param <ST> the sub target
 */
public interface ScbSubDelta<T extends MergeableScb<T>,
		ST extends MergeableScb<ST>> extends ScbDelta<T> {
	/**
	 * Obtains the sub delta.
	 * @return the sub delta
	 */
	ScbDelta<ST> sub_delta ();
}