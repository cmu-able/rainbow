package org.sa.rainbow.brass.analyses.p2_cp3;

import java.util.Collection;
import java.util.EnumSet;

import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.brass.model.p2_cp3.acme.TurtlebotModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState.Sensors;
import org.sa.rainbow.brass.model.robot.RobotStateModelInstance;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.analysis.IRainbowAnalysis;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;

public class ConfigurationAnalyzer extends AbstractRainbowRunnable implements IRainbowAnalysis {

	private IModelsManagerPort m_modelsManagerPort;
	private IModelUSBusPort m_modelUSPort;
	private CP3RobotState m_robotStateModel;
	private TurtlebotModelInstance m_turtlebotArchModel;

	public ConfigurationAnalyzer() {
		super("TurtlebotConfigurationAnalyzer");
		String period = Rainbow.instance().getProperty(RainbowConstants.PROPKEY_MODEL_EVAL_PERIOD);
		if (period != null) {
			setSleepTime(Long.parseLong(period));
		}
		else {
			setSleepTime(IRainbowRunnable.LONG_SLEEP_TIME);
		}
	}
	
	@Override
	public void initialize(IRainbowReportingPort port) throws RainbowConnectionException {
		super.initialize(port);
		initializeConnections();
	}
	
	private void initializeConnections() throws RainbowConnectionException {
		m_modelUSPort = RainbowPortFactory.createModelsManagerClientUSPort(this);
		m_modelsManagerPort = RainbowPortFactory.createModelsManagerRequirerPort();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void setProperty(String key, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getProperty(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void log(String txt) {
		m_reportingPort.info(RainbowComponentT.ANALYSIS, txt);
	}

	
	
	@Override
	protected void runAction() {
		CP3RobotState rs = getRobotState();
		TurtlebotModelInstance tb = getTurtlebotModel();	
		
		EnumSet<Sensors> sensors = rs.getSensors();
		Collection<String> components = tb.getActiveComponents();
		StringBuffer log = new StringBuffer("Components: ");
		for (String c : components) {
			log.append(c);
			log.append(" ");
		}
		log.append("\nSensors: ");
		for (Sensors s : sensors) {
			log.append(s.name());
		}
		log(log.toString());
	}

	private TurtlebotModelInstance getTurtlebotModel() {
		if (m_turtlebotArchModel == null) {
			m_turtlebotArchModel = (TurtlebotModelInstance )m_modelsManagerPort.<IAcmeSystem>getModelInstance(
					new ModelReference("Turtlebot", "Acme"));
		}
		return m_turtlebotArchModel;
	}

	private CP3RobotState getRobotState() {
		if (m_robotStateModel == null) 
			m_robotStateModel = (CP3RobotState) m_modelsManagerPort.<CP3RobotState>getModelInstance(
					new ModelReference("Robot", RobotStateModelInstance.ROBOT_STATE_TYPE)).getModelInstance();
		return m_robotStateModel;
	}
	

	@Override
	public RainbowComponentT getComponentType() {
		return RainbowComponentT.ANALYSIS;
		
	}

}
