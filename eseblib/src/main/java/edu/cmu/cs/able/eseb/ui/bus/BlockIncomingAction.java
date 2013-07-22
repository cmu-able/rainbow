package edu.cmu.cs.able.eseb.ui.bus;

import edu.cmu.cs.able.eseb.bus.rci.BlockingStatus;


/**
 * Action blocking incoming events in a connection.
 */
@SuppressWarnings("serial")
public class BlockIncomingAction extends RemoteConnectionAction {
	/**
	 * Creates a new action.
	 */
	public BlockIncomingAction() {
	}

	@Override
	protected boolean isValid() throws Exception {
		return BlockingStatus.status_of(m_remote.incoming_chain())
				!= BlockingStatus.BLOCKING;
	}

	@Override
	protected void perform() throws Exception {
		m_control_interface.incoming_blocking_status(m_remote.connection_id(),
				BlockingStatus.BLOCKING);
	}
}
