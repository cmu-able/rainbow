package org.sa.rainbow.brass.model.p2_cp1;

import org.sa.rainbow.brass.model.P2ModelAccessor;
import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapModelInstance;
import org.sa.rainbow.brass.model.p2_cp1.robot.CP1RobotStateModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionStateModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowState;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowStateModelInstance;
import org.sa.rainbow.brass.model.robot.RobotState;
import org.sa.rainbow.brass.model.robot.RobotStateModelInstance;
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
