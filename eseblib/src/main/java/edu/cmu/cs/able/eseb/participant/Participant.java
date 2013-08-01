package edu.cmu.cs.able.eseb.participant;

import incubator.dispatch.Dispatcher;
import incubator.dispatch.DispatcherOp;
import incubator.dispatch.LocalDispatcher;
import incubator.pval.Ensure;
import incubator.scb.Scb;
import incubator.scb.ScbField;
import incubator.scb.ScbLongField;
import incubator.scb.ScbUpdateListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.cs.able.typelib.type.DataValue;

/**
 * Participant in a bus. Participants are created by a
 * {@link ParticipantModel} usually maintained by a
 * {@link ParticipantModelFilter}. If a participant updates its information,
 * the data provided by this class will be updated dynamically.
 */
public class Participant implements Scb<Participant> {
	/**
	 * Default time, in milliseconds, each participant should renew its ID.
	 */
	public static final long PARTICIPANT_RENEW_TIME_MS = 1000;
	
	/**
	 * Participant ID.
	 */
	private long m_id;
	
	/**
	 * Meta data that was correctly decoded.
	 */
	private Map<String, DataValue> m_meta_data;
	
	/**
	 * Meta data that was not decodable.
	 */
	private Set<String> m_undecodable_meta_data;
	
	/**
	 * Event dispatcher.
	 */
	private LocalDispatcher<ScbUpdateListener<Participant>> m_dispatcher;
	
	/**
	 * Creates a participant.
	 * @param id the participant's ID
	 */
	Participant(long id) {
		m_id = id;
		m_meta_data = new HashMap<>();
		m_undecodable_meta_data = new HashSet<>();
		m_dispatcher = new LocalDispatcher<>();
	}
	
	/**
	 * Obtains the participant's ID.
	 * @return the ID
	 */
	public long id() {
		return m_id;
	}
	
	/**
	 * Updates the meta data list of this participant.
	 * @param meta_data the new successfully decoded meta data
	 * @param undecodable keys of undecodable meta data
	 */
	public void update(Map<String, DataValue> meta_data,
			Set<String> undecodable) {
		Ensure.not_null(meta_data);
		Ensure.not_null(undecodable);
		
		boolean changed = false;
		if (!m_meta_data.equals(meta_data)) {
			changed = true;
			m_meta_data = new HashMap<>(meta_data);
		}
		
		
		if (!m_undecodable_meta_data.equals(undecodable)) {
			changed = true;
			m_undecodable_meta_data = new HashSet<>(undecodable);
		}
		
		if (changed) {
			m_dispatcher.dispatch(
					new DispatcherOp<ScbUpdateListener<Participant>>() {
				@Override
				public void dispatch(ScbUpdateListener<Participant> l) {
					l.updated(Participant.this);
				}
			});
		}
	}
	
	/**
	 * Obtains all meta data keys.
	 * @return all meta data keys, including all keys that were not decoded
	 * successfully
	 */
	public Set<String> meta_data_keys() {
		Set<String> keys = new HashSet<>();
		keys.addAll(m_meta_data.keySet());
		keys.addAll(m_undecodable_meta_data);
		return keys;
	}
	
	/**
	 * Obtains the meta data with a given key.
	 * @param key the meta data
	 * @return the value associated with the key, <code>null</code> if not
	 * decodable or non-existent.
	 */
	public DataValue meta_data(String key) {
		Ensure.not_null(key);
		DataValue v = m_meta_data.get(key);
		if (v != null) {
			return v;
		}
		
		return null;
	}
	
	@Override
	public Dispatcher<ScbUpdateListener<Participant>> dispatcher() {
		return m_dispatcher;
	}

	@SuppressWarnings("javadoc")
	public static ScbLongField<Participant> c_id() {
		return new ScbLongField<Participant>("id", false, null) {
			@Override
			public void set(Participant t, Long value) {
				Ensure.unreachable();
			}

			@Override
			public Long get(Participant t) {
				return t.id();
			}
		};
	}

	@Override
	public List<ScbField<Participant, ?>> fields() {
		List<ScbField<Participant, ?>> f = new ArrayList<>();
		f.add(c_id());
		return f;
	}
}
