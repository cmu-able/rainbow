package incubator.scb.scbset;

import incubator.dispatch.Dispatcher;
import incubator.scb.CloneableScb;
import incubator.scb.MergeableIdScb;
import incubator.scb.Scb;

import java.util.List;

/**
 * <p>A change log keeps track of changes made to an SCB set. The change log
 * receives entries from a {@link ScbReadableSet}. Consumers (instances of
 * {@link ClConsumer} can register with the change log to receive changes
 * made to the set. The change log will only keep the entries necessary to
 * update consumers. Once all consumers have received a change in the change
 * log, the change is discarded.</p>
 * <p>Initial synchronization of consumers is made through
 * <em>checkpoints</em>. Checkpoints are requested by the change log when
 * consumers are added and contain a copy of the whole set. Before a consumer
 * has received a checkpoint, all entries are ignored (the consumer is not
 * informed). The first entry a consumer receives is a checkpoint. No further
 * checkpoints are sent to the consumer.</p> 
 * @param <T> the type of SCB
 */
public interface ChangeLog
		<T extends Scb<T> & MergeableIdScb<T> & CloneableScb<T>> {
	/**
	 * Adds a consumer to the change log. The consumer will receive a checkpoint
	 * and incremental updates afterwards.
	 * @param cp the consumer
	 */
	void add_consumer(ClConsumer<T> cp);
	
	/**
	 * Removes a consumer from the change log.
	 * @param cp the consumer
	 */
	void remove_consumer(ClConsumer<T> cp);
	
	/**
	 * Obtains all changes since the last time {@link #consume(ClConsumer)}
	 * was invoked for this consumer.
	 * @param cp the consumer, which must have been previously registered using
	 * {@link #add_consumer(ClConsumer)}
	 * @return a set of changes, possibly empty
	 */
	List<ChangeLogEntry<T>> consume(ClConsumer<T> cp);
	
	/**
	 * Obtains the change log dispatcher.
	 * @return the dispatcher
	 */
	Dispatcher<ChangeLogListener> dispatcher();
}
