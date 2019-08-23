package org.sa.rainbow.brass.plan.p2;

import java.util.Objects;

import org.sa.rainbow.brass.model.map.MapTranslator;

// To be relocated

/**
 * @author jcamara
 *
 */
public class BatteryPredictor {

	public static final double m_battery_scaling_factor = 5.0;
	public static final double m_charging_time_ratio = 8.35;
	
	public static double batteryConsumption (String speed, double time){
		
		boolean kinectEnabled = MapTranslator.ROBOT_LOC_MODE_HI_KINECT;
		double cpuAvgUsage= MapTranslator.ROBOT_LOC_MODE_HI_CPU_VAL;
		
		
		return batteryConsumption (speed, false, time);
	}
	
    /**
     * Returns the amount of energy consumed (in mWh) when the robot moves at a given speed
     * @param speed String constant (HALF_SPEED or FULL_SPEED for the time being)
     * @param time amount of seconds during which the robot moves.
     * @return
     */
	public static double batteryConsumption (String speed, boolean rotating, double time) {
        double base_consumption=0;
        double kinect_consumption=0;
        double nuc_consumption=0;
        
        if (Objects.equals(speed, MapTranslator.ROBOT_HALF_SPEED_CONST))
            base_consumption = 1.674f * time+287.5f;
        if (Objects.equals(speed, MapTranslator.ROBOT_FULL_SPEED_CONST)) 
        	base_consumption = 3.89f * time+582.6f;
        if (rotating)
        	base_consumption = 4.9f * time + 699f;
        
  //      if (kinectEnabled)
  //      	kinect_consumption = 1.426f * time;
  //      else 
  //      	kinect_consumption = 0.07f * time;

//        nuc_consumption = ( 0.032f * cpuAvgUsage + 1.925f) * time;
        
        return m_battery_scaling_factor * (base_consumption + kinect_consumption + nuc_consumption);
    }
    
    /**
     * Returns the amount of battery recharged (in mWh) for a given time lapse
     * @param time amount in seconds that the rogot stays in the charging base
     * @return
     */
    public static double batteryCharge (double time){
    	return m_battery_scaling_factor * m_charging_time_ratio * time; 
    }
    
    /**
     * Returns the amount of time required (in s) to charge a given amount of mWh
     * 
     */
    public static double timeToCharge (double charge){
    	return charge / (m_charging_time_ratio * m_battery_scaling_factor);
    }
    
    public static double getChargingTimeRatio(){
    	return m_charging_time_ratio * m_battery_scaling_factor;
    }
    

}
