package org.sa.rainbow.gui;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.core.ports.RainbowPortFactory;

public class ModelPanel extends JPanel implements IModelsManager, IRainbowModelChangeCallback{
	private JTable m_table;
	private IModelChangeBusSubscriberPort m_modelChangePort;
	private ModelReference m_ref;

	/**
	 * Create the panel.
	 * @throws RainbowConnectionException 
	 */
	public ModelPanel(ModelReference ref) throws RainbowConnectionException {
		m_ref = ref;
		setLayout(new BorderLayout(0, 0));
		Object[][] data = {};
		Object[] colNames = {"Operation", "Target", "Parameters", "Origin"};
		m_table = new JTable(new DefaultTableModel(data, colNames));
		
		JScrollPane p = new JScrollPane(m_table);
		p.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		p.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		
		add(p, BorderLayout.CENTER);
		m_table.setAutoscrolls(true);
		m_table.addComponentListener(new JTableCellDisplayer(m_table));
		try {
			m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort();
			
			m_modelChangePort.subscribe(new IRainbowChangeBusSubscription() {
				
				@Override
				public boolean matches(IRainbowMessage message) {
					return ref.getModelName().equals(message.getProperty(IModelChangeBusPort.MODEL_NAME_PROP)) &&
							ref.getModelType().equals(message.getProperty(IModelChangeBusPort.MODEL_TYPE_PROP));
				}
			}, this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void requestModelUpdate(IRainbowOperation command) throws IllegalStateException, RainbowException {
		if (!command.getModelReference().equals(m_ref)) return;
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
		data[3] = command.getOrigin();
		return data;
	}

	@Override
	public void requestModelUpdate(List<IRainbowOperation> commands, boolean transaction)
			throws IllegalStateException, RainbowException {
		DefaultTableModel tableModel = (DefaultTableModel )m_table.getModel();
		for (IRainbowOperation command : commands) {
			
		if (!command.getModelReference().equals(m_ref)) return;
			String[] data = getTableData(command);
			tableModel.addRow(data);
		}
		m_table.setModel(tableModel);
		tableModel.fireTableDataChanged();
	}

	@Override
	public <T> IModelInstance<T> getModelInstance(ModelReference modelRef) {
		return null;
	}

	@Override
	public void registerModelType(String typeName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<? extends String> getRegisteredModelTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends IModelInstance<?>> getModelsOfType(String modelType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void registerModel(ModelReference modelRef, IModelInstance<?> model) throws RainbowModelException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> IModelInstance<T> copyInstance(ModelReference modelRef, String copyName) throws RainbowModelException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void unregisterModel(IModelInstance<?> model) throws RainbowModelException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> IModelInstance<T> getModelInstanceByResource(String resource) {
		// TODO Auto-generated method stub
		return null;
	}

	public IRainbowOperation msgToOperation(IRainbowMessage message) {
		String modelName = (String) message.getProperty(IModelChangeBusPort.MODEL_NAME_PROP);
		if (modelName == null) throw new IllegalArgumentException ("The message does not represent an operation");
		String commandName = (String )message.getProperty(IModelChangeBusPort.COMMAND_PROP);
		String target = (String )message.getProperty(IModelChangeBusPort.TARGET_PROP);
		List<String> params = new LinkedList<> ();
		int i = 0;
		String numParams = (String )message.getProperty(IModelChangeBusPort.PARAMETER_PROP + i);
		while (numParams != null) {
			params.add(numParams);
			numParams = (String )message.getProperty(IModelChangeBusPort.PARAMETER_PROP + (++i));
		}
		OperationRepresentation rep = new OperationRepresentation(commandName, this.m_ref, target, params.toArray(new String[0]));
		return rep;
	}
	
	@Override
	public void onEvent(ModelReference reference, IRainbowMessage message) {
		if (message.getProperty(IModelChangeBusPort.PARENT_ID_PROP) != null)
			return;
		IRainbowOperation op = msgToOperation(message);
		addOperation(op);
	}

	void addOperation(IRainbowOperation op) {
		DefaultTableModel tableModel = (DefaultTableModel )m_table.getModel();
		String[] data = getTableData(op);
		tableModel.addRow(data);
		m_table.setModel(tableModel);
		tableModel.fireTableDataChanged();
	}

}
