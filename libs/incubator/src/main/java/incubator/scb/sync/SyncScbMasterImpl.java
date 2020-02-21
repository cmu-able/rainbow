package incubator.scb.sync;

import incubator.Pair;
import incubator.pval.Ensure;
import incubator.wt.WorkerThread;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;


/**
 * The <code>SyncScbMaster</code> keeps several master containers, indexed by
 * unique keys and allows clients to register and poll for updates as well
 * as send in changes.
 */
public class SyncScbMasterImpl implements SyncScbMaster {
	/**
	 * Logger to use.
	 */
	private static final Logger LOG =
			Logger.getLogger(SyncScbMasterImpl.class);
	
	/**
	 * Number of times to check for slave expiration in each slave expiration
	 * interval.
	 */
	private static final int SLAVE_CHECK_COUNT = 10;
	
	/**
	 * Maps container keys to their wrappers.
	 */
	private Map<String, ContainerWrapper<?, ?>> m_wrappers;
	
	/**
	 * Maps slave keys to their information.
	 */
	private Map<String, SlaveInfo> m_slaves;
	
	/**
	 * Timer to expire slaves.
	 */
	private WorkerThread m_timer;
	
	/**
	 * Slave expiration time in milliseconds.
	 */
	private long m_slave_expiration;
	
	/**
	 * Creates a new master.
	 * @param slave_expiration_ms time, in milliseconds, between slave
	 * expiration
	 */
	public SyncScbMasterImpl(long slave_expiration_ms) {
		Ensure.greater(slave_expiration_ms, 0);
		
		final long check_interval = slave_expiration_ms / SLAVE_CHECK_COUNT;
		Ensure.greater(check_interval, 0);
		
		m_wrappers = new HashMap<>();
		m_slaves = new HashMap<>();
		m_slave_expiration = slave_expiration_ms;
		m_timer = new WorkerThread("SCB Master") {
			@Override
			protected void do_cycle_operation() throws Exception {
				synchronized (SyncScbMasterImpl.this) {
					check_for_expiration();
					SyncScbMasterImpl.this.wait(check_interval);
				}
				
			}

			@Override
			protected void interrupt_wait() {
				synchronized (SyncScbMasterImpl.this) {
					SyncScbMasterImpl.this.notifyAll();
				}
			}
		};
		m_timer.start();
	}
	
	/**
	 * Checks if any of the slaves should expire.
	 */
	private void check_for_expiration() {
		Set<String> to_expire = new HashSet<>();
		long earliest = System.currentTimeMillis() - m_slave_expiration;
		
		for (String k : m_slaves.keySet()) {
			if (m_slaves.get(k).last_contact() < earliest) {
				to_expire.add(k);
			}
		}
		
		for (String k : to_expire) {
			LOG.info("Slave with UID=" + k + " has expired.");
			m_slaves.remove(k);
		}
	}
	
	/**
	 * Creates a container.
	 * @param k the container key
	 * @param idclass the class of the ID type
	 * @param tclass the class of the bean type
	 * @param <ID_TYPE> the type of the SCB's ID field
	 * @param <T> the SCB type
	 * @return the created container
	 */
	public synchronized <ID_TYPE, T extends SyncScb<ID_TYPE, T>>
			SyncScbMasterContainer<ID_TYPE, T> create_container(final String k,
			Class<ID_TYPE> idclass, Class<T> tclass) {
		Ensure.not_null(m_timer);
		Ensure.not_null(m_slaves);
		Ensure.not_null(m_wrappers);
		
		Ensure.not_null(k);
		Ensure.not_null(idclass);
		Ensure.not_null(tclass);
		Ensure.is_false(m_wrappers.containsKey(k));
		
		ContainerWrapper<ID_TYPE, T> w = new ContainerWrapper<>(idclass,
				tclass);
		m_wrappers.put(k, w);
		
		LOG.info("Created container with key " + k + " of type "
				+ tclass.getName() + ", with ID of type " + idclass.getName()
				+ ".");
		
		return w.container();
	}
	
	/**
	 * Adds an operation to all slaves. The operation will be sent when
	 * the slaves request pending operations except the given one (which is
	 * the one requesting the operation).
	 * @param op the operation
	 * @param rk the key of the slave that requested the operation
	 */
	private synchronized void add_to_all(ScbOperation op, String rk) {
		Ensure.not_null(m_timer);
		Ensure.not_null(m_slaves);
		Ensure.not_null(m_wrappers);
		
		Ensure.not_null(op);
		Ensure.not_null(rk);
		
		for (String k : m_slaves.keySet()) {
			SlaveInfo si = m_slaves.get(k);
			si.add(op);
		}
	}
	
	/**
	 * Processes a list of operations from a slave.
	 * @param operations the list of operations
	 * @param rk the key of the slave making the request
	 * @throws UnknownContainerException a reference to an unknown container
	 * was made
	 */
	public synchronized void process(String rk, List<ScbOperation> operations)
			throws UnknownContainerException {
		Ensure.not_null(m_timer);
		Ensure.not_null(m_slaves);
		Ensure.not_null(m_wrappers);
		
		Ensure.not_null(operations);
		for (ScbOperation op : operations) {
			Ensure.not_null(op);
			Ensure.not_null(op.container_key());
			ContainerWrapper<?, ?> w = m_wrappers.get(op.container_key());
			if (w == null) {
				throw new UnknownContainerException(op.container_key());
			}
			
			w.process(op, rk);
			add_to_all(op, rk);
		}
	}

	@Override
	public synchronized Pair<Boolean, List<ScbOperation>> slave_contact(
			String key, List<ScbOperation> ops)
			throws UnknownContainerException {
		Ensure.not_null(key);
		Ensure.not_null(ops);
		
		process(key, ops);
		
		boolean existing = m_slaves.containsKey(key);
		if (existing) {
			SlaveInfo si = m_slaves.get(key);
			List<ScbOperation> ret_ops = si.contact();
			return new Pair<>(false, ret_ops); 
		} else {
			LOG.info("New slave contact with UID=" + key + ".");
			
			SlaveInfo si = new SlaveInfo();
			m_slaves.put(key, si);
			
			List<ScbOperation> create_all = new LinkedList<>();
			for (String k : m_wrappers.keySet()) {
				ContainerWrapper<?, ?> w = m_wrappers.get(k);
				for (Object o : w.container().all_scbs()) {
					create_all.add(ScbOperation.make_incoming(k, o));
				}
			}
			
			si.contact();
			return new Pair<>(true, create_all);
		}
	}
	
	/**
	 * Shuts down the master.
	 */
	public void shutdown() {
		WorkerThread timer;
		
		synchronized (this) {
			Ensure.not_null(m_timer);
			Ensure.not_null(m_slaves);
			Ensure.not_null(m_wrappers);
			
			timer = m_timer;
			m_timer = null;
			m_slaves.clear();
			m_slaves = null;
			m_wrappers.clear();
			m_wrappers = null;
		}
		
		timer.stop();
	}
	
	/**
	 * Class wrapping a container around a generic interface that removes
	 * strict type checking.
	 * @param <ID_TYPE> the type of the SCB's ID field
	 * @param <T> the SCB type
	 */
	private static class ContainerWrapper<ID_TYPE,
			T extends SyncScb<ID_TYPE,T>> {
		/**
		 * The container itself.
		 */
		private SyncScbMasterContainer<ID_TYPE, T> m_container;
		
		/**
		 * The class of the ID type.
		 */
		private Class<ID_TYPE> m_idclass;
		
		/**
		 * The class of the bean type.
		 */
		private Class<T> m_tclass;
		
		/**
		 * Creates a new wrapper for a container.
		 * @param idclass the class of the ID type
		 * @param tclass the class of the bean type
		 */
		ContainerWrapper(Class<ID_TYPE> idclass, Class<T> tclass) {
			Ensure.not_null(idclass);
			Ensure.not_null(tclass);
			
			m_container = new SyncScbMasterContainerImpl<>();
			m_idclass = idclass;
			m_tclass = tclass;
		}
		
		/**
		 * Processes an operation on the container.
		 * @param op the operation
		 * @param rk the key of the slave requesting the processing
		 */
		void process(ScbOperation op, String rk) {
			Ensure.not_null(op);
			if (op.incoming() != null) {
				Ensure.is_null(op.delete_key());
				LOG.debug("Slave UID=" + rk + " requested create/update of "
						+ "SCB with ID=" + m_tclass.cast(op.incoming()).id()
						+ ".");
				m_container.incoming(m_tclass.cast(op.incoming()));
			} else {
				Ensure.not_null(op.delete_key());
				LOG.debug("Slave UID=" + rk + " requested delete of "
						+ "SCB with ID=" + op.delete_key() + ".");
				m_container.delete(m_idclass.cast(op.delete_key()));
			}
		}
		
		/**
		 * Obtains the container.
		 * @return the container
		 */
		SyncScbMasterContainer<ID_TYPE, T> container() {
			return m_container;
		}
	}
	
	/**
	 * Class that maintains information for a slave.
	 */
	private class SlaveInfo {
		/**
		 * Last time the client contacted us in system milliseconds
		 */
		private long m_last_contact;
		
		/**
		 * Pending operations to send to the slave.
		 */
		private List<ScbOperation> m_pending;
		
		/**
		 * Creates a new slave information.
		 */
		SlaveInfo() {
			m_last_contact = System.currentTimeMillis();
			m_pending = new LinkedList<>();
		}
		
		/**
		 * Obtains the time of last contact of the slave.
		 * @return the time of last contact in system milliseconds
		 */
		synchronized long last_contact() {
			return m_last_contact;
		}
		
		/**
		 * The slave has contacted us.
		 * @return the operations to send
		 */
		synchronized List<ScbOperation> contact() {
			List<ScbOperation> to_return = m_pending;
			m_pending = new LinkedList<>();
			m_last_contact = System.currentTimeMillis();
			return to_return;
		}
		
		/**
		 * Adds an operation to the pending list of operations of a slave.
		 * @param op the operation
		 */
		synchronized void add(ScbOperation op) {
			Ensure.not_null(op);
			m_pending.add(op);
		}
	}
}
