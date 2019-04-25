package org.sa.rainbow.gui.arch.elements;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.GridBagLayout;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JList;
import java.awt.Insets;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import org.sa.rainbow.core.models.UtilityPreferenceDescription;
import org.sa.rainbow.gui.widgets.TableColumnAdjuster;

import javax.swing.JTable;

public class UtilityModelPane extends JPanel {
	private JTextField m_scenarioName;
	private JTable m_weightTable;
	private JTextField m_selectedTacticField;
	private JTable m_impactTable;
	private JList m_scenarioList;
	private JList m_tacticList;
	private UtilityPreferenceDescription m_upd;

	/**
	 * Create the panel.
	 */
	public UtilityModelPane() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 150, 200, 150, 0, 200, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JLabel lblScenarios = new JLabel("Scenarios:");
		GridBagConstraints gbc_lblScenarios = new GridBagConstraints();
		gbc_lblScenarios.anchor = GridBagConstraints.SOUTHWEST;
		gbc_lblScenarios.insets = new Insets(0, 0, 5, 5);
		gbc_lblScenarios.gridx = 0;
		gbc_lblScenarios.gridy = 0;
		add(lblScenarios, gbc_lblScenarios);

		m_scenarioName = new JTextField();
		m_scenarioName.setEditable(false);
		GridBagConstraints gbc_scenarioName = new GridBagConstraints();
		gbc_scenarioName.anchor = GridBagConstraints.NORTH;
		gbc_scenarioName.insets = new Insets(0, 0, 5, 5);
		gbc_scenarioName.fill = GridBagConstraints.HORIZONTAL;
		gbc_scenarioName.gridx = 1;
		gbc_scenarioName.gridy = 0;
		add(m_scenarioName, gbc_scenarioName);
		m_scenarioName.setColumns(10);

		JLabel lblTactics = new JLabel("Tactics:");
		GridBagConstraints gbc_lblTactics = new GridBagConstraints();
		gbc_lblTactics.anchor = GridBagConstraints.SOUTHWEST;
		gbc_lblTactics.insets = new Insets(0, 0, 5, 5);
		gbc_lblTactics.gridx = 2;
		gbc_lblTactics.gridy = 0;
		add(lblTactics, gbc_lblTactics);

		JLabel lblSelectedTactic = new JLabel("Selected Tactic:");
		GridBagConstraints gbc_lblSelectedTactic = new GridBagConstraints();
		gbc_lblSelectedTactic.insets = new Insets(0, 0, 5, 5);
		gbc_lblSelectedTactic.anchor = GridBagConstraints.EAST;
		gbc_lblSelectedTactic.gridx = 3;
		gbc_lblSelectedTactic.gridy = 0;
		add(lblSelectedTactic, gbc_lblSelectedTactic);

		m_selectedTacticField = new JTextField();
		m_selectedTacticField.setEditable(false);
		GridBagConstraints gbc_selectedTacticField = new GridBagConstraints();
		gbc_selectedTacticField.insets = new Insets(0, 0, 5, 0);
		gbc_selectedTacticField.fill = GridBagConstraints.HORIZONTAL;
		gbc_selectedTacticField.gridx = 4;
		gbc_selectedTacticField.gridy = 0;
		add(m_selectedTacticField, gbc_selectedTacticField);
		m_selectedTacticField.setColumns(10);

		m_scenarioList = new JList();
		GridBagConstraints gbc_scenarioList = new GridBagConstraints();
		gbc_scenarioList.insets = new Insets(0, 0, 0, 5);
		gbc_scenarioList.fill = GridBagConstraints.BOTH;
		gbc_scenarioList.gridx = 0;
		gbc_scenarioList.gridy = 1;
		add(m_scenarioList, gbc_scenarioList);
		m_scenarioList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	
		m_weightTable = new JTable(new Object[][] {}, new String[] { "Utility", "Weight" });
		GridBagConstraints gbc_weightTable = new GridBagConstraints();
		gbc_weightTable.insets = new Insets(0, 0, 0, 5);
		gbc_weightTable.fill = GridBagConstraints.BOTH;
		gbc_weightTable.gridx = 1;
		gbc_weightTable.gridy = 1;
		JScrollPane wsp = new JScrollPane(m_weightTable);
		add(wsp, gbc_weightTable);

		m_tacticList = new JList();
		GridBagConstraints gbc_tacticList = new GridBagConstraints();
		gbc_tacticList.insets = new Insets(0, 0, 0, 5);
		gbc_tacticList.fill = GridBagConstraints.BOTH;
		gbc_tacticList.gridx = 2;
		gbc_tacticList.gridy = 1;
		add(m_tacticList, gbc_tacticList);

		m_impactTable = new JTable(new Object[][] {}, new String[] { "Utility", "Impact" });
		GridBagConstraints gbc_impactTable = new GridBagConstraints();
		gbc_impactTable.gridwidth = 2;
		gbc_impactTable.insets = new Insets(0, 0, 0, 5);
		gbc_impactTable.fill = GridBagConstraints.BOTH;
		gbc_impactTable.gridx = 3;
		gbc_impactTable.gridy = 1;
		JScrollPane isp = new JScrollPane(m_impactTable);
		add(isp, gbc_impactTable);

		m_scenarioList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				String scenario = (String)m_scenarioList.getSelectedValue();
				m_scenarioName.setText(scenario);
				Map<String, Double> weightTable = m_upd.weights.get(scenario);
//				m_weightTable.removeAll();
				DefaultTableModel tm = new DefaultTableModel(new Object[][] {}, new String[] { "Utility", "Weight" });
				for (Entry<String, Double> w : weightTable.entrySet()) {
					String label = m_upd.getUtilityFunctions().get(w.getKey()).label();
					tm.addRow(new Object[] {label, w.getValue()});
				}
				m_weightTable.setModel(tm);
			}
		});
		
		m_tacticList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				String tactic = (String) m_tacticList.getSelectedValue();
				m_selectedTacticField.setText(tactic + " Impact Vector");
				Map<String, Object> impactModel = m_upd.attributeVectors.get(tactic);
				DefaultTableModel tm = new DefaultTableModel(new Object[][] {}, new String[] { "Utility", "Impact" });

				for (Entry<String, Object> im : impactModel.entrySet()) {
					String label = m_upd.getUtilityFunctions().get(im.getKey()).label();
					tm.addRow(new Object[] {label,im.getValue()});
				}
				m_impactTable.setModel(tm);
			}
		});
		TableColumnAdjuster stca = new TableColumnAdjuster(m_weightTable);
		stca.setDynamicAdjustment(true);
		TableColumnAdjuster ttca = new TableColumnAdjuster(m_impactTable);
		ttca.setDynamicAdjustment(true);
	}

	public void initBindings(UtilityPreferenceDescription upd) {
		m_upd = upd;
		m_scenarioList.removeAll();
		m_weightTable.removeAll();
		m_tacticList.removeAll();
		m_impactTable.removeAll();
		m_scenarioName.setText("");
		m_selectedTacticField.setText("");
		DefaultListModel<String> scenarioListModel = new DefaultListModel<>();
		for (Entry<String, Map<String, Double>> s : upd.weights.entrySet()) {
			scenarioListModel.addElement(s.getKey());
		}
		m_scenarioList.setModel(scenarioListModel);
		
		DefaultListModel<String> tacticListModel = new DefaultListModel();
		for (Entry<String, Map<String, Object>> te : upd.attributeVectors.entrySet()) {
			tacticListModel.addElement(te.getKey());
		}
		m_tacticList.setModel(tacticListModel);

	}

}
