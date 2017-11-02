package edu.cmu.cs.able.eseb.bus;

import incubator.pval.Ensure;
import incubator.wt.CloseableListener;

import java.net.InetAddress;
import java.util.Date;

import edu.cmu.cs.able.eseb.BusDataQueue;
import edu.cmu.cs.able.eseb.ControlledDataTypeSocketConnection;
import edu.cmu.cs.able.eseb.filter.EventFilterChainInfo;

/**
 * Class with data kept by the bus server for each connection.
 */
public class EventBusConnectionData {
	/**
	 * The client ID.
	 */
	private int m_id;
	
	/**
	 * The client's address.
	 */
	private InetAddress m_address;
	
	/**
	 * The client's connection.
	 */
	private ControlledDataTypeSocketConnection m_connection;
	
	/**
	 * Time at which connection as established.
	 */
	private Date m_connect_time;
	
	/**
	 * Number of sent messages.
	 */
	private int m_publish_count;
	
	/**
	 * Number of received messages.
	 */
	private int m_subscribe_count;
	
	/**
	 * Queue used to receive data from the connection.
	 */
	private BusDataQueue m_input_queue;
	
	/**
	 * The closeable listener registered with the connection
	 */
	private CloseableListener m_closeable_listener;
	
	/**
	 * Creates a new client data structure.
	 * @param id the connection ID
	 * @param address the client's address
	 * @param connection the connection used to communicate with the client
	 * @param bdq queue used to receive data from the client
	 * @param cl the closeable listener registered with the connection
	 */
	EventBusConnectionData(int id, InetAddress address,
			ControlledDataTypeSocketConnection connection,
			BusDataQueue bdq, CloseableListener cl) {
		Ensure.greater(id, 0);
		Ensure.not_null(address);
		Ensure.not_null(connection);
		Ensure.not_null(bdq);
		Ensure.not_null(cl);
		
		m_id = id;
		m_address = address;
		m_connection = connection;
		m_connect_time = new Date();
		m_publish_count = 0;
		m_subscribe_count = 0;
		m_input_queue = bdq;
		m_closeable_listener = cl;
	}
	
	/**
	 * Obtains the connection's ID.
	 * @return the ID
	 */
	public int id() {
		return m_id;
	}
	
	/**
	 * Obtains the client's address.
	 * @return the address
	 */
	public InetAddress address() {
		return m_address;
	}
	
	/**
	 * Obtains the connection.
	 * @return the connection
	 */
	ControlledDataTypeSocketConnection connection() {
		return m_connection;
	}
	
	/**
	 * Obtains the time at which the client connected.
	 * @return the time
	 */
	public Date connect_time() {
		return m_connect_time;
	}
	
	/**
	 * Obtains the number of messages published by the connection.
	 * @return the number of messages
	 */
	public synchronized int publish_count() {
		return m_publish_count;
	}
	
	/**
	 * Obtains the number of messages subscribed by the client.
	 * @return the number of messages
	 */
	public synchronized int subscribe_count() {
		return m_subscribe_count;
	}
	
	/**
	 * Increases the connection's publish count.
	 */
	public synchronized void sent() {
		m_publish_count++;
	}
	
	/**
	 * Increases the connection subscribed count.
	 */
	public synchronized void received() {
		m_subscribe_count++;
	}
	
	/**
	 * Obtains the input queue used to receive data from the connection.
	 * @return the queue
	 */
	BusDataQueue input_queue() {
		return m_input_queue;
	}
	
	/**
	 * Obtains the closeable listener associated with the connection.
	 * @return the listener
	 */
	CloseableListener closeable_listener() {
		return m_closeable_listener;
	}
	
	/**
	 * Obtains the connection's incoming chain.
	 * @return the incoming chain
	 */
	public EventFilterChainInfo incoming_chain() {
		return m_connection.incoming_chain().info();
	}
	
	/**
	 * Obtains the connection's outgoing chain.
	 * @return the outgoing chain
	 */
	public EventFilterChainInfo outgoing_chain() {
		return m_connection.outgoing_chain().info();
	}
}
