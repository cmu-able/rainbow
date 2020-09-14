package incubator.scb;

/**
 * SCB container in which we can publicly add and remove SCBs.
 * @param <T> the type of SCBs
 */
public interface ScbEditableContainer<T extends Scb<T>>
		extends ScbContainer<T> {
	/**
	 * Adds an SCB to a container.
	 * @param t the SCB to add
	 */
	void add_scb(T t);

	/**
	 * Deletes an SCB that belongs to this container. Naturally, deletion
	 * is only performed by the garbage collector after there are no more
	 * references to it.
	 * @param t the SCB to delete
	 */
	void remove_scb(T t);
}