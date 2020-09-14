package org.sa.rainbow.brass.confsynthesis;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.sa.rainbow.brass.model.p2_cp3.ICP3ModelAccessor;
import org.sa.rainbow.brass.model.p2_cp3.acme.TurtlebotModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState.Sensors;

import com.google.common.base.Objects;

public class ReconfSynthReal extends ReconfSynth {

	private TurtlebotModelInstance m_tb;
	private CP3RobotState m_rb;
	

	public ReconfSynthReal(ICP3ModelAccessor models) {
		m_tb = models.getTurtlebotModel();
		m_rb = models.getRobotStateModel().getModelInstance();
	}
	
	public String getCurrentConfigurationInitConstants() {
		return getCurrentConfigurationInitConstants(Collections.<String>emptySet(), Collections.<Sensors>emptySet());
	}

	public String getCurrentConfigurationInitConstants(Set<String> forcedFailedComps, Set<Sensors> forcedFailedSensors){
		String res="";
		
		int i=0;
		for(String c : m_tb.getInactiveComponents()){
			if (!forcedFailedComps.contains(c) && !Objects.equal(null, COMPONENT_NAMES.get(c))){
				if (i>0)
					res+=",";
				res+=COMPONENT_NAMES.get(c)+"_INIT="+ConfigurationSynthesizer.m_component_modes.get("DISABLED");
				i++;
			}
		}
		
		for(String c : m_tb.getActiveComponents()){
			if (!forcedFailedComps.contains(c) && !Objects.equal(null, COMPONENT_NAMES.get(c))){
				res+=",";
				res+=COMPONENT_NAMES.get(c)+"_INIT="+ConfigurationSynthesizer.m_component_modes.get("ENABLED");
			}
		}
		
		Collection<String> failedComponents = (Set )m_tb.getFailedComponents();
		failedComponents.addAll(forcedFailedComps);
		for(String c : failedComponents){
			if (!Objects.equal(null, COMPONENT_NAMES.get(c))){
				res+=",";
				res+=COMPONENT_NAMES.get(c)+"_INIT="+ConfigurationSynthesizer.m_component_modes.get("OFFLINE");
			}
		}

		for (Sensors s: m_rb.getAvailableSensors()){
			if (!forcedFailedSensors.contains(s) && !Objects.equal(null, SENSOR_NAMES.get(s))){
				boolean sensorOn = false;
				switch (s){
				case KINECT:
					try{
						sensorOn = m_rb.isKinectOn();
					} catch(Exception e){
						logError("Illegal state exception determining if Sensor is On.");
					}
					break;
				case CAMERA:
					try{
						sensorOn = m_rb.isCameraOn();
					} catch(Exception e){
						logError("Illegal state exception determining if Sensor is On.");
					}
					break;
				case LIDAR:
					try{
						sensorOn = m_rb.isLidarOn();
					} catch(Exception e){
						logError("Illegal state exception determining if Sensor is On.");
					}
					break;
				case HEADLAMP:
					try{
						sensorOn = m_rb.isHeadlampOn();
					} catch(Exception e){
						System.out.println("Illegal state exception determining if Sensor is On.");
					}
					break;

				}
				String compModeStr = ConfigurationSynthesizer.m_component_modes.get("DISABLED");
				if (sensorOn)
					compModeStr = ConfigurationSynthesizer.m_component_modes.get("ENABLED");
				res+=",";
				res+=SENSOR_NAMES.get(s)+"_INIT="+compModeStr;
			}
		}
		
		EnumSet<Sensors> failedSensors = m_rb.getFailedSensors();
		failedSensors.addAll(forcedFailedSensors);
		for (Sensors s: failedSensors){
			res+=",";
			res+=SENSOR_NAMES.get(s)+"_INIT="+ConfigurationSynthesizer.m_component_modes.get("OFFLINE");
		}
		
		res+=",fullSpeedSetting0_INIT="+ConfigurationSynthesizer.m_component_modes.get("DISABLED"); // This has to be changed!! Hardwired for the time being.
		res+=",halfSpeedSetting0_INIT="+ConfigurationSynthesizer.m_component_modes.get("ENABLED");
		res+=",safeSpeedSetting0_INIT="+ConfigurationSynthesizer.m_component_modes.get("DISABLED");
		
		// Rework to this:
		// STOPPED = speed < 0.05ms, SAFE = 0.05 <= speed <= 0.25ms, SLOW = 0.25 < speed < 0.35, FULL otherwise.
		
		return res;		
	}
	
}
