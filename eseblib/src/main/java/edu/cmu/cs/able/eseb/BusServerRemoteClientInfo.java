package edu.cmu.cs.able.eseb;

import incubator.pval.Ensure;

import java.io.Serializable;
import java.util.Date;

/**
 * Class with remote information about a client.
 */
public class BusServerRemoteClientInfo implements Serializable {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Client ID.
	 */
	private int m_client_id;
	
	/**
	 * Client address.
	 */
	private String m_inet_address;
	
	/**
	 * When did the client connect?
	 */
	private Date m_connect_date;
	
	/**
	 * Number of events published to the server.
	 */
	private int m_publish_count;
	
	/**
	 * Number of events subscribed from the server.
	 */
	private int m_subscribe_count;
	
	/**
	 * Creates a new structure with information about a client.
	 * @param client_id
	 * @param inet_address
	 * @param connect_date
	 * @param publish_count
	 * @param subscribe_count
	 */
	public BusServerRemoteClientInfo(int client_id, String inet_address,
			Date connect_date, int publish_count, int subscribe_count) {
		Ensure.not_null(inet_address);
		Ensure.not_null(connect_date);
		Ensure.greater_equal(publish_count, 0);
		Ensure.greater_equal(subscribe_count, 0);
		
		m_client_id = client_id;
		m_inet_address = inet_address;
		m_connect_date = connect_date;
		m_publish_count = publish_count;
		m_subscribe_count = subscribe_count;
	}
	
	/**
	 * Obtains the client ID.
	 * @return the client ID
	 */
	public int client_id() {
		return m_client_id;
	}
	
	/**
	 * Obtains the IP address of the client.
	 * @return the IP address
	 */
	public String inet_address() {
		return m_inet_address;
	}
	
	/**
	 * Obtains the client's connect date.
	 * @return the connect date
	 */
	public Date connect_date() {
		return m_connect_date;
	}
	
	/**
	 * Obtains the number of events published by the client.
	 * @return the number of events
	 */
	public int publish_count() {
		return m_publish_count;
	}
	
	/**
	 * Obtains the number of events received by the client.
	 * @return the number of events
	 */
	public int subscribe_count() {
		return m_subscribe_count;
	}
}
