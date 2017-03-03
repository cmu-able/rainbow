package org.sa.rainbow.brass.analyses;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.sa.rainbow.brass.PropertiesConnector;
import org.sa.rainbow.brass.adaptation.IGToPrismActionSequence;
import org.sa.rainbow.brass.adaptation.PrismConnectorAPI;
import org.sa.rainbow.brass.model.instructions.IInstruction;
import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress;
import org.sa.rainbow.brass.model.instructions.MoveAbsHInstruction;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapModelInstance;
import org.sa.rainbow.brass.model.map.EnvMapNode;
import org.sa.rainbow.brass.model.map.MapTranslator;
import org.sa.rainbow.brass.model.mission.MissionState;
import org.sa.rainbow.brass.model.mission.MissionStateModelInstance;
import org.sa.rainbow.brass.model.mission.SetRobotOnTimeCmd;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.analysis.IRainbowAnalysis;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;

/**
 * Analyzes timing property of the current plan, and triggers adaptation if necessary.
 * @author rsukkerd
 *
 */
public class TimingAnalyzer extends AbstractRainbowRunnable implements IRainbowAnalysis {

    private static final long TIME_BUFFER = 10L; // seconds //TODO
    private static final String TMP_MODEL_FILENAME = "timing_analyzer/prismtmp.prism";

    public static final String NAME = "BRASS Timing Evaluator";
    private IModelsManagerPort	m_modelsManagerPort;
    private IModelUSBusPort		m_modelUSPort;

    private ModelReference m_igRef = new ModelReference("ExecutingInstructionGraph", InstructionGraphModelInstance.INSTRUCTION_GRAPH_TYPE);
    private ModelReference m_msRef = new ModelReference("RobotAndEnvironmentState", MissionStateModelInstance.MISSION_STATE_TYPE);
    private ModelReference m_emRef = new ModelReference ("Map", EnvMapModelInstance.ENV_MAP_TYPE);

    private InstructionGraphProgress m_igProgress;
    private MissionState m_missionState;
    private EnvMap m_envMap;

    // Keep track of the previously-analyzed instruction
    private IInstruction m_prevAnalyzedInstruction;

    public TimingAnalyzer() {
        super(NAME);
        String period = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_MODEL_EVAL_PERIOD);
        if (period != null) {
            setSleepTime (Long.parseLong (period));
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
    public void dispose() {
        m_reportingPort.dispose ();
        m_modelUSPort.dispose ();
    }

    @Override
    public String getProperty(String key) {
        return null;
    }

    @Override
    public void setProperty(String key, String value) {

    }

    @Override
    public RainbowComponentT getComponentType() {
        return RainbowComponentT.ANALYSIS;
    }

    @Override
    protected void log(String text) {
        m_reportingPort.info (RainbowComponentT.ANALYSIS, text);
    }

    @Override
    protected void runAction() {
        // Do the periodic analysis on the models of interest
        updateIGProgress();
        updateMissionState();
        updateEnvMap();

        if (m_igProgress != null && m_missionState != null && m_envMap != null) {
            // The current instruction
            IInstruction currentInstruction = m_igProgress.getCurrentInstruction();

            // Only performing timing analysis once per instruction
            if (isNewInstruction(currentInstruction)) {

                if (m_missionState != null) {
                    long deadline = m_missionState.getDeadline();
                    long deadlineLowerBound = deadline - TIME_BUFFER; //TODO
                    long deadlineUpperBound = deadline + TIME_BUFFER; //TODO

                    // The remaining instructions, excluding the current instruction
                    List<IInstruction> remainingInstructions = (List<IInstruction>) m_igProgress.getRemainingInstructions();

                    boolean isOnTime = isOnTime(deadlineLowerBound, deadlineUpperBound, currentInstruction, remainingInstructions);

                    if (!isOnTime) {
                        // Update MissionState model to indicate that the robot is NOT expected be on time
                        MissionStateModelInstance missionStateModel = (MissionStateModelInstance) m_modelsManagerPort
                                .<MissionState> getModelInstance(m_msRef);
                        SetRobotOnTimeCmd robotOnTimeCmd = missionStateModel.getCommandFactory().setRobotOnTimeCmd(isOnTime);
                        m_modelUSPort.updateModel (robotOnTimeCmd);
                    }

                    // Keep track of the latest instruction that we have analyzed the timing property
                    m_prevAnalyzedInstruction = currentInstruction;
                }
            }
        }
    }

    private void updateIGProgress() {
        InstructionGraphModelInstance igModel = (InstructionGraphModelInstance) m_modelsManagerPort
                .<InstructionGraphProgress> getModelInstance(m_igRef);
        m_igProgress = igModel != null ? igModel.getModelInstance() : null;
    }

    private void updateMissionState() {
        MissionStateModelInstance missionStateModel = (MissionStateModelInstance) m_modelsManagerPort
                .<MissionState> getModelInstance(m_msRef);
        m_missionState = missionStateModel != null ? missionStateModel.getModelInstance() : null;
    }

    private void updateEnvMap() {
        EnvMapModelInstance envMapModel = (EnvMapModelInstance) m_modelsManagerPort.<EnvMap> getModelInstance (m_emRef);
        m_envMap = envMapModel != null ? envMapModel.getModelInstance() : null;
    }

    /**
     * Checks if this instruction is different from the previously-analyzed instruction
     */
    private boolean isNewInstruction(IInstruction instruction) {
        return (m_prevAnalyzedInstruction == null && instruction != null)
                || (instruction != null && !instruction.equals (m_prevAnalyzedInstruction));
    }

    /**
     * Checks if the instructions can be completed within the deadline window
     */
    private boolean isOnTime(long deadlineLowerBound, long deadlineUpperBound, 
            IInstruction currentInstruction, List<IInstruction> remainingInstructions) {
        double currentTime = m_missionState.getCurrentTime();
        long planExecutionTime = getExpectedIGExecutionTime(currentInstruction, remainingInstructions);
        long expectedPlanCompletionTime = (long) currentTime + planExecutionTime;

        return expectedPlanCompletionTime >= deadlineLowerBound && expectedPlanCompletionTime <= deadlineLowerBound;
    }

    /**
     * Calculates the expected execution time of the current and the remaining instructions
     */
    private long getExpectedIGExecutionTime(IInstruction currentInstruction, List<IInstruction> remainingInstructions) {
        int remainingActionSeqExecTime = 0;

        if (!remainingInstructions.isEmpty()) {
            // Starting position of the remaining instructions
            double remainingStartX;
            double remainingStartY;

            MoveAbsHInstruction prevMoveAbsH;

            if (currentInstruction instanceof MoveAbsHInstruction) {
                // The current instruction is of type MoveAbsH
                MoveAbsHInstruction currentMoveAbsH = (MoveAbsHInstruction) currentInstruction;
                remainingStartX = currentMoveAbsH.getTargetX();
                remainingStartY = currentMoveAbsH.getTargetY();
            } else if ((prevMoveAbsH = getPreviousMoveAbsH(currentInstruction)) != null) {
                // There is a previous instruction of type MoveAbsH
                remainingStartX = prevMoveAbsH.getTargetX();
                remainingStartY = prevMoveAbsH.getTargetY();
            } else {
                // There is no previous instruction of type MoveAbsH
                remainingStartX = m_missionState.getInitialPose().getX();
                remainingStartY = m_missionState.getInitialPose().getY();
            }
            
			// Action sequence of the remaining instructions
			IGToPrismActionSequence igToActionSequence = new IGToPrismActionSequence(m_envMap, remainingInstructions, 
					remainingStartX, remainingStartY);
			List<String> remainingActionSequence = igToActionSequence.translate();
			MapTranslator.exportConstrainedToPlanMapTranslation(TMP_MODEL_FILENAME, remainingActionSequence);
			
			String modelFileName = PropertiesConnector.DEFAULT.getProperty(PropertiesConnector.PRISM_OUTPUT_DIR) + TMP_MODEL_FILENAME;
			String propertiesFileName = PropertiesConnector.DEFAULT.getProperty(PropertiesConnector.PRISM_PROPERTIES_PROPKEY);
			String strategyFileName = PropertiesConnector.DEFAULT.getProperty(PropertiesConnector.PRISM_ADV_EXPORT_PROPKEY);
			int propertyToCheck = 0; //TODO
			
			EnvMapNode sourceNode = igToActionSequence.getSourceNode();
			EnvMapNode targetNode = igToActionSequence.getTargetNode();
			String batteryLevel = "1700"; //TODO
			String robotHeading = "1"; //TODO: MapTranslator to use prism const
			
			String constSwitch = MapTranslator.INITIAL_ROBOT_LOCATION_CONST + "=" + String.valueOf(m_envMap.getNodeId(sourceNode.getLabel())) + "," 
					+ MapTranslator.TARGET_ROBOT_LOCATION_CONST + "=" + String.valueOf(m_envMap.getNodeId(targetNode.getLabel())) + "," 
					+ MapTranslator.INITIAL_ROBOT_BATTERY_CONST + "=" + batteryLevel + "," 
					+ MapTranslator.INITIAL_ROBOT_HEADING_CONST + "=" + robotHeading;
			
			String result = PrismConnectorAPI.modelCheckFromFileS(modelFileName, propertiesFileName, strategyFileName, propertyToCheck, constSwitch);
			remainingActionSeqExecTime += Long.valueOf(result);
		}
		
		long currentInstructionExecTime = getCurrentInstructionExecutionTime();
		long totalExecTime = currentInstructionExecTime + remainingActionSeqExecTime;
		return totalExecTime;
	}
	
	/**
	 * Calculates the expected execution time of the (remaining of the) current instruction
	 */
	private long getCurrentInstructionExecutionTime() {
		//TODO
		return 0L;
	}
	
	private MoveAbsHInstruction getPreviousMoveAbsH (IInstruction instruction) {    	
		int j = Integer.valueOf (instruction.getInstructionLabel()) - 1;
    	for (int i = j; i > 0; i--) {
    		String label = String.valueOf (i);
    		IInstruction inst = m_igProgress.getInstruction(label);
    		
    		if (inst instanceof MoveAbsHInstruction) {
    			return (MoveAbsHInstruction) inst;
    		}
    	}
    	
    	// No previous MoveAbsH instruction
    	return null;
    }
}
