package org.sa.rainbow.brass.model.map;

// To be relocated

public class BatteryPredictor {


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

}
