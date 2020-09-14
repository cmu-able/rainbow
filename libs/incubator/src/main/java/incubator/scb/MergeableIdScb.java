package incubator.scb;

/**
 * A mergeable identified SCB is a mergeable SCB which has a unique ID.
 * @param <T> the SCB type
 */
public interface MergeableIdScb<T extends MergeableIdScb<T>> extends
		MergeableScb<T> {
	/**
	 * Obatins the SCB's unique identifier.
	 * @return the unique identifier
	 */
	int id();
}
