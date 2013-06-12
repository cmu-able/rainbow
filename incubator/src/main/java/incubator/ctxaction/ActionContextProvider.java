package incubator.ctxaction;

/**
 * Interface implemented by objects that provide an action context.
 */
public interface ActionContextProvider {
	/**
	 * Obtains the action context.
	 * 
	 * @return the action context
	 */
	ActionContext getActionContext();
}
