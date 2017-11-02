package edu.cmu.cs.able.eseb.ui.bus;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.ActionContextProvider;
import incubator.pval.Ensure;
import incubator.rmi.RmiClientDiscovery;
import incubator.scb.ScbContainerListener;
import incubator.scb.ScbEditableContainerImpl;
import incubator.scb.ScbEnumField;
import incubator.scb.ScbField;
import incubator.scb.ScbIntegerField;
import incubator.scb.sync.SyncScbMaster;
import incubator.scb.sync.SyncScbSlave;
import incubator.scb.sync.ui.SlaveSynchronizationStatusComponent;
import incubator.scb.ui.ScbAutoTableModel;
import incubator.scb.ui.ScbTableScrollable;
import incubator.ui.MainApplicationFrame;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.cmu.cs.able.eseb.bus.rci.BlockingStatus;
import edu.cmu.cs.able.eseb.bus.rci.EventBusRemoteConnectionInfo;
import edu.cmu.cs.able.eseb.bus.rci.EventBusRemoteControlInterface;
import edu.cmu.cs.able.eseb.bus.rci.SyncConstants;
import edu.cmu.cs.able.eseb.filter.EventFilterInfo;

/**
 * Frame that shows information from an event bus.
 */
@SuppressWarnings("serial")
public class EventBusFrame extends JInternalFrame
		implements ActionContextProvider {
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
	 * Context of the frame.
	 */
	private ActionContext m_event_bus_context;
	
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
		m_event_bus_context = new ActionContext();
		m_event_bus_context.set(RemoteConnectionAction.REMOTE_INTERFACE_KEY,
				r);
		
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
		JPanel data_panel = new JPanel();
		contents_panel.add(data_panel, BorderLayout.CENTER);
		
		data_panel.setLayout(new BorderLayout());
		final JSplitPane data_split_pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		data_panel.add(data_split_pane, BorderLayout.CENTER);
		
		JPanel table_panel = new JPanel();
		data_split_pane.add(table_panel);
		JPanel filters_panel = new JPanel();
		data_split_pane.add(filters_panel);
		
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				data_split_pane.setDividerLocation(0.5);
			}
		});
		
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
		model.add_field_auto(create_incoming_filter_count_field());
		model.add_field_auto(create_outgoing_filter_count_field());
		model.add_field_auto(create_incoming_blocking_status_field());
		model.add_field_auto(create_outgoing_blocking_status_field());
		final ScbTableScrollable<EventBusRemoteConnectionInfo> table
				= new ScbTableScrollable<>(model);
		table_panel.setLayout(new BorderLayout());
		table_panel.add(table, BorderLayout.CENTER);
		
		filters_panel.setLayout(new GridLayout(1, 2));
		JPanel left_filters_panel = new JPanel();
		filters_panel.add(left_filters_panel);
		JPanel right_filters_panel = new JPanel();
		filters_panel.add(right_filters_panel);
		
		left_filters_panel.setLayout(new BorderLayout());
		left_filters_panel.setBorder(new TitledBorder(new EtchedBorder(),
				"Incoming Chain"));
		
		final ScbAutoTableModel<EventFilterInfo, Integer> left_model;
		left_model = new ScbAutoTableModel<>(null,
				EventFilterInfo.c_fields(), EventFilterInfo.c_index_field());
		ScbTableScrollable<EventFilterInfo> left_table =
				new ScbTableScrollable<>(left_model);
		left_filters_panel.add(left_table, BorderLayout.CENTER);
		
		right_filters_panel.setLayout(new BorderLayout());
		right_filters_panel.setBorder(new TitledBorder(new EtchedBorder(),
				"Outgoing Chain"));
		
		final ScbAutoTableModel<EventFilterInfo, Integer> right_model;
		right_model = new ScbAutoTableModel<>(null,
				EventFilterInfo.c_fields(), EventFilterInfo.c_index_field());
		ScbTableScrollable<EventFilterInfo> right_table =
				new ScbTableScrollable<>(right_model);
		right_filters_panel.add(right_table, BorderLayout.CENTER);
		
		m_sync_status = new SlaveSynchronizationStatusComponent(m_sync_slave);
		add(m_sync_status, BorderLayout.SOUTH);
		
		table.table().getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					@Override
			public void valueChanged(ListSelectionEvent arg0) {
				EventBusRemoteConnectionInfo ci = table.table().selected();
				m_event_bus_context.set(
						RemoteConnectionAction.REMOTE_CONNECTION_KEY, ci);
				if (ci == null) {
					left_model.switch_container(null);
					right_model.switch_container(null);
				} else {
					Ensure.not_null(ci.incoming_chain());
					left_model.switch_container(ci.incoming_chain());
					Ensure.not_null(ci.outgoing_chain());
					right_model.switch_container(ci.outgoing_chain());
				}
			}
		});
		
		/*
		 * Make sure we clear and set the object in context when it changes.
		 */
		m_conn_data.dispatcher().add(
				new ScbContainerListener<EventBusRemoteConnectionInfo>() {
			@Override
			public void scb_updated(final EventBusRemoteConnectionInfo t) {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (t == table.table().selected()) {
							m_event_bus_context.set(
									RemoteConnectionAction.
									REMOTE_CONNECTION_KEY, null);
							m_event_bus_context.set(
									RemoteConnectionAction.
									REMOTE_CONNECTION_KEY, t);
							left_model.switch_container(t.incoming_chain());
							right_model.switch_container(t.outgoing_chain());
						}
					}
				});
			}
			
			@Override
			public void scb_removed(EventBusRemoteConnectionInfo t) {
				/* */
			}
			
			@Override
			public void scb_added(EventBusRemoteConnectionInfo t) {
				/* */
			}
		});
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
	
	/**
	 * Creates a field that obtains the number of filters in the incoming
	 * chain.
	 * @return the field
	 */
	private static ScbField<EventBusRemoteConnectionInfo, Integer>
			create_incoming_filter_count_field() {
		return new ScbIntegerField<EventBusRemoteConnectionInfo>(
				"I-Chain #", false, null) {
			@Override
			public void set(EventBusRemoteConnectionInfo t,
					Integer value) {
				Ensure.unreachable();
			}

			@Override
			public Integer get(EventBusRemoteConnectionInfo t) {
				Ensure.not_null(t);
				return t.incoming_chain().filters().size();
			}
		};
	}
	
	/**
	 * Creates a field that obtains the number of filters in the outgoing
	 * chain.
	 * @return the field
	 */
	private static ScbField<EventBusRemoteConnectionInfo, Integer>
			create_outgoing_filter_count_field() {
		return new ScbIntegerField<EventBusRemoteConnectionInfo>(
				"O-Chain #", false, null) {
			@Override
			public void set(EventBusRemoteConnectionInfo t,
					Integer value) {
				Ensure.unreachable();
			}

			@Override
			public Integer get(EventBusRemoteConnectionInfo t) {
				Ensure.not_null(t);
				return t.outgoing_chain().filters().size();
			}
		};
	}
	
	/**
	 * Creates a field that obtains the blocking status of the incoming chain.
	 * @return the field
	 */
	private static ScbEnumField<EventBusRemoteConnectionInfo, BlockingStatus>
			create_incoming_blocking_status_field() {
		return new ScbEnumField<EventBusRemoteConnectionInfo, BlockingStatus>(
				"Incoming Block", false, null, BlockingStatus.class) {
			@Override
			public void set(EventBusRemoteConnectionInfo t,
					BlockingStatus value) {
				Ensure.unreachable();
			}
			
			@Override
			public BlockingStatus get(EventBusRemoteConnectionInfo t) {
				return BlockingStatus.status_of(t.incoming_chain());
			}
		};
	}
	
	/**
	 * Creates a field that obtains the blocking status of the outgoing chain.
	 * @return the field
	 */
	private static ScbEnumField<EventBusRemoteConnectionInfo, BlockingStatus>
			create_outgoing_blocking_status_field() {
		return new ScbEnumField<EventBusRemoteConnectionInfo, BlockingStatus>(
				"Outgoing Block", false, null, BlockingStatus.class) {
			@Override
			public void set(EventBusRemoteConnectionInfo t,
					BlockingStatus value) {
				Ensure.unreachable();
			}
			
			@Override
			public BlockingStatus get(EventBusRemoteConnectionInfo t) {
				return BlockingStatus.status_of(t.outgoing_chain());
			}
		};
	}

	@Override
	public ActionContext getActionContext() {
		return m_event_bus_context;
	}
}
