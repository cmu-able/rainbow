package incubator.scb.scbset;

/**
 * Listener that receives events from a {@link ChangeLogImpl}.
 */
public interface ChangeLogImplListener {
	/**
	 * A checkpoint is needed.
	 */
	void checkpoint_needed();
}
