package org.sa.rainbow.brass.analyses;

import java.util.List;

import org.sa.rainbow.brass.PropertiesConnector;
import org.sa.rainbow.brass.adaptation.IGToPrismActionSequence;
import org.sa.rainbow.brass.adaptation.PrismConnectorAPI;
import org.sa.rainbow.brass.model.instructions.ForwardInstruction;
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
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelUSBusPort;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;

/**
 * Analyzes timing property of the current plan, and triggers adaptation if necessary.
 * @author rsukkerd
 *
 */
public class TimingAnalyzer extends AbstractRainbowRunnable implements IRainbowAnalysis, IRainbowModelChangeCallback {

    private static final long DEADLINE_EARLY_BUFFER = 20L; // seconds //TODO
    private static final long DEADLINE_LATE_BUFFER = 10L; // seconds //TODO
    private static final String TMP_MODEL_FILENAME = "timing_analyzer/prismtmp.prism";

    public static final String NAME = "BRASS Timing Evaluator";
    
    private IModelChangeBusSubscriberPort		m_modelChangePort;
    private IModelsManagerPort					m_modelsManagerPort;
    private IModelUSBusPort						m_modelUSPort;
    
    private IRainbowChangeBusSubscription m_plannerFinish = new IRainbowChangeBusSubscription() {
		
		@Override
		public boolean matches(IRainbowMessage message) {
			String modelName = (String) message.getProperty (
                    IModelChangeBusPort.MODEL_NAME_PROP);
            String modelType = (String) message.getProperty (
                    IModelChangeBusPort.MODEL_TYPE_PROP);
            String commandName = (String) message.getProperty (
                    IModelChangeBusPort.COMMAND_PROP);

            //TODO
            return MissionStateModelInstance.MISSION_STATE_TYPE
                    .equals (modelType)
                    && "RobotAndEnvironmentState"
                    .equals (modelName)
                    && "setRobotObstructed"
                    .equals (commandName);
		}
	};
	
	// If adaptation planning is in progress, this analyzer will wait for it to finish
	private boolean m_waitForPlanner = false;

    private ModelReference m_igRef = new ModelReference("ExecutingInstructionGraph", InstructionGraphModelInstance.INSTRUCTION_GRAPH_TYPE);
    private ModelReference m_msRef = new ModelReference("RobotAndEnvironmentState", MissionStateModelInstance.MISSION_STATE_TYPE);
    private ModelReference m_emRef = new ModelReference ("Map", EnvMapModelInstance.ENV_MAP_TYPE);

    private InstructionGraphProgress m_igProgress;
    private MissionState m_missionState;
    private EnvMap m_envMap;

    // Keep track of the previously-analyzed instruction, which passed
    private IInstruction m_prevAnalyzedAndPassedInstruction;

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
        m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort ();
        m_modelChangePort.subscribe (m_plannerFinish, this);

        // Create a port to query things about a model
        m_modelsManagerPort = RainbowPortFactory.createModelsManagerRequirerPort ();

        // Create a port to change a model (e.g., to trigger adaptation, to set predicted score, etc.)
        m_modelUSPort = RainbowPortFactory.createModelsManagerClientUSPort (this);
    }

    @Override
    public void dispose() {
        m_reportingPort.dispose ();
        m_modelUSPort.dispose ();
        m_modelChangePort.dispose ();
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
	public void onEvent(ModelReference mr, IRainbowMessage message) {
    	synchronized (this) {
    		//TODO
            Boolean obstructed = Boolean
                    .parseBoolean ((String) message.getProperty (IModelChangeBusPort.PARAMETER_PROP + "0"));
            m_waitForPlanner = false;
        }
	}

	@Override
    protected void log(String text) {
        m_reportingPort.info (RainbowComponentT.ANALYSIS, text);
    }

    @Override
    protected void runAction() {
    	// If adaptation planning is in progress, wait for it to finish before performing analysis
    	if (!m_waitForPlanner) {
	        // Do the periodic analysis on the models of interest
	        updateIGProgress();
	        updateMissionState();
	        updateEnvMap();
	
	        if (m_igProgress != null && m_missionState != null && m_envMap != null) {
	            // The current instruction
	            IInstruction currentInstruction = m_igProgress.getCurrentInstruction();
	
	            // Only performing timing analysis once per instruction
	            if (isNewOrUnpassedInstruction(currentInstruction)) {
	
	                if (m_missionState != null) {
	                    long deadline = m_missionState.getDeadline();
	                    long deadlineLowerBound = deadline - DEADLINE_EARLY_BUFFER;
	                    long deadlineUpperBound = deadline + DEADLINE_LATE_BUFFER;
	
	                    // The remaining instructions, excluding the current instruction
	                    List<IInstruction> remainingInstructions = (List<IInstruction>) m_igProgress.getRemainingInstructions();
	
	                    boolean isOnTime = isOnTime(deadlineLowerBound, deadlineUpperBound, currentInstruction, remainingInstructions);
	                    
	                    if (isOnTime) {
	                    	// Keep track of the latest instruction that we have analyzed the timing property,
	                    	// and it passed
		                    m_prevAnalyzedAndPassedInstruction = currentInstruction;
	                    } 
	                    
	                    if (isOnTime && !m_missionState.isRobotOnTime()) {
	                    	// Previous plan was not on time; new plan is expected to be on time
	                    	// Update MissionState model to indicate that the robot is now expected be on time
	                        MissionStateModelInstance missionStateModel = (MissionStateModelInstance) m_modelsManagerPort
	                                .<MissionState> getModelInstance(m_msRef);
	                        SetRobotOnTimeCmd robotOnTimeCmd = missionStateModel.getCommandFactory().setRobotOnTimeCmd(true);
	                        m_modelUSPort.updateModel (robotOnTimeCmd);
	                    } else if (!isOnTime) {
	                        // Update MissionState model to indicate that the robot is NOT expected be on time
	                        MissionStateModelInstance missionStateModel = (MissionStateModelInstance) m_modelsManagerPort
	                                .<MissionState> getModelInstance(m_msRef);
	                        SetRobotOnTimeCmd robotOnTimeCmd = missionStateModel.getCommandFactory().setRobotOnTimeCmd(false);
	                        m_modelUSPort.updateModel (robotOnTimeCmd);
	                        
	                        // Wait for the planner to come up with an adaptation plan
	                        m_waitForPlanner = true;
	                    } 
	                }
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
     * Checks if this instruction is different from the previously-analyzed instruction,
     * or if this instruction has not passed the timing check previously
     */
    private boolean isNewOrUnpassedInstruction(IInstruction instruction) {
        return (m_prevAnalyzedAndPassedInstruction == null && instruction != null)
                || (instruction != null && !instruction.equals (m_prevAnalyzedAndPassedInstruction));
    }

    /**
     * Checks if the instructions can be completed within the deadline window
     */
    private boolean isOnTime(long deadlineLowerBound, long deadlineUpperBound, 
            IInstruction currentInstruction, List<IInstruction> remainingInstructions) {
        double currentTime = m_missionState.getCurrentTime();
        double planExecutionTime = getExpectedIGExecutionTime(currentInstruction, remainingInstructions);
        long expectedPlanCompletionTime = (long) (currentTime + planExecutionTime);

        return expectedPlanCompletionTime >= deadlineLowerBound && expectedPlanCompletionTime <= deadlineLowerBound;
    }

    /**
     * Calculates the expected execution time of the current and the remaining instructions
     */
    private double getExpectedIGExecutionTime(IInstruction currentInstruction, List<IInstruction> remainingInstructions) {
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
			String batteryLevel = Double.toString(m_missionState.getBatteryCharge());
			String robotHeading = Integer.toString(m_missionState.getCurrentPose().getHeading().ordinal()); // ref: MapTranslator.generateHeadingConstants()
			
			String constSwitch = MapTranslator.INITIAL_ROBOT_LOCATION_CONST + "=" + String.valueOf(m_envMap.getNodeId(sourceNode.getLabel())) + "," 
					+ MapTranslator.TARGET_ROBOT_LOCATION_CONST + "=" + String.valueOf(m_envMap.getNodeId(targetNode.getLabel())) + "," 
					+ MapTranslator.INITIAL_ROBOT_BATTERY_CONST + "=" + batteryLevel + "," 
					+ MapTranslator.INITIAL_ROBOT_HEADING_CONST + "=" + robotHeading;
			
			String result = PrismConnectorAPI.modelCheckFromFileS(modelFileName, propertiesFileName, strategyFileName, propertyToCheck, constSwitch);
			remainingActionSeqExecTime += Double.valueOf(result);
		}
		
		double currentInstructionExecTime = getCurrentInstructionExecutionTime(currentInstruction);
		double totalExecTime = currentInstructionExecTime + remainingActionSeqExecTime;
		return totalExecTime;
	}
	
	/**
	 * Calculates the expected execution time of the (remaining of the) current instruction
	 */
	private double getCurrentInstructionExecutionTime(IInstruction currentInstruction) {
		double currentX = m_missionState.getCurrentPose().getX();
		double currentY = m_missionState.getCurrentPose().getY();
		double currentW = m_missionState.getCurrentPose().getRotation();
		
		if (currentInstruction instanceof MoveAbsHInstruction) {
			MoveAbsHInstruction moveAbsH = (MoveAbsHInstruction) currentInstruction;
			double targetX = moveAbsH.getTargetX();
			double targetY = moveAbsH.getTargetY();
			double targetW = moveAbsH.getTargetW();
			double moveSpeed = moveAbsH.getSpeed();
			double rotateSpeed = MapTranslator.ROBOT_ROTATIONAL_SPEED_VALUE;
			double remainingManhattanDistance = Math.abs(currentX - targetX) + Math.abs(currentY - targetY);
			double moveTime = remainingManhattanDistance / moveSpeed;
			double rotateTime = Math.abs(currentW - targetW) / rotateSpeed;
			return moveTime + rotateTime;
		} else if (currentInstruction instanceof ForwardInstruction) {
			ForwardInstruction forward = (ForwardInstruction) currentInstruction;
			double distance = forward.getDistance();
			double speed = forward.getSpeed();
			// Approximate target location
			// Assume that the current location is close to the location where this Forward command was issued
			// TODO: ensure this
			double targetX = currentX + distance * Math.cos(currentW);
			double targetY = currentY + distance * Math.sin(currentW);
			double remainingEuclideanDistance = Math.sqrt(Math.pow(currentX - targetX, 2) + Math.pow(currentY - targetY, 2));
			return remainingEuclideanDistance / speed;
		} else {
			//TODO
			return 0;
		}		
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
