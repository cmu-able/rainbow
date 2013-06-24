package incubator.il.ui;

import incubator.ctxaction.ActionContext;
import incubator.il.IMutexStatus;
import incubator.il.srv.IMutexManagerRemoteAccess;
import incubator.ui.bean.BeanTableModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model with the mutex list.
 */
class MutexTableModel extends BeanTableModel {
	/**
	 * Version for serialization.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Last report received.
	 */
	private Map<String, IMutexStatus> report;
	
	/**
	 * The remote access.
	 */
	private IMutexManagerRemoteAccess m_remote;
	
	
	/**
	 * Creates a new model.
	 * @param remote the remote access object
	 * @param actionContext the execution context
	 * @throws Exception failed to create the model
	 */
	MutexTableModel(IMutexManagerRemoteAccess remote,
			ActionContext actionContext) throws Exception {
		super("/incubator/il/ui/MutexTableModel.properties",
				MutexTableModel.class.getClassLoader());
		report = new HashMap<>();
		m_remote = remote;
	}
	
	/**
	 * Updates the model data.
	 */
	void refresh_data() {
		report = m_remote.getStatusReport();
		List<Object> beans = new ArrayList<>();
		for (String s : report.keySet()) {
			IMutexStatus status = report.get(s);
			beans.add(new IMutexStatusBean(s, status));
		}
		
		synchronize(beans);
	}
	
	/**
	 * Obtains the state of a mutex given its name.
	 * @param name the mutex name
	 * @return the mutex status
	 */
	IMutexStatus mutex_status(String name) {
		assert name != null;
		return report.get(name);
	}
	
	/**
	 * Obtains the name of a mutex in a row.
	 * @param row the row number
	 * @return the mutex name
	 */
	String getMutexNameAt(int row) {
		return ((IMutexStatusBean) bean(row)).getName();
	}
}