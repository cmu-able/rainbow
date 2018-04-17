package org.sa.rainbow.brass.analyses.p2_cp3;

import org.sa.rainbow.brass.analyses.P2Analyzer;
import org.sa.rainbow.brass.model.p2_cp3.ModelAccessor;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IModelsManagerPort;

public abstract class P2CP3Analyzer extends P2Analyzer {


	private ModelAccessor m_modelAccessor;

	public P2CP3Analyzer(String name) {
		super(name);
		
	}

	@Override
	public void initializeConnections() throws RainbowConnectionException {
		super.initializeConnections();
		m_modelAccessor = new ModelAccessor(m_modelsManagerPort);
	}

	protected ModelAccessor getModels () {
		return m_modelAccessor;
	}
	

	

	
	

}
