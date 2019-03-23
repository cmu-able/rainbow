package org.sa.rainbow.gui.arch.elements;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.SwingConstants;

import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;
import org.sa.rainbow.translator.probes.IProbe.Kind;

import javax.swing.JTextField;

public class ProbeClassPanel extends JPanel {
	private JTextField m_classField;
	private JTextField m_argsField;
	private JLabel m_label;
	private JTextField m_periodField;
	public ProbeClassPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblClass = new JLabel("Class:");
		lblClass.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblClass = new GridBagConstraints();
		gbc_lblClass.insets = new Insets(0, 0, 5, 5);
		gbc_lblClass.anchor = GridBagConstraints.EAST;
		gbc_lblClass.gridx = 0;
		gbc_lblClass.gridy = 0;
		add(lblClass, gbc_lblClass);
		
		m_classField = new JTextField();
		GridBagConstraints gbc_classField = new GridBagConstraints();
		gbc_classField.insets = new Insets(0, 0, 5, 0);
		gbc_classField.fill = GridBagConstraints.HORIZONTAL;
		gbc_classField.gridx = 1;
		gbc_classField.gridy = 0;
		add(m_classField, gbc_classField);
		
		JLabel lblArguments = new JLabel("Arguments:");
		GridBagConstraints gbc_lblArguments = new GridBagConstraints();
		gbc_lblArguments.anchor = GridBagConstraints.EAST;
		gbc_lblArguments.insets = new Insets(0, 0, 5, 5);
		gbc_lblArguments.gridx = 0;
		gbc_lblArguments.gridy = 1;
		add(lblArguments, gbc_lblArguments);
		
		m_argsField = new JTextField();
		GridBagConstraints gbc_argsField = new GridBagConstraints();
		gbc_argsField.insets = new Insets(0, 0, 5, 0);
		gbc_argsField.fill = GridBagConstraints.HORIZONTAL;
		gbc_argsField.gridx = 1;
		gbc_argsField.gridy = 1;
		add(m_argsField, gbc_argsField);
		
		m_label = new JLabel("Period:");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.EAST;
		gbc_label.insets = new Insets(0, 0, 0, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 2;
		add(m_label, gbc_label);
		
		m_periodField = new JTextField();
		GridBagConstraints gbc_periodField = new GridBagConstraints();
		gbc_periodField.fill = GridBagConstraints.HORIZONTAL;
		gbc_periodField.gridx = 1;
		gbc_periodField.gridy = 2;
		add(m_periodField, gbc_periodField);
	}
	public void addDataBindings(ProbeAttributes attributes) {
		if (attributes.kind==Kind.SCRIPT) {
			m_classField.setText(attributes.getInfo().get("class"));
			StringBuffer arg = new StringBuffer(attributes.getInfo().get("argument"));
			for (Object o : attributes.getArrays().values()) {
				arg.append(", ");
				arg.append(o.toString());
			}
			
			m_argsField.setText(arg.toString());
			m_periodField.setText(attributes.getInfo().get("period"));
		}
	}
}
