package org.sa.rainbow.brass.analyses;

import java.util.ArrayList;
import java.util.List;

import org.sa.rainbow.brass.model.instructions.ForwardInstruction;
import org.sa.rainbow.brass.model.instructions.IInstruction;
import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress;
import org.sa.rainbow.brass.model.instructions.MoveAbsHInstruction;
import org.sa.rainbow.brass.model.map.BatteryPredictor;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapModelInstance;
import org.sa.rainbow.brass.model.map.MapTranslator;
import org.sa.rainbow.brass.model.mission.MissionState;
import org.sa.rainbow.brass.model.mission.MissionState.Heading;
import org.sa.rainbow.brass.model.mission.MissionStateModelInstance;
import org.sa.rainbow.brass.model.mission.SetRobotAccurateCmd;
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
 * Analyzes accuracy (distance-to-goal) property of the current plan, and triggers adaptation if necessary.
 * @author rsukkerd
 *
 */
public class AccuracyAnalyzer extends AbstractRainbowRunnable implements IRainbowAnalysis, IRainbowModelChangeCallback {
	
	public static final String NAME = "BRASS Accuracy Evaluator";
	
	private static final int DISTANCE_BUFFER = 50; // centimeters //TODO
    
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
	
	// Keep track of the previously-analyzed instruction, which passed
    private IInstruction m_prevAnalyzedAndPassedInstruction;

    private ModelReference m_igRef = new ModelReference("ExecutingInstructionGraph", InstructionGraphModelInstance.INSTRUCTION_GRAPH_TYPE);
    private ModelReference m_msRef = new ModelReference("RobotAndEnvironmentState", MissionStateModelInstance.MISSION_STATE_TYPE);
    private ModelReference m_emRef = new ModelReference ("Map", EnvMapModelInstance.ENV_MAP_TYPE);

    private InstructionGraphProgress m_igProgress;
    private MissionState m_missionState;
    private EnvMap m_envMap;
	
	public AccuracyAnalyzer() {
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
	public void onEvent(ModelReference mr, IRainbowMessage message) {
		synchronized (this) {
    		//TODO
            Boolean obstructed = Boolean
                    .parseBoolean ((String) message.getProperty (IModelChangeBusPort.PARAMETER_PROP + "0"));
            m_waitForPlanner = false;
        }
	}

	@Override
	public String getProperty(String key) {
		return null;
	}

	@Override
	public void setProperty(String arg0, String arg1) {

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
	                    // The remaining instructions, excluding the current instruction
	                    List<IInstruction> remainingInstructions = (List<IInstruction>) m_igProgress.getRemainingInstructions();
	                    
	                    boolean hasEnoughEnergy = hasEnoughEnergy(currentInstruction, remainingInstructions);
	                    
	                    if (hasEnoughEnergy) {
	                    	// Keep track of the latest instruction that we have analyzed the accuracy property,
	                    	// and it passed
		                    m_prevAnalyzedAndPassedInstruction = currentInstruction;
	                    }
	                    
	                    if (hasEnoughEnergy && !m_missionState.isRobotAccurate()) {
	                    	// Previous plan was not accurate; new plan is expected to be accurate
	                    	// Update MissionState model to indicate that the robot can now get close enough to the goal
	                        MissionStateModelInstance missionStateModel = (MissionStateModelInstance) m_modelsManagerPort
	                                .<MissionState> getModelInstance(m_msRef);
	                        SetRobotAccurateCmd robotAccurateCmd = missionStateModel.getCommandFactory().setRobotAccurateCmd(true);
	                        m_modelUSPort.updateModel (robotAccurateCmd);
	                    } else if (!hasEnoughEnergy) {
	                    	// Update MissionState model to indicate that the robot cannot get close enough to the goal
	                        MissionStateModelInstance missionStateModel = (MissionStateModelInstance) m_modelsManagerPort
	                                .<MissionState> getModelInstance(m_msRef);
	                        SetRobotAccurateCmd robotAccurateCmd = missionStateModel.getCommandFactory().setRobotAccurateCmd(false);
	                        m_modelUSPort.updateModel (robotAccurateCmd);
	                        
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
     * Checks if the instructions can be completed with the remaining energy
     */
    private boolean hasEnoughEnergy(IInstruction currentInstruction, List<IInstruction> remainingInstructions) {
    	double batteryCharge = m_missionState.getBatteryCharge();
    	double planEnergyConsumption = getExpectedIGEnergyConsumption(currentInstruction, remainingInstructions);
    	return batteryCharge >= planEnergyConsumption;
    }
    
    /**
     * Calculates the expected energy consumption of the current and the remaining instructions
     */
    private double getExpectedIGEnergyConsumption(IInstruction currentInstruction, List<IInstruction> remainingInstructions) {
    	double totalEnergy = 0;
    	
    	double sourceX = m_missionState.getCurrentPose().getX();
		double sourceY = m_missionState.getCurrentPose().getY();
		double sourceW = m_missionState.getCurrentPose().getRotation();
		Heading sourceHeading = m_missionState.getCurrentPose().getHeading();
		
		List<IInstruction> allInstructions = new ArrayList<>();
		allInstructions.add(currentInstruction);
		allInstructions.addAll(remainingInstructions);
		
    	for (IInstruction instruction : allInstructions) {
    		double instEnergy = 0;
    		
    		if (instruction instanceof MoveAbsHInstruction) {
    			MoveAbsHInstruction moveAbsH = (MoveAbsHInstruction) instruction;
    			double targetX = moveAbsH.getTargetX();
    			double targetY = moveAbsH.getTargetY();
    			double targetW = moveAbsH.getTargetW();
    			Heading targetHeading = Heading.convertFromRadians(targetW);
    			double moveSpeed = moveAbsH.getSpeed();
    			double rotateSpeed = MapTranslator.ROBOT_ROTATIONAL_SPEED_VALUE;
    			
    			double manhattanDistance = Math.abs(sourceX - targetX) + Math.abs(sourceY - targetY);
    			boolean rotating = sourceHeading != targetHeading;
    			boolean kinectEnabled = true;
    			double cpuAvgUsage = 0; //TODO
    			double moveTime = manhattanDistance / moveSpeed;
    			double rotateTime = Math.abs(sourceW - targetW) / rotateSpeed;
    			
    			String moveSpeedStr;
    			
    			if (moveSpeed == MapTranslator.ROBOT_HALF_SPEED_VALUE) {
    				moveSpeedStr = MapTranslator.ROBOT_HALF_SPEED_CONST;
    			} else if (moveSpeed == MapTranslator.ROBOT_FULL_SPEED_VALUE) {
    				moveSpeedStr = MapTranslator.ROBOT_FULL_SPEED_CONST;
    			} else {
    				moveSpeedStr = MapTranslator.ROBOT_HALF_SPEED_CONST;
    			}
    			
    			// Energy consumed by straight move
    			double moveEnergy = BatteryPredictor.batteryConsumption(moveSpeedStr, false, kinectEnabled, cpuAvgUsage, moveTime);
    			instEnergy += moveEnergy;
    			
    			// Energy consumed by rotation, if any
    			if (rotating) {
    				double rotateEnergy = BatteryPredictor.batteryConsumption("", true, kinectEnabled, cpuAvgUsage, rotateTime);
    				instEnergy += rotateEnergy;
    			}
    			
    			// Update source pose for the next instruction
    			sourceX = targetX;
    			sourceY = targetY;
    			sourceW = targetW;
    			sourceHeading = targetHeading;
    		} else if (instruction instanceof ForwardInstruction) {
    			ForwardInstruction forward = (ForwardInstruction) instruction;
    			double distance = forward.getDistance();
    			double speed = forward.getSpeed();
    			// Approximate target location
    			// Assume that the current location is close to the location where this Forward command was issued
    			// TODO: ensure this
    			double targetX = sourceX + distance * Math.cos(sourceW);
    			double targetY = sourceY + distance * Math.sin(sourceW);
    			
    			double euclideanDistance = Math.sqrt(Math.pow(sourceX - targetX, 2) + Math.pow(sourceY - targetY, 2));
    			boolean rotating = false;
    			boolean kinectEnabled = false;
    			double cpuAvgUsage = 0; //TODO
    			double moveTime = euclideanDistance / speed;
    			
    			String speedStr;
    			
    			if (speed == MapTranslator.ROBOT_HALF_SPEED_VALUE) {
    				speedStr = MapTranslator.ROBOT_HALF_SPEED_CONST;
    			} else if (speed == MapTranslator.ROBOT_FULL_SPEED_VALUE) {
    				speedStr = MapTranslator.ROBOT_FULL_SPEED_CONST;
    			} else {
    				speedStr = MapTranslator.ROBOT_HALF_SPEED_CONST;
    			}
    			
    			// Energy consumed by straight move
    			instEnergy += BatteryPredictor.batteryConsumption(speedStr, rotating, kinectEnabled, cpuAvgUsage, moveTime);
    			
    			// Update source pose for the next instruction
    			sourceX = targetX;
    			sourceY = targetY;
    		}
    		
    		totalEnergy += instEnergy;
    	}
    	
    	return totalEnergy;
    }

}
