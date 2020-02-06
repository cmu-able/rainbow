package org.sa.rainbow.brass.confsynthesis;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState.Sensors;

public abstract class ReconfSynth {
	public static final HashMap<Sensors, String> SENSOR_NAMES;
	public static Logger LOGGER;
	static{
		SENSOR_NAMES = new HashMap<Sensors, String>();
		SENSOR_NAMES.put(Sensors.KINECT, "kinect0");
		SENSOR_NAMES.put(Sensors.CAMERA, "camera0");
		SENSOR_NAMES.put(Sensors.LIDAR, "lidar0");
		SENSOR_NAMES.put(Sensors.HEADLAMP, "headlamp0");
	}
	
	public static final HashMap<String, String> COMPONENT_NAMES;
	static{
		COMPONENT_NAMES = new HashMap<String, String>();
		COMPONENT_NAMES.put("amcl", "amcl0");
		COMPONENT_NAMES.put("mrpt", "mrpt0");
		COMPONENT_NAMES.put("laserScan_nodelet", "laserscanNodelet0");
		COMPONENT_NAMES.put("marker_pose_publisher", "markerLocalization0");
		COMPONENT_NAMES.put("aruco_marker_publisher_front", "markerRecognizer0");
		COMPONENT_NAMES.put("map_server", "mapServerStd0");
		COMPONENT_NAMES.put("map_server_obs", "mapServerObs0");
	}
	
	abstract public String getCurrentConfigurationInitConstants();
	
	public static final void logInfo(String msg) {
		if (LOGGER == null) {
			System.out.println(msg);
		}
		else 
			LOGGER.info(msg);
	}
	
	public static final void logError(String err) {
		if (LOGGER == null) {
			System.out.println(err);
		}
		else 
			LOGGER.error(err);
	}
}
