package incubator.rmi.ui;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.ContextualAction;
import incubator.il.IMutexManager;
import incubator.pval.Ensure;
import incubator.rmi.RmiScanner;
import incubator.rmi.RmiScannerListener;

import java.awt.EventQueue;

import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

/**
 * Action that starts scanning a host.
 */
public abstract class ScanHostAction extends ContextualAction {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Mutex manager.
	 */
	private IMutexManager m_mutex_manager;
	
	/**
	 * The remote class.
	 */
	private Class<?> m_search_class;
	
	/**
	 * Creates a new action.
	 * @param mutex_manager the mutex manager
	 * @param search_class the remote class type
	 */
	public ScanHostAction(IMutexManager mutex_manager, Class<?> search_class) {
		Ensure.not_null(mutex_manager);
		Ensure.not_null(search_class);
		
		this.m_mutex_manager = mutex_manager;
		this.m_search_class = search_class;
	}

	@Override
	protected boolean isValid(ActionContext context) {
		return true;
	}

	@Override
	public void perform(ActionContext context) {
		String host = JOptionPane.showInputDialog(null,
				"What is the host to scan?", "Start Scan",
				JOptionPane.QUESTION_MESSAGE);
		if (host == null || host.trim().length() == 0) {
			return;
		}
		
		host = host.trim();
		
		final String scanHost = host;
		RmiScanner scanner = new RmiScanner(host, "Mutex scanner",
				m_mutex_manager, m_search_class);
		scanner.add_listener(new RmiScannerListener() {
			@Override
			public void client_found(final int port, final Object client) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						do_client_found(scanHost, port, client);
					}
				});
			}
			@Override
			public void port_scanned(int port) {
				/*
				 * Nothing to do.
				 */
			}
			@Override
			public void scan_finished() {
				/*
				 * Nothing to do.
				 */
			}
			@Override
			public void scan_paused() {
				/*
				 * Nothing to do.
				 */
			}
			@Override
			public void scan_resumed() {
				/*
				 * Nothing to do.
				 */
			}
			@Override
			public void scan_started(int range) {
				/*
				 * Nothing to do.
				 */
			}
			@Override
			public void scan_stopped() {
				/*
				 * Nothing to do.
				 */
			}
		});
		
		RmiScannerIFrame frame = new RmiScannerIFrame(scanner);
		frame.setVisible(true);
		do_add_frame(frame);
	}
	
	/**
	 * Method invoked when a client is found.
	 * @param host the host scanned
	 * @param port the port where the client was found
	 * @param client the client found
	 */
	protected abstract void do_client_found(String host, int port,
			Object client);
	
	/**
	 * Method invoked to add a frame to the application.
	 * @param frame the frame to add
	 */
	protected abstract void do_add_frame(JInternalFrame frame);
}
