package org.sa.rainbow.gui.arch.elements;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.gauges.OperationRepresentation;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.util.Pair;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.gui.arch.GaugeInfo;
import org.sa.rainbow.gui.arch.model.RainbowArchGaugeModel;

public class GaugeDetailPanel extends JPanel {
	private JTextField m_nameField;
	private JTextField m_typeField;
	private JTextField m_modelField;
	private JTable m_operations;
	private JTable m_setup;
	private JTable m_config;
	public RainbowArchGaugeModel m_gaugeInfo;

	public GaugeDetailPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JLabel lblName = new JLabel("Name:");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 5, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		add(lblName, gbc_lblName);

		m_nameField = new JTextField();
		m_nameField.setEditable(false);
		GridBagConstraints gbc_nameField = new GridBagConstraints();
		gbc_nameField.insets = new Insets(0, 0, 5, 5);
		gbc_nameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameField.gridx = 1;
		gbc_nameField.gridy = 0;
		add(m_nameField, gbc_nameField);

		JLabel lblNewLabel = new JLabel("Setup Values:");
		lblNewLabel.setVerticalAlignment(SwingConstants.TOP);
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 2;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);

		m_setup = new JTable(new DefaultTableModel(new Object[][] {}, new String[] { "Name", "Type", "Value" }));
		GridBagConstraints gbc_setup = new GridBagConstraints();
		gbc_setup.gridheight = 3;
		gbc_setup.insets = new Insets(0, 0, 5, 0);
		gbc_setup.fill = GridBagConstraints.BOTH;
		gbc_setup.gridx = 3;
		gbc_setup.gridy = 0;
		JScrollPane sp = new JScrollPane(m_setup);
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(sp, gbc_setup);

//		JLabel lblCommands = new JLabel("Published operations:");
//		GridBagConstraints gbc_lblCommands = new GridBagConstraints();
//		gbc_lblCommands.insets = new Insets(0, 0, 5, 0);
//		gbc_lblCommands.anchor = GridBagConstraints.WEST;
//		gbc_lblCommands.gridx = 4;
//		gbc_lblCommands.gridy = 0;
//		add(lblCommands, gbc_lblCommands);

		JLabel lblType = new JLabel("Type:");
		GridBagConstraints gbc_lblType = new GridBagConstraints();
		gbc_lblType.anchor = GridBagConstraints.EAST;
		gbc_lblType.insets = new Insets(0, 0, 5, 5);
		gbc_lblType.gridx = 0;
		gbc_lblType.gridy = 1;
		add(lblType, gbc_lblType);

		m_typeField = new JTextField();
		m_typeField.setEditable(false);
		GridBagConstraints gbc_typeField = new GridBagConstraints();
		gbc_typeField.insets = new Insets(0, 0, 5, 5);
		gbc_typeField.fill = GridBagConstraints.HORIZONTAL;
		gbc_typeField.gridx = 1;
		gbc_typeField.gridy = 1;
		add(m_typeField, gbc_typeField);

//		m_publishedOperations = new JTable(
//				new DefaultTableModel(new Object[][] {}, new String[] { "Operation", "Target", "Parameters" }));
//		GridBagConstraints gbc_publishedOperations = new GridBagConstraints();
//		gbc_publishedOperations.fill = GridBagConstraints.BOTH;
//		gbc_publishedOperations.gridheight = 3;
//		gbc_publishedOperations.gridx = 4;
//		gbc_publishedOperations.gridy = 1;
//		 sp = new JScrollPane(m_publishedOperations);
//		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//		add(sp, gbc_publishedOperations);

		JLabel lblModel = new JLabel("Model:");
		GridBagConstraints gbc_lblModel = new GridBagConstraints();
		gbc_lblModel.anchor = GridBagConstraints.EAST;
		gbc_lblModel.insets = new Insets(0, 0, 5, 5);
		gbc_lblModel.gridx = 0;
		gbc_lblModel.gridy = 2;
		add(lblModel, gbc_lblModel);

		m_modelField = new JTextField();
		m_modelField.setEditable(false);
		GridBagConstraints gbc_modelField = new GridBagConstraints();
		gbc_modelField.insets = new Insets(0, 0, 5, 5);
		gbc_modelField.fill = GridBagConstraints.HORIZONTAL;
		gbc_modelField.gridx = 1;
		gbc_modelField.gridy = 2;
		add(m_modelField, gbc_modelField);

		JLabel lblOperations = new JLabel("Operations:");
		GridBagConstraints gbc_lblOperations = new GridBagConstraints();
		gbc_lblOperations.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblOperations.insets = new Insets(0, 0, 0, 5);
		gbc_lblOperations.gridx = 0;
		gbc_lblOperations.gridy = 3;
		add(lblOperations, gbc_lblOperations);

		m_operations = new JTable(
				new DefaultTableModel(new Object[][] {}, new String[] { "Key", "Operation", "Target", "Parameters" }));
		GridBagConstraints gbc_operations = new GridBagConstraints();
		gbc_operations.insets = new Insets(0, 0, 0, 5);
		gbc_operations.fill = GridBagConstraints.BOTH;
		gbc_operations.gridx = 1;
		gbc_operations.gridy = 3;
		sp = new JScrollPane(m_operations);
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(sp, gbc_operations);

		JLabel lblConfigValues = new JLabel("Config Values:");
		GridBagConstraints gbc_lblConfigValues = new GridBagConstraints();
		gbc_lblConfigValues.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblConfigValues.insets = new Insets(0, 0, 0, 5);
		gbc_lblConfigValues.gridx = 2;
		gbc_lblConfigValues.gridy = 3;
		add(lblConfigValues, gbc_lblConfigValues);

		m_config = new JTable(new DefaultTableModel(new Object[][] {}, new String[] { "Name", "Type", "Value" }));
		GridBagConstraints gbc_config = new GridBagConstraints();
		gbc_config.fill = GridBagConstraints.BOTH;
		gbc_config.gridx = 3;
		gbc_config.gridy = 3;
		sp = new JScrollPane(m_config);
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(sp, gbc_config);

	}

	public void initDataBindings(RainbowArchGaugeModel gi) {
		if (gi == m_gaugeInfo) return;
		if (gi != m_gaugeInfo) {
			m_gaugeInfo = gi;
			GaugeTabbedPane.clearTable((DefaultTableModel) m_operations.getModel());
			GaugeTabbedPane.clearTable((DefaultTableModel) m_setup.getModel());
			GaugeTabbedPane.clearTable((DefaultTableModel) m_config.getModel());
			m_nameField.setText("");
			m_typeField.setText("");
			m_modelField.setText("");
		}
		if (m_gaugeInfo != null) {
			GaugeInstanceDescription desc = m_gaugeInfo.getGaugeDesc();
			m_nameField.setText(desc.gaugeName());
			m_typeField.setText(desc.gaugeType());
			m_modelField.setText(desc.modelDesc().getName() + ":" + desc.modelDesc().getType());
			List<Pair<String, OperationRepresentation>> commandSignatures = desc.commandSignatures();
			for (Pair<String, OperationRepresentation> cs : commandSignatures) {
				String[] od = GaugeTabbedPane.getOperationData(cs.secondValue());
				String[] row = new String[] { cs.firstValue(), od[0], od[1], od[2] };
				((DefaultTableModel) m_operations.getModel()).addRow(row);
			}
			fillPAram(desc.configParams(), m_config);
			fillPAram(desc.setupParams(), m_setup);

			List<Pair<Date, IRainbowOperation>> operations = new ArrayList<>();
			for (Entry<String, List<Pair<Date, IRainbowOperation>>> op : gi.getOperations().entrySet()) {
				operations.addAll(op.getValue());
			}
			Collections.sort(operations, new Comparator<Pair<Date, IRainbowOperation>>() {

				@Override
				public int compare(Pair<Date, IRainbowOperation> o1, Pair<Date, IRainbowOperation> o2) {
					return Long.compare(o1.firstValue().getTime(), o2.firstValue().getTime());
				}
			});

			
		}
	}



	protected void fillPAram(List<TypedAttributeWithValue> configParams, JTable t) {
		for (TypedAttributeWithValue tav : configParams) {
			String[] row = new String[] { tav.getName(), tav.getType(), tav.getValue().toString() };
			((DefaultTableModel) t.getModel()).addRow(row);
		}
	}

	

	

}
