package incubator.scb.scbset;

import incubator.scb.CloneableScb;
import incubator.scb.MergeableIdScb;
import incubator.scb.Scb;

/**
 * A change log consumer can poll a change log to get changes since its last
 * poll. Consequently, a change log consumer can fetch changes in bulk from a
 * {@link ChangeLog}.
 * @param <T> the type of SCB
 */
public interface ClConsumer
		<T extends Scb<T> & MergeableIdScb<T> & CloneableScb<T>> {
	/*
	 * No methods required.
	 */
}
