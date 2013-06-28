package incubator.scb;


/**
 * Listener that is informed when a sync SCB changes.
 * @param <T> the type of the sync SCB
 */
public interface ScbUpdateListener<T extends Scb<T>> {
	/**
	 * THe SCB has been updated.
	 * @param t the SCB
	 */
	public void updated(T t);
}
