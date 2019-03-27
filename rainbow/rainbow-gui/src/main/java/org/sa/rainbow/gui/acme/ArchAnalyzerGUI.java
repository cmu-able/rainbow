package org.sa.rainbow.gui.acme;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.gui.RainbowWindow;
import org.sa.rainbow.gui.arch.elements.IUIReporter;
import org.sa.rainbow.gui.arch.elements.IUIUpdater;

public class ArchAnalyzerGUI extends JPanel implements IUIReporter {
	private JTextField m_textField;
	private JLabel m_statusLabel;
	
	private static final Color OK_COLOR = Color.GREEN;
	private static final Color ERROR_COLOR = Color.RED;
	private static final Color CHECKING_COLOR = RainbowWindow.ANALYZERS_COLOR;

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
		m_textField.setFont(new Font(m_textField.getFont().getFontName(), m_textField.getFont().getStyle(), 8));
		add(m_textField, gbc_textField);
		Dimension s = new Dimension(200, 40);
		setPreferredSize(new Dimension(260, 40));
		setSize(s);
	}

	
	Pattern CONSTRAINT_PATTERN = Pattern.compile(".*Design rule (.*) fails to typecheck..*", Pattern.DOTALL);
	private long m_setTime;
	
	@Override
	public void processReport(ReportType type, String message) {
		Matcher m = CONSTRAINT_PATTERN.matcher(message);
		if (message.contains("ok")) {
			m_statusLabel.setText("OK. Will check later.");
			m_statusLabel.setForeground(OK_COLOR);
			m_textField.setText("");
			processBorder();
		}
		else if (message.contains("Checking")) {
			m_statusLabel.setText("Checking...");
			m_statusLabel.setForeground(CHECKING_COLOR);
			m_textField.setText("");
			this.setBorder(new LineBorder(RainbowWindow.ANALYZERS_COLOR, 2));
			m_setTime = new Date().getTime();
		}
		else if (m.matches()) {
			m_statusLabel.setText("Error! Will check later.");
			m_statusLabel.setForeground(ERROR_COLOR);
			m_textField.setText(m.group(1) + " failed.");
			processBorder();
		}
		else {
			m_textField.setText(message);
		}
		
	}

	private void processBorder() {
		long time = new Date().getTime();
		if (time - m_setTime < 1000) {
			final java.util.Timer timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							setBorder(null);
						}
					});
				}
			}, 1000);
		}
	}


}
