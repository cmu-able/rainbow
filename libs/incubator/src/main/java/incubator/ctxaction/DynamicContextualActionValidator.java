package incubator.ctxaction;

/**
 * Interface implemented by classes that may perform as validators for the
 * {@link DynamicContextualAction}.
 */
public interface DynamicContextualActionValidator {
	/**
	 * Determines whether an action is valid in the given context?
	 * 
	 * @param context the action context
	 * 
	 * @return is the action valid?
	 */
	boolean isValid(ActionContext context);
}
