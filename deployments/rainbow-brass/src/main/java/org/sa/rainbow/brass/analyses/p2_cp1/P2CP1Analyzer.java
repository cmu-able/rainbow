package org.sa.rainbow.brass.analyses.p2_cp1;

import org.sa.rainbow.brass.analyses.P2Analyzer;
import org.sa.rainbow.brass.model.p2_cp1.ModelAccessor;
import org.sa.rainbow.core.error.RainbowConnectionException;

public abstract class P2CP1Analyzer extends P2Analyzer {
	public P2CP1Analyzer(String name) {
		super(name);
	}

	private ModelAccessor m_modelAccessor;
	
	@Override
	public void initializeConnections() throws RainbowConnectionException {
		super.initializeConnections();
		m_modelAccessor = new ModelAccessor(m_modelsManagerPort);
	}

	protected ModelAccessor getModels () {
		return m_modelAccessor;
	}

}
