package org.sa.rainbow.gui.arch.elements;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier.Kind;

public class EffectorDetailPanel extends JPanel {
	private JTextField m_nameField;
	private JTextField m_locationField;
	private JTextField m_commandField;
	private JTextField m_pathField;
	private JTextField m_argsField;
	private EffectorAttributes m_ea;
	private JInternalFrame m_internalFrame;
	private JLabel m_lblPath;

	/**
	 * Create the panel.
	 */
	public EffectorDetailPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblName = new JLabel("Name:");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		add(lblName, gbc_lblName);
		
		m_nameField = new JTextField();
		m_nameField.setEditable(false);
		GridBagConstraints gbc_nameField = new GridBagConstraints();
		gbc_nameField.insets = new Insets(0, 0, 5, 0);
		gbc_nameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameField.gridx = 1;
		gbc_nameField.gridy = 0;
		add(m_nameField, gbc_nameField);
		m_nameField.setColumns(10);
		
		JLabel lblLocation = new JLabel("Location:");
		GridBagConstraints gbc_lblLocation = new GridBagConstraints();
		gbc_lblLocation.anchor = GridBagConstraints.EAST;
		gbc_lblLocation.insets = new Insets(0, 0, 5, 5);
		gbc_lblLocation.gridx = 0;
		gbc_lblLocation.gridy = 1;
		add(lblLocation, gbc_lblLocation);
		
		m_locationField = new JTextField();
		m_locationField.setEditable(false);
		GridBagConstraints gbc_locationField = new GridBagConstraints();
		gbc_locationField.insets = new Insets(0, 0, 5, 0);
		gbc_locationField.fill = GridBagConstraints.HORIZONTAL;
		gbc_locationField.gridx = 1;
		gbc_locationField.gridy = 1;
		add(m_locationField, gbc_locationField);
		m_locationField.setColumns(10);
		
		JLabel lblRespondsToOperation = new JLabel("Responds To Operation:");
		GridBagConstraints gbc_lblRespondsToOperation = new GridBagConstraints();
		gbc_lblRespondsToOperation.anchor = GridBagConstraints.EAST;
		gbc_lblRespondsToOperation.insets = new Insets(0, 0, 5, 5);
		gbc_lblRespondsToOperation.gridx = 0;
		gbc_lblRespondsToOperation.gridy = 2;
		add(lblRespondsToOperation, gbc_lblRespondsToOperation);
		
		m_commandField = new JTextField();
		m_commandField.setEditable(false);
		GridBagConstraints gbc_commandField = new GridBagConstraints();
		gbc_commandField.insets = new Insets(0, 0, 5, 0);
		gbc_commandField.fill = GridBagConstraints.HORIZONTAL;
		gbc_commandField.gridx = 1;
		gbc_commandField.gridy = 2;
		add(m_commandField, gbc_commandField);
		m_commandField.setColumns(10);
		
		m_internalFrame = new JInternalFrame("ScriptInfo");
		GridBagConstraints gbc_m_internalFrame = new GridBagConstraints();
		gbc_m_internalFrame.fill = GridBagConstraints.BOTH;
		gbc_m_internalFrame.gridwidth = 2;
		gbc_m_internalFrame.insets = new Insets(0, 0, 0, 5);
		gbc_m_internalFrame.gridx = 0;
		gbc_m_internalFrame.gridy = 3;
		add(m_internalFrame, gbc_m_internalFrame);
		GridBagLayout gridBagLayout_1 = new GridBagLayout();
		gridBagLayout_1.columnWidths = new int[]{0, 0, 0};
		gridBagLayout_1.rowHeights = new int[]{0, 0, 0};
		gridBagLayout_1.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout_1.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		m_internalFrame.getContentPane().setLayout(gridBagLayout_1);
		
		m_lblPath = new JLabel("Path:");
		GridBagConstraints gbc_m_lblPath = new GridBagConstraints();
		gbc_m_lblPath.insets = new Insets(0, 0, 5, 5);
		gbc_m_lblPath.anchor = GridBagConstraints.EAST;
		gbc_m_lblPath.gridx = 0;
		gbc_m_lblPath.gridy = 0;
		m_internalFrame.getContentPane().add(m_lblPath, gbc_m_lblPath);
		
		m_pathField = new JTextField();
		m_pathField.setEditable(false);
		GridBagConstraints gbc_pathField = new GridBagConstraints();
		gbc_pathField.insets = new Insets(0, 0, 5, 0);
		gbc_pathField.fill = GridBagConstraints.HORIZONTAL;
		gbc_pathField.gridx = 1;
		gbc_pathField.gridy = 0;
		m_internalFrame.getContentPane().add(m_pathField, gbc_pathField);
		m_pathField.setColumns(10);
		
		JLabel lblArgs = new JLabel("Args:");
		GridBagConstraints gbc_lblArgs = new GridBagConstraints();
		gbc_lblArgs.anchor = GridBagConstraints.EAST;
		gbc_lblArgs.insets = new Insets(0, 0, 0, 5);
		gbc_lblArgs.gridx = 0;
		gbc_lblArgs.gridy = 1;
		m_internalFrame.getContentPane().add(lblArgs, gbc_lblArgs);
		
		m_argsField = new JTextField();
		m_argsField.setEditable(false);
		GridBagConstraints gbc_argsField = new GridBagConstraints();
		gbc_argsField.fill = GridBagConstraints.HORIZONTAL;
		gbc_argsField.gridx = 1;
		gbc_argsField.gridy = 1;
		m_internalFrame.getContentPane().add(m_argsField, gbc_argsField);
		m_argsField.setColumns(10);
		m_internalFrame.setVisible(true);

	}
	
	public void initBindings(EffectorAttributes ea) {
		m_ea = ea;
		m_nameField.setText(ea.name);
		m_locationField.setText(ea.location);
		m_commandField.setText(ea.getCommandPattern().toString());
		if (ea.getKind() == Kind.SCRIPT) {
			m_internalFrame.setTitle("Script Information");
			m_lblPath.setText("Path:");
			m_pathField.setText(ea.getArrays().get("path").toString());
			m_argsField.setText(ea.getArrays().get("args").toString());
		}
	}
}
