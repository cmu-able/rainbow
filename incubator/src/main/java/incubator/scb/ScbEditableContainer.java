package incubator.scb;

/**
 * Container which can create and remove objects.
 * @param <T> the bean type
 */
public interface ScbEditableContainer<T> extends ScbContainer<T> {
	/**
	 * Creates a new SCB and adds it to the container.
	 * @return the created SCB
	 */
	T new_scb();
	
	/**
	 * Deletes an SCB that belongs to this container. Naturally, deletion
	 * is only performed by the garbage collector after there are no more
	 * references to it.
	 * @param t the SCB to delete
	 */
	void delete_scb(T t);
}
