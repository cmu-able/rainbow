package edu.cmu.cs.able.eseb.participant;

import incubator.exh.LocalCollector;
import incubator.pval.Ensure;
import incubator.wt.WorkerThread;

import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;

import edu.cmu.cs.able.eseb.BusData;
import edu.cmu.cs.able.eseb.conn.BusConnection;
import edu.cmu.cs.able.eseb.filter.EventFilter;
import edu.cmu.cs.able.eseb.filter.EventFilterChain;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * <p>The <code>ParticipantIdentifier</code> class provides the basis for
 * participant identification. When generated it creates a random ID unless
 * a specific ID is provided. Associated with it one may provide a
 * <em>description</em> of the participant. The description is any data value
 * which can be encoded.</p>
 */
public class ParticipantIdentifier implements Closeable {
	/**
	 * The randomly-generated participant identifier.
	 */
	private String m_id;
	
	/**
	 * Access to the participant types.
	 */
	private ParticipantTypes m_ptypes;
	
	/**
	 * Throwable collector.
	 */
	private LocalCollector m_collector;
	
	/**
	 * Thread used to send announces.
	 */
	private WorkerThread m_send_thread;
	
	/**
	 * All known filters. We don't prevent filters from garbage-collecting.
	 */
	private Set<WeakReference<SendFilter>> m_filters;
	
	/**
	 * Participant meta data.
	 */
	private Map<String, DataValue> m_meta_data;
	
	/**
	 * The bus connection.
	 */
	private BusConnection m_connection;
	
	/**
	 * Creates a new identifier with the default renewal time.
	 * @param connection the event bus connection to build the identifier on
	 * @throws ParticipantException failed to initialize the participant
	 * identifier
	 */
	public ParticipantIdentifier(BusConnection connection)
			throws ParticipantException {
		this(connection, Participant.PARTICIPANT_RENEW_TIME_MS);
	}
	
	/**
	 * Creates a new identifier.
	 * @param connection the event bus connection to build the identifier on
	 * @param participant_renew_time how much time, in milliseconds, between
	 * renewal requests
	 * @throws ParticipantException failed to initialize the participant
	 * identifier
	 */
	public ParticipantIdentifier(BusConnection connection,
			final long participant_renew_time) throws ParticipantException {
		this(connection, participant_renew_time,
				RandomStringUtils.randomAlphanumeric(10));
	}
	
	/**
	 * Creates a new identifier with a specific participant ID.
	 * @param connection the event bus connection to build the identifier on
	 * @param participant_renew_time how much time, in milliseconds, between
	 * renewal requests
	 * @param id the ID to use for this participant identifier
	 * @throws ParticipantException failed to initialize the participant
	 * identifier
	 */
	public ParticipantIdentifier(BusConnection connection,
			final long participant_renew_time, String id)
			throws ParticipantException {
		Ensure.not_null(connection, "connection == null");
		Ensure.greater(participant_renew_time, 0,
				"participant_renew_time <= 0");
		Ensure.not_null(id, "id == null");
		
		m_connection = connection;
		m_ptypes = new ParticipantTypes(connection.primitive_scope(),
				connection.encoding());
		m_id = id;
		m_collector = new LocalCollector("Participant '" + m_id
				+ "' identifier.");
		m_filters = new HashSet<>();
		m_meta_data = new HashMap<>();
		m_send_thread = new WorkerThread("Participant '" + m_id
				+ "' event sender") {
			@Override
			protected synchronized void do_cycle_operation() throws Exception {
				send_announce_all();
				wait(participant_renew_time);
			}
		};
		m_send_thread.start();
		
		install(connection.outgoing_chain());
	}
	
	@Override
	public void close() throws IOException {
		WorkerThread wt = null;
		synchronized (this) {
			wt = m_send_thread;
			m_send_thread = null;
			
			for (EventFilter f : m_connection.outgoing_chain().filters()) {
				if (f instanceof SendFilter) {
					m_connection.outgoing_chain().remove_filter(f);
					break;
				}
			}
		}
		
		if (wt != null) {
			wt.stop();
		}
	}
	
	/**
	 * Obtains the participant ID.
	 * @return the participant ID
	 */
	public String id() {
		return m_id;
	}
	
	/**
	 * Obtains the participant's description, or <code>null</code> if there
	 * is none.
	 * @return the description
	 */
	public DataValue description() {
		return null;
	}
	
	/**
	 * Determines whether the identification send filter is installed in the
	 * given chain.
	 * @param chain the chains
	 * @return is the client installed?
	 */
	private boolean installed(EventFilterChain chain) {
		Ensure.not_null(chain);
		
		for (EventFilter f : chain.filters()) {
			if (f instanceof SendFilter) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Installs the client in the given chain.
	 * @param chain the chain
	 */
	private void install(EventFilterChain chain) {
		Ensure.not_null(chain);
		Ensure.is_false(installed(chain));
		SendFilter f = new SendFilter();
		m_filters.add(new WeakReference<>(f));
		chain.add_filter(f);
	}
	
	/**
	 * Sends the announce of all filters.
	 */
	private synchronized void send_announce_all() {
		Set<WeakReference<SendFilter>> clean = new HashSet<>();
		
		for (WeakReference<SendFilter> ref : new HashSet<>(m_filters)) {
			SendFilter f = ref.get();
			if (f == null) {
				clean.add(ref);
			} else {
				f.send_announce();
			}
		}
		
		m_filters.removeAll(clean);
	}
	
	/**
	 * Sets meta data with the given key.
	 * @param k the meta data key
	 * @param v the value to set (or unset, if <code>null</code>)
	 */
	public synchronized void meta_data(String k, DataValue v) {
		Ensure.not_null(k);
		if (v == null) {
			m_meta_data.remove(k);
		} else {
			m_meta_data.put(k, v);
		}
	}
	
	/**
	 * Obtains all meta data keys.
	 * @return all keys
	 */
	public synchronized Set<String> meta_data_keys() {
		return new HashSet<>(m_meta_data.keySet());
	}
	
	/**
	 * Obtains meta data with the given key.
	 * @param k the meta data key
	 * @return the meta data or <code>null</code> if none
	 */
	public synchronized DataValue meta_data(String k) {
		Ensure.not_null(k);
		return m_meta_data.get(k);
	}
	
	/**
	 * Filter used to regularly send the ID.
	 */
	private class SendFilter extends EventFilter {
		/**
		 * Sends a participant announce.
		 */
		private void send_announce() {
			try {
				DataValue dv = m_ptypes.announce(m_id, new HashMap<>(
						m_meta_data));
				forward(new BusData(dv));
			} catch (IOException e) {
				m_collector.collect(e, "Send participant announce.");
			}
		}
		
		@Override
		public void sink(BusData data) throws IOException {
			forward(data);
		}
	}
}
