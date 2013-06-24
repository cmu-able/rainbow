package incubator.il.ui;

import incubator.ctxaction.ActionContext;
import incubator.ctxaction.ActionContextProvider;
import incubator.il.IMutexRequest;
import incubator.il.srv.IMutexManagerRemoteAccess;
import incubator.ui.DataRefresher;
import incubator.ui.bean.BeanTable;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.ObjectUtils;

/**
 * Frame that shows information about a mutex manager.
 */
public class IMutexManagerFrame extends JInternalFrame
		implements ActionContextProvider {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Host where the manager was found.
	 */
	private String m_host;
	
	/**
	 * Port where the manager was found.
	 */
	private int m_port;
	
	/**
	 * Remote access.
	 */
	private IMutexManagerRemoteAccess m_remote;
	
	/**
	 * Selected mutex whose data is in the detail section.
	 */
	private String m_selected_mutex;
	
	/**
	 * Mutex table.
	 */
	private JTable m_mutex_table;
	
	/**
	 * Model for the mutex table.
	 */
	private MutexTableModel m_mutex_model;
	
	/**
	 * Table with the lock requests.
	 */
	private JTable m_request_table;
	
	/**
	 * Model with the lock requests.
	 */
	private MutexDataTableModel m_request_model;
	
	/**
	 * Lock acquisition trace.
	 */
	private JTextArea m_trace_area;
	
	/**
	 * Action execution context.
	 */
	private ActionContext m_action_context;
	
	/**
	 * Who is going to refresh the models?
	 */
	private DataRefresher m_refresher;
	
	/**
	 * Creates a new window.
	 * @param host the host we connect to
	 * @param port the port where the connection has been established
	 * @param remote the remote object
	 * @throws Exception init failed
	 */
	public IMutexManagerFrame(String host, int port,
			IMutexManagerRemoteAccess remote) throws Exception {
		super(remote.manager_name() + "@" + host + ":" + port);
		
		assert host != null;
		assert port > 0;
		assert remote != null;
		
		this.m_host = host;
		this.m_port = port;
		this.m_remote = remote;
		m_action_context = new ActionContext();
		
		init();
		
		m_refresher = new DataRefresher(500, true, m_action_context) {
			@Override
			public void refresh() {
				m_mutex_model.refresh_data();
				m_request_model.refresh_data();
			}
		};
		
		pack();
		setSize(new Dimension(500, 350));
		show();
	}
	
	/**
	 * Initializes the components.
	 * @throws Exception initialization failed
	 */
	private void init() throws Exception {
		getContentPane().setLayout(new BorderLayout());
		JPanel generalDataPanel = new JPanel();
		getContentPane().add(generalDataPanel, BorderLayout.NORTH);
		JPanel dataPanel = new JPanel();
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		
		generalDataPanel.setBorder(new TitledBorder(new EtchedBorder(),
				"General manager information"));
		generalDataPanel.setLayout(new GridLayout(3, 1));
		generalDataPanel.add(new JLabel("Host: " + m_host));
		generalDataPanel.add(new JLabel("Port: " + m_port));
		generalDataPanel.add(new JLabel("Name: " + m_remote.manager_name()));
		
		dataPanel.setLayout(new BorderLayout());
		JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		mainSplit.setResizeWeight(0.5);
		dataPanel.add(mainSplit);
		JScrollPane mutexListScroll = new JScrollPane();
		mainSplit.add(mutexListScroll);
		JSplitPane secondarySplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		secondarySplit.setResizeWeight(0.5);
		mainSplit.add(secondarySplit);
		JScrollPane requestScroll = new JScrollPane();
		secondarySplit.add(requestScroll);
		JScrollPane traceScroll = new JScrollPane();
		secondarySplit.add(traceScroll);
		
		mutexListScroll.setHorizontalScrollBarPolicy(
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		mutexListScroll.setVerticalScrollBarPolicy(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		m_mutex_model = new MutexTableModel(m_remote, m_action_context);
		m_mutex_table = new BeanTable(m_mutex_model);
		mutexListScroll.setViewportView(m_mutex_table);
		ListSelectionModel selmodel = m_mutex_table.getSelectionModel();
		selmodel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selmodel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				update_mutex_selection();
			}
		});
		
		requestScroll.setHorizontalScrollBarPolicy(
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		requestScroll.setVerticalScrollBarPolicy(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		m_request_model = new MutexDataTableModel(m_mutex_model, m_action_context);
		m_request_table = new BeanTable(m_request_model);
		requestScroll.setViewportView(m_request_table);
		ListSelectionModel reqselmodel = m_request_table.getSelectionModel();
		reqselmodel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		reqselmodel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				update_request_selection();
			}
		});
		
		traceScroll.setHorizontalScrollBarPolicy(
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		traceScroll.setVerticalScrollBarPolicy(
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		m_trace_area = new JTextArea();
		m_trace_area.setEditable(false);
		traceScroll.setViewportView(m_trace_area);
		
		setClosable(true);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		
		addInternalFrameListener(new InternalFrameListener() {
			@Override
			public void internalFrameActivated(InternalFrameEvent e) {
				/* */
			}
			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				/* */
			}
			@Override
			public void internalFrameClosing(InternalFrameEvent e) {
				dispose();
			}
			@Override
			public void internalFrameDeactivated(InternalFrameEvent e) {
				/* */
			}
			@Override
			public void internalFrameDeiconified(InternalFrameEvent e) {
				/* */
			}
			@Override
			public void internalFrameIconified(InternalFrameEvent e) {
				/* */
			}
			@Override
			public void internalFrameOpened(InternalFrameEvent e) {
				/* */
			}
		});
	}
	
	/**
	 * Method invoked every time there is a change in the mutex table
	 * selection.
	 */
	private void update_mutex_selection() {
		String selmutexname = null;
		int row = m_mutex_table.getSelectedRow();
		if (row >= 0) {
			selmutexname = m_mutex_model.getMutexNameAt(row);
		}
		
		if (ObjectUtils.equals(m_selected_mutex, selmutexname)) {
			return;
		}
		
		m_selected_mutex = selmutexname;
		m_request_model.set_mutex(selmutexname);
	}
	
	@Override
	public ActionContext getActionContext() {
		return m_action_context;
	}
	
	/**
	 * Method invoked every time a lock request table selection is changed.
	 */
	private void update_request_selection() {
		int selectedRequest = m_request_table.getSelectedRow();
		if (selectedRequest >= 0) {
			IMutexRequest req = m_request_model.request(selectedRequest);
			m_trace_area.setText(req.acquisition_trace());
		}
	}
	
	@Override
	public void dispose() {
		m_refresher.refresh_rate(0);
		super.dispose();
	}
}
