package edu.cmu.cs.able.eseb.ui;

import incubator.ui.ColorCircles;
import incubator.ui.ColorCircles.Color;
import incubator.wt.WorkerThread;

import java.awt.EventQueue;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.cmu.cs.able.eseb.conn.BusConnectionListener;
import edu.cmu.cs.able.eseb.conn.BusConnectionState;
import edu.cmu.cs.able.eseb.conn.BusConnection;

/**
 * Component that shows the state of a client bus connection.
 */
@SuppressWarnings("serial")
public class ConnectionStateComponent extends JPanel {
	/**
	 * The connection.
	 */
	private BusConnection m_client;
	
	/**
	 * Current state.
	 */
	private BusConnectionState m_state;
	
	/**
	 * Label with text.
	 */
	private JLabel m_text_label;
	
	/**
	 * Label with icon.
	 */
	private JLabel m_icon_label;
	
	/**
	 * Connection is OK.
	 */
	private ImageIcon m_connected_icon;
	
	/**
	 * We're trying to connect.
	 */
	private ImageIcon m_trying_icon;
	
	/**
	 * Connection is not established
	 */
	private ImageIcon m_bad_icon;
	
	/**
	 * No client icon.
	 */
	private ImageIcon m_no_client_icon;
	
	/**
	 * Client listener.
	 */
	private BusConnectionListener m_listener;
	
	/**
	 * The worker thread.
	 */
	private WorkerThread m_wthread;
	
	/**
	 * Creates a new component.
	 * @param client the bus client (<code>null</code> if no client)
	 */
	public ConnectionStateComponent(BusConnection client) {
		m_client = null;
		m_state = null;
		m_bad_icon = ColorCircles.get_icon(Color.RED, 16);
		m_connected_icon = ColorCircles.get_icon(Color.GREEN, 16);
		m_no_client_icon = ColorCircles.get_icon(Color.GREY, 16);
		m_trying_icon = ColorCircles.get_icon(Color.YELLOW, 16);
		
		m_text_label = new JLabel();
		m_icon_label = new JLabel();
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(m_icon_label);
		add(m_text_label);
		
		m_listener = new BusConnectionListener() {
			@Override
			public void connection_state_changed() {
				review_state();
			}
		};
		
		set_client(client);
		
		m_wthread = new WorkerThread("Connection state component") {
			@Override
			protected void do_cycle_operation() throws Exception {
				synchronized (this) {
					review_state();
					wait(250);
				}
			}
		};
	}
	
	/**
	 * Starts this component.
	 */
	public void start() {
		m_wthread.start();
	}
	
	/**
	 * Stops this component.
	 */
	public void stop() {
		m_wthread.stop();
	}
	
	/**
	 * Sets the client to be monitored by this component.
	 * @param client the client or <code>null</code> if none
	 */
	public synchronized void set_client(BusConnection client) {
		if (m_client != null) {
			m_client.remove_listener(m_listener);
		}
		
		m_client = client;
		if (m_client != null) {
			m_client.add_listener(m_listener);
		}
		
		review_state();
	}
	
	/**
	 * Updated the component's text depending on the component state.
	 */
	private synchronized void review_state() {
		if (m_client != null) {
			m_state = m_client.state();
		} else {
			m_state = null;
		}
		
		ImageIcon icn = null;
		String text = null;
		
		if (m_state == null) {
			icn = m_no_client_icon;
			text = "No connection.";
		} else {
			String hp = m_client.host() + ":" + m_client.port();
			if (m_state == BusConnectionState.CONNECTED) {
				icn = m_connected_icon;
				text = "Connected to " + hp;
			} else if (m_state == BusConnectionState.CONNECTING) {
				icn = m_trying_icon;
				text = "Connecting to " + hp;
			} else if (m_state == BusConnectionState.DISCONNECTED) {
				icn = m_bad_icon;
				text = "Disconnected from " + hp;
			}
			
			text += ". Connection count: " + m_client.connect_count()
					+ ". Messages: " + m_client.sent_count()
					+ "/" + m_client.receive_count() + ".";
		}
		
		final String set_text = text;
		final ImageIcon set_icon = icn;
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				m_text_label.setText(set_text);
				m_icon_label.setIcon(set_icon);
			}
		});
	}
}
