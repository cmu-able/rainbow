package incubator.il.ui;

import incubator.ctxaction.ActionContext;
import incubator.il.IMutexRequest;
import incubator.il.IMutexStatus;
import incubator.ui.bean.BeanTableModel;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.lang.ObjectUtils;

/**
 * Model with the list of waiting threads.
 */
class MutexDataTableModel extends BeanTableModel {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Mutex name to show.
	 */
	private String m_mutex_name;
	
	/**
	 * Mutex state.
	 */
	private IMutexStatus m_status;
	
	/**
	 * Mutex table model.
	 */
	private MutexTableModel m_mutex_table_model;

	/**
	 * Creates a new model.
	 * @param table_model the mutex table model
	 * @param action_context the action context
	 * @throws Exception failed to initialize the model
	 */
	public MutexDataTableModel(MutexTableModel table_model,
			ActionContext action_context) throws Exception {
		super("/incubator/il/ui/MutexDataTableModel.properties",
				MutexDataTableModel.class.getClassLoader());
		this.m_mutex_table_model = table_model;
		this.m_mutex_name = null;
		this.m_status = null;
	}
	
	/**
	 * Defines which is the new mutex to show data from.
	 * @param name the mutex name, can be <code>null</code>
	 */
	public void set_mutex(String name) {
		if (ObjectUtils.equals(name, m_mutex_name)) {
			return;
		}
		
		m_mutex_name = name;
		if (name == null) {
			m_status = null;
		} else {
			m_status = m_mutex_table_model.mutex_status(m_mutex_name);
		}
		
		do_resync();
	}
	
	/**
	 * Synchronizes the data model.
	 */
	private void do_resync() {
		if (m_status == null || m_status.lock_request() == null) {
			synchronize(new ArrayList<>());
		} else {
			ArrayList<Object> l = new ArrayList<>();
			l.add(new DataBean(0, m_status.lock_request()));
			int row = 1;
			for (Iterator<IMutexRequest> it =
					m_status.wait_list().iterator(); it.hasNext(); row++) {
				l.add(new DataBean(row, it.next()));
			}
			
			synchronize(l);
		}
	}
	
	/**
	 * Refreshes the model data.
	 */
	void refresh_data() {
		if (m_mutex_name == null) {
			return;
		}
		
		m_status = m_mutex_table_model.mutex_status(m_mutex_name);
		do_resync();
	}
	
	/**
	 * Obtains the request corresponding to a model line.
	 * @param row the line
	 * @return the mutex request
	 */
	public IMutexRequest request(int row) {
		if (m_status.lock_request() != null) {
			if (row == 0) {
				return m_status.lock_request();
			} else {
				row--;
			}
		}
		
		return m_status.wait_list().get(row);
	}
}