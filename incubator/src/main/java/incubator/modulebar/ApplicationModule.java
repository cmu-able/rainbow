package incubator.modulebar;

import incubator.ctxaction.ContextualAction;

import javax.swing.*;

/**
 * Interface implemented by application modules.
 */
public interface ApplicationModule {
	/**
	 * Obtains the component that contains the module's user interface.
	 * 
	 * @return the module's user interface component
	 */
	JPanel getModulePanel ();
	
	/**
	 * Obtains the action used to activate the module (the action that will
	 * appear in the toolbar).
	 * 
	 * @return the action
	 */
	ContextualAction getActivationAction ();
	
	/**
	 * Invoked when the module has been activated.
	 */
	void activated ();
	
	/**
	 * Invoked when the module has been deactivated.
	 */
	void deactivated ();
}
