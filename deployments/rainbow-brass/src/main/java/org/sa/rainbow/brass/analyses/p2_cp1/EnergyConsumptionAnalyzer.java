package org.sa.rainbow.brass.analyses.p2_cp1;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sa.rainbow.brass.confsynthesis.Configuration;
import org.sa.rainbow.brass.confsynthesis.SimpleConfigurationStore;
import org.sa.rainbow.brass.model.instructions.ChargeInstruction;
import org.sa.rainbow.brass.model.instructions.ForwardInstruction;
import org.sa.rainbow.brass.model.instructions.IInstruction;
import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress;
import org.sa.rainbow.brass.model.instructions.MoveAbsHInstruction;
import org.sa.rainbow.brass.model.instructions.SetConfigInstruction;
import org.sa.rainbow.brass.model.instructions.SetExecutionFailedCmd;
import org.sa.rainbow.brass.model.map.BatteryPredictor;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapModelInstance;
import org.sa.rainbow.brass.model.map.MapTranslator;
import org.sa.rainbow.brass.model.mission.MissionStateModelInstance;
import org.sa.rainbow.brass.model.mission.MissionState.Heading;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState.LocationRecording;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowState.CP3ModelState;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.util.Pair;

public class EnergyConsumptionAnalyzer extends P2CP1Analyzer {
	public static final String NAME = "BRASS Accuracy Evaluator";
	private static final int GOAL_RADIUS = 50; // centimeters //TODO
	private IInstruction m_prevAnalyzedAndPassedInstruction;
	private SimpleConfigurationStore m_powerModel;

	public EnergyConsumptionAnalyzer() {
		super(NAME);
	}
	
	@Override
	public void initialize(IRainbowReportingPort port) throws RainbowConnectionException {
		super.initialize(port);
		m_powerModel = new SimpleConfigurationStore(Rainbow.instance().allProperties());
		m_powerModel.populate();
	}

	@Override
	protected void runAction() {
		if (getModels().getInstructionGraphModel() == null)
			return;
		// Need to work out how to wait for planner in CP1 -- is it

		InstructionGraphProgress igModel = getModels().getInstructionGraphModel().getModelInstance();
		IInstruction currentInstruction = igModel.getCurrentInstruction();
		if (!(currentInstruction instanceof ChargeInstruction)) {
			List<? extends IInstruction> remainingInstructions = igModel.getRemainingInstructions();
			double batteryCharge = -1;
			try {
				batteryCharge = getModels().getRobotStateModel().getModelInstance().getCharge();
			} catch (IllegalStateException e) {
				// Don't have battery information yet
				return;
			}
			
			// We need to break the instructions into chunks that are segmented by charge and then ask
			// is there enough energy to get to each charge instruction or the target?
			
			
			double planEnergyConsumption = hasEnoughEnergy(currentInstruction, remainingInstructions);
			boolean hasEnoughEnergy = batteryCharge >= planEnergyConsumption;
			log("Current charge = " + batteryCharge + ", needed charge = " + planEnergyConsumption);
			if (hasEnoughEnergy) {
				// Keep track of the latest instruction that we have analyzed the accuracy
				// property,
				// and it passed
				m_prevAnalyzedAndPassedInstruction = currentInstruction;
			}
			boolean knowAboutLowBattery = getModels().getRainbowStateModel().getModelInstance().getProblems()
					.contains(CP3ModelState.LOW_ON_BATTERY);
			if (hasEnoughEnergy && knowAboutLowBattery) {
				IRainbowOperation op = getModels().getRainbowStateModel().getCommandFactory()
						.removeModelProblem(CP3ModelState.LOW_ON_BATTERY);
				m_modelUSPort.updateModel(op);
			} else if (!hasEnoughEnergy && !knowAboutLowBattery) {
				log("Do not have enough battery. Current charge = " + batteryCharge + ", needed charge = "
						+ planEnergyConsumption);
				LocationRecording pose = getModels().getMissionStateModel().getModelInstance().getCurrentPose();
				insertNodeIntoMap(pose, currentInstruction);
				IRainbowOperation op = getModels().getRainbowStateModel().getCommandFactory()
						.setModelProblem(CP3ModelState.LOW_ON_BATTERY);
				m_modelUSPort.updateModel(op);

			}
		} else if (getModels().getRainbowStateModel().getModelInstance().getProblems()
				.contains(CP3ModelState.LOW_ON_BATTERY)) {
			IRainbowOperation op = getModels().getRainbowStateModel().getCommandFactory()
					.removeModelProblem(CP3ModelState.LOW_ON_BATTERY);
			m_modelUSPort.updateModel(op);
		}
	}
	
	   /**
     * Checks if the instructions can be completed with the remaining energy
     */
    private double hasEnoughEnergy (IInstruction currentInstruction, List<? extends IInstruction> remainingInstructions) {
        double planEnergyConsumption = getExpectedIGEnergyConsumption(currentInstruction, remainingInstructions);
        return planEnergyConsumption;
    }
    
    private double getExpectedIGEnergyConsumption(IInstruction currentInstruction, List<? extends IInstruction> remainingInstructions) {
        double totalEnergy = 0;

        // Goal location
        EnvMap envMap = getModels().getEnvMapModel().getModelInstance();
		MissionState missionState = getModels().getMissionStateModel().getModelInstance();
		double goalX = envMap.getNodeX(missionState.getTargetWaypoint());
        double goalY = envMap.getNodeY(missionState.getTargetWaypoint());
        if (goalX == Double.NEGATIVE_INFINITY || goalY == Double.NEGATIVE_INFINITY) return 0;

        double sourceX = missionState.getCurrentPose().getX();
        double sourceY = missionState.getCurrentPose().getY();
        double sourceW = missionState.getCurrentPose().getRotation();

        List<IInstruction> allInstructions = new ArrayList<>();
        allInstructions.add(currentInstruction);
        allInstructions.addAll(remainingInstructions);
        String config = getModels().getRobotStateModel().getModelInstance().getConfigId();
        int i = 0;
        for (IInstruction instruction : allInstructions) {
            double instEnergy = 0;

            // Special case: for the last instruction, only calculate the energy required to get
            // within a certain radius from the goal location
            if (i == allInstructions.size() - 1 && instruction instanceof MoveAbsHInstruction) {
                // For MoveAbsH, use the target location that is GOAL_RADIUS from the goal, and is nearest the source
                Pair<Double, Double> targetLocation = getNearestTargetLocation(sourceX, sourceY, goalX, goalY, GOAL_RADIUS);
                double targetX = targetLocation.firstValue();
                double targetY = targetLocation.secondValue();

                MoveAbsHInstruction moveAbsH = (MoveAbsHInstruction) instruction;
                String newMoveAbsH = 
                        MessageFormat.format (
                                "MoveAbsH({0,number,##.##}, {1,number,##.##}, {2,number,##.##}, {3,number,##.###})",
                                targetX, targetY, moveAbsH.getSpeed (), moveAbsH.getTargetW ());
//                        String.format("MoveAbsH(%d, %d, %d, %d)", targetX, targetY, moveAbsH.getSpeed(), moveAbsH.getTargetW());
                MoveAbsHInstruction moveAbsHCopy = 
                        new MoveAbsHInstruction(moveAbsH.getInstructionLabel(), newMoveAbsH, moveAbsH.getNextInstructionLabel());
                instEnergy = getMoveAbsHEnergyConsumption(moveAbsHCopy, sourceX, sourceY, sourceW, config);

            } else if (i == allInstructions.size() - 1 && instruction instanceof ForwardInstruction) {
                // For Forward, use the distance that is GOAL_RADIUS shorter than the original distance
                ForwardInstruction forward = (ForwardInstruction) instruction;
                String newForward = MessageFormat.format ("Forward({0,number,##.##}, {1,number,##.##})",
                        forward.getDistance () - GOAL_RADIUS, forward.getSpeed ());
//                        String.format("Forward(%d, %d)", forward.getDistance() - GOAL_RADIUS, forward.getSpeed());
                ForwardInstruction forwardCopy = 
                        new ForwardInstruction(forward.getInstructionLabel(), newForward, forward.getNextInstructionLabel());
                instEnergy = getForwardEnergyConsumption(forwardCopy, sourceX, sourceY, sourceW, config);

            } else if (instruction instanceof MoveAbsHInstruction) {
                MoveAbsHInstruction moveAbsH = (MoveAbsHInstruction) instruction;
                instEnergy = getMoveAbsHEnergyConsumption(moveAbsH, sourceX, sourceY, sourceW, config);

                // Update source pose for the next instruction
                sourceX = moveAbsH.getTargetX();;
                sourceY = moveAbsH.getTargetY();
                sourceW = moveAbsH.getTargetW();
            } else if (instruction instanceof ForwardInstruction) {
                ForwardInstruction forward = (ForwardInstruction) instruction;
                instEnergy = getForwardEnergyConsumption(forward, sourceX, sourceY, sourceW, config);

                // Update source pose for the next instruction
                sourceX = sourceX + forward.getDistance() * Math.cos(sourceW);;
                sourceY = sourceY + forward.getDistance() * Math.sin(sourceW);
            } else if (instruction instanceof ChargeInstruction) {
                ChargeInstruction charge = (ChargeInstruction) instruction;
                EnergyConsumptionPredictor bp = new EnergyConsumptionPredictor();
//                double energyGain = bp.batteryCharge(charge.getChargingTime());
                double energyGain = 1;
                // Charge should be refilling the battery, and so totalEnergy consumed
                // should be reset to 0 (i.e., after this instruction we have 
                
                instEnergy = -1 * energyGain;
            }
            else if (instruction instanceof SetConfigInstruction) {
				SetConfigInstruction configI = (SetConfigInstruction) instruction;
            	config = configI.getConfig();
            } else {
                // This instruction doesn't consume or produce energy
                instEnergy = 0;
            }

            totalEnergy += instEnergy;
            i++;
        }

        return totalEnergy;
    }
    
    /**
     * Finds the nearest (x, y) location to the source location that is a given distance away from the goal.
     */
    private Pair<Double, Double> getNearestTargetLocation(double sourceX, double sourceY, 
            double goalX, double goalY, double buffer) {
        double lowerBoundX = goalX - buffer;
        double upperBoundX = goalX + buffer;
        double lowerBoundY = goalY - buffer;
        double upperBoundY = goalY + buffer;

        List<Double> targetXs = new ArrayList<>();
        List<Double> targetYs = new ArrayList<>();
        targetXs.add(goalX);
        targetYs.add(upperBoundY);
        targetXs.add(upperBoundX);
        targetYs.add(goalY);
        targetXs.add(goalX);
        targetYs.add(lowerBoundY);
        targetXs.add(lowerBoundX);
        targetYs.add(goalY);

        List<Double> manhattanDistances = new ArrayList<>();
        manhattanDistances.add(getManhattanDistance(sourceX, sourceY, targetXs.get(0), targetXs.get(0)));
        manhattanDistances.add(getManhattanDistance(sourceX, sourceY, targetXs.get(1), targetXs.get(1)));
        manhattanDistances.add(getManhattanDistance(sourceX, sourceY, targetXs.get(2), targetXs.get(2)));
        manhattanDistances.add(getManhattanDistance(sourceX, sourceY, targetXs.get(3), targetXs.get(3)));
        int minIndex = manhattanDistances.indexOf(Collections.min(manhattanDistances));
        double targetX = targetXs.get(minIndex);
        double targetY = targetYs.get(minIndex);
        Pair<Double, Double> targetLocation = new Pair<Double, Double>(targetX, targetY);
        return targetLocation;
    }
    
    private double getManhattanDistance(double sourceX, double sourceY, double targetX, double targetY) {
        return Math.abs(sourceX - targetX) + Math.abs(sourceY - targetY);
    }
    
    /**
     * Calculates the energy consumption of MoveAbs(x, y, v, w)
     */
    private double getMoveAbsHEnergyConsumption(MoveAbsHInstruction moveAbsH, double sourceX, double sourceY, double sourceW, String config) {
        double targetX = moveAbsH.getTargetX();
        double targetY = moveAbsH.getTargetY();
        double targetW = moveAbsH.getTargetW();
        double moveSpeed = moveAbsH.getSpeed();
        double rotateSpeed = MapTranslator.ROBOT_ROTATIONAL_SPEED_VALUE;

        
        double moveAbsHEnergy = getMovementEnergyConsumption (false, sourceX, sourceY, sourceW, targetX, targetY,
                targetW, 
                moveSpeed, rotateSpeed, config);
        return moveAbsHEnergy;
    }
    
    /**
     * Calculates the energy consumption of Forward(d, v)
     */
    private double getForwardEnergyConsumption(ForwardInstruction forward, double sourceX, double sourceY, double sourceW, String config) {
        double distance = forward.getDistance();
        double speed = forward.getSpeed();
        // Approximate target location
        // Assume that the current location is close to the location where this Forward command was issued
        // TODO: ensure this
        double targetX = sourceX + distance * Math.cos(sourceW);
        double targetY = sourceY + distance * Math.sin(sourceW);
        double forwardEnergy = getMovementEnergyConsumption(false, sourceX, sourceY, sourceW, targetX, targetY, sourceW, 
                speed, 0, config);
        return forwardEnergy;
    }
    
    /**
     * Calculates the energy consumption of a movement
     */
    private double getMovementEnergyConsumption(boolean isManhattanDistance,
            double sourceX, double sourceY, double sourceW,
            double targetX, double targetY, double targetW,
            double moveSpeed, double rotateSpeed, String config) {
        String moveSpeedStr;

        if (moveSpeed == MapTranslator.ROBOT_HALF_SPEED_VALUE) {
            moveSpeedStr = MapTranslator.ROBOT_HALF_SPEED_CONST;
        } else if (moveSpeed == MapTranslator.ROBOT_FULL_SPEED_VALUE) {
            moveSpeedStr = MapTranslator.ROBOT_FULL_SPEED_CONST;
        } else {
            moveSpeedStr = MapTranslator.ROBOT_HALF_SPEED_CONST;
        }

        double distance;
        if (isManhattanDistance) {
            // Manhattan distance
            distance = Math.abs(sourceX - targetX) + Math.abs(sourceY - targetY);
        } else {
            // Euclidean distance
            distance = Math.sqrt(Math.pow(sourceX - targetX, 2) + Math.pow(sourceY - targetY, 2));
        }

        // Energy consumed by straight move
        EnergyConsumptionPredictor energyConsumptionPredictor = new EnergyConsumptionPredictor();
		Configuration configuration = m_powerModel.getConfigurations().get(config);
		double moveEnergy =  
				energyConsumptionPredictor.getEnergyConsumption(configuration, distance/configuration.getSpeed()); 
        		
        double instEnergy = moveEnergy;

        boolean rotating = Heading.convertFromRadians(sourceW) != Heading.convertFromRadians(targetW);

        // Energy consumed by rotation, if any
        if (rotating) {
            double abs = Math.abs(sourceW - targetW);
            abs = (abs > Math.PI) ? 2 * Math.PI - abs : abs;
            double rotateTime = abs / rotateSpeed;
            double rotateEnergy = energyConsumptionPredictor.getEnergyConsumption(configuration, rotateTime);
            instEnergy += rotateEnergy;
        }

        return instEnergy;
    }
}
