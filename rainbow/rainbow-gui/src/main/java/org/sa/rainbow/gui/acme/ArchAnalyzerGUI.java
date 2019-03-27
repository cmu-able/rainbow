package org.sa.rainbow.gui.acme;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.gui.RainbowWindow;
import org.sa.rainbow.gui.arch.elements.IUIReporter;
import org.sa.rainbow.gui.arch.elements.IUIUpdater;

public class ArchAnalyzerGUI extends JPanel implements IUIUpdater, IUIReporter {
	private JTextField m_textField;
	private JLabel m_statusLabel;
	
	private static final Color OK_COLOR = RainbowWindow.bleach(Color.GREEN, 0.75);
	private static final Color ERROR_COLOR = RainbowWindow.bleach(Color.RED, 0.75);
	private static final Color CHECKING_COLOR = RainbowWindow.ANALYZERS_COLOR_LIGHT;

	public ArchAnalyzerGUI() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblStatus = new JLabel("Status:");
		GridBagConstraints gbc_lblStatus = new GridBagConstraints();
		gbc_lblStatus.anchor = GridBagConstraints.WEST;
		gbc_lblStatus.insets = new Insets(0, 0, 5, 5);
		gbc_lblStatus.gridx = 0;
		gbc_lblStatus.gridy = 0;
		add(lblStatus, gbc_lblStatus);
		
		m_statusLabel = new JLabel("Idle");
		GridBagConstraints gbc_m_statusLabel = new GridBagConstraints();
		gbc_m_statusLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_m_statusLabel.insets = new Insets(0, 0, 5, 0);
		gbc_m_statusLabel.gridx = 1;
		gbc_m_statusLabel.gridy = 0;
		add(m_statusLabel, gbc_m_statusLabel);
		
		m_textField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.fill = GridBagConstraints.BOTH;
		gbc_textField.anchor = GridBagConstraints.WEST;
		gbc_textField.gridwidth = 2;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 1;
		add(m_textField, gbc_textField);
		setPreferredSize(new Dimension(200, 40));
	}

	
	Pattern CONSTRAINT_PATTERN = Pattern.compile(".*Design rule (.*) fails to typecheck..*", Pattern.DOTALL);
	
	@Override
	public void processReport(ReportType type, String message) {
		Matcher m = CONSTRAINT_PATTERN.matcher(message);
		if (message.contains("ok")) {
			m_statusLabel.setText("OK");
			m_statusLabel.setBackground(OK_COLOR);
			m_textField.setText("");
			this.setBorder(null);
		}
		else if (message.contains("Checking")) {
			m_statusLabel.setText("Checking...");
			m_statusLabel.setBackground(CHECKING_COLOR);
			m_textField.setText("");
			this.setBorder(new LineBorder(RainbowWindow.ANALYZERS_COLOR, 2));
		}
		else if (m.matches()) {
			m_statusLabel.setText("Error");
			m_statusLabel.setBackground(ERROR_COLOR);
			m_textField.setText(m.group(1));
			this.setBorder(null);
		}
		else {
			m_textField.setText(message);
		}
		
	}

	@Override
	public void addUpdateListener(Runnable listener) {
		// TODO Auto-generated method stub
		
	}
	
}
