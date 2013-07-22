package edu.cmu.cs.able.eseb.filter.participant;

import incubator.pval.Ensure;
import incubator.wt.WorkerThread;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.cmu.cs.able.eseb.BusData;
import edu.cmu.cs.able.eseb.filter.EventFilter;
import edu.cmu.cs.able.typelib.enc.InvalidEncodingException;
import edu.cmu.cs.able.typelib.prim.PrimitiveScope;
import edu.cmu.cs.able.typelib.txtenc.TextEncoding;
import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Filter that drops information about participant IDs and maintains a model
 * with all participants detected.
 */
public class ParticipantModelFilter extends EventFilter {
	/**
	 * How many renews does a participant need to fail before being dropped
	 * from the model?
	 */
	public static final int RENEWS_BEFORE_DROP = 2;
	
	/**
	 * The primitive data type scope.
	 */
	private PrimitiveScope m_pscope;
	
	/**
	 * The participant model maintained.
	 */
	private ParticipantModelImpl m_model;
	
	/**
	 * Time when participants were renewed. Maps participant IDs to system
	 * milliseconds of the renew time.
	 */
	private Map<Long, Long> m_renewed;
	
	/**
	 * Participant types.
	 */
	private ParticipantTypes m_types;
	
	/**
	 * Worker thread the expires participants.
	 */
	private WorkerThread m_expire_worker;
	
	/**
	 * Number of milliseconds between renewals.
	 */
	private long m_renew_time_ms;
	
	/**
	 * Number of renews that have to fail before participant being dropped.
	 */
	private int m_renews_before_drop;
	
	/**
	 * Creates a new filter with the default expire time and renews before
	 * drop.
	 * @param pscope the primitive data type scope
	 * @param encoding the encoding to encode and decode meta data types
	 * @throws ParticipantException failed to parse / read participant data
	 * types
	 */
	public ParticipantModelFilter(PrimitiveScope pscope, TextEncoding encoding)
			throws ParticipantException {
		this(Participant.PARTICIPANT_RENEW_TIME_MS, RENEWS_BEFORE_DROP,
				pscope, encoding);
	}
	
	/**
	 * Creates a new filter.
	 * @param renew_time how much time, in milliseconds, between renews?
	 * @param renews_before_drop how many renews must a participant miss
	 * before being dropped? Note that the minimum value is <code>0</code>
	 * @param pscope the primitive data type scope
	 * @param encoding the encoding to encode and decode meta data types
	 * @throws ParticipantException failed to parse / read participant data
	 * types
	 */
	public ParticipantModelFilter(long renew_time, int renews_before_drop,
			PrimitiveScope pscope, TextEncoding encoding)
			throws ParticipantException {
		Ensure.greater(renew_time, 0);
		Ensure.greater_equal(renews_before_drop, 0);
		
		m_pscope = pscope;
		m_model = new ParticipantModelImpl();
		m_renewed = new HashMap<>();
		m_types = new ParticipantTypes(m_pscope, encoding);
		m_renew_time_ms = renew_time;
		m_renews_before_drop = renews_before_drop;
		m_expire_worker = new WorkerThread("Participant model expirer") {
			@Override
			protected synchronized void do_cycle_operation() throws Exception {
				check_expires();
				wait(m_renew_time_ms / 2);
			}
		};
		m_expire_worker.start();
	}
	
	/**
	 * Removes all participants from the model and does not add any more.
	 */
	public void shutdown() {
		synchronized (this) {
			Ensure.not_null(m_model);
			m_model = null;
		}
		
		m_expire_worker.stop();
	}
	
	/**
	 * Obtains the participant model maintained by this filter.
	 * @return the model or <code>null</code> if shutdown
	 */
	public synchronized ParticipantModel model() {
		return m_model;
	}

	@Override
	public void sink(BusData data) throws IOException {
		Ensure.not_null(data);
		
		if (model() == null) {
			forward(data);
			return;
		}
		
		if (m_types.is_announce(data.value())) {
			long id = m_types.announce_id(data.value());
			Map<String, DataValue> decoded = new HashMap<>();
			Set<String> undecodable = new HashSet<>();
			
			for (String s : m_types.announce_meta_data_keys(data.value())) {
				try {
					DataValue v = m_types.announce_meta_data(data.value(), s);
					decoded.put(s, v);
				} catch (InvalidEncodingException e) {
					undecodable.add(s);
				}
			}
			
			announce(id, decoded, undecodable);
		} else {
			forward(data);
		}
	}
	
	/**
	 * Processes an announcement of a participant.
	 * @param id the participant ID
	 * @param decoded meta data keys that were successfully decoded
	 * @param undecodable meta data keys that could not be decoded
	 */
	private synchronized void announce(long id, Map<String, DataValue> decoded,
			Set<String> undecodable) {
		Participant p;
		if (!m_renewed.containsKey(id)) {
			p = new_participant(id);
		} else {
			p = renew(id);
		}
		
		p.update(decoded, undecodable);
	}
	
	/**
	 * A new participant has been detected.
	 * @param id the ID of the participant
	 * @return the created participant
	 */
	private Participant new_participant(long id) {
		Participant p = new Participant(id);
		m_model.add_scb(p);
		m_renewed.put(id, System.currentTimeMillis());
		return p;
	}
	
	/**
	 * Renews a participant.
	 * @param id the ID of the participant
	 * @return the participant
	 */
	private Participant renew(long id) {
		m_renewed.put(id, System.currentTimeMillis());
		Participant found = null;
		for (Participant p : m_model.all_scbs()) {
			if (p.id() == id) {
				found = p;
				break;
			}
		}
		
		Ensure.not_null(found);
		return found;
	}
	
	/**
	 * Checks if there any participants to expire and, if so, removes them
	 * from the model.
	 */
	private synchronized void check_expires() {
		long now = System.currentTimeMillis();
		long old_renew = now - (long) (m_renew_time_ms
				* (m_renews_before_drop + 1.5));
		for (Participant p : m_model.all_scbs()) {
			Long last_renew = m_renewed.get(p.id());
			Ensure.not_null(last_renew);
			
			if (last_renew < old_renew) {
				m_renewed.remove(p.id());
				m_model.remove_scb(p);
			}
		}
	}
}
