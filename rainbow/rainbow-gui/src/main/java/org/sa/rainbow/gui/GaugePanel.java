package org.sa.rainbow.gui;

import javax.swing.JPanel;

import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelUpdater;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

public class GaugePanel extends JPanel implements IModelUpdater{
	private JTable m_table;
	private IModelUSBusPort m_usPort;
	private String m_gaugeId;

	/**
	 * Create the panel.
	 */
	public GaugePanel(String gaugeId) {
		m_gaugeId = gaugeId;
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		Object[][] data = {};
		Object[] colNames = {"Operation", "Target", "Parameters"};
		m_table = new JTable(new DefaultTableModel(data, colNames));
		m_table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		scrollPane.setViewportView(m_table);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		m_table.setAutoscrolls(true);
		m_table.addComponentListener(new JTableCellDisplayer(m_table));
		try {
			m_usPort = RainbowPortFactory.createModelsManagerUSPort(this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void requestModelUpdate(IRainbowOperation command) throws IllegalStateException, RainbowException {
		if (!command.getOrigin().equals(m_gaugeId)) return;
		addOperation(command);
	}
	
	private String[] getTableData(IRainbowOperation command) {
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
	public void requestModelUpdate(List<IRainbowOperation> commands, boolean transaction)
			throws IllegalStateException, RainbowException {
		DefaultTableModel tableModel = (DefaultTableModel )m_table.getModel();
		for (IRainbowOperation command : commands) {
			
			if (!command.getOrigin().equals(m_gaugeId)) return;
			String[] data = getTableData(command);
			tableModel.addRow(data);
		}
		m_table.setModel(tableModel);
		tableModel.fireTableDataChanged();		
	}

	@Override
	public <T> IModelInstance<T> getModelInstance(ModelReference modelRef) {
		// TODO Auto-generated method stub
		return null;
	}
	
	void addOperation(IRainbowOperation op) {
		DefaultTableModel tableModel = (DefaultTableModel )m_table.getModel();
		String[] data = getTableData(op);
		tableModel.addRow(data);
		m_table.setModel(tableModel);
		tableModel.fireTableDataChanged();
	}

	public void processReport(ReportType type, String message) {
		// TODO Auto-generated method stub
		
	}

}
