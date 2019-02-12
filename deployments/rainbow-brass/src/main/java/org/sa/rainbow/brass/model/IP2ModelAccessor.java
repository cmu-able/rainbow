package org.sa.rainbow.brass.model;

import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.map.EnvMapModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionStateModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowStateModelInstance;

public interface IP2ModelAccessor {

	EnvMapModelInstance getEnvMapModel();

	InstructionGraphModelInstance getInstructionGraphModel();

	MissionStateModelInstance getMissionStateModel();

	RainbowStateModelInstance getRainbowStateModel();

}
