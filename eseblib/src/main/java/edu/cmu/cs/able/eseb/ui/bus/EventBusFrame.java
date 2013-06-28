package edu.cmu.cs.able.eseb.ui.bus;

import incubator.ctxaction.ActionContext;
import incubator.pval.Ensure;
import incubator.rmi.RmiClientDiscovery;
import incubator.scb.ScbEditableContainerImpl;
import incubator.scb.sync.SyncScbMaster;
import incubator.scb.sync.SyncScbSlave;
import incubator.scb.sync.ui.SlaveSynchronizationStatusComponent;
import incubator.scb.ui.ScbAutoTableModel;
import incubator.scb.ui.ScbTableScrollable;
import incubator.ui.MainApplicationFrame;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import edu.cmu.cs.able.eseb.bus.rci.EventBusRemoteConnectionInfo;
import edu.cmu.cs.able.eseb.bus.rci.EventBusRemoteControlInterface;
import edu.cmu.cs.able.eseb.bus.rci.SyncConstants;

/**
 * Frame that shows information from an event bus.
 */
@SuppressWarnings("serial")
public class EventBusFrame extends JInternalFrame {
	/**
	 * Interval, in milliseconds, between data poll.
	 */
	private static final long POLL_INTERVAL_MS = 500;
	
	/**
	 * The remote interface.
	 */
	private EventBusRemoteControlInterface m_ri;
	
	/**
	 * Data with all connections.
	 */
	private ScbEditableContainerImpl<EventBusRemoteConnectionInfo> m_conn_data;
	
	/**
	 * Synchronization slave.
	 */
	private SyncScbSlave m_sync_slave;
	
	/**
	 * Component showing the synchronization status.
	 */
	private SlaveSynchronizationStatusComponent m_sync_status;
	
	/**
	 * Creates a new frame.
	 * @param host the host name
	 * @param port the port number
	 * @param r the server remote interface
	 * @param maf the main application frame
	 */
	public EventBusFrame(String host, short port,
			EventBusRemoteControlInterface r, MainApplicationFrame maf) {
		super("Event bus @" + Ensure.not_null(host) + ":" + port,
				true, true, true, true);
		
		Ensure.not_null(host);
		Ensure.greater(port, 0);
		Ensure.not_null(r);
		Ensure.not_null(maf);
		
		m_ri = r;
		
		setup_data_container(host);
		setup_ui();
		setup_actions(host, port, maf);
		pack();
		setVisible(true);
		
		addInternalFrameListener(new InternalFrameListener() {
			@Override
			public void internalFrameOpened(InternalFrameEvent arg0) {
				/* */
			}
			
			@Override
			public void internalFrameIconified(InternalFrameEvent arg0) {
				/* */
			}
			
			@Override
			public void internalFrameDeiconified(InternalFrameEvent arg0) {
				/* */
			}
			
			@Override
			public void internalFrameDeactivated(InternalFrameEvent arg0) {
				/* */
			}
			
			@Override
			public void internalFrameClosing(InternalFrameEvent arg0) {
				/* */
			}
			
			@Override
			public void internalFrameClosed(InternalFrameEvent arg0) {
				m_sync_slave.shutdown();
				m_sync_status.shutdown();
			}
			
			@Override
			public void internalFrameActivated(InternalFrameEvent arg0) {
				/* */
			}
		});
	}
	
	/**
	 * Sets up the user interface.
	 */
	private void setup_ui() {
		setLayout(new BorderLayout());
		JPanel contents_panel = new JPanel();
		add(contents_panel, BorderLayout.CENTER);
		
		contents_panel.setLayout(new BorderLayout());
		JPanel port_panel_container = new JPanel();
		contents_panel.add(port_panel_container, BorderLayout.NORTH);
		JPanel table_panel = new JPanel();
		contents_panel.add(table_panel, BorderLayout.CENTER);
		
		port_panel_container.setLayout(new FlowLayout(FlowLayout.LEFT));
		JPanel accept_port_panel = new JPanel();
		port_panel_container.add(accept_port_panel);
		JPanel data_port_panel = new JPanel();
		port_panel_container.add(data_port_panel);
		
		accept_port_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		accept_port_panel.add(new JLabel("Accept port: " + m_ri.port()));
		
		data_port_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		data_port_panel.add(new JLabel("Data port: "
					+ m_ri.data_master_port()));
		
		ScbAutoTableModel<EventBusRemoteConnectionInfo, Integer> model;
		model = new ScbAutoTableModel<>(m_conn_data,
				EventBusRemoteConnectionInfo.c_fields(),
				EventBusRemoteConnectionInfo.c_id_field());
		ScbTableScrollable<EventBusRemoteConnectionInfo> table
				= new ScbTableScrollable<>(model);
		table_panel.setLayout(new BorderLayout());
		table_panel.add(table, BorderLayout.CENTER);
		
		m_sync_status = new SlaveSynchronizationStatusComponent(m_sync_slave);
		add(m_sync_status, BorderLayout.SOUTH);
	}
	
	/**
	 * Sets up actions.
	 * @param host the host we're connected to
	 * @param port the port we're connected to
	 * @param maf the main application frame
	 */
	private void setup_actions(String host, short port,
			MainApplicationFrame maf) {
		ShowBusEventsAction seba = new ShowBusEventsAction(host, port,
				m_ri, maf);
		seba.bind(new ActionContext());
		
		JToolBar toolbar = new JToolBar();
		add(toolbar, BorderLayout.NORTH);
		toolbar.add(seba.createJButton(false));
	}
	
	/**
	 * Sets up the container with connection information synchronized with
	 * the master.
	 * @param host the event bus host
	 */
	private void setup_data_container(String host) {
		m_conn_data = new ScbEditableContainerImpl<>();
		SyncScbMaster master = RmiClientDiscovery.find_rmi_client(host,
				m_ri.data_master_port(), SyncScbMaster.class);
		Ensure.not_null(master);
		m_sync_slave = new SyncScbSlave(master, POLL_INTERVAL_MS);
		
		m_sync_slave.add_container(SyncConstants.CONTAINER_KEY, m_conn_data,
				Integer.class, EventBusRemoteConnectionInfo.class);
	}
}
