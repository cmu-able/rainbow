package org.sa.rainbow.brass.p3_cp1.analysis;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sa.rainbow.brass.confsynthesis.Configuration;
import org.sa.rainbow.brass.confsynthesis.SimpleConfigurationStore;
import org.sa.rainbow.brass.model.instructions.ChargeInstruction;
import org.sa.rainbow.brass.model.instructions.ForwardInstruction;
import org.sa.rainbow.brass.model.instructions.IInstruction;
import org.sa.rainbow.brass.model.instructions.MoveAbsHInstruction;
import org.sa.rainbow.brass.model.instructions.SetConfigInstruction;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.MapTranslator;
import org.sa.rainbow.brass.model.mission.MissionState.Heading;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState;
import org.sa.rainbow.core.util.Pair;

public class EnergyConsumptionPredictor {
	private static final int GOAL_RADIUS = 50; // centimeters //TODO

	private EnvMap m_envMap;
	private MissionState m_missionState;
	private String m_config;

	private SimpleConfigurationStore m_powerModel;
	
	public EnergyConsumptionPredictor(EnvMap e, MissionState m, SimpleConfigurationStore powerModel) {
		m_envMap = e;
		m_missionState = m;
		m_powerModel = powerModel;
	}
	
	public void setConfig(String config) {
		m_config = config;
	}

    // TODO check the signature
    public double getEnergyConsumption(Configuration config, double time) {
        if(config == null) {
            throw new IllegalArgumentException("The configuration cannot be null");
        }

        double powerLoad = config.getEnergyDischargeRate();

        if(powerLoad <= 0.0) {
            throw new IllegalArgumentException("The power load has to be greater than 0");
        }

        if(time == 0.0) {
            throw new IllegalArgumentException("There are no more tasks to do");
        }

        if(time < 0.0) {
            throw new IllegalArgumentException("The time to complete tasks cannot be negative");
        }


        return this.getEnergyConsumption(powerLoad, time);
    }

    /**
     * Get the energy consumed in a given time period.
     *
     * @param powerLoad
     * @param time
     * @return
     */
    public double getEnergyConsumption(double powerLoad, double time) {
        if(powerLoad <= 0.0) {
            throw new IllegalArgumentException("The power load has to be greater than 0");
        }

        if(time == 0.0) {
            throw new IllegalArgumentException("There are no more tasks to do");
        }

        if(time < 0.0) {
            throw new IllegalArgumentException("The time to complete tasks cannot be negative");
        }

        double energy = powerLoad * time;
        return energy;
    }

    /**
     * Check if the task can be completed based on the energyConsumption that will be consumed and the current energyConsumption.
     *
     * @param curEnergy
     * @param energyConsumed
     * @return
     */
    public boolean canCompleteTask(double curEnergy, double energyConsumed) {
        if(curEnergy < 0.0) {
            throw new IllegalArgumentException("The current energy cannot be less than 0");
        }

        if(curEnergy == 0.0) {
            throw new IllegalArgumentException("The battery is empty");
        }

        if(energyConsumed <= 0.0) {
            throw new IllegalArgumentException("The energy consumed has to be greater than 0");
        }

        return curEnergy >= energyConsumed;
    }
    
	   /**
  * Checks if the instructions can be completed with the remaining energy
  */
 public double getPlanEnergyConsumption (IInstruction currentInstruction, List<? extends IInstruction> remainingInstructions, String tgtWP) {
     double planEnergyConsumption = getExpectedIGEnergyConsumption(currentInstruction, remainingInstructions, tgtWP);
     return planEnergyConsumption;
 }
 
 private double getExpectedIGEnergyConsumption(IInstruction currentInstruction, List<? extends IInstruction> remainingInstructions, String tgtWP) {
     double totalEnergy = 0;

     // Goal location
		double goalX = m_envMap.getNodeX(m_missionState.getTargetWaypoint());
     double goalY = m_envMap.getNodeY(m_missionState.getTargetWaypoint());
     if (goalX == Double.NEGATIVE_INFINITY || goalY == Double.NEGATIVE_INFINITY) return 0;

     double sourceX = m_missionState.getCurrentPose().getX();
     double sourceY = m_missionState.getCurrentPose().getY();
     double sourceW = m_missionState.getCurrentPose().getRotation();

     List<IInstruction> allInstructions = new ArrayList<>();
     if (currentInstruction != null) allInstructions.add(currentInstruction);
     allInstructions.addAll(remainingInstructions);
     int i = 0;
     for (IInstruction instruction : allInstructions) {
         double instEnergy = 0;

         // Special case: for the last instruction, only calculate the energy required to get
         // within a certain radius from the goal location
         if (i == allInstructions.size() - 1 && instruction instanceof MoveAbsHInstruction && (((MoveAbsHInstruction )instruction).getTargetWaypoint().equals(tgtWP))) {
             // For MoveAbsH, use the target location that is GOAL_RADIUS from the goal, and is nearest the source
             Pair<Double, Double> targetLocation = getNearestTargetLocation(sourceX, sourceY, goalX, goalY, GOAL_RADIUS);
             double targetX = targetLocation.firstValue();
             double targetY = targetLocation.secondValue();

             MoveAbsHInstruction moveAbsH = (MoveAbsHInstruction) instruction;
             String newMoveAbsH = 
                     MessageFormat.format (
                             "MoveAbsH({0,number,##.##}, {1,number,##.##}, {2,number,##.##}, {3,number,##.###})",
                             targetX, targetY, moveAbsH.getSpeed (), moveAbsH.getTargetW ());
//                     String.format("MoveAbsH(%d, %d, %d, %d)", targetX, targetY, moveAbsH.getSpeed(), moveAbsH.getTargetW());
             MoveAbsHInstruction moveAbsHCopy = 
                     new MoveAbsHInstruction(moveAbsH.getInstructionLabel(), newMoveAbsH, moveAbsH.getNextInstructionLabel());
             instEnergy = getMoveAbsHEnergyConsumption(moveAbsHCopy, sourceX, sourceY, sourceW, m_config);

         } else if (i == allInstructions.size() - 1 && instruction instanceof ForwardInstruction) {
             // For Forward, use the distance that is GOAL_RADIUS shorter than the original distance
             ForwardInstruction forward = (ForwardInstruction) instruction;
             String newForward = MessageFormat.format ("Forward({0,number,##.##}, {1,number,##.##})",
                     forward.getDistance () - GOAL_RADIUS, forward.getSpeed ());
//                     String.format("Forward(%d, %d)", forward.getDistance() - GOAL_RADIUS, forward.getSpeed());
             ForwardInstruction forwardCopy = 
                     new ForwardInstruction(forward.getInstructionLabel(), newForward, forward.getNextInstructionLabel());
             instEnergy = getForwardEnergyConsumption(forwardCopy, sourceX, sourceY, sourceW, m_config);

         } else if (instruction instanceof MoveAbsHInstruction) {
             MoveAbsHInstruction moveAbsH = (MoveAbsHInstruction) instruction;
             instEnergy = getMoveAbsHEnergyConsumption(moveAbsH, sourceX, sourceY, sourceW, m_config);

             // Update source pose for the next instruction
             sourceX = moveAbsH.getTargetX();;
             sourceY = moveAbsH.getTargetY();
             sourceW = moveAbsH.getTargetW();
         } else if (instruction instanceof ForwardInstruction) {
             ForwardInstruction forward = (ForwardInstruction) instruction;
             instEnergy = getForwardEnergyConsumption(forward, sourceX, sourceY, sourceW, m_config);

             // Update source pose for the next instruction
             sourceX = sourceX + forward.getDistance() * Math.cos(sourceW);;
             sourceY = sourceY + forward.getDistance() * Math.sin(sourceW);
         } else if (instruction instanceof ChargeInstruction) {
         	// This should not happen because we should segment on Charges
         	//                double energyGain = bp.batteryCharge(charge.getChargingTime());
             // Charge should be refilling the battery, and so totalEnergy consumed
             // should be reset to 0 (i.e., after this instruction we have 
            
         }
         else if (instruction instanceof SetConfigInstruction) {
				SetConfigInstruction configI = (SetConfigInstruction) instruction;
         	m_config = configI.getConfig();
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

     if (moveSpeed == (double )MapTranslator.ROBOT_HALF_SPEED_VALUE) {
         moveSpeedStr = MapTranslator.ROBOT_HALF_SPEED_CONST;
     } else if (moveSpeed == (double )MapTranslator.ROBOT_FULL_SPEED_VALUE) {
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
		Configuration configuration = m_powerModel.getConfiguration(config);
		double moveEnergy =  
				getEnergyConsumption(configuration, distance/configuration.getSpeed()); 
     		
     double instEnergy = moveEnergy;

     boolean rotating = Heading.convertFromRadians(sourceW) != Heading.convertFromRadians(targetW);

     // Energy consumed by rotation, if any
     if (rotating) {
         double abs = Math.abs(sourceW - targetW);
         abs = (abs > Math.PI) ? 2 * Math.PI - abs : abs;
         double rotateTime = abs / rotateSpeed;
         double rotateEnergy = getEnergyConsumption(configuration, rotateTime);
         instEnergy += rotateEnergy;
     }

     return instEnergy;
 }

}