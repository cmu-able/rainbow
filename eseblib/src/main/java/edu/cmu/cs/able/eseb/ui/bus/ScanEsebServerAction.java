package edu.cmu.cs.able.eseb.ui.bus;

import incubator.exh.LocalCollector;
import incubator.il.IMutexManager;
import incubator.pval.Ensure;
import incubator.rmi.ui.ScanHostAction;
import incubator.ui.MainApplicationFrame;

import javax.swing.JInternalFrame;

import edu.cmu.cs.able.eseb.bus.rci.EventBusRemoteControlInterface;

/**
 * Action that scans for an eseb server.
 */
@SuppressWarnings("serial")
public class ScanEsebServerAction extends ScanHostAction {
	/**
	 * The application frame to add frames to.
	 */
	private MainApplicationFrame m_maf;
	
	/**
	 * The exception collector.
	 */
	private LocalCollector m_tc;
	
	/**
	 * Creates a new action.
	 * @param mutex_manager the mutex manager to use
	 * @param maf the application frame
	 */
	public ScanEsebServerAction(IMutexManager mutex_manager,
			MainApplicationFrame maf) {
		super(mutex_manager, EventBusRemoteControlInterface.class);
		Ensure.not_null(maf);
		m_maf = maf;
		m_tc = new LocalCollector(ScanEsebServerAction.class.getName());
	}

	@Override
	protected void do_client_found(String host, int port, Object client) {
		Ensure.not_null(host);
		Ensure.greater(port, 0);
		Ensure.not_null(client);
		Ensure.is_instance(client, EventBusRemoteControlInterface.class);
		
		try {
			m_maf.add_frame(new EventBusFrame(host, (short) port,
					(EventBusRemoteControlInterface) client, m_maf));
		} catch (Exception e) {
			m_tc.collect(e, "Client found");
		}
	}

	@Override
	protected void do_add_frame(JInternalFrame frame) {
		Ensure.not_null(frame);
		m_maf.add_frame(frame);
	}
}
