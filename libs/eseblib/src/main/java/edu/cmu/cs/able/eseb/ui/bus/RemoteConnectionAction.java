package edu.cmu.cs.able.eseb.ui.bus;

import incubator.ctxaction.FixedKeyContextualAction;
import incubator.ctxaction.Key;
import incubator.ctxaction.MandatoryKey;
import incubator.exh.LocalCollector;
import edu.cmu.cs.able.eseb.bus.rci.EventBusRemoteConnectionInfo;
import edu.cmu.cs.able.eseb.bus.rci.EventBusRemoteControlInterface;

/**
 * Abstract action that operates on a remote connection.
 */
@SuppressWarnings("serial")
public abstract class RemoteConnectionAction extends FixedKeyContextualAction {
	/**
	 * Key for remote connections.
	 */
	public static final String REMOTE_CONNECTION_KEY = "remote-connection:key";
	
	/**
	 * Key for remote interface.
	 */
	public static final String REMOTE_INTERFACE_KEY = "remote-interface:key";
	
	/**
	 * The remote information.
	 */
	@Key(contextKey = REMOTE_CONNECTION_KEY)
	@MandatoryKey
	public EventBusRemoteConnectionInfo m_remote;
	
	/**
	 * The control interface.
	 */
	@Key(contextKey = REMOTE_INTERFACE_KEY)
	@MandatoryKey
	public EventBusRemoteControlInterface m_control_interface;
	
	/**
	 * Exception collector.
	 */
	private LocalCollector m_collector;
	
	/**
	 * Creates a new action.
	 */
	public RemoteConnectionAction() {
		m_collector = new LocalCollector("Connection Action: "
				+ getClass().getName());
	}

	@Override
	protected void handleError(Exception e, boolean duringPerform) {
		m_collector.collect(e, duringPerform? "During perform"
				: "During validation");
	}
}
