package org.sa.rainbow.brass.analyses.p2_cp3;

import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState;

import org.sa.rainbow.brass.model.p2_cp3.mission.MissionStateModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.ModelAccessor;
import org.sa.rainbow.brass.model.p2_cp3.acme.TurtlebotModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowState;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowStateModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotStateModelInstance;
import org.sa.rainbow.brass.model.robot.RobotState;
import org.sa.rainbow.brass.model.robot.RobotStateModelInstance;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.analysis.IRainbowAnalysis;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;

public abstract class P2CP3Analyzer extends AbstractRainbowRunnable implements IRainbowAnalysis {

	private IModelsManagerPort m_modelsManagerPort;
	protected IModelUSBusPort m_modelUSPort;
	protected IModelChangeBusSubscriberPort m_modelChangePort;
	private ModelAccessor m_modelAccessor;

	public P2CP3Analyzer(String name) {
		super(name);
		String period = Rainbow.instance().getProperty(RainbowConstants.PROPKEY_MODEL_EVAL_PERIOD);
		if (period != null) {
			setSleepTime(Long.parseLong(period));
		} else {
			setSleepTime(IRainbowRunnable.LONG_SLEEP_TIME);
		}
	}

	@Override
	public void dispose() {
		m_reportingPort.dispose();
		m_modelUSPort.dispose();
		
	}

	@Override
	public String getProperty(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProperty(String arg0, String arg1) {
		// TODO Auto-generated method stub

	}



	@Override
	protected void runAction() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initialize(IRainbowReportingPort port) throws RainbowConnectionException {
		super.initialize(port);
		initializeConnections();
	}

	private void initializeConnections() throws RainbowConnectionException {
		m_modelUSPort = RainbowPortFactory.createModelsManagerClientUSPort(this);
		m_modelsManagerPort = RainbowPortFactory.createModelsManagerRequirerPort();
		m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort();
		m_modelAccessor = new ModelAccessor(m_modelsManagerPort);
	}

	protected ModelAccessor getModels () {
		return m_modelAccessor;
	}
	

	@Override
	protected void log(String txt) {
		m_reportingPort.info(RainbowComponentT.ANALYSIS, txt);
	}

	
	@Override
	public RainbowComponentT getComponentType() {
		return RainbowComponentT.ANALYSIS;
	}

}
