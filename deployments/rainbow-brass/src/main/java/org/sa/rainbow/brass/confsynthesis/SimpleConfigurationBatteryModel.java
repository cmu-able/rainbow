package org.sa.rainbow.brass.confsynthesis;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Properties;
import java.util.LinkedList;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.sa.rainbow.core.ConfigHelper;

public class SimpleConfigurationBatteryModel {
	

	public static Double m_battery_capacity=32823.0;
	public static Double m_charging_rate=69.9555;
	public static Double m_energy_weight=0.5;
	public static Double m_timeliness_weight=0.5;
	
	public static Double getBatteryCapacity() {
		return m_battery_capacity;
	}

	public static void setBatteryCapacity(Double m_battery_capacity) {
		SimpleConfigurationBatteryModel.m_battery_capacity = m_battery_capacity;
	}

	public static Double getChargingRate() {
		return m_charging_rate;
	}

	public static void setChargingRate(Double m_charging_rate) {
		SimpleConfigurationBatteryModel.m_charging_rate = m_charging_rate;
	}

	public static Double getEnergyWeight() {
		return m_energy_weight;
	}

	public static void setEnergyWeight(Double m_energy_weight) {
		SimpleConfigurationBatteryModel.m_energy_weight = m_energy_weight;
	}

	public static Double getTimelinessWeight() {
		return m_timeliness_weight;
	}

	public static void setTimelinessWeight(Double m_timeliness_weight) {
		SimpleConfigurationBatteryModel.m_timeliness_weight = m_timeliness_weight;
	}
	
	
	private synchronized void loadConfig(String confFile){
	      JSONParser parser = new JSONParser();
	        Object obj=null;
	                
	        try{
	            obj = parser.parse(new FileReader(confFile)); 
	        } catch (Exception e) {
	        	System.out.println(e.getMessage());
	            System.out.println("Could not load Configuration File: "+confFile);
	            e.printStackTrace();
	        }

	        JSONObject jsonObject = (JSONObject) obj;

	        m_battery_capacity = Double.parseDouble(String.valueOf(((JSONObject)obj).get("battery_capacity")));
	        System.out.println("Battery capacity in prism is not set to: " + m_battery_capacity);
	        m_charging_rate = Double.parseDouble(String.valueOf(((JSONObject)obj).get("charging_rate")));
	        
	        JSONObject uobj = (JSONObject) ((JSONObject)obj).get("utility");
            m_energy_weight = Double.parseDouble(String.valueOf(uobj.get("energy")));
            m_timeliness_weight = Double.parseDouble(String.valueOf(uobj.get("timeliness")));

            System.out.println("Read config:\n\t* Battery Capacity: " + m_battery_capacity.toString() +
            					"\n\t* Charge rate: " + m_charging_rate.toString() +
            				    "\n\t* Energy Utility Weight: " + m_energy_weight.toString() +
            				    "\n\t* Timeliness Utility Weight: " + m_timeliness_weight.toString());
	    }

	public SimpleConfigurationBatteryModel(Properties props) {
		System.out.println(PropertiesSimpleConfigurationStore.BATTERY_CONFIGURATION_PROPKEY);
		System.out.println(props.getProperty(PropertiesSimpleConfigurationStore.BATTERY_CONFIGURATION_PROPKEY));
		loadConfig(ConfigHelper
				.convertToAbsolute(props.getProperty(PropertiesSimpleConfigurationStore.BATTERY_CONFIGURATION_PROPKEY)));
	}
	 
}
