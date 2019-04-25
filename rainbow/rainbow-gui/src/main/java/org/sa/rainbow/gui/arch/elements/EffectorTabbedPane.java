package org.sa.rainbow.gui.arch.elements;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.sa.rainbow.gui.arch.model.RainbowArchEffectorModel;

public class EffectorTabbedPane extends JTabbedPane {

	private JTextArea m_execText;
	private EffectorDetailPanel m_effectorDetailPanel;

	public EffectorTabbedPane() {
		setTabPlacement(JTabbedPane.BOTTOM);
		JScrollPane sp = new JScrollPane();
		addTab("Executions", null, sp, null);
		m_execText = new JTextArea();
		sp.setViewportView(m_execText);
		
		m_effectorDetailPanel = new EffectorDetailPanel();
		addTab("Specification", m_effectorDetailPanel);
	}
	
	public void initBindings (RainbowArchEffectorModel effModel) {
		m_effectorDetailPanel.initBindings(effModel.getEffectorAttributes());
	}
	
}
