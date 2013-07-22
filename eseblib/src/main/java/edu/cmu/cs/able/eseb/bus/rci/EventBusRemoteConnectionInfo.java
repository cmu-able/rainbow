package edu.cmu.cs.able.eseb.bus.rci;

import incubator.pval.Ensure;
import incubator.scb.ScbDateField;
import incubator.scb.ScbField;
import incubator.scb.ScbIntegerField;
import incubator.scb.ScbTextField;
import incubator.scb.sync.SyncScb;
import incubator.scb.sync.SyncStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.cmu.cs.able.eseb.filter.EventFilterChainInfo;

/**
 * Class with remote information about a connection.
 */
public class EventBusRemoteConnectionInfo
		extends SyncScb<Integer, EventBusRemoteConnectionInfo> {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Connection ID.
	 */
	private int m_connection_id;
	
	/**
	 * Connection address.
	 */
	private String m_inet_address;
	
	/**
	 * When was the connection established?
	 */
	private Date m_connect_date;
	
	/**
	 * Number of events published to the bus.
	 */
	private int m_publish_count;
	
	/**
	 * Number of events subscribed from the bus.
	 */
	private int m_subscribe_count;
	
	/**
	 * Information about the connection's incoming chain.
	 */
	private EventFilterChainInfo m_incoming_chain;
	
	/**
	 * Information about the connection's outgoing chain.
	 */
	private EventFilterChainInfo m_outgoing_chain;
	
	/**
	 * Creates a new structure with information about a connection.
	 * @param connection_id the ID of the connection
	 * @param inet_address the connection's address
	 * @param connect_date the date in which the connection started
	 * @param publish_count the number of published events
	 * @param subscribe_count the number of subscribed events
	 * @param incoming_chain the connection's incoming chain
	 * @param outgoing_chain the connection's outgoing chain
	 */
	public EventBusRemoteConnectionInfo(int connection_id, String inet_address,
			Date connect_date, int publish_count, int subscribe_count,
			EventFilterChainInfo incoming_chain,
			EventFilterChainInfo outgoing_chain) {
		super(connection_id, SyncStatus.UNKNOWN,
				EventBusRemoteConnectionInfo.class);
		
		Ensure.not_null(inet_address);
		Ensure.not_null(connect_date);
		Ensure.greater_equal(publish_count, 0);
		Ensure.greater_equal(subscribe_count, 0);
		Ensure.not_null(incoming_chain);
		Ensure.not_null(outgoing_chain);
		
		m_connection_id = connection_id;
		m_inet_address = inet_address;
		m_connect_date = connect_date;
		m_publish_count = publish_count;
		m_subscribe_count = subscribe_count;
		m_incoming_chain = incoming_chain;
		m_outgoing_chain = outgoing_chain;
	}
	
	/**
	 * Obtains the connection ID.
	 * @return the connection ID
	 */
	public int connection_id() {
		return m_connection_id;
	}
	
	/**
	 * Obtains the IP address of the connection.
	 * @return the IP address
	 */
	public String inet_address() {
		return m_inet_address;
	}
	
	/**
	 * Obtains when was the connection established.
	 * @return the connect date
	 */
	public Date connect_date() {
		return m_connect_date;
	}
	
	/**
	 * Obtains the number of events published by the connection.
	 * @return the number of events
	 */
	public int publish_count() {
		return m_publish_count;
	}
	
	/**
	 * Sets the number of events published by the connection.
	 * @param pc the number of events
	 */
	public void publish_count(int pc) {
		Ensure.greater_equal(pc, 0);
		if (m_publish_count != pc) {
			m_publish_count = pc;
			fire_update();
		}
	}
	
	/**
	 * Obtains the number of events received by the connection.
	 * @return the number of events
	 */
	public int subscribe_count() {
		return m_subscribe_count;
	}
	
	/**
	 * Sets the number of events subscribed by the connection.
	 * @param sc the number of events
	 */
	public void subscribe_count(int sc) {
		Ensure.greater_equal(sc, 0);
		if (m_subscribe_count != sc) {
			m_subscribe_count = sc;
			fire_update();
		}
	}
	
	/**
	 * Obtains the connection's incoming chain.
	 * @return the incoming chain
	 */
	public EventFilterChainInfo incoming_chain() {
		return m_incoming_chain;
	}
	
	/**
	 * Obtains the connection's outgoing chain.
	 * @return the outgoing chain
	 */
	public EventFilterChainInfo outgoing_chain() {
		return m_outgoing_chain;
	}
	
	/**
	 * Defines the connection's incoming chain.
	 * @param i the incoming chain.
	 */
	public void incoming_chain(EventFilterChainInfo i) {
		Ensure.not_null(i);
		if (!i.equals(m_incoming_chain)) {
			m_incoming_chain = i;
			fire_update();
		}
	}
	
	/**
	 * Defines the connection's outgoing chain.
	 * @param o the outgoing chain.
	 */
	public void outgoing_chain(EventFilterChainInfo o) {
		Ensure.not_null(o);
		if (!o.equals(m_outgoing_chain)) {
			m_outgoing_chain = o;
			fire_update();
		}
	}

	@Override
	protected void sync(EventBusRemoteConnectionInfo t) {
		Ensure.not_null(t);
		publish_count(t.publish_count());
		subscribe_count(t.subscribe_count());
		incoming_chain(t.incoming_chain());
		outgoing_chain(t.outgoing_chain());
	}
	
	@SuppressWarnings("javadoc")
	public static ScbIntegerField<EventBusRemoteConnectionInfo> c_id_field() {
		return new ScbIntegerField<EventBusRemoteConnectionInfo>("ID",
				false, null) {
			@Override
			public void set(EventBusRemoteConnectionInfo t,
					Integer value) {
				Ensure.unreachable();
			}

			@Override
			public Integer get(EventBusRemoteConnectionInfo t) {
				return t.m_connection_id;
			}
		};
	}
	
	@SuppressWarnings("javadoc")
	public static ScbTextField<EventBusRemoteConnectionInfo>
			c_address_field() {
		return new ScbTextField<EventBusRemoteConnectionInfo>("Address",
				false, null) {
			@Override
			public void set(EventBusRemoteConnectionInfo t,
					String value) {
				Ensure.unreachable();
			}

			@Override
			public String get(EventBusRemoteConnectionInfo t) {
				return t.m_inet_address;
			}
		};
	}
	
	@SuppressWarnings("javadoc")
	public static ScbDateField<EventBusRemoteConnectionInfo>
			c_connect_field() {
		return new ScbDateField<EventBusRemoteConnectionInfo>("Connected",
				false, null) {
			@Override
			public void set(EventBusRemoteConnectionInfo t, Date value) {
				Ensure.unreachable();
			}

			@Override
			public Date get(EventBusRemoteConnectionInfo t) {
				return t.m_connect_date;
			}
		};
	}
	
	@SuppressWarnings("javadoc")
	public static ScbIntegerField<EventBusRemoteConnectionInfo>
			c_publish_count_field() {
		return new ScbIntegerField<EventBusRemoteConnectionInfo>(
				"Publish Count", false, null) {
			@Override
			public void set(EventBusRemoteConnectionInfo t,
					Integer value) {
				Ensure.unreachable();
			}

			@Override
			public Integer get(EventBusRemoteConnectionInfo t) {
				return t.m_publish_count;
			}
		};
	}
	
	@SuppressWarnings("javadoc")
	public static ScbIntegerField<EventBusRemoteConnectionInfo>
			c_subscribe_count_field() {
		return new ScbIntegerField<EventBusRemoteConnectionInfo>(
				"Subscribe Count", false, null) {
			@Override
			public void set(EventBusRemoteConnectionInfo t,
					Integer value) {
				Ensure.unreachable();
			}

			@Override
			public Integer get(EventBusRemoteConnectionInfo t) {
				return t.m_subscribe_count;
			}
		};
	}
	
	@SuppressWarnings("javadoc")
	public static List<ScbField<EventBusRemoteConnectionInfo, ?>> c_fields() {
		List<ScbField<EventBusRemoteConnectionInfo, ?>> l = new ArrayList<>();
		l.add(c_id_field());
		l.add(c_address_field());
		l.add(c_connect_field());
		l.add(c_publish_count_field());
		l.add(c_subscribe_count_field());
		return l;
	}

	@Override
	public List<ScbField<EventBusRemoteConnectionInfo, ?>> fields() {
		return c_fields();
	}

	@Override
	protected Class<EventBusRemoteConnectionInfo> my_class() {
		return EventBusRemoteConnectionInfo.class;
	}
}
