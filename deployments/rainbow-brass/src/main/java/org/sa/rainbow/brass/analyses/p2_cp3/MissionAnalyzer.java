package org.sa.rainbow.brass.analyses.p2_cp3;

import org.sa.rainbow.brass.das.BRASSHttpConnector;
import org.sa.rainbow.brass.das.IBRASSConnector.Phases;
import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RemoveModelProblemCmd;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.SetModelProblemCmd;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowState.CP3ModelState;

public class MissionAnalyzer extends P2CP3Analyzer {

	private boolean m_reportedReady = false;
	private boolean m_wasOK;

	public MissionAnalyzer() {
		super("Mission evaluator");
	}

	@Override
	protected void runAction() {
		InstructionGraphModelInstance ig = getModels().getInstructionGraphModel();
		
		MissionState ms = getModels().getMissionStateModel().getModelInstance();

	
		
		if (ms.isMissionStarted()) {
			if (!m_reportedReady) {
				m_reportedReady = true;
				BRASSHttpConnector.instance(Phases.Phase2).reportReady(true);
				m_wasOK = true;
			}
			boolean currentOK = ig.getModelInstance().getCurrentOK();
			
			if (!currentOK && m_wasOK) {
				m_wasOK = false;
				SetModelProblemCmd cmd = getModels().getRainbowStateModel ().getCommandFactory ().setModelProblem(CP3ModelState.INSTRUCTION_GRAPH_FAILED);
				m_modelUSPort.updateModel(cmd);
			}
			else if (currentOK && !m_wasOK) {
				m_wasOK = true;
				RemoveModelProblemCmd cmd = getModels().getRainbowStateModel().getCommandFactory ().removeModelProblem(CP3ModelState.INSTRUCTION_GRAPH_FAILED);
				m_modelUSPort.updateModel(cmd);
			}
		}
	}
}
