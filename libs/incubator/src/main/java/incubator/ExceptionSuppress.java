package incubator;

import incubator.pval.Ensure;

/**
 * Class used to handle loops in which multiple exceptions may be thrown and
 * only the first one should be thrown (all others should be registered as
 * suppressed).
 * @param <T> the exception type
 */
public class ExceptionSuppress<T extends Exception> {
	/**
	 * The main exception, <code>null</code> if none.
	 */
	private T m_exception;
	
	/**
	 * Creates a new suppress.
	 */
	public ExceptionSuppress() {
		m_exception = null;
	}
	
	/**
	 * Adds an exception. If this is the first exception, this one will be
	 * thrown in {@link #maybe_throw()}. If it is not, it will be added as
	 * a suppressed exception of the first one
	 * @param t the exception
	 */
	public void add(T t) {
		Ensure.not_null(t, "t == null");
		
		if (m_exception == null) {
			m_exception = t;
		} else {
			m_exception.addSuppressed(t);
		}
	}
	
	/**
	 * Throw the first added expression, if any. If no exception was added,
	 * no exception will be thrown.
	 * @throws T the exception
	 */
	public void maybe_throw() throws T {
		if (m_exception != null) {
			throw m_exception;
		}
	}
}
