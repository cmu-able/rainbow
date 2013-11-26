package incubator.scb.sync;

import incubator.pval.Ensure;
import incubator.scb.SerializableScb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Superclass for all SCBs that can be synchronized.
 * @param <ID_TYPE> data type of the bean's ID
 * @param <T> data type of the bean
 */
public abstract class SyncScb<ID_TYPE, T extends SyncScb<ID_TYPE, T>>
		extends SerializableScb<T> {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The bean's ID.
	 */
	private ID_TYPE m_id;
	
	/**
	 * Synchronization status.
	 */
	private transient SyncStatus m_sync_status;
	
	/**
	 * Are we currently synchronizing. If we are, notifications should not
	 * be sent but the {@link #m_fired} variable should be set to
	 * <code>true</code> instead.
	 */
	private transient boolean m_synchronizing;
	
	/**
	 * If {@link #m_synchronizing} is <code>true</code>, this variable is
	 * set to <code>true</code> instead of sending notifications. It allows
	 * detection of whether the bean has changed.
	 */
	private transient boolean m_fired;
	
	/**
	 * Creates a new bean.
	 * @param id the bean's ID
	 * @param sync_status the initial synchronization status
	 * @param my_class the bean class
	 */
	protected SyncScb(ID_TYPE id, SyncStatus sync_status, Class<T> my_class) {
		Ensure.not_null(sync_status);
		Ensure.not_null(my_class);
		Ensure.is_instance(this, my_class);
		
		m_id = id;
		m_sync_status = sync_status;
	}
	
	/**
	 * Obtains the bean ID.
	 * @return the ID
	 */
	public synchronized ID_TYPE id() {
		return m_id;
	}
	
	/**
	 * Obtains the bean's synchronization status.
	 * @return the synchronization status
	 */
	public synchronized SyncStatus sync_status() {
		return m_sync_status;
	}
	
	/**
	 * Defines the synchronization status.
	 * @param s the status
	 */
	public synchronized void sync_status(SyncStatus s) {
		Ensure.not_null(s);
		if (s != m_sync_status) {
			m_sync_status = s;
			fire_update();
		}
	}
	
	@SuppressWarnings("javadoc")
	private void readObject(java.io.ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		Ensure.not_null(in);
		in.defaultReadObject();
		m_sync_status = SyncStatus.UNKNOWN;
	}
	
	@Override
	protected void fire_update() {
		if (m_synchronizing) {
			m_fired = true;
		} else {
			super.fire_update();
		}
	}
	
	/**
	 * Synchronizes this SCB with the given one.
	 * @param t the SCB to synchronize with
	 */
	public synchronized final void sync_with(T t) {
		m_synchronizing = true;
		m_fired = false;
		
		try {
			sync(t);
		} finally {
			m_synchronizing = false;
		}
		
		/*
		 * Fire now if something changed but, because we're doing it here,
		 * we only fire once.
		 */
		if (m_fired) {
			fire_update();
		}
	}
	
	/**
	 * Makes a copy of a sync SCB marshalling and unmarshalling it.
	 * @param t the bean
	 * @param <ID_TYPE> data type of the bean's ID
	 * @param <T> data type of the bean
	 * @return the bean copy
	 */
	static <ID_TYPE, T extends SyncScb<ID_TYPE, T>> T duplicate(T t) {
		Ensure.not_null(t);
		
		byte[] d = null;
		try (ByteArrayOutputStream bo = new ByteArrayOutputStream();
				ObjectOutputStream os = new ObjectOutputStream(bo)) {
			os.writeObject(t);
			os.flush();
			d = bo.toByteArray();
		} catch (IOException e) {
			Ensure.never_thrown(e);
		}
		
		try (ByteArrayInputStream bi = new ByteArrayInputStream(d);
				ObjectInputStream is = new ObjectInputStream(bi)) {
			@SuppressWarnings("unchecked")
			T tt = (T) is.readObject();
			t = tt;
		} catch (IOException | ClassNotFoundException e) {
			Ensure.never_thrown(e);
		}
		
		return t;
	}
	
	/**
	 * Updates this bean setting its data to the same one <code>t</code> has.
	 * {@link #fire_update()} should be fired if there are modifications.
	 * It may be fired multiple times.
	 * @param t the object to copy data from
	 */
	protected abstract void sync(T t);
}
