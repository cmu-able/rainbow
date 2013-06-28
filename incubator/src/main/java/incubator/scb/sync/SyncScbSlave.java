package incubator.scb.sync;

import incubator.Pair;
import incubator.exh.LocalCollector;
import incubator.pval.Ensure;
import incubator.scb.ScbContainerListener;
import incubator.scb.ScbEditableContainer;
import incubator.scb.ScbUpdateListener;
import incubator.wt.WorkerThread;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;

/**
 * The <code>SyncScbSlave</code> will keep several containers (mapped by
 * keys) synchronized with a server. The containers may be freely changed,
 * as well as their beans. The client will forward changes to the server
 * and receive updates the server updating the beans in the container.
 */
public class SyncScbSlave {
	/**
	 * Length of the client's unique ID.
	 */
	private static final int UID_LENGTH = 5;
	
	/**
	 * Time to wait for synchronization to perform.
	 */
	private static final long WAIT_FOR_SYNC_TIME_MS = 25;
	
	/**
	 * The master we're contacting.
	 */
	private SyncScbMaster m_master;
	
	/**
	 * The polling interval in milliseconds.
	 */
	private long m_poll_interval_ms;
	
	/**
	 * Worker thread that performs synchronization.
	 */
	private WorkerThread m_worker;
	
	/**
	 * When did we, in system milliseconds, last polled the server?
	 */
	private long m_last_poll;
	
	/**
	 * The slave's unique ID.
	 */
	private String m_uid;
	
	/**
	 * Operations pending to send to the server
	 */
	private List<ScbOperation> m_pending;
	
	/**
	 * Exception collector.
	 */
	private LocalCollector m_collector;
	
	/**
	 * Containers mapped by their keys.
	 */
	private Map<String, ContainerWrapper<?, ?>> m_containers;
	
	/**
	 * Synchronization slave status.
	 */
	private SyncSlaveState m_state;
	
	/**
	 * Number of times we've synchronized with the server.
	 */
	private int m_sync_count;
	
	/**
	 * Date when last synchronization finished successfully.
	 */
	private Date m_last_sync_date;
	
	/**
	 * Creates a new client.
	 * @param master the master to synchronize with
	 * @param poll_interval_ms the interval, in milliseconds, between server
	 * contacts 
	 */
	public SyncScbSlave(SyncScbMaster master, long poll_interval_ms) {
		Ensure.not_null(master);
		Ensure.greater(poll_interval_ms, 0);
		
		m_master = master;
		m_poll_interval_ms = poll_interval_ms;
		m_last_poll = 0;
		m_uid = RandomStringUtils.randomAlphanumeric(UID_LENGTH);
		m_pending = new LinkedList<>();
		m_collector = new LocalCollector("SyncScbSlave");
		m_containers = new HashMap<>();
		m_state = SyncSlaveState.WAITING;
		m_sync_count = 0;
		m_last_sync_date = null;
		m_worker = new WorkerThread("Sync SCB Slave") {
			@Override
			protected void do_cycle_operation() throws Exception {
				sync_cycle();
			}

			@Override
			protected void interrupt_wait() {
				synchronized (SyncScbSlave.this) {
					SyncScbSlave.this.notifyAll();
				}
			}
		};
		
		m_worker.start();
	}
	
	/**
	 * Obtains the time the last synchronization completed successfully.
	 * @return the time of the last synchronization
	 */
	public synchronized Date last_sync_date() {
		return m_last_sync_date;
	}
	
	/**
	 * Performs a synchronization cycle.
	 * @throws InterruptedException wait interrupted
	 */
	private void sync_cycle() throws InterruptedException {
		long now = System.currentTimeMillis();
		List<ScbOperation> pending;
		String uid;
		
		synchronized (this) {
			m_state = SyncSlaveState.WAITING;
			if (m_last_poll + m_poll_interval_ms > now) {
				wait(m_last_poll + m_poll_interval_ms - now);
				return;
			}
			
			m_state = SyncSlaveState.SYNCHRONIZING;
			pending = m_pending;
			m_pending = new LinkedList<>();
			uid = m_uid;
		}
		
		Pair<Boolean, List<ScbOperation>> r = null;
		try {
			r = m_master.slave_contact(uid, pending);
			
			synchronized (this) {
				if (r.first()) {
					/*
					 * We need to reset everything.
					 */
					for (ContainerWrapper<?, ?> w : m_containers.values()) {
						w.reset();
					}
				}
				
				for (ScbOperation op : r.second()) {
					ContainerWrapper<?, ?> w = m_containers.get(
							op.container_key());
					if (w != null) {
						w.process(op);
					}
				}
			}
		} catch (Exception e) {
			m_collector.collect(e, "Contacting server.");
		}
		
		synchronized (this) {
			m_last_poll = now;
			m_sync_count++;
			m_last_sync_date = new Date();
		}
	}
	
	/**
	 * Forces synchronization with the server, if it is not already
	 * happening.
	 */
	public synchronized void sync_now() {
		Ensure.is_true(m_state == SyncSlaveState.WAITING
				|| m_state == SyncSlaveState.SYNCHRONIZING);
		
		m_last_poll = 0;
		notifyAll();
	}
	
	/**
	 * Forces synchronization with the server, if it is not already
	 * happening and waits for the synchronization to end.
	 */
	public synchronized void sync_now_wait() {
		int prev_count = m_sync_count;
		
		do {
			sync_now();
			try {
				wait(WAIT_FOR_SYNC_TIME_MS);
			} catch (InterruptedException e) {
				/*
				 * We've been interrupted. Just try again.
				 */
			}
		} while (m_sync_count == prev_count);
	}
	
	/**
	 * Obtains the synchronization state of the client.
	 * @return the state
	 */
	public synchronized SyncSlaveState state() {
		return m_state;
	}
	
	/**
	 * Adds a new container to be managed by the slave.
	 * @param key the container key (used to synchronize with the server).
	 * @param container the container
	 * @param idclass the type of the ID field
	 * @param tclass the type of the bean
	 */
	public synchronized <ID_TYPE, T extends SyncScb<ID_TYPE, T>> void
			add_container(String key, ScbEditableContainer<T> container,
			Class<ID_TYPE> idclass, Class<T> tclass) {
		Ensure.is_true(m_state == SyncSlaveState.WAITING
				|| m_state == SyncSlaveState.SYNCHRONIZING);
		
		Ensure.not_null(key);
		Ensure.not_null(container);
		Ensure.not_null(idclass);
		Ensure.not_null(tclass);
		Ensure.is_false(m_containers.containsKey(key));
		
		m_containers.put(key, new ContainerWrapper<>(container, idclass,
				tclass, key));
		m_uid = RandomStringUtils.randomAlphanumeric(UID_LENGTH);
		m_last_poll = 0;
		notifyAll();
	}
	
	/**
	 * Invoked when an SCB has been added in a container.
	 * @param key the container key
	 * @param scb the SCB
	 */
	private synchronized void scb_added_in_container(String key, Object scb) {
		Ensure.not_null(key);
		Ensure.not_null(scb);
		Ensure.is_true(m_containers.containsKey(key));
		
		m_pending.add(ScbOperation.make_incoming(key, scb));
		
	}
	
	/**
	 * Invoked when an SCB has been updated in a container.
	 * @param key the container key
	 * @param scb the SCB
	 */
	private synchronized void scb_updated_in_container(String key, Object scb) {
		Ensure.not_null(key);
		Ensure.not_null(scb);
		Ensure.is_true(m_containers.containsKey(key));
		
		m_pending.add(ScbOperation.make_incoming(key, scb));
	}
	
	/**
	 * Invoked when an SCB has been deleted from a container.
	 * @param key the container key
	 * @param id the SCB ID
	 */
	private synchronized void scb_removed_from_container(String key,
			Object id) {
		Ensure.not_null(key);
		Ensure.not_null(id);
		Ensure.is_true(m_containers.containsKey(key));
		
		m_pending.add(ScbOperation.make_delete(key, id));
	}
	
	/**
	 * Shuts down the slave.
	 */
	public void shutdown() {
		synchronized (this) {
			Ensure.is_true(m_state == SyncSlaveState.WAITING
					|| m_state == SyncSlaveState.SYNCHRONIZING);
			m_state = SyncSlaveState.SHUTDOWN;
		}
		
		m_worker.stop();
		
		synchronized (this) {
			/*
			 * In some race conditions, m_worker may do an extra cycle and
			 * update the state.
			 */
			m_state = SyncSlaveState.SHUTDOWN;
		}
	}
	
	/**
	 * Wrapper to remove static type-safety around SCB containers. The wrapper
	 * also registers itself as listener to all SCBs and informs the
	 * container when any of the SCBs change.
	 * @param <ID_TYPE> the type of the ID field of the SCB
	 * @param <T> the type of the SCB
	 */
	private class ContainerWrapper<ID_TYPE,
			T extends SyncScb<ID_TYPE, T>> {
		/**
		 * The wrapped container.
		 */
		private ScbEditableContainer<T> m_container;
		
		/**
		 * Class for the ID type.
		 */
		private Class<ID_TYPE> m_idclass;
		
		/**
		 * Class for the bean type.
		 */
		private Class<T> m_tclass;
		
		/**
		 * Listener for changes in the container.
		 */
		private ScbContainerListener<T> m_clistner;
		
		/**
		 * The container key.
		 */
		private String m_key;
		
		/**
		 * Listener for updates in an SCB.
		 */
		private ScbUpdateListener<T> m_ulistener;
		
		/**
		 * Creates a new wrapper
		 * @param ec the container
		 * @param idclass the class of the ID type
		 * @param tclass the class of the bean
		 * @param key the container key
		 */
		ContainerWrapper(ScbEditableContainer<T> ec, Class<ID_TYPE> idclass,
				Class<T> tclass, String key) {
			Ensure.not_null(ec);
			Ensure.not_null(idclass);
			Ensure.not_null(tclass);
			Ensure.not_null(key);
			
			m_container = ec;
			m_idclass = idclass;
			m_tclass = tclass;
			m_key = key;
			m_clistner = new ScbContainerListener<T>() {
				@Override
				public void scb_added(T t) {
					t.sync_status(SyncStatus.LOCAL_CHANGES);
					t.dispatcher().add(m_ulistener);
					
					scb_added_in_container(m_key, t);
				}

				@Override
				public void scb_removed(T t) {
					scb_removed_from_container(m_key, t.id());
					t.dispatcher().remove(m_ulistener);
				}

				@Override
				public void scb_updated(T t) {
					/*
					 * We can't use this notification because it would come
					 * later: we change the SCB and later the container
					 * informs us. Because of the delay, we can't unregister
					 * from the container to avoid receiving the notification.
					 */
				}
			};
			
			m_ulistener = new ScbUpdateListener<T>() {
				@Override
				public void updated(T t) {
					synchronized (t) {
						t.dispatcher().remove(m_ulistener);
						t.sync_status(SyncStatus.LOCAL_CHANGES);
						t.dispatcher().add(m_ulistener);
					}
					
					scb_updated_in_container(m_key, t);
				}
			};
			
			ec.add_listener(m_clistner);
		}
		
		/**
		 * Resets the container removing all elements.
		 */
		private synchronized void reset() {
			Set<T> all = new HashSet<>(m_container.all_scbs());
			for (T t : all) {
				m_container.remove_scb(t);
			}
			
			Ensure.equals(0, m_container.all_scbs().size());
		}
		
		/**
		 * Processes an operation in the container send by the master.
		 * @param op the operation
		 */
		private synchronized void process(ScbOperation op) {
			Ensure.not_null(op);
			
			if (op.delete_key() != null) {
				process_delete(m_idclass.cast(op.delete_key()));
			} else {
				process_incoming(m_tclass.cast(op.incoming()));
			}
		}
		
		/**
		 * Deletes an SCB with the given ID.
		 * @param id the ID
		 */
		private void process_delete(ID_TYPE id) {
			Ensure.not_null(id);
			for (T t : m_container.all_scbs()) {
				if (t.id().equals(id)) {
					synchronized (m_container) {
						m_container.remove_listener(m_clistner);
						t.dispatcher().remove(m_ulistener);
						m_container.remove_scb(t);
						m_container.add_listener(m_clistner);
					}
					break;
				}
			}
		}
		
		/**
		 * Adds or updates an SCB with the given ID.
		 * @param t the SCB
		 */
		private void process_incoming(T t) {
			Ensure.not_null(t);
			boolean found = false;
			for (T c_t : m_container.all_scbs()) {
				if (c_t.id().equals(t.id())) {
					c_t.dispatcher().remove(m_ulistener);
					c_t.sync(t);
					c_t.sync_status(SyncStatus.SYNCHRONIZED);
					c_t.dispatcher().add(m_ulistener);
					found = true;
					break;
				}
			}
			
			if (!found) {
				t = SyncScb.duplicate(t);
				t.sync_status(SyncStatus.SYNCHRONIZED);
				t.dispatcher().add(m_ulistener);
				synchronized (m_container) {
					m_container.remove_listener(m_clistner);
					m_container.add_scb(t);
					m_container.add_listener(m_clistner);
				}
			}
		}
	}
}
