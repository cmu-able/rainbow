package incubator.scb.filter;

import incubator.pval.Ensure;

/**
 * SCB filter that performs an <em>or</em> of two filters.
 * @param <T> the data type to filter
 */
public class ScbOrFilter<T> implements ScbFilter<T> {
	/**
	 * The first filter.
	 */
	private ScbFilter<T> m_f1;
	
	/**
	 * The second filter.
	 */
	private ScbFilter<T> m_f2;
	
	/**
	 * Creates a new <em>or</em> filter.
	 * @param f1 the first filter
	 * @param f2 the second filter
	 */
	public ScbOrFilter(ScbFilter<T> f1, ScbFilter<T> f2) {
		Ensure.not_null(f1, "f1 == null");
		Ensure.not_null(f2, "f2 == null");
		
		m_f1 = f1;
		m_f2 = f2;
	}

	@Override
	public boolean accepts(T t) {
		return m_f1.accepts(t) || m_f2.accepts(t);
	}
	
	/**
	 * Makes a filter from two filters. If both filters are <code>null</code>
	 * this method returns <code>null</code>. If one of the filters is
	 * <code>null</code> but the other isn't returns the non-null filter.
	 * If both are non-<code>null</code>, returns an <em>or</em> filter
	 * with both filters.
	 * @param f1 the first filter
	 * @param f2 the second filter
	 * @param <T> the type of SCB
	 * @return the filter
	 */
	public static <T> ScbFilter<T> make(ScbFilter<T> f1, ScbFilter<T> f2) {
		if (f1 == null) {
			return f2;
		}
		
		if (f2 == null) {
			return f1;
		}
		
		return new ScbOrFilter<>(f1, f2);
	}
}
