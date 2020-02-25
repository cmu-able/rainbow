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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import org.sa.rainbow.core.IRainbowEnvironment;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowEnvironmentDelegate;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.commands.AbstractLoadModelCmd;
import org.sa.rainbow.core.models.commands.AbstractRainbowModelOperation;
import org.sa.rainbow.core.models.commands.AbstractSaveModelCmd;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.gui.JTableCellDisplayer;
import org.sa.rainbow.gui.arch.model.RainbowArchModelModel;
import org.sa.rainbow.gui.arch.model.RainbowArchModelModel.RainbowModelOperationRepresentation;
import org.sa.rainbow.gui.widgets.ModelErrorRenderer;
import org.sa.rainbow.gui.widgets.TableColumnAdjuster;

public class ModelInfoPanel extends JPanel {

	private JTextField m_modelName;
	private JTextField m_modelType;
	private JTextField m_sourceText;
	private JTable m_table;
	private RainbowArchModelModel m_selectedModel;
	
	protected static IRainbowEnvironment m_rainbowEnvironment = new RainbowEnvironmentDelegate();


	public ModelInfoPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.3, 0.7, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
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
		gbc_modelName.insets = new Insets(0, 0, 5, 5);
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
		gbc_modelType.insets = new Insets(0, 0, 5, 5);
		gbc_modelType.fill = GridBagConstraints.HORIZONTAL;
		gbc_modelType.gridx = 1;
		gbc_modelType.gridy = 1;
		add(m_modelType, gbc_modelType);
		m_modelType.setColumns(10);

		JLabel lblSource = new JLabel("Source:");
		lblSource.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblSource = new GridBagConstraints();
		gbc_lblSource.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblSource.insets = new Insets(0, 0, 0, 5);
		gbc_lblSource.gridx = 0;
		gbc_lblSource.gridy = 2;
		add(lblSource, gbc_lblSource);

		m_sourceText = new JTextField();
		m_sourceText.setEditable(false);
		GridBagConstraints gbc_sourceText = new GridBagConstraints();
		gbc_sourceText.anchor = GridBagConstraints.NORTH;
		gbc_sourceText.insets = new Insets(0, 0, 0, 5);
		gbc_sourceText.fill = GridBagConstraints.HORIZONTAL;
		gbc_sourceText.gridx = 1;
		gbc_sourceText.gridy = 2;
		add(m_sourceText, gbc_sourceText);
		m_sourceText.setColumns(10);

		JLabel lblOperations = new JLabel("Operations:");
		GridBagConstraints gbc_lblOperations = new GridBagConstraints();
		gbc_lblOperations.anchor = GridBagConstraints.WEST;
		gbc_lblOperations.insets = new Insets(0, 0, 5, 0);
		gbc_lblOperations.gridx = 2;
		gbc_lblOperations.gridy = 0;
		add(lblOperations, gbc_lblOperations);

		JScrollPane scrollBar = new JScrollPane();
		GridBagConstraints gbc_table = new GridBagConstraints();
		gbc_table.gridheight = 2;
		gbc_table.fill = GridBagConstraints.BOTH;
		gbc_table.gridx = 2;
		gbc_table.gridy = 1;
		add(scrollBar, gbc_table);

		m_table = new JTable(
				new DefaultTableModel(new Object[][] {}, new String[] { "Name", "Target", "Arguments", "Issues" }));
		scrollBar.setViewportView(m_table);
		TableColumnAdjuster tca = new TableColumnAdjuster(m_table);
		m_table.addComponentListener(new JTableCellDisplayer(m_table));
		m_table.setDefaultRenderer(Object.class,
				new ModelErrorRenderer((tm, row) -> !"".equals(tm.getValueAt(row, 3))));

	}

	public void initDataBinding(RainbowArchModelModel model) {
		if (model == m_selectedModel)
			return;
//		if (m_selectedModel != null) m_selected
		GaugeTabbedPane.clearTable((DefaultTableModel) m_table.getModel());
		m_selectedModel = model;
		IModelInstance<Object> rm = m_rainbowEnvironment.getRainbowMaster().modelsManager()
				.getModelInstance(model.getModelRef());
		m_modelName.setText(model.getModelRef().getModelName());
		m_modelType.setText(model.getModelRef().getModelType());
		if (rm != null) {
			m_sourceText.setText(rm.getOriginalSource());
			Map<String, RainbowModelOperationRepresentation> operationsAccepted = model.getOperationsAccepted();
			for (RainbowModelOperationRepresentation o : operationsAccepted.values()) {
				Object[] row = new Object[] {o.getName(), o.getTarget(), "", o.getWarning()};
				String[] parameters = o.getParameters();
				StringBuffer b = new StringBuffer ();
				for (int i = 0; i < parameters.length; i++) {
					String p = parameters[i];
					b.append(p);
					if (i+1 < parameters.length) b.append(", ");
				}
				row[2] = b.toString();
				((DefaultTableModel) m_table.getModel()).addRow(row);

			}
		}
	}



}
