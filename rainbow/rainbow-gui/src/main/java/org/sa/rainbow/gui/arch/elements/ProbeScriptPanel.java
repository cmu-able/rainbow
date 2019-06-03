package org.sa.rainbow.gui.arch.elements;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;
import org.sa.rainbow.translator.probes.IProbe.Kind;

public class ProbeScriptPanel extends JPanel {
	private JTextField m_scriptField;
	private JTextField m_argumentsField;
	private JCheckBox m_chckbxContinual;
	
	
	public ProbeScriptPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblScript = new JLabel("Script:");
		lblScript.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblScript = new GridBagConstraints();
		gbc_lblScript.anchor = GridBagConstraints.EAST;
		gbc_lblScript.insets = new Insets(0, 5, 5, 5);
		gbc_lblScript.gridx = 0;
		gbc_lblScript.gridy = 0;
		add(lblScript, gbc_lblScript);
		
		m_scriptField = new JTextField();
		m_scriptField.setEditable(false);
		GridBagConstraints gbc_scriptField = new GridBagConstraints();
		gbc_scriptField.insets = new Insets(0, 0, 5, 0);
		gbc_scriptField.fill = GridBagConstraints.HORIZONTAL;
		gbc_scriptField.gridx = 1;
		gbc_scriptField.gridy = 0;
		add(m_scriptField, gbc_scriptField);
		m_scriptField.setColumns(10);
		
		JLabel lblArguments = new JLabel("Arguments:");
		GridBagConstraints gbc_lblArguments = new GridBagConstraints();
		gbc_lblArguments.anchor = GridBagConstraints.EAST;
		gbc_lblArguments.insets = new Insets(0, 5, 5, 5);
		gbc_lblArguments.gridx = 0;
		gbc_lblArguments.gridy = 1;
		add(lblArguments, gbc_lblArguments);
		
		m_argumentsField = new JTextField();
		m_argumentsField.setEditable(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 1;
		add(m_argumentsField, gbc_textField);
		m_argumentsField.setColumns(10);
		
		m_chckbxContinual = new JCheckBox("Continual");
		GridBagConstraints gbc_chckbxContinual = new GridBagConstraints();
		gbc_chckbxContinual.anchor = GridBagConstraints.WEST;
		gbc_chckbxContinual.gridx = 1;
		gbc_chckbxContinual.gridy = 2;
		add(m_chckbxContinual, gbc_chckbxContinual);
	}
	
	public void addDataBindings(ProbeAttributes attributes) {
		if (attributes.kind==Kind.SCRIPT) {
			m_scriptField.setText(attributes.getInfo().get("path"));
			m_argumentsField.setText(attributes.getInfo().get("argument"));
			m_chckbxContinual.setSelected("continual".equals(attributes.getInfo().get("mode")));
		}
	}

}