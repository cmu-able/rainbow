package org.sa.rainbow.brass.model;

import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionStateModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowState;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowStateModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelsManagerPort;

public class P2ModelAccessor {

	protected IModelsManagerPort m_modelsManagerPort;
	private RainbowStateModelInstance m_rainbowStateModel;
	private MissionStateModelInstance m_missionStateModel;
	private InstructionGraphModelInstance m_instructionGraphModel;
	private EnvMapModelInstance m_envMapModel;

	public P2ModelAccessor(IModelsManagerPort mmp) {
		m_modelsManagerPort = mmp;
	}

	public RainbowStateModelInstance getRainbowStateModel() {
		if (m_rainbowStateModel == null)
			m_rainbowStateModel = (RainbowStateModelInstance) m_modelsManagerPort
					.<RainbowState>getModelInstance(new ModelReference("RainbowState", RainbowStateModelInstance.TYPE));
		return m_rainbowStateModel;
	}

	public MissionStateModelInstance getMissionStateModel() {
		if (m_missionStateModel == null) {
			m_missionStateModel = (MissionStateModelInstance )m_modelsManagerPort.<MissionState>getModelInstance(new ModelReference("MissionState", MissionStateModelInstance.MISSION_STATE_TYPE));
		}
		return m_missionStateModel;
	}

	public InstructionGraphModelInstance getInstructionGraphModel() {
		if (m_instructionGraphModel == null) {
			m_instructionGraphModel = (InstructionGraphModelInstance )m_modelsManagerPort.<InstructionGraphProgress>getModelInstance(new ModelReference("ExecutingInstructionGraph", InstructionGraphModelInstance.INSTRUCTION_GRAPH_TYPE));
		}
		return m_instructionGraphModel;
	}

	public EnvMapModelInstance getEnvMapModel() {
		if (m_envMapModel == null) {
			m_envMapModel = (EnvMapModelInstance )m_modelsManagerPort.<EnvMap>getModelInstance(new ModelReference("Map", EnvMapModelInstance.ENV_MAP_TYPE));
		}
		return m_envMapModel;
	}

}
