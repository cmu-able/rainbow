package incubator.ui;

/**
 * Interface implemented by classes that are informed of progress made by a
 * {@link ProgressTask}.
 */
public interface ProgressListener {
	/**
	 * Current progress is undefined.
	 */
	void undefined();
	
	/**
	 * Current progress is defined.
	 * @param current work done so far (greater or equal to zero and less or
	 * equal to the total)
	 * @param total total work to do
	 */
	void defined(int current, int total);
	
	/**
	 * Description of current task
	 * @param text the description
	 */
	void text(String text);
	
	/**
	 * Task is completed.
	 */
	void done();
}
