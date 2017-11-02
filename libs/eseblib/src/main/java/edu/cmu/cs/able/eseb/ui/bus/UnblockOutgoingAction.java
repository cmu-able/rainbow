package edu.cmu.cs.able.eseb.ui.bus;

import edu.cmu.cs.able.eseb.bus.rci.BlockingStatus;


/**
 * Action unblocking outgoing events in a connection.
 */
@SuppressWarnings("serial")
public class UnblockOutgoingAction extends RemoteConnectionAction {
	/**
	 * Creates a new action.
	 */
	public UnblockOutgoingAction() {
	}

	@Override
	protected boolean isValid() throws Exception {
		return BlockingStatus.status_of(m_remote.outgoing_chain())
				!= BlockingStatus.NOT_BLOCKING;
	}

	@Override
	protected void perform() throws Exception {
		m_control_interface.outgoing_blocking_status(m_remote.connection_id(),
				BlockingStatus. NOT_BLOCKING);
	}
}
