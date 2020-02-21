package incubator.scb.sync;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;

import incubator.pval.Ensure;
import incubator.scb.ScbField;

/**
 * SCB for tests that contains a string of data.
 */
public class TestSyncScb extends SyncScb<Integer, TestSyncScb> {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The SCB's data.
	 */
	private String m_data;

	/**
	 * Creates a new sync SCB.
	 * @param id the SCB's ID
	 * @param sync_status the synchronization status
	 * @param data the SCB data
	 */
	protected TestSyncScb(Integer id, SyncStatus sync_status, String data) {
		super(id, sync_status, TestSyncScb.class);
		m_data = data;
	}
	
	/**
	 * Obtains the SCB's data.
	 * @return the SCB's data
	 */
	public String data() {
		return m_data;
	}
	
	/**
	 * Sets the SCB's data.
	 * @param data the SCB's data
	 */
	public void data(String data) {
		if (!ObjectUtils.equals(data, m_data)) {
			m_data = data;
			fire_update();
		}
	}
	
	@Override
	public void sync(TestSyncScb t) {
		Ensure.not_null(t);
		data(t.m_data);
	}

	@Override
	public List<ScbField<TestSyncScb, ?>> fields() {
		return null;
	}

	@Override
	protected Class<TestSyncScb> my_class() {
		return TestSyncScb.class;
	}
}

