package edu.cmu.cs.able.eseb.ui.srv;

import javax.swing.JInternalFrame;

import incubator.exh.LocalCollector;
import incubator.il.IMutexManager;
import incubator.il.srv.IMutexManagerRemoteAccess;
import incubator.il.ui.IMutexManagerFrame;
import incubator.pval.Ensure;
import incubator.rmi.ui.ScanHostAction;
import incubator.ui.MainApplicationFrame;

/**
 * Action that scans for a mutex manager.
 */
@SuppressWarnings("serial")
public class ScanMutexManagerAction extends ScanHostAction {
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
	public ScanMutexManagerAction(IMutexManager mutex_manager,
			MainApplicationFrame maf) {
		super(mutex_manager, IMutexManagerRemoteAccess.class);
		Ensure.not_null(maf);
		m_maf = maf;
		m_tc = new LocalCollector(ScanMutexManagerAction.class.getName());
	}

	@Override
	protected void do_client_found(String host, int port, Object client) {
		Ensure.not_null(host);
		Ensure.greater(port, 0);
		Ensure.not_null(client);
		Ensure.is_instance(client, IMutexManagerRemoteAccess.class);
		
		try {
			m_maf.add_frame(new IMutexManagerFrame(host, port,
					(IMutexManagerRemoteAccess) client));
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
