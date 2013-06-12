package incubator.modulebar;

import incubator.ctxaction.ContextualAction;

import javax.swing.JPanel;

/**
 * Interface implemented by application modules.
 */
public interface ApplicationModule {
	/**
	 * Obtains the component that contains the module's user interface.
	 * 
	 * @return the module's user interface component
	 */
	public JPanel getModulePanel();
	
	/**
	 * Obtains the action used to activate the module (the action that will
	 * appear in the toolbar).
	 * 
	 * @return the action
	 */
	public ContextualAction getActivationAction();
	
	/**
	 * Invoked when the module has been activated.
	 */
	public void activated();
	
	/**
	 * Invoked when the module has been deactivated.
	 */
	public void deactivated();
}
