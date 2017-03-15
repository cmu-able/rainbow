package org.sa.rainbow.brass.model.map;

import java.util.Objects;

import org.sa.rainbow.brass.PropertiesConnector;
import org.sa.rainbow.brass.model.map.MapTranslator;

/**
 * @author jcamara
 *
 */
public class SpeedPredictor {
	
	public static final double m_move_forward_max_error = 0.05; // Maximum measurement error in the times - return worst-case time
	
	public static double moveForwardTime(double distance, String speedSetting ){
		double res=0.0;
		if (distance<0) {
			return res;
		}
		if (Objects.equals(speedSetting, MapTranslator.ROBOT_HALF_SPEED_CONST)){
			res = 0.55055 + 2.8904 * distance + m_move_forward_max_error;
		}
		if (Objects.equals(speedSetting, MapTranslator.ROBOT_FULL_SPEED_CONST)){
			res = 0.55055 + 2.8904 * distance + m_move_forward_max_error;
		}
		return res;
	}
	
	public static double moveForwardTimeSimple (double distance, String speedSetting){
		double res=0.0;
		if (distance<0) {
			return res;
		}
		if (Objects.equals(speedSetting, MapTranslator.ROBOT_HALF_SPEED_CONST)){
			res = distance / MapTranslator.ROBOT_HALF_SPEED_VALUE;
			res += res * 0.1;
		}
		if (Objects.equals(speedSetting, MapTranslator.ROBOT_FULL_SPEED_CONST)){
			res = distance / MapTranslator.ROBOT_FULL_SPEED_VALUE;
		}
		if (Objects.equals(speedSetting, MapTranslator.ROBOT_DR_SPEED_CONST)){
			res = distance / MapTranslator.ROBOT_DR_SPEED_VALUE;
			res += res * 0.2;
		}
		return res;		
	}
	
	 /**
     * Class test
     * @param args
     */
    public static void main(String[] args) {
        String ss = MapTranslator.ROBOT_FULL_SPEED_CONST;
        String sh = MapTranslator.ROBOT_HALF_SPEED_CONST;
        
        
    	System.out.println("l1 to l2: "+String.valueOf(moveForwardTimeSimple(5.0, ss)/16.0));
    	System.out.println("l2 to c2: "+String.valueOf(moveForwardTimeSimple(10.3, ss)/24.0));
    	System.out.println("c2 to l2: "+String.valueOf(moveForwardTimeSimple(10.3, ss)/20.0));
    	System.out.println("l2 to c1: "+String.valueOf(moveForwardTimeSimple(4.0, ss)/7.0));
    	System.out.println("c1 to l7: "+String.valueOf(moveForwardTimeSimple(6.2, ss)/12.0));
       	System.out.println("l7 to c4: "+String.valueOf(moveForwardTimeSimple(11.3, ss)/19.0));
       	System.out.println("c4 to l6: "+String.valueOf(moveForwardTimeSimple(11.3, ss)/19.0));
       	
//       	System.out.println("l1 to l2: "+String.valueOf(moveForwardTimeSimple(5.0, sh)/16.0));
//    	System.out.println("l2 to c2: "+String.valueOf(moveForwardTimeSimple(10.3, sh)/34.0));
//    	System.out.println("c2 to l3: "+String.valueOf(moveForwardTimeSimple(11.39, sh)/40.0));
//    	System.out.println("l3 to c3: "+String.valueOf(moveForwardTimeSimple(4, sh)/12.0));
//    	System.out.println("c3 to l6: "+String.valueOf(moveForwardTimeSimple(6.19, sh)/20.0));
       	                  	
    }

	
}
