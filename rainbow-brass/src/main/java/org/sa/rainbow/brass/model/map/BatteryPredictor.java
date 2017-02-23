package org.sa.rainbow.brass.model.map;

// To be relocated

/**
 * @author jcamara
 *
 */
public class BatteryPredictor {

	
    /**
     * Returns the amount of energy consumed (in mWh) when the robot moves at a given speed
     * @param speed String constant (HALF_SPEED or FULL_SPEED for the time being)
     * @param time amount of seconds during which the robot moves.
     * @return
     */
    public double batteryConsumption (String speed, double time) {
        double base_consumption;
        if (speed.equals("HALF_SPEED")){ 
            base_consumption = 1.674f*time+287.5f;
        }
        else{ 
            base_consumption = 3.89f*time+582.6f;
        }
        double kinect_consumption = 1.0386f * time;
        double nuc_consumption = 3.08f * time;
        return base_consumption + kinect_consumption + nuc_consumption;
    }
    
    /**
     * Returns the amount of battery recharged (in mWh) for a given time lapse
     * @param time amount in seconds that the rogot stays in the charging base
     * @return
     */
    public double batteryCharge (double time){
    	return 0.1389 * time; // Charges 500 mws, or 500/3600 mwh
    }

}
