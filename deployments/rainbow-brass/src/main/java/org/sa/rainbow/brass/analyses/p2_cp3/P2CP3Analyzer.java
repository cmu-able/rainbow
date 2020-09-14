package org.sa.rainbow.brass.analyses.p2_cp3;

import org.sa.rainbow.brass.analyses.P2Analyzer;
import org.sa.rainbow.brass.model.P2ModelAccessor;
import org.sa.rainbow.brass.model.p2_cp3.CP3ModelAccessor;
import org.sa.rainbow.brass.model.p2_cp3.ICP3ModelAccessor;
import org.sa.rainbow.core.IRainbowEnvironment;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IModelsManagerPort;

public abstract class P2CP3Analyzer extends P2Analyzer {


	private ICP3ModelAccessor m_modelAccessor;

	public P2CP3Analyzer(String name) {
		super(name);
		
	}

	public P2CP3Analyzer(String name, IRainbowEnvironment env) {
		super(name, env);
	}


	@Override
	public void initializeConnections() throws RainbowConnectionException {
		super.initializeConnections();
		m_modelAccessor = new CP3ModelAccessor(m_modelsManagerPort);
	}

	protected ICP3ModelAccessor getModels () {
		return m_modelAccessor;
	}
	

	

	
	

}
