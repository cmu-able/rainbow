package incubator.scb.filter;

import incubator.pval.Ensure;

/**
 * SCB filter that performs a <em>not</em> of another filter.
 * @param <T> the data type to filter
 */
public class ScbNotFilter<T> implements ScbFilter<T> {
	/**
	 * The inner filter.
	 */
	private ScbFilter<T> m_inner;
	
	/**
	 * Creates a new filter.
	 * @param inner the inner filter
	 */
	public ScbNotFilter(ScbFilter<T> inner) {
		Ensure.not_null(inner, "inner == null");
		m_inner = inner;
	}
	
	@Override
	public boolean accepts(T t) {
		return !m_inner.accepts(t);
	}
}
