package incubator.ctxaction;

/**
 * Interface implemented by classes that may perform as executers for the
 * {@link DynamicContextualAction}.
 */
public interface DynamicContextualActionExecuter {
	/**
	 * Executes an action.
	 * 
	 * @param context the action context
	 * 
	 * @throws Throwable failed execution
	 */
	void execute(ActionContext context) throws Throwable;
}
