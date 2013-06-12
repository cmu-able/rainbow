package incubator.ctxaction;

/**
 * Listener that can be registered on a {@link ContextualAction} to be informed
 * of changes or executions in the action.
 */
public interface ContextualActionListener {
	/**
	 * The action has been performed.
	 */
	public void actionPerformed();
}
