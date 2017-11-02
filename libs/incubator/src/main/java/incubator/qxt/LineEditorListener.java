package incubator.qxt;

/**
 * <p>
 * Interface implemented by classes that want to be notified of changes in
 * lines on a <code>qxt</code>. Note that this interface is only informed if
 * the table is in line mode.
 * </p>
 * <p>
 * Line editor listeners follow a two-phase commit protocol. When line
 * commit is going to occur, but before it occurs, the
 * {@link #tryLineEditingCommitted(Object, int)} method is invoked. If this
 * method fails on any listener (by returning <code>false</code>) the commit
 * is canceled and the {@link #lineEditingCommitFailed(Object, int)} method
 * is invoked on all listeners. If all listeners accept the commit, the
 * {@link #lineEditingCommitted(Object, int)} is invoked.
 * </p>
 * 
 * @param <T> the bean type
 */
public interface LineEditorListener<T> {
	/**
	 * Informs all listeners that line commit has failed and the line has
	 * not been committed.
	 * 
	 * @param t the object associated with the line
	 * @param line the line number (in model coordinates)
	 */
	void lineEditingCommitFailed(T t, int line);

	/**
	 * Invoked to try to commit a line.
	 * 
	 * @param t the object associated with the line
	 * @param line the line number (in model coordinates)
	 * 
	 * @return was the commit successful?
	 */
	boolean tryLineEditingCommitted(T t, int line);

	/**
	 * Informs when the user begins to edit a line.
	 * 
	 * @param t the object associated with the line
	 * @param line the line number (in model coordinates)
	 */
	void lineEditingStarted(T t, int line);

	/**
	 * Informs when a line has been committed.
	 * 
	 * @param t the object associated with the line
	 * @param line the line number (in model coordinates)
	 */
	void lineEditingCommitted(T t, int line);

	/**
	 * Informs when a line editing has been canceled.
	 * 
	 * @param t the object associated with the line
	 * @param line the line number (in model coordinates)
	 */
	void lineEditingCanceled(T t, int line);
}
