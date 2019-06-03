package org.sa.rainbow.gui.arch.elements;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;

import org.sa.rainbow.gui.arch.model.RainbowArchModelElement;

import java.awt.Insets;

public class ThreadInformationPane extends JPanel {
	private JTextField m_textField;

	/**
	 * Create the panel.
	 */
	public ThreadInformationPane() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblClass = new JLabel("Class:");
		GridBagConstraints gbc_lblClass = new GridBagConstraints();
		gbc_lblClass.insets = new Insets(0, 0, 0, 5);
		gbc_lblClass.anchor = GridBagConstraints.EAST;
		gbc_lblClass.gridx = 0;
		gbc_lblClass.gridy = 0;
		add(lblClass, gbc_lblClass);
		
		m_textField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		add(m_textField, gbc_textField);
		m_textField.setColumns(10);

	}

	public void initBindings(RainbowArchModelElement el) {
		m_textField.setText(el.getRunnable().getClass().getName());
	}

}
