package incubator.scb.sync;

import incubator.scb.ScbContainer;

/**
 * Interface implemented by containers that act as master SCB containers.
 * Master SCB containers are normal observable containers which have two
 * extra operations: an incoming and a delete. The incoming operation will
 * create or update an SCB based on a given SCB which is usually sent by the
 * slave or read from storage on initialization. The delete will remove an
 * SCN given its ID.
 *
 * @param <ID_TYPE> type of the SCB's unique identifier
 * @param <T> the SCB's type
 */
public interface SyncScbMasterContainer<ID_TYPE, T extends SyncScb<ID_TYPE, T>>
		extends ScbContainer<T> {
	/**
	 * An SCB should be added or updated in the container. If added,
	 * <code>t</code> will be placed inside the container.
	 * @param t the SCB
	 */
	void incoming(T t);
	
	/**
	 * The SCB with the given ID should be deleted. If no SCB with the
	 * given ID exists, this method should do nothing.
	 * @param id the SCB's ID
	 */
	void delete(ID_TYPE id);
}
