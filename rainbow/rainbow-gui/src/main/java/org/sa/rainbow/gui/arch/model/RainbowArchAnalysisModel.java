package org.sa.rainbow.gui.arch.model;

import org.sa.rainbow.core.analysis.IRainbowAnalysis;
import org.sa.rainbow.gui.arch.controller.IRainbowUIController;
import org.sa.rainbow.gui.arch.controller.RainbowAnalysisController;

public class RainbowArchAnalysisModel extends RainbowArchModelElement {

	private final IRainbowAnalysis m_analysis;

	public RainbowArchAnalysisModel(IRainbowAnalysis a) {
		super();
		m_analysis = a;
	}

	@Override
	public String getId() {
		return m_analysis.id();
	}

	public IRainbowAnalysis getAnalysis() {
		return m_analysis;
	}

	@Override
	public RainbowAnalysisController getController() {
		return (RainbowAnalysisController) super.getController();
	}
	
}
