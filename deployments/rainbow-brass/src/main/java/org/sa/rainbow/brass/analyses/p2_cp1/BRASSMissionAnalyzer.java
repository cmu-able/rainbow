package org.sa.rainbow.brass.analyses.p2_cp1;

import java.util.Arrays;

import org.sa.rainbow.brass.das.BRASSHttpConnector;
import org.sa.rainbow.brass.das.IBRASSConnector.Phases;
import org.sa.rainbow.brass.model.instructions.IInstruction;
import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress.IGExecutionStateT;
import org.sa.rainbow.brass.model.instructions.MoveAbsHInstruction;
import org.sa.rainbow.brass.model.instructions.SetExecutionFailedCmd;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.InsertNodeCmd;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowState;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowState.CP3ModelState;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RemoveModelProblemCmd;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.SetModelProblemCmd;
import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IRainbowReportingPort;

/**
 * Created by schmerl on 12/13/2016. Analyzes the current situation and triggers
 * adaptation if necessary
 */
public class BRASSMissionAnalyzer extends P2CP1Analyzer {

	public static final String NAME = "BRASS Mission Evaluator";

	private boolean m_reportedReady = false;
	private boolean m_wasOK;

	private boolean m_awaitingNewIG;

	private boolean m_reportedCompleted;

	public BRASSMissionAnalyzer() {
		super(NAME);
		String per = Rainbow.instance().getProperty(RainbowConstants.PROPKEY_MODEL_EVAL_PERIOD);
		if (per != null) {
			setSleepTime(Long.parseLong(per));
		} else {
			setSleepTime(IRainbowRunnable.LONG_SLEEP_TIME);
		}
	}

	@Override
	public void initialize(IRainbowReportingPort port) throws RainbowConnectionException {
		super.initialize(port);
		log("Initialized missions analyzer");
	}

	@Override
	protected void runAction() throws RainbowException {
		MissionState ms = getModels().getMissionStateModel().getModelInstance();
		if (!m_reportedReady && ms.getInitialPose() != null) {
			m_reportedReady = true;
            BRASSHttpConnector.instance(Phases.Phase2).setClock(getModels().getMissionStateModel().getModelInstance());
			BRASSHttpConnector.instance(Phases.Phase2).reportReady(true);
			m_wasOK = true;
		}
		if (getModels().getRainbowStateModel().getModelInstance().waitForIG()) {
			m_wasOK = true;
			return;
		}
		// Do the periodic analysis on the models of interest
		InstructionGraphModelInstance ig = getModels().getInstructionGraphModel();


		if (ms.isMissionStarted() && ms.getInitialPose() != null) {
			
			boolean currentOK = ig.getModelInstance().getCurrentOK();
//			if (ig.getModelInstance().getInstructionGraphState() == IGExecutionStateT.FINISHED_SUCCESS && !m_reportedCompleted)  {
//				m_reportedCompleted = true;
//				m_awaitingNewIG = true;
//				SetModelProblemCmd cmd1 = getModels().getRainbowStateModel().getCommandFactory().setModelProblem(CP3ModelState.MISSION_COMPLETED);
//				return;
//			}
			if (!currentOK && m_wasOK && !getModels().getRainbowStateModel().getModelInstance().waitForIG()) {
				m_wasOK = false;
				m_reportingPort.info(getComponentType(), "Instruction graph failed...updating map model");
				SetModelProblemCmd cmd1 = getModels().getRainbowStateModel().getCommandFactory()
						.setModelProblem(CP3ModelState.INSTRUCTION_GRAPH_FAILED);
				SetModelProblemCmd cmd2 = getModels().getRainbowStateModel().getCommandFactory()
						.setModelProblem(CP3ModelState.IS_OBSTRUCTED);
				m_modelUSPort.updateModel(Arrays.asList(new IRainbowOperation[] { cmd1, cmd2 }), true);
				m_awaitingNewIG = true;
			}
			else if (currentOK && !emptyInstructions(ig.getModelInstance()) && getModels().getRainbowStateModel().getModelInstance()
					.getProblems().contains(RainbowState.CP3ModelState.IS_OBSTRUCTED)) {
				// New IG resumed after robot obstructed
				log("New instruction model was detected. Reseting models to ok");
				m_reportingPort.info(getComponentType(), "New instruction graph detected");
				m_awaitingNewIG = false;
				getModels().getRainbowStateModel().getModelInstance().m_waitForIG = false;
				// Clear robot obstructed flag
				RemoveModelProblemCmd cmd1 = getModels().getRainbowStateModel().getCommandFactory()
						.removeModelProblem(CP3ModelState.INSTRUCTION_GRAPH_FAILED);
				RemoveModelProblemCmd cmd2 = getModels().getRainbowStateModel().getCommandFactory()
						.removeModelProblem(CP3ModelState.IS_OBSTRUCTED);
				m_modelUSPort.updateModel(Arrays.asList(new IRainbowOperation[] { cmd1, cmd2 }), true);

			}
		}
		
		
//		if (getModels().getInstructionGraphModel() == null)
//			return;
//		InstructionGraphProgress igModel = getModels().getInstructionGraphModel().getModelInstance();
//		boolean currentOK = igModel.getCurrentOK();

//		org.sa.rainbow.brass.model.p2_cp3.mission.MissionState missionState = getModels().getMissionStateModel()
//				.getModelInstance();
//		EnvMap envMap = getModels().getEnvMapModel().getModelInstance();
//		if (missionState.getCurrentPose() == null) {
//			m_awaitingPose = true;
//		} else if (missionState.getCurrentPose() != null && m_awaitingPose) {
//			m_awaitingPose = false;
//			BRASSHttpConnector.instance(Phases.Phase2).reportReady(true);
//
//		} else if (!currentOK && igModel.getExecutingInstruction() != null && !m_awaitingNewIG) {
//			// Current IG failed
//			m_reportingPort.info(getComponentType(), "Instruction graph failed...updating map model");
//			// BRASSHttpConnector.instance(Phases.Phase2).reportStatus(DASPhase2StatusT.PERTURBATION_DETECTED,
//			// "Obstruction to path detected");
//			// // Get current robot position
//			// org.sa.rainbow.brass.model.p2_cp3.mission.MissionState.LocationRecording pose
//			// = missionState
//			// .getCurrentPose();
//			//
//			// // Get source and target positions of the failing instruction
//			// IInstruction currentInst = igModel.getCurrentInstruction();
//			//
//			// // The current instruction is of type MoveAbsH
//			// insertNodeIntoMap(pose, currentInst);
//			SetModelProblemCmd cmd1 = getModels().getRainbowStateModel().getCommandFactory()
//					.setModelProblem(CP3ModelState.INSTRUCTION_GRAPH_FAILED);
//			SetModelProblemCmd cmd2 = getModels().getRainbowStateModel().getCommandFactory()
//					.setModelProblem(CP3ModelState.IS_OBSTRUCTED);
//			m_modelUSPort.updateModel(Arrays.asList(new IRainbowOperation[] { cmd1, cmd2 }), true);
//			m_awaitingNewIG = true;
//		} else if (currentOK && !emptyInstructions(igModel) && getModels().getRainbowStateModel().getModelInstance()
//				.getProblems().contains(RainbowState.CP3ModelState.IS_OBSTRUCTED)) {
//			// New IG resumed after robot obstructed
//			log("New instruction model was detected. Reseting models to ok");
//			m_reportingPort.info(getComponentType(), "New instruction graph detected");
//			m_awaitingNewIG = false;
//			getModels().getRainbowStateModel().getModelInstance().m_waitForIG = false;
//			// Clear robot obstructed flag
//
//			RemoveModelProblemCmd cmd1 = getModels().getRainbowStateModel().getCommandFactory()
//					.removeModelProblem(CP3ModelState.INSTRUCTION_GRAPH_FAILED);
//			RemoveModelProblemCmd cmd2 = getModels().getRainbowStateModel().getCommandFactory()
//					.removeModelProblem(CP3ModelState.IS_OBSTRUCTED);
//			m_modelUSPort.updateModel(Arrays.asList(new IRainbowOperation[] { cmd1, cmd2 }), true);
//
//		}

	}

	boolean emptyInstructions(InstructionGraphProgress igProgress) {
		return igProgress.getInstructions() == null || igProgress.getInstructions().isEmpty();
	}

}
