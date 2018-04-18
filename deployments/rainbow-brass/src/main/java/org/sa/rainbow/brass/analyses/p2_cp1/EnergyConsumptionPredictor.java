package org.sa.rainbow.brass.analyses.p2_cp1;
import org.sa.rainbow.brass.confsynthesis.Configuration;

public class EnergyConsumptionPredictor {

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

}