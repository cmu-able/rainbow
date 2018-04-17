package org.sa.rainbow.brass.analyses.p2_cp1;

import java.util.Arrays;

import org.sa.rainbow.brass.das.BRASSHttpConnector;
import org.sa.rainbow.brass.das.IBRASSConnector.DASStatusT;
import org.sa.rainbow.brass.model.instructions.IInstruction;
import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress.IGExecutionStateT;
import org.sa.rainbow.brass.model.instructions.MoveAbsHInstruction;
import org.sa.rainbow.brass.model.instructions.SetExecutionFailedCmd;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapModelInstance;
import org.sa.rainbow.brass.model.map.InsertNodeCmd;
import org.sa.rainbow.brass.model.mission.MissionState;
import org.sa.rainbow.brass.model.mission.MissionState.LocationRecording;
import org.sa.rainbow.brass.model.mission.MissionStateModelInstance;
import org.sa.rainbow.brass.model.mission.SetRobotObstructedCmd;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowState;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RemoveModelProblemCmd;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.SetModelProblemCmd;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowState.CP3ModelState;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.analysis.IRainbowAnalysis;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;

/**
 * Created by schmerl on 12/13/2016. Analyzes the current situation and triggers
 * adaptation if necessary
 */
public class BRASSMissionAnalyzer extends P2CP1Analyzer {

	public static final String NAME = "BRASS Mission Evaluator";

	private boolean m_awaitingNewIG;
	private boolean m_awaitingPose;

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
	protected void runAction() {
		// Do the periodic analysis on the models of interest
		if (getModels().getInstructionGraphModel() == null) return;
		InstructionGraphProgress igModel = getModels().getInstructionGraphModel().getModelInstance();
		boolean currentOK = igModel.getCurrentOK();
		if (igModel.getInstructionGraphState() == IGExecutionStateT.FINISHED_SUCCESS) {
			// Report done to the Rainbow State -- planner should execute the effector to
			// call Pooyan's stuff
			return;
		}
		org.sa.rainbow.brass.model.p2_cp3.mission.MissionState missionState = getModels().getMissionStateModel()
				.getModelInstance();
		EnvMap envMap = getModels().getEnvMapModel().getModelInstance();
		if (missionState.getCurrentPose() == null) {
			m_awaitingPose = true;
		} else if (missionState.getCurrentPose() != null && m_awaitingPose) {
			m_awaitingPose = false;
			BRASSHttpConnector.instance().reportReady(true);

		} else if (!currentOK && igModel.getExecutingInstruction() != null && !m_awaitingNewIG) {
			// Current IG failed
			m_reportingPort.info(getComponentType(), "Instruction graph failed...updating map model");
			BRASSHttpConnector.instance().reportStatus(DASStatusT.PERTURBATION_DETECTED,
					"Obstruction to path detected");
			// Get current robot position
			org.sa.rainbow.brass.model.p2_cp3.mission.MissionState.LocationRecording pose = missionState
					.getCurrentPose();

			// Get source and target positions of the failing instruction
			IInstruction currentInst = igModel.getCurrentInstruction();

			// The current instruction is of type MoveAbsH
			if (currentInst instanceof MoveAbsHInstruction) {
				MoveAbsHInstruction currentMoveAbsH = (MoveAbsHInstruction) currentInst;
				MoveAbsHInstruction prevMoveAbsH = getPreviousMoveAbsH(currentMoveAbsH, igModel);

				double sourceX;
				double sourceY;
				double targetX = currentMoveAbsH.getTargetX();
				double targetY = currentMoveAbsH.getTargetY();

				if (prevMoveAbsH != null) {
					sourceX = prevMoveAbsH.getTargetX();
					sourceY = prevMoveAbsH.getTargetY();
				} else {
					// The current instruction is the first MoveAbsH instruction in IG
					// Use the initial pose as the source pose
					sourceX = missionState.getInitialPose().getX();
					sourceY = missionState.getInitialPose().getY();
				}

				// Find the corresponding environment map nodes of the source and target
				// positions
				// Node naming assumption: node's label is lX where X is the order in which the
				// node is added
				int numNodes = envMap.getNodeCount() + 1;
				String n = "l" + numNodes;
				String na = envMap.getNode(sourceX, sourceY).getLabel();
				String nb = envMap.getNode(targetX, targetY).getLabel();

				// Update the environment map
				String rx = Double.toString(pose.getX());
				String ry = Double.toString(pose.getY());
				InsertNodeCmd insertNodeCmd = getModels().getEnvMapModel().getCommandFactory().insertNodeCmd(n, na, nb,
						rx, ry, "true");
				log("Inserting node '" + n + "' at (" + rx + ", " + ry + ") between " + na + " and " + nb);

				// Set robot obstructed flag -- trigger planning for adaptation
				SetModelProblemCmd cmd1 = getModels().getRainbowStateModel().getCommandFactory()
						.setModelProblem(CP3ModelState.INSTRUCTION_GRAPH_FAILED);
				SetModelProblemCmd cmd2 = getModels().getRainbowStateModel().getCommandFactory()
						.setModelProblem(CP3ModelState.IS_OBSTRUCTED);

				SetExecutionFailedCmd resetFailedCmd = getModels().getInstructionGraphModel().getCommandFactory()
						.setExecutionFailedCmd("false");

				// Send the commands -- different models, so can't bundle them
				m_modelUSPort.updateModel(resetFailedCmd);
				m_modelUSPort.updateModel(insertNodeCmd);
				m_modelUSPort.updateModel(Arrays.asList(new IRainbowOperation[] { cmd1, cmd2 }), true);
				m_awaitingNewIG = true;
			}
		} else if (currentOK && !emptyInstructions(igModel) && getModels().getRainbowStateModel().getModelInstance()
				.getProblems().contains(RainbowState.CP3ModelState.IS_OBSTRUCTED)) {
			// New IG resumed after robot obstructed
			log("New instruction model was detected. Reseting models to ok");
			m_reportingPort.info(getComponentType(), "New instruction graph detected");
			m_awaitingNewIG = false;
			// Clear robot obstructed flag
			
			RemoveModelProblemCmd cmd1 = getModels().getRainbowStateModel().getCommandFactory()
					.removeModelProblem(CP3ModelState.INSTRUCTION_GRAPH_FAILED);
			RemoveModelProblemCmd cmd2 = getModels().getRainbowStateModel().getCommandFactory()
					.removeModelProblem(CP3ModelState.IS_OBSTRUCTED);
			m_modelUSPort.updateModel(Arrays.asList(new IRainbowOperation[] { cmd1, cmd2 }), true);

		}

	}

	boolean emptyInstructions(InstructionGraphProgress igProgress) {
		return igProgress.getInstructions() == null || igProgress.getInstructions().isEmpty();
	}

	private MoveAbsHInstruction getPreviousMoveAbsH(MoveAbsHInstruction currentMoveAbsH,
			InstructionGraphProgress igProgress) {
		int j = Integer.valueOf(currentMoveAbsH.getInstructionLabel()) - 1;
		for (int i = j; i > 0; i--) {
			String label = String.valueOf(i);
			IInstruction instruction = igProgress.getInstruction(label);

			if (instruction instanceof MoveAbsHInstruction)
				return (MoveAbsHInstruction) instruction;
		}

		// No previous MoveAbsH instruction
		return null;
	}

}
