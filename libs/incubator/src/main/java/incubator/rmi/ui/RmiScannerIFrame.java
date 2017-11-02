package incubator.rmi.ui;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.ActionContextProvider;
import incubator.pval.Ensure;
import incubator.rmi.RmiScanner;
import incubator.rmi.RmiScannerListener;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToolBar;
import javax.swing.border.EtchedBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

/**
 * Window that shows a scan progress.
 */
public class RmiScannerIFrame extends JInternalFrame
		implements ActionContextProvider {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Key with which the scanner is placed in the context.
	 */
	public static final String SCANNER_KEY = RmiScanner.class + ".ctx-key";
	
	/**
	 * Key with which the scanner state is placed in context.
	 */
	public static final String SCANNER_STATUS_KEY =
			RmiScanner.class + ".status-ctx-key";
	
	/**
	 * Scanner.
	 */
	private RmiScanner m_scanner;
	
	/**
	 * Last port scanned.
	 */
	private int m_last_port_scanned;
	
	/**
	 * Label with the last port scanned.
	 */
	private JLabel m_last_port_scan_label;
	
	/**
	 * Scan progress bar.
	 */
	private JProgressBar m_scan_progress;
	
	/**
	 * Label with the current scan status.
	 */
	private JLabel m_status_label;
	
	/**
	 * Number of clients found.
	 */
	private int m_client_count;
	
	/**
	 * Label with the number of clients found.
	 */
	private JLabel m_client_count_label;
	
	/**
	 * Action context.
	 */
	private ActionContext m_action_context;
	
	/**
	 * Registered listener.
	 */
	private RmiScannerListener m_listener;
	
	/**
	 * Creates a new frame.
	 * @param scanner the scanner
	 */
	public RmiScannerIFrame(RmiScanner scanner) {
		super("Scanner for " + scanner.host());
		
		this.m_scanner = scanner;
		m_action_context = new ActionContext();
		
		m_last_port_scanned = 0;
		m_client_count = 0;
		m_listener = new RmiScannerListener() {
			@Override
			public void client_found(int port, Object client) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						m_client_count++;
						setup();
					}
				});
			}
			@Override
			public void port_scanned(final int port) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						m_last_port_scanned = port;
						setup();
					}
				});
			}
			@Override
			public void scan_finished() {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						m_action_context.set(SCANNER_STATUS_KEY,
								new Integer(RmiScanner.FINISHED));
						setup();
					}
				});
			}
			@Override
			public void scan_paused() {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						m_action_context.set(SCANNER_STATUS_KEY,
								new Integer(RmiScanner.PAUSED));
						setup();
					}
				});
			}
			@Override
			public void scan_resumed() {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						m_action_context.set(SCANNER_STATUS_KEY,
								new Integer(RmiScanner.SCANNING));
						setup();
					}
				});
			}
			@Override
			public void scan_started(int range) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						m_action_context.set(SCANNER_STATUS_KEY,
								new Integer(RmiScanner.SCANNING));
						m_last_port_scanned = 0;
						m_client_count = 0;
						setup();
					}
				});
			}
			@Override
			public void scan_stopped() {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						m_action_context.set(SCANNER_STATUS_KEY,
								new Integer(RmiScanner.STOPPED));
						setup();
					}
				});
			}
		};
		
		scanner.add_listener(m_listener);
		
		init();
		setup();
		
		pack();
		
		m_action_context.set(SCANNER_KEY, scanner);
		
		if (scanner.state() == RmiScanner.STOPPED) {
			scanner.start();
		}
	}
	
	/**
	 * Initializes the window.
	 */
	private void init() {
		getContentPane().setLayout(new BorderLayout());
		JPanel contentPanel = new JPanel();
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		JToolBar toolbar = new JToolBar();
		getContentPane().add(toolbar, BorderLayout.NORTH);
		
		// Content panel.
		contentPanel.setLayout(new BorderLayout());
		JPanel hostPanel = new JPanel();
		contentPanel.add(hostPanel, BorderLayout.NORTH);
		JPanel lowHostPanel = new JPanel();
		contentPanel.add(lowHostPanel, BorderLayout.CENTER);
		
		// Host panel.
		hostPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		hostPanel.add(new JLabel("Host: " + m_scanner.host()));
		
		// Low host panel.
		lowHostPanel.setLayout(new BorderLayout());
		JPanel portsPanel = new JPanel();
		lowHostPanel.add(portsPanel, BorderLayout.NORTH);
		JPanel lowPortsPanel = new JPanel();
		lowHostPanel.add(lowPortsPanel, BorderLayout.CENTER);
		
		// Ports panel.
		GridLayout portsPanelLayout = new GridLayout(1, 3);
		portsPanel.setLayout(portsPanelLayout);
		portsPanelLayout.setHgap(20);
		portsPanel.setBorder(new EtchedBorder());
		portsPanel.add(new JLabel("Low port: " + m_scanner.min_port()));
		m_last_port_scan_label = new JLabel();
		portsPanel.add(m_last_port_scan_label);
		portsPanel.add(new JLabel("High port: " + m_scanner.max_port()));
		
		// Low ports panel.
		lowPortsPanel.setLayout(new BorderLayout());
		JPanel statusPanel = new JPanel();
		lowPortsPanel.add(statusPanel, BorderLayout.NORTH);
		JPanel lowStatusPanel = new JPanel();
		lowPortsPanel.add(lowStatusPanel, BorderLayout.CENTER);
		
		// Status panel.
		statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		m_status_label = new JLabel();
		statusPanel.add(m_status_label);
		
		// Low status panel.
		lowStatusPanel.setLayout(new BorderLayout());
		JPanel progressPanel = new JPanel();
		lowStatusPanel.add(progressPanel, BorderLayout.NORTH);
		JPanel clientCountPanel = new JPanel();
		lowStatusPanel.add(clientCountPanel, BorderLayout.CENTER);
		
		// Progress panel.
		progressPanel.setLayout(new BorderLayout());
		m_scan_progress = new JProgressBar();
		progressPanel.add(m_scan_progress, BorderLayout.CENTER);
		
		// Client count panel.
		clientCountPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		m_client_count_label = new JLabel();
		clientCountPanel.add(m_client_count_label);
		
		StartScanAction ssa = new StartScanAction();
		ssa.bind(m_action_context);
		PauseScanAction psa = new PauseScanAction();
		psa.bind(m_action_context);
		ResumeScanAction rsa = new ResumeScanAction();
		rsa.bind(m_action_context);
		StopScanAction stsa = new StopScanAction();
		stsa.bind(m_action_context);
		
		// Toolbar actions.
		toolbar.add(ssa);
		toolbar.addSeparator();
		toolbar.add(psa);
		toolbar.add(rsa);
		toolbar.addSeparator();
		toolbar.add(stsa);
		
		// Window stuff.
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);
		setResizable(true);
		addInternalFrameListener(new InternalFrameListener() {
			@Override
			public void internalFrameActivated(InternalFrameEvent e) {
				/*
				 * Nothing to do.
				 */
			}
			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				/*
				 * Nothing to do.
				 */
			}
			@Override
			public void internalFrameClosing(InternalFrameEvent e) {
				m_scanner.remove_listener(m_listener);
				dispose();
				if (m_scanner.state() == RmiScanner.PAUSED
						|| m_scanner.state() == RmiScanner.SCANNING) {
					m_scanner.stop();
				}
			}
			@Override
			public void internalFrameDeactivated(InternalFrameEvent e) {
				/*
				 * Nothing to do.
				 */
			}
			@Override
			public void internalFrameDeiconified(InternalFrameEvent e) {
				/*
				 * Nothing to do.
				 */
			}
			@Override
			public void internalFrameIconified(InternalFrameEvent e) {
				/*
				 * Nothing to do.
				 */
			}
			@Override
			public void internalFrameOpened(InternalFrameEvent e) {
				/*
				 * Nothing to do.
				 */
			}
		});
	}
	
	/**
	 * Sets up the component state.
	 */
	private void setup() {
		m_last_port_scan_label.setText("Scanned: " + m_last_port_scanned);
		m_client_count_label.setText("Clients found: " + m_client_count);
		
		boolean progress_known = false;
		
		switch (m_scanner.state()) {
			case RmiScanner.FINISHED:
				m_status_label.setText("Scan status: Finished");
				progress_known = false;
				break;
			case RmiScanner.PAUSED:
				m_status_label.setText("Scan status: Paused");
				progress_known = true;
				break;
			case RmiScanner.SCANNING:
				m_status_label.setText("Scan status: Scanning");
				progress_known = true;
				break;
			case RmiScanner.STOPPED:
				m_status_label.setText("Scan status: Stopped");
				progress_known = false;
				break;
			default:
				Ensure.unreachable();
		}
		
		if (progress_known) {
			m_scan_progress.setEnabled(true);
			m_scan_progress.setMinimum(0);
			m_scan_progress.setMaximum(m_scanner.max_port()
					- m_scanner.min_port() + 1);
			m_scan_progress.setValue(m_last_port_scanned == 0? 0 :
				m_last_port_scanned - m_scanner.min_port() + 1);
		} else {
			m_scan_progress.setEnabled(false);
			m_scan_progress.setMinimum(0);
			m_scan_progress.setMaximum(1);
			m_scan_progress.setValue(1);
		}
	}

	@Override
	public ActionContext getActionContext() {
		return m_action_context;
	}
}
