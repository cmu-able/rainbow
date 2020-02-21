package org.sa.rainbow.brass.analyses;

import org.sa.rainbow.brass.das.BRASSHttpConnector;
import org.sa.rainbow.brass.das.IBRASSConnector.DASPhase1StatusT;
import org.sa.rainbow.brass.das.IBRASSConnector.Phases;
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
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.analysis.IRainbowAnalysis;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;

/**
 * Created by schmerl on 12/13/2016.
 * Analyzes the current situation and triggers adaptation if necessary
 */
public class BRASSMissionAnalyzer extends AbstractRainbowRunnable implements IRainbowAnalysis {

    public static final String NAME = "BRASS Mission Evaluator";
    private IModelsManagerPort m_modelsManagerPort;
    private IModelUSBusPort m_modelUSPort;
    private boolean            m_awaitingNewIG;
    private boolean            m_awaitingPose;

    public BRASSMissionAnalyzer () {
        super(NAME);
        String per = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_MODEL_EVAL_PERIOD);
        if (per != null) {
            setSleepTime (Long.parseLong (per));
        } else {
            setSleepTime (IRainbowRunnable.LONG_SLEEP_TIME);
        }
    }

    @Override
    public void initialize (IRainbowReportingPort port) throws RainbowConnectionException {
        super.initialize (port);
        initializeConnections ();
        log ("Initialized missions analyzer");
    }

    private void initializeConnections () throws RainbowConnectionException {
        // Create a port to subscribe to model changes (if analyzer is event based)
        // m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort ();

        // Create a port to query things about a model
        m_modelsManagerPort = RainbowPortFactory.createModelsManagerRequirerPort ();

        // Create a port to change a model (e.g., to trigger adaptation, to set predicted score, etc.)
        m_modelUSPort = RainbowPortFactory.createModelsManagerClientUSPort (this);

    }

    @Override
    public void setProperty (String key, String value) {

    }

    @Override
    public String getProperty (String key) {
        return null;
    }

    @Override
    public void dispose () {
        m_reportingPort.dispose ();
        m_modelUSPort.dispose ();
    }

    @Override
    protected void log (String txt) {
        m_reportingPort.info (RainbowComponentT.ANALYSIS, txt);
    }

    @Override
    protected void runAction () {
        // Do the periodic analysis on the models of interest
        ModelReference missionStateRef = new ModelReference("RobotAndEnvironmentState", MissionStateModelInstance.MISSION_STATE_TYPE);
        ModelReference igRef = new ModelReference("ExecutingInstructionGraph", InstructionGraphModelInstance.INSTRUCTION_GRAPH_TYPE);
        MissionStateModelInstance missionStateModel = (MissionStateModelInstance) m_modelsManagerPort
                .<MissionState> getModelInstance(missionStateRef);
        InstructionGraphModelInstance igModel = (InstructionGraphModelInstance) m_modelsManagerPort
                .<InstructionGraphProgress> getModelInstance(igRef);
        ModelReference emRef = new ModelReference ("Map", EnvMapModelInstance.ENV_MAP_TYPE);
        EnvMapModelInstance envModel = (EnvMapModelInstance) m_modelsManagerPort.<EnvMap> getModelInstance (emRef);

        try {
			if (missionStateModel != null && igModel != null && envModel != null) {
			    MissionState missionState = missionStateModel.getModelInstance();
			    InstructionGraphProgress igProgress = igModel.getModelInstance();
			    EnvMap envMap = envModel.getModelInstance();
			    boolean currentOK = igProgress.getCurrentOK();
//            if (missionState.isAdaptationNeeded ()) return;

			    // If we start off with nothing (i.e., no instruction graph), this is a problem
//            if (emptyInstructions (igProgress)
//                    && !missionState.isRobotObstructed () && missionState.getCurrentPose () != null) {
//                log ("Robot has no instructions - triggering planning to get started");
//                SetRobotObstructedCmd cmd = missionStateModel.getCommandFactory ().setRobotObstructedCmd (true);
//                m_modelUSPort.updateModel (cmd);
//            }

//            log ("Mission Progress Analyzer");
//            log ("Finished succesfully? "
//                    + (igProgress.getInstructionGraphState () == IGExecutionStateT.FINISHED_SUCCESS));
//            log ("Finished failed? " + (igProgress.getInstructionGraphState () == IGExecutionStateT.FINISHED_FAILED));
//            log ("Awaiting pose? " + (missionState.getCurrentPose () == null) + ", " + m_awaitingPose);
//            log ("CurrentOK? " + currentOK);
//            log ("Executing Instruction?" + (igProgress.getExecutingInstruction () != null));
//            log ("Awaiting new IG?" + m_awaitingNewIG);
//            log ("Empty instructions?" + emptyInstructions (igProgress));
//            log ("Robot obstructed known? " + missionState.isRobotObstructed ());
			    if (igProgress.getInstructionGraphState () == IGExecutionStateT.FINISHED_SUCCESS) {
			        BRASSHttpConnector.instance (Phases.Phase1).reportDone (false, "Finished all the instructions in the task");
			        Rainbow.instance ().signalTerminate ();
			        return;

			    }
			    if (missionState.getCurrentPose () == null) {
			        m_awaitingPose = true;
			    }
			    else if (missionState.getCurrentPose () != null && m_awaitingPose) {
			        m_awaitingPose = false;
			        BRASSHttpConnector.instance (Phases.Phase1).reportReady (true);

			    }
			    else if (!currentOK && igProgress.getExecutingInstruction () != null && !m_awaitingNewIG
			            /*&& igProgress.getInstructionGraphState () == IGExecutionStateT.FINISHED_FAILED*/) {
			        // Current IG failed
			        m_reportingPort.info (getComponentType (), "Instruction graph failed...updating map model");
			        BRASSHttpConnector.instance (Phases.Phase1).reportStatus (DASPhase1StatusT.PERTURBATION_DETECTED.name(),
			                "Obstruction to path detected");
			        // Get current robot position
			        LocationRecording pose = missionState.getCurrentPose ();

			        // Get source and target positions of the failing instruction
			        IInstruction currentInst = igProgress.getCurrentInstruction();

			        // The current instruction is of type MoveAbsH
			        if (currentInst instanceof MoveAbsHInstruction) {
			            MoveAbsHInstruction currentMoveAbsH = (MoveAbsHInstruction) currentInst;
			            MoveAbsHInstruction prevMoveAbsH = getPreviousMoveAbsH(currentMoveAbsH, igProgress);

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

			            // Find the corresponding environment map nodes of the source and target positions
			            // Node naming assumption: node's label is lX where X is the order in which the node is added
			            int numNodes = envMap.getNodeCount() + 1;
			            String n = "l" + numNodes;
			            String na = envMap.getNode (sourceX, sourceY).getLabel ();
			            String nb = envMap.getNode (targetX, targetY).getLabel ();

			            // Update the environment map
			            String rx = Double.toString (pose.getX ());
			            String ry = Double.toString (pose.getY ());
			            InsertNodeCmd insertNodeCmd = envModel.getCommandFactory ()
			                    .insertNodeCmd (n, na, nb, rx, ry, "true");
			            log ("Inserting node '" + n + "' at (" + rx + ", " + ry + ") between " + na + " and " + nb);

			            // Set robot obstructed flag -- trigger planning for adaptation
			            SetRobotObstructedCmd robotObstructedCmd = missionStateModel.getCommandFactory ()
			                    .setRobotObstructedCmd (true);

			            SetExecutionFailedCmd resetFailedCmd = igModel.getCommandFactory ().setExecutionFailedCmd ("false");

			            // Send the commands -- different models, so can't bundle them
			            m_modelUSPort.updateModel (resetFailedCmd);
			            m_modelUSPort.updateModel (insertNodeCmd);
			            m_modelUSPort.updateModel (robotObstructedCmd);
			            m_awaitingNewIG = true;
			        }
			    }
			    else if (currentOK && !emptyInstructions (igProgress) && missionState.isRobotObstructed ()) {
			        // New IG resumed after robot obstructed
			        log ("New instruction model was detected. Reseting models to ok");
			        m_reportingPort.info (getComponentType (), "New instruction graph detected");
			        m_awaitingNewIG = false;
			        // Clear robot obstructed flag
			        SetRobotObstructedCmd clearRobotObstructedCmd = missionStateModel.getCommandFactory ()
			                .setRobotObstructedCmd (false);
			        m_modelUSPort.updateModel(clearRobotObstructedCmd);
			    }
			}
		} catch (RainbowException e) {
			e.printStackTrace();
		}
    }

    boolean emptyInstructions (InstructionGraphProgress igProgress) {
        return igProgress.getInstructions () == null || igProgress.getInstructions ().isEmpty ();
    }

    private MoveAbsHInstruction getPreviousMoveAbsH (MoveAbsHInstruction currentMoveAbsH, InstructionGraphProgress igProgress) {
        int j = Integer.valueOf (currentMoveAbsH.getInstructionLabel()) - 1;
        for (int i = j; i > 0; i--) {
            String label = String.valueOf (i);
            IInstruction instruction = igProgress.getInstruction(label);

            if (instruction instanceof MoveAbsHInstruction) return (MoveAbsHInstruction) instruction;
        }

        // No previous MoveAbsH instruction
        return null;
    }

    @Override
    public RainbowComponentT getComponentType () {
        return RainbowComponentT.ANALYSIS;
    }
}
