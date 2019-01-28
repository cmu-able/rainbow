package org.sa.rainbow.brass.p3_cp1.model;

import org.sa.rainbow.brass.model.P2ModelAccessor;
import org.sa.rainbow.brass.model.robot.RobotState;
import org.sa.rainbow.brass.model.robot.RobotStateModelInstance;
import org.sa.rainbow.brass.p3_cp1.model.robot.CP1RobotStateModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelsManagerPort;

public class CP1ModelAccessor extends P2ModelAccessor{
	private CP1RobotStateModelInstance m_robotStateModel;
	
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
	

}
