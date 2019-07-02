package org.sa.rainbow.stitch.gui.executor;

import javax.swing.JTabbedPane;

import org.sa.rainbow.gui.arch.elements.ReportHistoryPane;
import org.sa.rainbow.gui.arch.model.RainbowArchExecutorModel;
import org.sa.rainbow.gui.arch.model.RainbowArchModelElement;
import org.sa.rainbow.gui.arch.elements.DefaultThreadInfoPane;

public class StitchExecutorTabbedPane extends DefaultThreadInfoPane {

	private StrategyExecutionPanel m_strategyExecutionPanel;

	public StitchExecutorTabbedPane() {
		setTabPlacement(JTabbedPane.BOTTOM);

		m_strategyExecutionPanel = new StrategyExecutionPanel();
		addTab("Activity", m_strategyExecutionPanel);
	}
	
	@Override
	public void initBindings(RainbowArchModelElement el) {
		m_strategyExecutionPanel.initBinding((RainbowArchExecutorModel )el);
		
	}
	
}
