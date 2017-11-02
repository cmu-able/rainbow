package incubator.qxt;

/**
 * Interface implemented by objects that create lines for a <code>qxt</code>.
 * 
 * @param <T> the bean type
 */
public interface LineFactory<T> {
	/**
	 * Create a new line.
	 * 
	 * @return the new line (cannot return <code>null</code>)
	 */
	T makeLine();

	/**
	 * Destroy a line.
	 * 
	 * @param line the line to destroy
	 */
	void destroyLine(T line);
}
