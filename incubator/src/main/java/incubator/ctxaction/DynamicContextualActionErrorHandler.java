package incubator.ctxaction;

/**
 * Interface implemented by classes that may perform as error handlers for
 * the {@link DynamicContextualAction}.
 */
public interface DynamicContextualActionErrorHandler {
	/**
	 * Handles an error that was raised during action execution.
	 * 
	 * @param context the action context
	 * @param throwable the error raised
	 */
	void handleError(ActionContext context, Throwable throwable);
}
