package edu.cmu.cs.able.eseb.ui.cli;

import incubator.exh.ThrowableCollector;
import incubator.ui.RegexValidationTextField;
import incubator.ui.RegexValidationTextFieldListener;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;

import edu.cmu.cs.able.eseb.BusClient;
import edu.cmu.cs.able.eseb.BusData;
import edu.cmu.cs.able.eseb.BusDataQueue;
import edu.cmu.cs.able.eseb.BusDataQueueListener;
import edu.cmu.cs.able.eseb.ui.ConnectionStateComponent;
import edu.cmu.cs.able.eseb.ui.EventListViewComponent;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Main user interface for the client.
 */
@SuppressWarnings("serial")
public class ClientFrame extends JFrame {
	/**
	 * The bus client.
	 */
	private BusClient m_client;
	
	/**
	 * Text field with the bus hostname. 
	 */
	private RegexValidationTextField m_bus_host;
	
	/**
	 * Text field with the bus port.
	 */
	private RegexValidationTextField m_bus_port;
	
	/**
	 * Connect button.
	 */
	private JButton m_connect;
	
	/**
	 * Disconnect button.
	 */
	private JButton m_disconnect;
	
	/**
	 * Exception collector.
	 */
	private ThrowableCollector m_collector;
	
	/**
	 * The types primitive scope
	 */
	private PrimitiveScope m_primitive_scope;
	
	/**
	 * The connection state.
	 */
	private ConnectionStateComponent m_connection_state;
	
	/**
	 * Button used to send data to the bus.
	 */
	private JButton m_send_data;
	
	/**
	 * Component with list of events.
	 */
	private EventListViewComponent m_event_list;
	
	/**
	 * Queue used to receive events.
	 */
	private BusDataQueue m_queue;
	
	/**
	 * Creates a new frame.
	 */
	public ClientFrame() {
		super("eseb client");
		
		m_client = null;
		m_collector = new ThrowableCollector("ClientFrame");
		m_primitive_scope = new PrimitiveScope();
		m_queue = new BusDataQueue();
		
		setup_ui();
		
		setVisible(true);
	}
	
	/**
	 * Sets the client's user interface controls.
	 */
	private void setup_ui() {
		addWindowListener(new WindowListener() {
			@Override public void windowOpened(WindowEvent e) { /**/ }
			@Override public void windowIconified(WindowEvent e) { /**/ }
			@Override public void windowDeiconified(WindowEvent e) { /**/ }
			@Override public void windowDeactivated(WindowEvent e) { /**/ }
			@Override public void windowClosed(WindowEvent e) { /**/ }
			@Override public void windowActivated(WindowEvent e) { /**/ }
			@Override
			public void windowClosing(WindowEvent e) {
				do_quit();
			}
		});
		
		setLayout(new BorderLayout());
		
		JPanel connect_panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		add(connect_panel, BorderLayout.NORTH);
		
		connect_panel.add(new JLabel("Bus Host:"));
		m_bus_host = new RegexValidationTextField(Pattern.compile("^\\S+$"),
				10);
		connect_panel.add(m_bus_host);
		connect_panel.add(new JSeparator());
		connect_panel.add(new JLabel("Bus Port:"));
		m_bus_port = new RegexValidationTextField(Pattern.compile("^\\d+$$"),
				5);
		connect_panel.add(m_bus_port);
		m_connect = new JButton("Connect");
		connect_panel.add(m_connect);
		m_disconnect = new JButton("Disconnect");
		connect_panel.add(m_disconnect);
		
		m_connection_state = new ConnectionStateComponent(null);
		add(m_connection_state, BorderLayout.SOUTH);
		m_connection_state.start();
		
		JSplitPane main_split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		add(main_split, BorderLayout.CENTER);
		
		m_send_data = new JButton("Send data to bus");
		main_split.add(m_send_data);
		m_event_list = new EventListViewComponent();
		main_split.add(m_event_list);
		main_split.setDividerLocation(0.5);
		
		m_bus_host.add_listener(new RegexValidationTextFieldListener() {
			@Override
			public void text_field_changed() {
				check_enables();
			}
		});
		
		m_bus_port.add_listener(new RegexValidationTextFieldListener() {
			@Override
			public void text_field_changed() {
				check_enables();
			}
		});
		
		m_connect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				do_connect();
			}
		});
		
		m_disconnect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				do_disconnect();
			}
		});
		
		m_send_data.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				do_send_data_to_bus();
			}
		});
		
		m_queue.dispatcher().add(new BusDataQueueListener() {
			@Override
			public void data_added_to_queue() {
				BusData bd = null;
				while ((bd = m_queue.poll()) != null) {
					final DataValue v = bd.value();
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							m_event_list.add(v);
						}
					});
				}
			}
		});
		
		check_enables();
		
		pack();
	}
	
	/**
	 * Checks which controls need to be enabled and which should not.
	 */
	private void check_enables() {
		if (m_bus_host.is_valid() && m_bus_port.is_valid()) {
			m_connect.setEnabled(true);
		} else {
			m_connect.setEnabled(false);
		}
		
		if (m_client != null) {
			m_disconnect.setEnabled(true);
			m_send_data.setEnabled(true);
		} else {
			m_disconnect.setEnabled(false);
			m_send_data.setEnabled(false);
		}
	}
	
	/**
	 * Invoked to terminate the client.
	 */
	private void do_quit() {
		do_disconnect();
		m_connection_state.stop();
		dispose();
	}
	
	/**
	 * Connects to the event bus.
	 */
	private void do_connect() {
		do_disconnect();
		
		m_client = new BusClient(m_bus_host.getText(), Short.parseShort(
				m_bus_port.getText()), m_primitive_scope);
		m_connection_state.set_client(m_client);
		m_client.start();
		m_client.queue_group().add(m_queue);
		check_enables();
	}
	
	/**
	 * Disconnects from the event bus if we're connected.
	 */
	private void do_disconnect() {
		try {
			if (m_client != null) {
				m_client.close();
			}
		} catch (IOException e) {
			m_collector.collect(e, "Closing client connection");
		} finally {
			m_client = null;
		}
		
		m_connection_state.set_client(null);
		check_enables();
	}
	
	/**
	 * Sends all pending events to the event bus.
	 */
	private void do_send_data_to_bus() {
		m_client.send(m_primitive_scope.int64().make(
				System.currentTimeMillis()));
	}
	
	/**
	 * Test method.
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		new ClientFrame();
	}
}
