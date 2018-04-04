package org.sa.rainbow.brass.model.p2_cp3;

import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress;
import org.sa.rainbow.brass.model.p2_cp3.acme.TurtlebotModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionStateModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowState;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowStateModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotStateModelInstance;
import org.sa.rainbow.brass.model.robot.RobotState;
import org.sa.rainbow.brass.model.robot.RobotStateModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelsManagerPort;

public class ModelAccessor {
	private IModelsManagerPort m_modelsManagerPort;
	private CP3RobotStateModelInstance m_robotStateModel;
	private TurtlebotModelInstance m_turtlebotArchModel;
	private RainbowStateModelInstance m_rainbowStateModel;
	private MissionStateModelInstance m_missionStateModel;
	private InstructionGraphModelInstance m_instructionGraphModel;
	
	public ModelAccessor (IModelsManagerPort mmp) {
		m_modelsManagerPort = mmp;
	}
	
	public TurtlebotModelInstance getTurtlebotModel() {
		if (m_turtlebotArchModel == null) {
			m_turtlebotArchModel = (TurtlebotModelInstance) m_modelsManagerPort
					.<IAcmeSystem>getModelInstance(new ModelReference("Turtlebot", "Acme"));
		}
		return m_turtlebotArchModel;
	}

	public CP3RobotStateModelInstance getRobotStateModel() {
		if (m_robotStateModel == null) {
			CP3RobotStateModelInstance modelInstance = (CP3RobotStateModelInstance) m_modelsManagerPort.<RobotState>getModelInstance(
					new ModelReference("Robot", RobotStateModelInstance.ROBOT_STATE_TYPE));
			m_robotStateModel = modelInstance;
		}
		return m_robotStateModel;
	}
	
	public RainbowStateModelInstance getRainbowStateModel() {
		if (m_rainbowStateModel == null)
			m_rainbowStateModel = (RainbowStateModelInstance) m_modelsManagerPort
					.<RainbowState>getModelInstance(new ModelReference("RainbowState", RainbowStateModelInstance.TYPE));
		return m_rainbowStateModel;
	}

	public MissionStateModelInstance getMissionStateModel () {
		if (m_missionStateModel == null) {
			m_missionStateModel = (MissionStateModelInstance )m_modelsManagerPort.<MissionState>getModelInstance(new ModelReference("MissionState", MissionStateModelInstance.MISSION_STATE_TYPE));
		}
		return m_missionStateModel;
	}
	
	public InstructionGraphModelInstance getInstructionGraphModel () {
		if (m_instructionGraphModel == null) {
			m_instructionGraphModel = (InstructionGraphModelInstance )m_modelsManagerPort.<InstructionGraphProgress>getModelInstance(new ModelReference("ExecutionInstructionGraph", InstructionGraphModelInstance.INSTRUCTION_GRAPH_TYPE));
		}
		return m_instructionGraphModel;
	}
}
