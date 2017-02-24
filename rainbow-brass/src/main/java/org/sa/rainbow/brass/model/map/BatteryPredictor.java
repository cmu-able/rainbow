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
	
	public double batteryConsumption (String speed, double time){
		return batteryConsumption (speed, false, true, 20, time);
	}
	
    public double batteryConsumption (String speed, boolean rotating, boolean kinectEnabled, double cpuAvgUsage, double time) {
        double base_consumption=0;
        double kinect_consumption=0;
        double nuc_consumption=0;
        
        if (speed.equals("HALF_SPEED"))
            base_consumption = 1.674f*time+287.5f;
        if (speed.equals("FULL_SPEED")) 
        	base_consumption = 3.89f*time+582.6f;
        if (rotating)
        	base_consumption = 4.9f*time + 699f;
        
        if (kinectEnabled)
        	kinect_consumption = 1.426f * time;
        else 
        	kinect_consumption = 0.07f * time;

        nuc_consumption = ( 0.032f * cpuAvgUsage + 1.925f) * time;
        
        return base_consumption + kinect_consumption + nuc_consumption;
    }
    
    /**
     * Returns the amount of battery recharged (in mWh) for a given time lapse
     * @param time amount in seconds that the rogot stays in the charging base
     * @return
     */
    public double batteryCharge (double time){
    	return 8.35 * time; 
    }

}
