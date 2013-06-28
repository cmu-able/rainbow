package incubator.scb.sync;

import incubator.Pair;

import java.util.List;

/**
 * Interface implemented by a remote synchronization master.
 */
public interface SyncScbMaster {
	/**
	 * A slave has contacted us.
	 * @param key the slave key
	 * @param ops all operations to perform in the master
	 * @return the first element contains whether the slave should reset
	 * its data, the second are all operations to perform in the slave
	 * @throws UnknownContainerException the slave refers to a container
	 * which is unknown in the master
	 */
	public Pair<Boolean,List<ScbOperation>> slave_contact(String key,
			List<ScbOperation> ops) throws UnknownContainerException;
}
