package incubator.qxt;

/**
 * Interface implemented by filters that can be set on the <code>qxt</code>.
 * 
 * @param <T> bean type
 */
public interface QxtFilter<T> {
	/**
	 * Determines if an object should be displayed.
	 * 
	 * @param t the object
	 * 
	 * @return should be displayed?
	 */
	boolean accept(T t);
}
