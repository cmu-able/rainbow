package incubator.scb.sync;

import incubator.pval.Ensure;

import java.io.Serializable;

/**
 * Operation performed in an SCB container. Lists of these operations are
 * sent from slaves to the master and from the master to slaves to keep
 * data synchronized.
 */
public class ScbOperation implements Serializable {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The key used to identify the container.
	 */
	private String m_key;
	
	/**
	 * The incoming object if the operation is an incoming operation (create
	 * or update), <code>null</code> otherwise.
	 */
	private Object m_incoming;
	
	/**
	 * The ID key if the operation is a delete operation, <code>null</code
	 * otherwise.
	 */
	private Object m_id;
	
	/**
	 * Creates a new operation.
	 * @param key the key used to identify the operation
	 * @param incoming the incoming object if the operation is an incoming
	 * (create or update) operation, <code>null</code> otherwise
	 * @param id the ID of the SCB to delete, <code>null</code> otherwise
	 */
	private ScbOperation(String key, Object incoming, Object id) {
		Ensure.not_null(key);
		m_key = key;
		m_incoming = incoming;
		
		if (m_incoming == null) {
			Ensure.not_null(id);
		} else {
			Ensure.is_null(id);
		}
		
		m_id = id;
	}
	
	/**
	 * Creates a new incoming operation (create or update).
	 * @param key the container key where the bean should be created or
	 * update
	 * @param scb the bean to create or update
	 * @return the created operation
	 */
	public static ScbOperation make_incoming(String key, Object scb) {
		Ensure.not_null(key);
		Ensure.not_null(scb);
		return new ScbOperation(key, scb, null);
	}
	
	/**
	 * Creates a new delete operation.
	 * @param key the container key where the bean should be delete
	 * @param id the ID of the SCB to delete
	 * @return the created operation
	 */
	public static ScbOperation make_delete(String key, Object id) {
		Ensure.not_null(key);
		Ensure.not_null(id);
		return new ScbOperation(key, null, id);
	}
	
	/**
	 * The key identifying the container as in
	 * {@link SyncScbMasterImpl#create_container(String, Class, Class)}
	 * @return the key
	 */
	public String container_key() {
		return m_key;
	}
	
	/**
	 * Obtains the ID of the SCB to delete.
	 * @return the key or <code>null</code> if this is not a delete operation
	 */
	public Object delete_key() {
		return m_id;
	}
	
	/**
	 * Obtains the incoming SCB.
	 * @return the incoming SCB or <code>null</code> if this is not an
	 * incoming operation
	 */
	public Object incoming() {
		return m_incoming;
	}
}
