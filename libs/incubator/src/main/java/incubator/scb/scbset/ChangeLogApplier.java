package incubator.scb.scbset;

import incubator.scb.CloneableScb;
import incubator.scb.MergeableIdScb;
import incubator.scb.Scb;
import incubator.scb.filter.ScbFilter;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Class that applies a change log to a set keeping the set updated with
 * respect to the changes provided in the change log. A filter may be
 * provided that limits which SCBs are to be synchronized.
 * @param <T> the SCB type
 */
public class ChangeLogApplier
		<T extends Scb<T> & MergeableIdScb<T> & CloneableScb<T>> {
	/**
	 * The source change log, <code>null</code> if none.
	 */
	private ChangeLog<T> m_source;
	
	/**
	 * The set to apply the change log to update, <code>null</code> if none.
	 */
	private ScbWritableSet<T> m_destination;
	
	/**
	 * An optional filter to filter SCBs.
	 */
	private ScbFilter<T> m_filter;
	
	/**
	 * The change log consumer.
	 */
	private ClConsumer<T> m_consumer;
	
	/**
	 * Listener that receives changes from a change log.
	 */
	private ChangeLogListener m_change_log_listener;
	
	/**
	 * Creates a new applier.
	 * @param source an optional change log to read data from
	 * @param destination an optional destination to update with the change log
	 * @param filter an optional filter to apply
	 */
	public ChangeLogApplier(ChangeLog<T> source,
			ScbWritableSet<T> destination, ScbFilter<T> filter) {
		m_source = null;
		m_destination = null;
		m_filter = null;
		m_consumer = new ClConsumer<T>() { /* Nothing */ };
		m_change_log_listener = new ChangeLogListener() {
			@Override
			public void changed() {
				feed();
			}
		};
		
		if (source != null) {
			source(source);
		}
	}
	
	/**
	 * Changes the source to apply data from.
	 * @param source the source
	 */
	public synchronized void source(ChangeLog<T> source) {
		if (Objects.equals(source, m_source)) {
			return;
		}
		
		if (m_source != null) {
			m_source.remove_consumer(m_consumer);
			m_source.dispatcher().remove(m_change_log_listener);
		}
		
		m_source = source;
		
		if (m_source != null) {
			m_source.dispatcher().add(m_change_log_listener);
			m_source.add_consumer(m_consumer);
		}
		
		feed();
	}
	
	/**
	 * Changes the destination to apply data to.
	 * @param destination the destination
	 */
	public synchronized void destination(ScbWritableSet<T> destination) {
		if (Objects.equals(destination, m_destination)) {
			return;
		}
		
		if (m_destination != null) {
			m_destination.sync(new HashSet<ScbIw<T>>());
		}
		
		m_destination = destination;
		feed();
	}
	
	/**
	 * Changes the filter to apply.
	 * @param filter the filter
	 */
	public synchronized void filter(ScbFilter<T> filter) {
		if (Objects.equals(filter, m_filter)) {
			return;
		}
		
		m_filter = filter;
		if (m_source != null) {
			/*
			 * Force a reset to re-read all the data.
			 */
			m_source.remove_consumer(m_consumer);
			m_source.add_consumer(m_consumer);
		}
	}
	
	/**
	 * Feeds all data from the source to the destination.
	 */
	private synchronized void feed() {
		if (m_source == null || m_destination == null) {
			return;
		}
		
		List<ChangeLogEntry<T>> changes = m_source.consume(m_consumer);
		for (ChangeLogEntry<T> ch : changes) {
			ch.apply(m_destination, m_filter);
		}
	}
}
