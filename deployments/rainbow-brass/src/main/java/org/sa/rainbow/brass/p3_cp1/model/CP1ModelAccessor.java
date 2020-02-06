package org.sa.rainbow.brass.p3_cp1.model;

import org.sa.rainbow.brass.confsynthesis.SimpleConfigurationStore;
import org.sa.rainbow.brass.model.P2ModelAccessor;
import org.sa.rainbow.brass.model.robot.RobotState;
import org.sa.rainbow.brass.model.robot.RobotStateModelInstance;
import org.sa.rainbow.brass.p3_cp1.model.power.CP1PowerModelInstance;
import org.sa.rainbow.brass.p3_cp1.model.robot.CP1RobotStateModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelsManagerPort;

public class CP1ModelAccessor extends P2ModelAccessor{
	private CP1RobotStateModelInstance m_robotStateModel;
	private CP1PowerModelInstance m_powerModel;
	
	public CP1ModelAccessor (IModelsManagerPort mmp) {
		super(mmp);
	}
	
	public CP1RobotStateModelInstance getRobotStateModel() {
		if (m_robotStateModel == null) {
			CP1RobotStateModelInstance modelInstance = (CP1RobotStateModelInstance) m_modelsManagerPort.<RobotState>getModelInstance(
					new ModelReference("Robot", RobotStateModelInstance.ROBOT_STATE_TYPE));
			m_robotStateModel = modelInstance;
		}
		return m_robotStateModel;
	}
	
	public CP1PowerModelInstance getPowerModel() {
		if (m_powerModel == null) {
			CP1PowerModelInstance modelInstance = (CP1PowerModelInstance )m_modelsManagerPort.<SimpleConfigurationStore>getModelInstance(new ModelReference("Power", CP1PowerModelInstance.POWER_MODEL_TYPE));
			m_powerModel = modelInstance;
		}
		return m_powerModel;
	}
	

}
