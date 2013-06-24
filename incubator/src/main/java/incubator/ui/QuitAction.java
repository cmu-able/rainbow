package incubator.ui;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.ContextualAction;

/**
 * Action that violently closes the application.
 */
public class QuitAction extends ContextualAction {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new action.
	 */
	public QuitAction() {
		/*
		 * Nothing to do.
		 */
	}

	@Override
	protected boolean isValid(ActionContext context) {
		return true;
	}

	@Override
	public void perform(ActionContext context) {
		System.exit(0);
	}
}