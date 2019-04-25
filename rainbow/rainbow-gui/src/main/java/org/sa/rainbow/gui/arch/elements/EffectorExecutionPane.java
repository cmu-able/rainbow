package org.sa.rainbow.gui.arch.elements;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.sa.rainbow.gui.arch.model.RainbowArchEffectorModel;
import org.sa.rainbow.gui.arch.model.RainbowArchEffectorModel.EffectorExecutions;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;

public class EffectorExecutionPane extends JPanel implements PropertyChangeListener {
	private JTable m_table;
	private RainbowArchEffectorModel m_effModel;
	private JLabel m_lblEffector;
	
	private static final DateFormat DF = new SimpleDateFormat("MM/dd HH:mm:ss");

	/**
	 * Create the panel.
	 */
	public EffectorExecutionPane() {
		setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);

		m_table = new JTable();
		scrollPane.setViewportView(m_table);
		
		m_lblEffector = new JLabel("Effector:");
		add(m_lblEffector, BorderLayout.NORTH);

	}

	public void initBindings(RainbowArchEffectorModel em) {
		if (m_effModel != em) {
			if (m_effModel != null)
				m_effModel.removePropertyChangeListener(this);
			m_effModel = em;
			m_lblEffector.setText("Effector: " + em.getId());
			DefaultTableModel tm = new DefaultTableModel(new Object[][] {}, new String[] {"Duration/Date","Arguments", "Outcome"});
			for (EffectorExecutions ex : m_effModel.getExecutions()) {
				tm.addRow(createRow(ex));
			}
			m_table.setModel(tm);
			m_effModel.addPropertyChangeListener(this);
		}

	}

	protected Object[] createRow(EffectorExecutions ex) {
		return new Object[] {getDateCol(ex.executionDuration, ex.outcome), ex.args.toArray().toString(), ex.outcome!=null?ex.outcome:"Pending"};
	}

	private Object getDateCol(long executionDuration, Outcome outcome) {
		if (outcome == null) return "" + executionDuration/1000 + "s";
		return DF.format(new Date(executionDuration));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (RainbowArchEffectorModel.EFFECTOR_EXECUTING.equals(evt.getPropertyName()) || RainbowArchEffectorModel.EFFECTOR_EXECUTED.equals(evt.getPropertyName())) {
			((DefaultTableModel )m_table.getModel()).insertRow(0, createRow((EffectorExecutions) evt.getNewValue()));
		}
	}

}
