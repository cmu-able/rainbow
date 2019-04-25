package org.sa.rainbow.gui.arch.elements;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.sa.rainbow.gui.arch.model.RainbowArchEffectorModel;

public class EffectorTabbedPane extends JTabbedPane {

	private JTextArea m_execText;
	private EffectorDetailPanel m_effectorDetailPanel;
	private EffectorExecutionPane m_exPane;

	public EffectorTabbedPane() {
		setTabPlacement(JTabbedPane.BOTTOM);
		m_exPane = new EffectorExecutionPane();
		addTab("Executions", null, m_exPane, null);
		
		m_effectorDetailPanel = new EffectorDetailPanel();
		addTab("Specification", m_effectorDetailPanel);
	}
	
	public void initBindings (RainbowArchEffectorModel effModel) {
		m_effectorDetailPanel.initBindings(effModel.getEffectorAttributes());
		m_exPane.initBindings(effModel);
	}
	
}
