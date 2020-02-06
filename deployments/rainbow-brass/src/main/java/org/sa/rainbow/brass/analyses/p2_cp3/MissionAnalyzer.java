package org.sa.rainbow.brass.analyses.p2_cp3;

import org.sa.rainbow.brass.das.BRASSHttpConnector;
import org.sa.rainbow.brass.das.IBRASSConnector.Phases;
import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress.IGExecutionStateT;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RemoveModelProblemCmd;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.SetModelProblemCmd;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowState.CP3ModelState;

public class MissionAnalyzer extends P2CP3Analyzer {

	private boolean m_reportedReady = false;
	private boolean m_wasOK;

	public MissionAnalyzer() {
		super("Mission evaluator");
	}

	@Override
	protected void runAction() throws RainbowException {
		InstructionGraphModelInstance ig = getModels().getInstructionGraphModel();
		
		MissionState ms = getModels().getMissionStateModel().getModelInstance();

		if (!m_reportedReady && ms.getInitialPose() != null) {
			m_reportedReady = true;
            BRASSHttpConnector.instance(Phases.Phase2).setClock(getModels().getMissionStateModel().getModelInstance());
			BRASSHttpConnector.instance(Phases.Phase2).reportReady(true);
			m_wasOK = true;
		}
		
		if (ms.isMissionStarted() && ms.getInitialPose() != null) {
			
			boolean currentOK = ig.getModelInstance().getCurrentOK();
			
			if (!currentOK && m_wasOK) {
				m_wasOK = false;
				log("MissionAnalyser reporting INSTRUCTION_GRAPH_FAILED");
				SetModelProblemCmd cmd = getModels().getRainbowStateModel ().getCommandFactory ().setModelProblem(CP3ModelState.INSTRUCTION_GRAPH_FAILED);
				m_modelUSPort.updateModel(cmd);
			}
			else if (currentOK && !m_wasOK) {
				m_wasOK = true;
				log("MissionAnalyzer removing INSTRUCTION_GRAPH_FAILED");
				RemoveModelProblemCmd cmd = getModels().getRainbowStateModel().getCommandFactory ().removeModelProblem(CP3ModelState.INSTRUCTION_GRAPH_FAILED);
				m_modelUSPort.updateModel(cmd);
			}
			else if (ig.getModelInstance().getInstructionGraphState() == IGExecutionStateT.FINISHED_SUCCESS) {
				log("Reporting that we are done");
				BRASSHttpConnector.instance(Phases.Phase2).reportDone(false, "Finished the IG");
			}
		}
	}
}
