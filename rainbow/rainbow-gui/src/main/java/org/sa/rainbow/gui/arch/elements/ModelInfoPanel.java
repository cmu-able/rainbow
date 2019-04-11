package org.sa.rainbow.gui.arch.elements;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.gui.arch.model.RainbowArchModelModel;
import org.sa.rainbow.gui.widgets.TableColumnAdjuster;

public class ModelInfoPanel extends JPanel {
	private JTextField m_modelName;
	private JTextField m_modelType;
	private JTextField m_sourceText;
	private JTable m_table;
	private RainbowArchModelModel m_selectedModel;

	public ModelInfoPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JLabel lblName = new JLabel("Name:");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		add(lblName, gbc_lblName);

		m_modelName = new JTextField();
		m_modelName.setEditable(false);
		GridBagConstraints gbc_modelName = new GridBagConstraints();
		gbc_modelName.insets = new Insets(0, 0, 5, 0);
		gbc_modelName.fill = GridBagConstraints.HORIZONTAL;
		gbc_modelName.gridx = 1;
		gbc_modelName.gridy = 0;
		add(m_modelName, gbc_modelName);
		m_modelName.setColumns(10);

		JLabel lblType = new JLabel("Type:");
		GridBagConstraints gbc_lblType = new GridBagConstraints();
		gbc_lblType.anchor = GridBagConstraints.EAST;
		gbc_lblType.insets = new Insets(0, 0, 5, 5);
		gbc_lblType.gridx = 0;
		gbc_lblType.gridy = 1;
		add(lblType, gbc_lblType);

		m_modelType = new JTextField();
		m_modelType.setEditable(false);
		GridBagConstraints gbc_modelType = new GridBagConstraints();
		gbc_modelType.insets = new Insets(0, 0, 5, 0);
		gbc_modelType.fill = GridBagConstraints.HORIZONTAL;
		gbc_modelType.gridx = 1;
		gbc_modelType.gridy = 1;
		add(m_modelType, gbc_modelType);
		m_modelType.setColumns(10);

		JLabel lblSource = new JLabel("Source:");
		lblSource.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblSource = new GridBagConstraints();
		gbc_lblSource.anchor = GridBagConstraints.EAST;
		gbc_lblSource.insets = new Insets(0, 0, 5, 5);
		gbc_lblSource.gridx = 0;
		gbc_lblSource.gridy = 2;
		add(lblSource, gbc_lblSource);

		m_sourceText = new JTextField();
		m_sourceText.setEditable(false);
		GridBagConstraints gbc_sourceText = new GridBagConstraints();
		gbc_sourceText.insets = new Insets(0, 0, 5, 0);
		gbc_sourceText.fill = GridBagConstraints.HORIZONTAL;
		gbc_sourceText.gridx = 1;
		gbc_sourceText.gridy = 2;
		add(m_sourceText, gbc_sourceText);
		m_sourceText.setColumns(10);

		JLabel lblOperations = new JLabel("Operations:");
		GridBagConstraints gbc_lblOperations = new GridBagConstraints();
		gbc_lblOperations.anchor = GridBagConstraints.EAST;
		gbc_lblOperations.insets = new Insets(0, 0, 5, 5);
		gbc_lblOperations.gridx = 0;
		gbc_lblOperations.gridy = 3;
		add(lblOperations, gbc_lblOperations);

		m_table = new JTable(new DefaultTableModel(new Object[][] {}, new String[] {"Name", "Target", "Arguments", "Issues"}));
		GridBagConstraints gbc_table = new GridBagConstraints();
		gbc_table.anchor = GridBagConstraints.EAST;
		gbc_table.gridwidth = 2;
		gbc_table.insets = new Insets(0, 0, 0, 5);
		gbc_table.fill = GridBagConstraints.VERTICAL;
		gbc_table.gridx = 0;
		gbc_table.gridy = 4;
		add(m_table, gbc_table);
		TableColumnAdjuster tca = new TableColumnAdjuster(m_table);
	}

	public void initDataBinding(RainbowArchModelModel model) {
		if (model == m_selectedModel) return;
//		if (m_selectedModel != null) m_selected
		GaugeTabbedPane.clearTable((DefaultTableModel )m_table.getModel());
		m_selectedModel = model;
		IModelInstance<Object> rm = Rainbow.instance().getRainbowMaster().modelsManager()
				.getModelInstance(model.getModelRef());
		m_modelName.setText(model.getModelRef().getModelName());
		m_modelType.setText(model.getModelRef().getModelType());
		if (rm != null) {
			m_sourceText.setText(rm.getOriginalSource());
			Method[] methods = rm.getCommandFactory().getClass().getMethods();
			Map<String, Class<? extends AbstractRainbowModelOperation<?, Object>>> commands = rm.getCommandFactory()
					.getCommands();
			Map<String, Method> commandMethods = new HashMap<>();
			Set<String> unhandledCommands = new HashSet<>(commands.keySet());
			Set<Method> unhandledMethods = new HashSet<>();
			for (Method method : methods) {
				if (IRainbowOperation.class.isAssignableFrom(method.getReturnType())) {
					String name = method.getName().toLowerCase();
					if (commands.containsKey(name)) {
						commandMethods.put(name, method);
						unhandledCommands.remove(name);
					} else {
						name = name + "cmd";
						if (commands.containsKey(name)) {
							commandMethods.put(name, method);
							unhandledCommands.remove(name);
						}
						else unhandledMethods.add(method);
					}
					
				}
			}

			for (Entry<String, Method> e : commandMethods.entrySet()) {
				String commandName = e.getKey();
				Object[] row = new Object[] {commandName, "target", "args", "ok"};
				Parameter[] parameters = e.getValue().getParameters();
				if (parameters.length > 0) {
					row[1] = parameters[0].getName() + " : " + parameters[0].getType().getName();
					fillParameters(row, parameters, 1);
				}
				((DefaultTableModel )m_table.getModel()).addRow(row);
			}
			for (String command : unhandledCommands) {
				Object[] row = new Object[] {command, "target : String", "unknown", "unhandled"};
				Class<? extends AbstractRainbowModelOperation<?, Object>> class1 = commands.get(command);
			    Constructor<?> constructor = class1.getConstructors()[0];
			    fillParameters(row, constructor.getParameters(), 2);
				((DefaultTableModel )m_table.getModel()).addRow(row);
			}
			for (Method m : unhandledMethods) {
				Object[] row = new Object[] {m.getName(), "target : String", "unknown", "potential"};
				Parameter[] parameters = m.getParameters();
				if (parameters.length > 0) {
					row[1] = parameters[0].getName() + " : " + parameters[0].getType().getName();
					fillParameters(row, parameters, 1);
				}
				((DefaultTableModel )m_table.getModel()).addRow(row);
			}
		}
	}

	protected void fillParameters(Object[] row, Parameter[] parameters, int start) {
		StringBuffer b = new StringBuffer();
		for (int i = start; i < parameters.length; i++) {
			b.append(parameters[i].getName());
			b.append(" : ");
			b.append(parameters[i].getType().getName());
			if (i + 1 < parameters.length)
				b.append(", ");
		}
		if (b.length() > 0) 
			row[2] = b.toString();
	}

}
