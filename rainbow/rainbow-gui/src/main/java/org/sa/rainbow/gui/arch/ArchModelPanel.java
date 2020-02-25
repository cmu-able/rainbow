package org.sa.rainbow.gui.arch;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import org.sa.rainbow.core.IRainbowEnvironment;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowEnvironmentDelegate;
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
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.gui.JTableCellDisplayer;
import org.sa.rainbow.gui.ModelPanel;
import org.sa.rainbow.gui.arch.elements.IUIReporter;
import org.sa.rainbow.gui.arch.elements.IUIUpdater;
import org.sa.rainbow.gui.widgets.ModelErrorRenderer;
import org.sa.rainbow.gui.widgets.TableColumnAdjuster;

public class ArchModelPanel extends JPanel
		implements IUIUpdater, IUIReporter, IModelsManager, IRainbowModelChangeCallback {
	public JTable m_table;
	private IModelChangeBusSubscriberPort m_modelChangePort;
	private ModelReference m_ref;
	private ArrayList<Runnable> m_updaters = new ArrayList<>(1);
	private HashMap<String, Integer> m_op2row = new HashMap<>();
	
	protected static IRainbowEnvironment m_rainbowEnvironment = new RainbowEnvironmentDelegate();


	public ArchModelPanel(ModelReference ref) {
		m_ref = ref;
		setLayout(new BorderLayout(0, 0));
		Object[][] data = {};
		Object[] colNames = { "Operation", "Target", "Parameters", "Origin", "State" };
		m_table = new JTable(new DefaultTableModel(data, colNames));
		m_table.removeColumn(m_table.getColumnModel().getColumn(4));
		m_table.setDefaultRenderer(Object.class,
				new ModelErrorRenderer((tm, row) -> "true".equals(tm.getValueAt(row, 4))));
		JScrollPane p = new JScrollPane(m_table);
		p.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		p.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		add(p, BorderLayout.CENTER);
//		m_table.setAutoscrolls(true);
		m_table.addComponentListener(new JTableCellDisplayer(m_table));

		IModelInstance<Object> mi = m_rainbowEnvironment.getRainbowMaster().modelsManager().getModelInstance(ref);
		m_table.setPreferredScrollableViewportSize(new Dimension(200, 120));
		m_table.setFont(new Font(m_table.getFont().getFontName(), m_table.getFont().getStyle(), 8));
		m_table.getTableHeader()
				.setFont(new Font(m_table.getTableHeader().getFont().getFontName(), m_table.getFont().getStyle(), 8));

		TableColumnAdjuster tca = new TableColumnAdjuster(m_table);
		tca.setDynamicAdjustment(true);

	}

	@Override
	public void requestModelUpdate(IRainbowOperation command) throws IllegalStateException, RainbowException {
		addOperation(command, false, false);
	}

	@Override
	public void requestModelUpdate(List<IRainbowOperation> commands, boolean transaction)
			throws IllegalStateException, RainbowException {
		for (IRainbowOperation op : commands) {
			addOperation(op, false, true);
		}
	}

	@Override
	public <T> IModelInstance<T> getModelInstance(ModelReference modelRef) {
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
		EventQueue.invokeLater(() -> {
			if (message.getProperty(IModelChangeBusPort.PARENT_ID_PROP) != null)
				return;
			IRainbowOperation op = msgToOperation(message);
			addOperation(op, false, false);
			for (Runnable runnable : m_updaters) {
				runnable.run();
			}
		});
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

	public void addOperation(IRainbowOperation op, boolean error, boolean extend) {
		DefaultTableModel tableModel = (DefaultTableModel) m_table.getModel();
		String[] data = getTableData(op, error);
		Integer row = m_op2row.get(op.getName() + op.getTarget());
		if (row == null) {
			row = m_op2row.size();
			m_op2row.put(op.getName() + op.getTarget(), row);
			tableModel.addRow(data);
		} else {
			tableModel.setValueAt(data[1], row, 1);
			tableModel.setValueAt(data[2], row, 2);
			tableModel.setValueAt(data[3], row, 3);
			tableModel.setValueAt(data[4], row, 4);
		}
		if (!error)
			m_table.changeSelection(row, 0, false, extend);
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

	public void processReport(ReportType type, String message) {
		if (type == ReportType.ERROR || type == ReportType.FATAL) {
			// if (message.startsWith("Error executing command") ||
			// message.startsWith("Could not form the command")) {
			try {
				OperationRepresentation op = ModelPanel.pullOutOfString(message);
				if (op != null && op.getModelReference().equals(m_ref))
					addOperation(op, type == ReportType.ERROR || type == ReportType.FATAL, false);
			} catch (Throwable t) {
			}
		}
	}

	@Override
	public void addUpdateListener(Runnable listener) {
		m_updaters.add(listener);
	}

	@Override
	public boolean isModelLocked(ModelReference modelRef) {
		// TODO Auto-generated method stub
		return false;
	}

}
