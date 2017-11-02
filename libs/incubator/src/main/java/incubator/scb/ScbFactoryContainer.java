package incubator.scb;

/**
 * Container which can create its own SCBs.
 * @param <T> the bean type
 */
public interface ScbFactoryContainer<T extends Scb<T>>
		extends ScbEditableContainer<T> {
	/**
	 * Creates a new SCB and adds it to the container.
	 * @return the created SCB
	 */
	T new_scb();
}
