package org.sa.rainbow.brass.analyses;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.IRainbowEnvironment;
import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.analysis.IRainbowAnalysis;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;

public abstract class P2Analyzer extends AbstractRainbowRunnable implements IRainbowAnalysis {
	static protected final Logger LOGGER = Logger.getLogger("Analyzer");

	protected IModelUSBusPort m_modelUSPort;
	protected IModelsManagerPort m_modelsManagerPort;
	protected IModelChangeBusSubscriberPort m_modelChangePort;

	public P2Analyzer(String name) {
		super(name);

	}

	public P2Analyzer(String name, IRainbowEnvironment env) {
		super(name, env);
	}

	@Override
	public RainbowComponentT getComponentType() {
		return RainbowComponentT.ANALYSIS;

	}

	@Override
	public void log(String txt) {
		m_reportingPort.info(RainbowComponentT.ANALYSIS, id() + ": " + txt, LOGGER);
	}

	protected void initializeConnections() throws RainbowConnectionException {
		m_modelUSPort = RainbowPortFactory.createModelsManagerClientUSPort(this);
		m_modelsManagerPort = RainbowPortFactory.createModelsManagerRequirerPort();
		m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort();
	}

	@Override
	public void initialize(IRainbowReportingPort port) throws RainbowConnectionException {
		super.initialize(port);
		String period = Rainbow.instance().getProperty(RainbowConstants.PROPKEY_MODEL_EVAL_PERIOD);
		if (period != null) {
			setSleepTime(Long.parseLong(period));
		} else {
			setSleepTime(IRainbowRunnable.LONG_SLEEP_TIME);
		}
		initializeConnections();
	}

	@Override
	public void setProperty(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getProperty(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dispose() {
		m_reportingPort.dispose();
		m_modelUSPort.dispose();
		
	}



}
