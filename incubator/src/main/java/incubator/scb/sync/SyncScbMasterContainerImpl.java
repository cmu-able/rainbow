package incubator.scb.sync;

import incubator.pval.Ensure;
import incubator.scb.ScbContainerImpl;

/**
 * Implementation of a master SCB connection.
 * @param <ID_TYPE> the type of ID of the SCBs
 * @param <T> the type of the SCBs
 */
public class SyncScbMasterContainerImpl<ID_TYPE, T extends SyncScb<ID_TYPE, T>>
		extends ScbContainerImpl<T>
		implements SyncScbMasterContainer<ID_TYPE, T> {
	/**
	 * Creates a new implementation.
	 */
	public SyncScbMasterContainerImpl() {
	}

	@Override
	public synchronized void incoming(T t) {
		Ensure.not_null(t);
		
		/*
		 * Search for the SCB that has the same ID as this one.
		 */
		boolean found = false;
		for (T my_t : inner_set()) {
			if (my_t.id().equals(t.id())) {
				my_t.sync_with(t);
				found = true;
				break;
			}
		}
		
		if (!found) {
			/*
			 * Make a copy of the bean.
			 */
			t = SyncScb.duplicate(t);
			t.sync_status(SyncStatus.MASTER);
			inner_set().add(t);
		}
	}

	@Override
	public synchronized void delete(ID_TYPE id) {
		Ensure.not_null(id);
		
		for (T my_t : inner_set()) {
			if (my_t.id().equals(id)) {
				boolean removed = inner_set().remove(my_t);
				Ensure.is_true(removed);
				break;
			}
		}
	}
}
