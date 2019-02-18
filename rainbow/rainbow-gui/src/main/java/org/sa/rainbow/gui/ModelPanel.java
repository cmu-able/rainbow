package org.sa.rainbow.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.IModelsManager;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.core.ports.RainbowPortFactory;

public class ModelPanel extends JPanel implements IModelsManager, IRainbowModelChangeCallback {
	public class ModelErrorRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			TableModel tm = table.getModel();
			if (!"false".equals(tm.getValueAt(row, 4))) {
				c.setBackground(Color.RED);
			}
			return c;
		}
	}

	private JTable m_table;
	private IModelChangeBusSubscriberPort m_modelChangePort;
	private ModelReference m_ref;
	private ArrayList<Runnable> m_updaters = new ArrayList<> (1);

	/**
	 * Create the panel.
	 * 
	 * @throws RainbowConnectionException
	 */
	public ModelPanel(ModelReference ref) throws RainbowConnectionException {
		m_ref = ref;
		setLayout(new BorderLayout(0, 0));
		Object[][] data = {};
		Object[] colNames = { "Operation", "Target", "Parameters", "Origin", "State" };
		m_table = new JTable(new DefaultTableModel(data, colNames));
		m_table.removeColumn(m_table.getColumnModel().getColumn(4));
		m_table.setDefaultRenderer(Object.class, new ModelErrorRenderer());
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
					return ref.getModelName().equals(message.getProperty(IModelChangeBusPort.MODEL_NAME_PROP))
							&& ref.getModelType().equals(message.getProperty(IModelChangeBusPort.MODEL_TYPE_PROP));
				}
			}, this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void requestModelUpdate(IRainbowOperation command) throws IllegalStateException, RainbowException {
		if (!command.getModelReference().equals(m_ref))
			return;
		addOperation(command, false);
		for (Runnable r : m_updaters) {
			r.run();
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
	public void requestModelUpdate(List<IRainbowOperation> commands, boolean transaction)
			throws IllegalStateException, RainbowException {
		DefaultTableModel tableModel = (DefaultTableModel) m_table.getModel();
		for (IRainbowOperation command : commands) {

			if (!command.getModelReference().equals(m_ref))
				return;
			String[] data = getTableData(command, false);
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
		if (modelName == null)
			throw new IllegalArgumentException("The message does not represent an operation");
		String commandName = (String) message.getProperty(IModelChangeBusPort.COMMAND_PROP);
		String target = (String) message.getProperty(IModelChangeBusPort.TARGET_PROP);
		List<String> params = new LinkedList<>();
		int i = 0;
		String numParams = (String) message.getProperty(IModelChangeBusPort.PARAMETER_PROP + i);
		while (numParams != null) {
			params.add(numParams);
			numParams = (String) message.getProperty(IModelChangeBusPort.PARAMETER_PROP + (++i));
		}
		OperationRepresentation rep = new OperationRepresentation(commandName, this.m_ref, target,
				params.toArray(new String[0]));
		return rep;
	}

	@Override
	public void onEvent(ModelReference reference, IRainbowMessage message) {
		if (message.getProperty(IModelChangeBusPort.PARENT_ID_PROP) != null)
			return;
		IRainbowOperation op = msgToOperation(message);
		addOperation(op, false);
		for (Runnable runnable : m_updaters) {
			runnable.run();
		}
	}

	void addOperation(IRainbowOperation op, boolean error) {
		DefaultTableModel tableModel = (DefaultTableModel) m_table.getModel();
		String[] data = getTableData(op, error);
		tableModel.addRow(data);
		m_table.setModel(tableModel);
		tableModel.fireTableDataChanged();
	}

	private static final Pattern strPattern = Pattern.compile("O\\[(.*):(.*)/(.*)\\.(.*)\\((.*)\\)<?(.*)\\]");

	public static OperationRepresentation pullOutOfString(String msg) {
		Matcher m = strPattern.matcher(msg);
		if (m.find()) {
			OperationRepresentation rep = new OperationRepresentation(m.group(4),
					new ModelReference(m.group(1), m.group(2)), m.group(3),
					m.group(5).replaceAll("\\[", "").replaceAll("\\]", "").split(","));
			rep.setOrigin(m.group(6));
			return rep;
		}
		return null;
	}

	public void processReport(ReportType type, String message) {
		if (type == ReportType.ERROR || type == ReportType.FATAL) {
			// if (message.startsWith("Error executing command") ||
			// message.startsWith("Could not form the command")) {
			try {
				OperationRepresentation op = pullOutOfString(message);
				if (op != null && op.getModelReference().equals(m_ref))
					addOperation(op, type == ReportType.ERROR || type == ReportType.FATAL);
			} catch (Throwable t) {
			}
		 }
	}

	public void addUpdateListener(Runnable listener) {
		m_updaters.add(listener);
	}

}
