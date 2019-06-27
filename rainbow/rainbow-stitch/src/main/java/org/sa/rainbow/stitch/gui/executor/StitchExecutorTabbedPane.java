package org.sa.rainbow.stitch.gui.executor;

import javax.swing.JTabbedPane;

import org.sa.rainbow.gui.arch.elements.ReportHistoryPane;
import org.sa.rainbow.gui.arch.model.RainbowArchExecutorModel;

public class StitchExecutorTabbedPane extends JTabbedPane {

	private StrategyExecutionPanel m_strategyExecutionPanel;

	public StitchExecutorTabbedPane() {
		setTabPlacement(JTabbedPane.BOTTOM);

		m_strategyExecutionPanel = new StrategyExecutionPanel();
		addTab("Activity", m_strategyExecutionPanel);
	}
	
	public void initBinding(RainbowArchExecutorModel model) {
	}
}
