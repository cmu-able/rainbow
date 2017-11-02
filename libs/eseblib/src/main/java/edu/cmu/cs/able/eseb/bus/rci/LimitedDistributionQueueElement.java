package edu.cmu.cs.able.eseb.bus.rci;

import incubator.pval.Ensure;

import java.io.Serializable;
import java.util.Date;

/**
 * Single element in a {@link LimitedDistributionQueue}. This represents a
 * distributed event in the bus. It is used to keep information about the
 * event even after it has been distributed.
 */
public class LimitedDistributionQueueElement implements Serializable {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Distribution date.
	 */
	private Date m_date;
	
	/**
	 * Distributed contents.
	 */
	private byte[] m_contents;
	
	/**
	 * Distributed connection ID.
	 */
	private int m_connection_id;
	
	/**
	 * Creates a new queue element.
	 * @param date the distribution date
	 * @param contents the event itself (may be <code>null</code>); for
	 * performance this byte is kept as is in the object, it is not cloned
	 * @param connection_id the ID of the connection that distributed the event
	 */
	public LimitedDistributionQueueElement(Date date, byte[] contents,
			int connection_id) {
		Ensure.not_null(date);
		Ensure.not_null(contents);
		
		m_date = date;
		m_contents = contents;
		m_connection_id = connection_id;
	}
	
	/**
	 * Obtains the date in which the event was distributed.
	 * @return the date
	 */
	public Date date() {
		return m_date;
	}
	
	/**
	 * Obtains the contents of the event.
	 * @return the contents; for performance reasons, this array is not
	 * cloned, is a direct reference to the array kept in the object
	 */
	public byte[] contents() {
		return m_contents;
	}
	
	/**
	 * Obtains the ID of the connection that distributed the event.
	 * @return the connection ID
	 */
	public int client_id() {
		return m_connection_id;
	}
}
