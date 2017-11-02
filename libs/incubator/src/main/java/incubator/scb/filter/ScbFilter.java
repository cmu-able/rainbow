package incubator.scb.filter;

/**
 * Interface of a filter.
 * @param <T> the type of data in the filter
 */
public interface ScbFilter<T> {
	/**
	 * Is the data accepted by the filter?
	 * @param t the data
	 * @return is it accepted?
	 */
	boolean accepts(T t);
}
