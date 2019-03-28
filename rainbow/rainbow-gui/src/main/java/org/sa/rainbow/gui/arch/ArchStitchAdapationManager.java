package org.sa.rainbow.gui.arch;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.gui.RainbowWindow;
import org.sa.rainbow.gui.arch.elements.IUIReporter;
import org.sa.rainbow.gui.widgets.TableColumnAdjuster;


public class ArchStitchAdapationManager extends JPanel implements IUIReporter{
	private JLabel m_lblStatus;
	private JTable m_table;
	private long m_borderSetTime;
	public ArchStitchAdapationManager() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblStatus = new JLabel("Status:");
		GridBagConstraints gbc_lblStatus = new GridBagConstraints();
		gbc_lblStatus.insets = new Insets(0, 0, 5, 5);
		gbc_lblStatus.gridx = 0;
		gbc_lblStatus.gridy = 0;
		add(lblStatus, gbc_lblStatus);
		
		m_lblStatus = new JLabel("Idle");
		GridBagConstraints gbc_m_lblStatus = new GridBagConstraints();
		gbc_m_lblStatus.insets = new Insets(0, 0, 5, 0);
		gbc_m_lblStatus.fill = GridBagConstraints.HORIZONTAL;
		gbc_m_lblStatus.gridx = 1;
		gbc_m_lblStatus.gridy = 0;
		add(m_lblStatus, gbc_m_lblStatus);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridwidth = 2;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		add(scrollPane, gbc_scrollPane);
		
		m_table = new JTable(new DefaultTableModel(new Object[][] {new Object[] {"", "",""}}, new String[] {"Strategy", "Score", "Disposition"}));
		m_table.setPreferredScrollableViewportSize(new Dimension(200, 100));
		m_table.setFont(new Font(m_table.getFont().getFontName(), m_table.getFont().getStyle(), 8));
		m_table.getTableHeader().setFont(new Font(m_table.getTableHeader().getFont().getFontName(), m_table.getFont().getStyle(), 8));

		TableColumnAdjuster tca = new TableColumnAdjuster(m_table);
		tca.setDynamicAdjustment(true);
		scrollPane.setViewportView(m_table);
	}
	
	private static final Pattern Q_PATTERN = Pattern.compile("Queuing (.*)");
	private static final Pattern S_PATTERN = Pattern.compile("Scores:.*\\[(.*)\\].*", Pattern.DOTALL);
	private static final Pattern F_PATTERN = Pattern.compile("Finished\\s*(.*):(.*)");
	
	@Override
	public synchronized void processReport(ReportType type, String message) {
		// Messages may come out of order.
		DefaultTableModel model = (DefaultTableModel) m_table.getModel();
		if (message.contains("Considering")) {
			for (int i = 0; i < model.getRowCount(); i++) {
				model.removeRow(i);
			}
			m_lblStatus.setText("Running");
			setBorder(new LineBorder(RainbowWindow.ADAPTION_MANAGER_COLOR, 2));
			m_borderSetTime = new Date().getTime();
		}
		else if (message.contains("Queuing")) {
			Matcher qM = Q_PATTERN.matcher(message);
			if (qM.matches()) {
				String s = qM.group(1);
				boolean added = false;
				for (int row=0; row < model.getRowCount(); row++) {
					if (model.getValueAt(row, 0).equals(s)) {
						model.setValueAt("Queued", row, 2);
						m_table.setRowSelectionInterval(row, row);
						added = true;
					}
				}
				if (!added) {
					model.addRow(new Object[] {s,"","Queued"});
				}
			}
		}
		else if (message.contains("No applicable")) {
			for (int r=0; r < model.getRowCount(); r++) {
				model.setValueAt("Inapplicable", r, 2);
			}
			m_lblStatus.setText("None applicable");
			processBorder();
		}
		else if (message.contains("Scores")) {
			Matcher sM = S_PATTERN.matcher(message);
			NumberFormat formatter = new DecimalFormat("#.###");
			if (sM.matches()) {
				String[] scores = sM.group(1).split("\n");
				Map<String, String> scoreTable = new HashMap<>();
				for (String score : scores) {
					String[] parts = score.split(":");
					if (parts.length != 2) continue;
//					model.addRow(new Object[] {parts[0], formatter.format(Double.parseDouble(parts[1].trim())), ""});
					scoreTable.put(parts[0].trim(), formatter.format(Double.parseDouble(parts[1].trim())));
				}
				for (int r=0; r < model.getRowCount(); r++) {
					Object valueAt = model.getValueAt(0, 0);
					String s = scoreTable.get(valueAt);
					if (s != null) {
						model.setValueAt(s, r, 1);
						scoreTable.remove(valueAt);
					}
				}
				for (Entry<String,String> e : scoreTable.entrySet()) {
					model.addRow(new Object[] {e.getKey(), e.getValue(), ""});

				}
			}
		}
		else if (message.contains("Finished")) {
			Matcher fM = F_PATTERN.matcher(message);
			if (fM.matches()) {
				for (int r=0; r<model.getRowCount();r++) {
					if (fM.group(1).equals(model.getValueAt(r, 0)))
						model.setValueAt(fM.group(2), r, 2);
				}
			}
			m_lblStatus.setText("Idle");
			processBorder();
			
		}
	}
	
	private void processBorder() {
		long time = new Date().getTime();
		if (time - m_borderSetTime < 1000) {
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
