package org.sa.rainbow.gui.arch.model;

import org.sa.rainbow.core.analysis.IRainbowAnalysis;
import org.sa.rainbow.gui.arch.controller.AbstractRainbowController;

public class RainbowArchAnalyzerModel extends RainbowArchModelElement {

	private IRainbowAnalysis m_analysis;

	public RainbowArchAnalyzerModel(IRainbowAnalysis a) {
		super();
		m_analysis = a;
	}

	@Override
	public String getId() {
		return m_analysis.id();
	}

}
