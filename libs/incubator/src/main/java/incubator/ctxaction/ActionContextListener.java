package incubator.ctxaction;

/**
 * Interface implemented by an object which is a listener on the current
 * application context.
 */
public interface ActionContextListener {
	/**
	 * The context has been changed.
	 * 
	 * @param context the application context
	 */
	void contextChanged(ActionContext context);
}
