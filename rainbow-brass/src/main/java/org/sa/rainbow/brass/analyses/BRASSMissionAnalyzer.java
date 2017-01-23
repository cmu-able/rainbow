package org.sa.rainbow.brass.analyses;

import java.util.ArrayList;
import java.util.List;

import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapModelInstance;
import org.sa.rainbow.brass.model.map.InsertNodeCmd;
import org.sa.rainbow.brass.model.mission.MissionCommandFactory;
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
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
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
        ModelReference emRef = new ModelReference ("Map", EnvMapModelInstance.ENV_MAP_TYPE);
        MissionStateModelInstance missionStateModel = (MissionStateModelInstance) m_modelsManagerPort
                .<MissionState> getModelInstance(missionStateRef);
        InstructionGraphModelInstance igModel = (InstructionGraphModelInstance) m_modelsManagerPort
                .<InstructionGraphProgress> getModelInstance(igRef);
        EnvMapModelInstance envModel = (EnvMapModelInstance) m_modelsManagerPort.<EnvMap> getModelInstance (emRef);

        if (missionStateModel != null && igModel != null && envModel != null) {
            MissionState missionState = missionStateModel.getModelInstance();
            InstructionGraphProgress igProgress = igModel.getModelInstance();
            EnvMap envMap = envModel.getModelInstance();
            boolean currentOK = igProgress.getCurrentOK();

            if (!currentOK && igProgress.getExecutingInstruction () != null) {
            	// Current IG failed
                m_reportingPort.warn (getComponentType (), "Instruction graph failed");

                // Get current robot position
                LocationRecording pose = missionState.getCurrentPose ();
                
                // Get source and target positions of the failing instruction
                InstructionGraphProgress.Instruction currentInst = igProgress.getCurrentInstruction();
                double sourceX;
                double sourceY;
                double targetX = currentInst.getTargetX();
                double targetY = currentInst.getTargetY();
                
                if (currentInst.hasMoveAbsSourcePose()) {
	                sourceX = currentInst.getSourceX();
	                sourceY = currentInst.getSourceY();
                } else {
                	// The current instruction is the first instruction in IG
                	// Use the initial pose as the source pose
                	sourceX = missionState.getInitialPose().getX();
                	sourceY = missionState.getInitialPose().getY();
                }
                
                // Find the corresponding environment map nodes of the source and target positions
                // Node naming assumption: node's label is lX where X is the order in which the node is added
                int numNodes = envMap.getNodeCount() + 1;
                String n = "l" + numNodes;
                String na = envMap.getNode(sourceX, sourceY).getLabel();
                String nb = envMap.getNode(targetX, targetY).getLabel();
                
                // Update the environment map
                InsertNodeCmd insertNodeCmd = envModel.getCommandFactory ()
                		.insertNodeCmd (n, na, nb, Double.toString (pose.getX ()), Double.toString (pose.getY ()));
                
                // Set robot obstructed flag -- trigger planning for adaptation
                SetRobotObstructedCmd robotObstructedCmd = missionStateModel.getCommandFactory().setRobotObstructedCmd("true");
                
                // Send the commands
                List<IRainbowOperation> cmds = new ArrayList<> ();
                cmds.add(insertNodeCmd);
                cmds.add(robotObstructedCmd);
                m_modelUSPort.updateModel(cmds, true);
            } else if (currentOK && igProgress.getCurrentInstruction () != null && missionState.isRobotObstructed ()) {
            	// New IG resumed after robot obstructed
            	// Clear robot obstructed flag
            	SetRobotObstructedCmd clearRobotObstructedCmd = missionStateModel.getCommandFactory().setRobotObstructedCmd("false");
            	m_modelUSPort.updateModel(clearRobotObstructedCmd);
            }
        }
    }

    @Override
    public RainbowComponentT getComponentType () {
        return RainbowComponentT.ANALYSIS;
    }
}
