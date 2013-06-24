package edu.cmu.cs.able.eseb.ui.srv;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.ContextualAction;
import incubator.pval.Ensure;
import incubator.ui.MainApplicationFrame;

/**
 * Action that shows the exception browser.
 */
@SuppressWarnings("serial")
public class ShowExceptionBrowserAction extends ContextualAction {
	/**
	 * The main application frame.
	 */
	private MainApplicationFrame m_maf;
	
	/**
	 * Creates a new action.
	 * @param maf the application frame to add the browser to
	 */
	public ShowExceptionBrowserAction(MainApplicationFrame maf) {
		Ensure.not_null(maf);
		m_maf = maf;
	}
	
	@Override
	protected boolean isValid(ActionContext context) {
		return true;
	}

	@Override
	protected void perform(ActionContext context) {
		m_maf.add_frame(new ExceptionBrowserIFrame());
	}
}
