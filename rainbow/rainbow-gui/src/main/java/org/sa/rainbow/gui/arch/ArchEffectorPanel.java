package org.sa.rainbow.gui.arch;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;

import org.sa.rainbow.core.ports.IEffectorLifecycleBusPort;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.gui.JTableCellDisplayer;
import org.sa.rainbow.gui.RainbowWindow;
import org.sa.rainbow.gui.arch.elements.IUIReporter;
import org.sa.rainbow.gui.widgets.TableColumnAdjuster;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;

public class ArchEffectorPanel extends JPanel implements IEffectorLifecycleBusPort {

	private JTable m_table;

	public ArchEffectorPanel() {
		setLayout(new BorderLayout(0, 0));

		JScrollPane p = new JScrollPane();
		add(p, BorderLayout.CENTER);
		m_table = new JTable(new DefaultTableModel(new Object[][] { new Object[] { 0, "", "" } },
				new String[] { "#", "Arguments", "Outcome" }));
		p.setViewportView(m_table);
		p.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		p.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		m_table.addComponentListener(new JTableCellDisplayer(m_table));
		m_table.setPreferredScrollableViewportSize(new Dimension(150, 75));
		m_table.setFont(new Font(m_table.getFont().getFontName(), m_table.getFont().getStyle(), 8));
		m_table.getTableHeader()
				.setFont(new Font(m_table.getTableHeader().getFont().getFontName(), m_table.getFont().getStyle(), 8));

		TableColumnAdjuster tca = new TableColumnAdjuster(m_table);
		tca.setDynamicAdjustment(true);

		m_table.setSelectionBackground(RainbowWindow.SYSTEM_COLOR_LIGHT);

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reportCreated(IEffectorIdentifier effector) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reportDeleted(IEffectorIdentifier effector) {
		// TODO Auto-generated method stub

	}

	@Override
	public void reportExecuted(IEffectorIdentifier effector, Outcome outcome, List<String> args) {
		EventQueue.invokeLater(() -> {
			DefaultTableModel tableModel = (DefaultTableModel) m_table.getModel();
			tableModel.setValueAt(args.toString(), 0, 1);
			tableModel.setValueAt(outcome.toString(), 0, 2);
			m_table.clearSelection();
		});
	}

	@Override
	public void reportExecuting(IEffectorIdentifier effector, List<String> args) {
		EventQueue.invokeLater(() -> {
			DefaultTableModel tableModel = (DefaultTableModel) m_table.getModel();
			tableModel.setValueAt(((Integer) tableModel.getValueAt(0, 0)) + 1, 0, 0);
			tableModel.setValueAt(args.toString(), 0, 1);
			m_table.changeSelection(0, 0, false, false);
			m_table.changeSelection(0, 1, false, true);
			m_table.changeSelection(0, 2, false, true);
		});
	}

}
