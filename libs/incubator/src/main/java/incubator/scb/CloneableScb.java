package incubator.scb;

/**
 * Interface implemented by SCBs that can be cloned.
 * @param <T> the type of SCB
 */
public interface CloneableScb<T extends CloneableScb<T>> {
	/**
	 * Clones the SCB.
	 * @return a clone of the SCB
	 */
	T deep_clone();
}
