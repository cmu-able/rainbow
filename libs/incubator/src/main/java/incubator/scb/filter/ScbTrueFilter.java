package incubator.scb.filter;

/**
 * SCB filter that accepts all SCBs.
 * @param <T> the data to filter
 */
public class ScbTrueFilter<T> implements ScbFilter<T> {
	/**
	 * Creates a new filter.
	 */
	public ScbTrueFilter() {
	}

	@Override
	public boolean accepts(T t) {
		return true;
	}
}
