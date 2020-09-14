package edu.cmu.cs.able.eseb.ui.bus;

import edu.cmu.cs.able.eseb.bus.rci.BlockingStatus;


/**
 * Action removing the blocking filter from a connection.
 */
@SuppressWarnings("serial")
public class RemoveBlockingFilterOutgoingAction extends RemoteConnectionAction {
	/**
	 * Creates a new action.
	 */
	public RemoveBlockingFilterOutgoingAction() {
	}

	@Override
	protected boolean isValid() throws Exception {
		return BlockingStatus.status_of(m_remote.outgoing_chain())
				!= BlockingStatus.NO_BLOCKING_FILTER;
	}

	@Override
	protected void perform() throws Exception {
		m_control_interface.outgoing_blocking_status(m_remote.connection_id(),
				BlockingStatus.NO_BLOCKING_FILTER);
	}
}
