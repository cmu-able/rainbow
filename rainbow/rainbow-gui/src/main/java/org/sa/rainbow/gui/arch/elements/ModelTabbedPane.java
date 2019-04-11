package org.sa.rainbow.gui.arch.elements;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.Pair;
import org.sa.rainbow.gui.arch.model.RainbowArchModelModel;
import org.sa.rainbow.gui.widgets.TableColumnAdjuster;

public class ModelTabbedPane extends JTabbedPane implements PropertyChangeListener {

	private RainbowArchModelModel m_model;
	private ModelInfoPanel m_modelInfo;
	private JTable m_table;

	class OperationData {
		IRainbowOperation op;
		Date date;
		boolean error;

		public OperationData(IRainbowOperation op, Date d, boolean error) {
			this.op = op;
			date = d;
			this.error = error;

		}
	}

	public ModelTabbedPane() {
		setTabPlacement(JTabbedPane.BOTTOM);

		JScrollPane scrollPane = new JScrollPane();
		addTab("Operations", null, scrollPane, null);
		Object[][] data = {};
		Object[] colNames = { "Operation", "Target", "Parameters", "Origin", "State" };
		m_table = new JTable(new DefaultTableModel(data, colNames));
		scrollPane.setViewportView(m_table);
		TableColumnAdjuster tca = new TableColumnAdjuster(m_table);
		tca.setDynamicAdjustment(true);
		
		m_modelInfo = new ModelInfoPanel();
		addTab("Specification", m_modelInfo);
	}

	public void initDataBindings(RainbowArchModelModel model) {
		if (model == m_model)
			return;
		if (m_model != null)
			m_model.removePropertyChangeListener(this);
		m_modelInfo.initDataBinding(model);
		List<Pair<Date, IRainbowOperation>> reports = new LinkedList<>();
		List<OperationData> collection = m_model.getReports().stream()
				.map(p -> new OperationData(p.secondValue(), p.firstValue(), false)).collect(Collectors.toList());
		collection.addAll(m_model.getErrors().stream()
				.map(p -> new OperationData(p.secondValue(), p.firstValue(), true)).collect(Collectors.toList()));
		Collections.sort(collection, new Comparator<OperationData>() {

			@Override
			public int compare(OperationData o1, OperationData o2) {
				return o2.date.compareTo(o1.date);
			}
		});

		for (OperationData d : collection) {
			for (int i = 0; i < Math.min(100, collection.size()); i++) {
				OperationData od = collection.get(i);
				String[] row = getTableData(od.op, od.error);
				((DefaultTableModel) m_table.getModel()).addRow(row);
			}
		}

	}

	private String[] getTableData(IRainbowOperation command, boolean inerror) {
		String[] data = new String[5];
		data[0] = command.getName();
		data[1] = command.getTarget();
		StringBuffer params = new StringBuffer();
		for (String p : command.getParameters()) {
			params.append(p);
			params.append(",");
		}
		data[2] = params.toString();
		data[3] = command.getOrigin();
		data[4] = Boolean.toString(inerror);
		return data;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (evt.getPropertyName()) {
		case RainbowArchModelModel.OPERATION_PROP:
			String[] row = getTableData((IRainbowOperation )evt.getNewValue(), false);
			((DefaultTableModel) m_table.getModel()).insertRow(0,row);
			break;
		case RainbowArchModelModel.OPERATION__ERROR_PROP:
			row = getTableData((IRainbowOperation )evt.getNewValue(), true);
			((DefaultTableModel) m_table.getModel()).insertRow(0,row);
			break;

		}
	}
}
