package incubator.koolform;

/**
 * Interface implemented by classes that want to be notified of chanegs in a
 * kool form.
 */
public interface KoolFormListener {
	/**
	 * The form's focus has changed (gained or lost).
	 * 
	 * @param hasFocus does the form currently has the focus?
	 */
	void formFocusChanged(boolean hasFocus);
}
