package edu.cmu.cs.able.eseb.ui.bus;

import edu.cmu.cs.able.eseb.bus.rci.BlockingStatus;


/**
 * Action blocking outgoing events in a connection.
 */
@SuppressWarnings("serial")
public class BlockOutgoingAction extends RemoteConnectionAction {
	/**
	 * Creates a new action.
	 */
	public BlockOutgoingAction() {
	}

	@Override
	protected boolean isValid() throws Exception {
		return BlockingStatus.status_of(m_remote.outgoing_chain())
				!= BlockingStatus.BLOCKING;
	}

	@Override
	protected void perform() throws Exception {
		m_control_interface.outgoing_blocking_status(m_remote.connection_id(),
				BlockingStatus.BLOCKING);
	}
}
