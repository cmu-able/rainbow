package edu.cmu.cs.able.eseb.ui.bus;

import edu.cmu.cs.able.eseb.bus.rci.EventBusRemoteControlInterface;
import incubator.ctxaction.ActionContext;
import incubator.ctxaction.ContextualAction;
import incubator.pval.Ensure;
import incubator.ui.MainApplicationFrame;

/**
 * Action that shows the events of an event bus.
 */
@SuppressWarnings("serial")
public class ShowBusEventsAction extends ContextualAction {
	/**
	 * The remote interface.
	 */
	private EventBusRemoteControlInterface m_remote;
	
	/**
	 * The main application frame.
	 */
	private MainApplicationFrame m_maf;
	
	/**
	 * Host we're connected to.
	 */
	private String m_host;
	
	/**
	 * Port we're connected to.
	 */
	private short m_port;
	
	/**
	 * Creates a new action.
	 * @param host the host we're connected to
	 * @param port the port we're connected to
	 * @param ri the remote interface
	 * @param maf the main application frame
	 */
	public ShowBusEventsAction(String host, short port,
			EventBusRemoteControlInterface ri, MainApplicationFrame maf) {
		Ensure.not_null(host);
		Ensure.greater(port, 0);
		Ensure.not_null(ri);
		m_host = host;
		m_port = port;
		m_remote = ri;
		m_maf = maf;
	}

	@Override
	protected boolean isValid(ActionContext context) {
		return true;
	}

	@Override
	protected void perform(ActionContext context) {
		BusEventsFrame bef = new BusEventsFrame(m_host, m_port, m_remote);
		m_maf.add_frame(bef);
	}
}
