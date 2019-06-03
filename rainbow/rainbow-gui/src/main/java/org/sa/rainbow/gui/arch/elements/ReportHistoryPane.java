package org.sa.rainbow.gui.arch.elements;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.sa.rainbow.gui.arch.model.IReportingModel;
import org.sa.rainbow.gui.arch.model.IReportingModel.ReportDatum;
import org.sa.rainbow.gui.widgets.TableColumnAdjuster;

public class ReportHistoryPane extends JPanel implements PropertyChangeListener{
	private JTable m_table;
	private IReportingModel m_model;
	
	private static final DateFormat DF = new SimpleDateFormat("MM/dd HH:mm:ss");

	/**
	 * Create the panel.
	 */
	public ReportHistoryPane() {
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		m_table = new JTable();
		scrollPane.setViewportView(m_table);
		TableColumnAdjuster tca = new TableColumnAdjuster(m_table);
		tca.setDynamicAdjustment(true);
	}
	
	public void initBindings(IReportingModel model) {
		if (m_model != model) {
			if (m_model != null) m_model.removePropertyChangeListener(this);
			m_model = model;
			if (m_model != null) {
				DefaultTableModel tm = new DefaultTableModel(new Object[][] {}, new String[] {"Date", "Report"});
				for (ReportDatum rd : m_model.getReports()) {
					tm.addRow(new Object[] {DF.format(new Date(rd.time)), rd.message});
				}
				m_table.setModel(tm);
				m_model.addPropertyChangeListener(this);
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (IReportingModel.REPORT_PROP.equals(evt.getPropertyName())) {
			ReportDatum rd = (ReportDatum) evt.getNewValue();
			((DefaultTableModel )m_table.getModel()).insertRow(0, new Object[] {DF.format(new Date(rd.time)),rd.message});
		}
	}

}
