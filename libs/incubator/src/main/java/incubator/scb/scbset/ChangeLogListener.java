package incubator.scb.scbset;

/**
 * Listener for changes in the change log.
 */
public interface ChangeLogListener {
	/**
	 * The change log has been changed.
	 */
	void changed();
}
