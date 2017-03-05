package org.sa.rainbow.brass.model.map;

import java.util.Objects;
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
		}
		if (Objects.equals(speedSetting, MapTranslator.ROBOT_FULL_SPEED_CONST)){
			res = distance / MapTranslator.ROBOT_FULL_SPEED_VALUE;
		}
		if (Objects.equals(speedSetting, MapTranslator.ROBOT_DR_SPEED_CONST)){
			res = distance / MapTranslator.ROBOT_DR_SPEED_VALUE;
		}
		return res;		
	}
	
}
