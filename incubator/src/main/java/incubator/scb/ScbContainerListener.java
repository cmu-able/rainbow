package incubator.scb;

/**
 * Interface that is implemented by objects that listen to changes to an
 * SCB container.
 * @param <T> the bean class
 */
public interface ScbContainerListener<T> {
	/**
	 * An SCB has been added to the container.
	 * @param t the SCB
	 */
	void scb_added(T t);
	
	/**
	 * An SCB has been removed from the container.
	 * @param t the SCB
	 */
	void scb_removed(T t);
	
	/**
	 * An SCB has been updated in the container.
	 * @param t the SCB
	 */
	void scb_updated(T t);
}
