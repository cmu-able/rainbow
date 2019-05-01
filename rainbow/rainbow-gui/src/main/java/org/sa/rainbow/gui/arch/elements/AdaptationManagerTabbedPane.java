package org.sa.rainbow.gui.arch.elements;

import javax.swing.JTabbedPane;

import org.sa.rainbow.gui.arch.model.RainbowArchAdapationManagerModel;

public class AdaptationManagerTabbedPane extends JTabbedPane {
	protected ReportHistoryPane m_reportHistoryPanel;
	
	public AdaptationManagerTabbedPane() {
		setTabPlacement(JTabbedPane.BOTTOM);
		
		m_reportHistoryPanel = new ReportHistoryPane();
		addTab("Activity", m_reportHistoryPanel);
	}
	
	public void initBindings(RainbowArchAdapationManagerModel amModel) {
		m_reportHistoryPanel.initBindings(amModel);

	}

}
