package org.sa.rainbow.stitch.gui.executor;

import javax.swing.JTabbedPane;

import org.sa.rainbow.gui.arch.elements.ReportHistoryPane;
import org.sa.rainbow.gui.arch.model.RainbowArchExecutorModel;
import org.sa.rainbow.gui.arch.elements.DefaultThreadInfoPane;

public class StitchExecutorTabbedPane extends DefaultThreadInfoPane {

	private EventBasedStitchExecutorPanel m_strategyExecutionPanel;

	public StitchExecutorTabbedPane() {
		setTabPlacement(JTabbedPane.BOTTOM);

		m_strategyExecutionPanel = new EventBasedStitchExecutorPanel();
		addTab("Activity", m_strategyExecutionPanel);
	}
	
	public void initBinding(RainbowArchExecutorModel model) {
	}
}
