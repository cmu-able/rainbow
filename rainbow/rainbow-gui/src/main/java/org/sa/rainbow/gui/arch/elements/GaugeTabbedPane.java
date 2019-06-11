package org.sa.rainbow.gui.arch.elements;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.Pair;
import org.sa.rainbow.gui.arch.model.RainbowArchGaugeModel;

public class GaugeTabbedPane extends JTabbedPane implements PropertyChangeListener {
	private RainbowArchGaugeModel m_gaugeModel;

	public RainbowArchGaugeModel getGaugeModel() {
		return m_gaugeModel;
	}

	public void setGaugeModel(RainbowArchGaugeModel gaugeModel) {
		m_gaugeModel = gaugeModel;
	}

	private GaugeDetailPanel m_gaugeDetailPanel;
	private JTable m_publishedOperations;

	public GaugeTabbedPane() {
		setTabPlacement(JTabbedPane.BOTTOM);

		JScrollPane scrollPane = new JScrollPane();
		addTab("Operations", null, scrollPane, null);

		m_publishedOperations = new JTable(
				new DefaultTableModel(new Object[][] {}, new String[] { "Operation", "Target", "Parameters" }));

		scrollPane.setViewportView(m_publishedOperations);

		m_gaugeDetailPanel = new GaugeDetailPanel();
		addTab("Specification", null, m_gaugeDetailPanel, null);
	}

	public void initDataBindings(RainbowArchGaugeModel model) {
		if (model == m_gaugeModel)
			return;
		if (model != null) {
			model.removePropertyChangeListener(this);
		}
		m_gaugeModel = model;
//		clearTable((DefaultTableModel) m_publishedOperations.getModel());
		m_publishedOperations.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "Operation", "Target", "Parameters" }));
		m_gaugeDetailPanel.initDataBindings(model);
		updateOperationTable(model);
		m_gaugeModel.addPropertyChangeListener(this);
	}

	protected void updateOperationTable(RainbowArchGaugeModel model) {
		List<Pair<Date, IRainbowOperation>> operations = new ArrayList<>();
		for (Entry<String, List<Pair<Date, IRainbowOperation>>> op : model.getOperations().entrySet()) {
			operations.addAll(op.getValue());
		}
		for (int i = 0; i < Math.min(100, operations.size()); i++) {
			String[] row = getOperationData(operations.get(i).secondValue());
			((DefaultTableModel) m_publishedOperations.getModel()).addRow(row);
		}
	}

	public static void clearTable(DefaultTableModel m) {
		for (int i = 0; i < m.getRowCount(); i++) {
			m.removeRow(0);
		}
	}

	public static String[] getOperationData(IRainbowOperation command) {
		String[] data = new String[4];
		data[0] = command.getName();
		data[1] = command.getTarget();
		StringBuffer params = new StringBuffer();
		for (String p : command.getParameters()) {
			params.append(p);
			params.append(",");
		}
		data[2] = params.toString();
		return data;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		switch (evt.getPropertyName()) {
		case RainbowArchGaugeModel.GAUGEREPORT:
			IRainbowOperation op = (IRainbowOperation) evt.getNewValue();
			if (m_gaugeModel.getId().equals(op.getOrigin())) {
				String[] operationData = getOperationData(op);
				((DefaultTableModel) m_publishedOperations.getModel()).insertRow(0, operationData);
			}
			break;
		case RainbowArchGaugeModel.GAUGEREPORTS:
			List<IRainbowOperation> ops = (List) evt.getNewValue();
			for (IRainbowOperation o : ops) {
				if (m_gaugeModel.getId().equals(o.getOrigin()))
					((DefaultTableModel) m_publishedOperations.getModel()).insertRow(0, getOperationData(o));
			}
			break;
		}
	}
}
