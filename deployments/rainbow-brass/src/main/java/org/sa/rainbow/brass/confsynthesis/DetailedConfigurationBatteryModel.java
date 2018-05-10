package org.sa.rainbow.brass.confsynthesis;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Properties;
import java.util.LinkedList;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class DetailedConfigurationBatteryModel {
	private static final Double BATTERY_SCALE = 3600.0;
	
	private static HashMap<String, Double> m_drs_on, m_drs_off;
	static{
		m_drs_on = new HashMap<String, Double>();
		m_drs_off = new HashMap<String, Double>();
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
	        JSONArray nodes = (JSONArray) jsonObject.get("consumption");

	        for (Object node : nodes) {
	            JSONObject jsonNode = (JSONObject) node;
	            String c_id = jsonNode.get("id").toString();
	            Double c_mdr_on=0.0, c_mdr_off=0.0;
	            try{
	            c_mdr_on = Double.parseDouble(String.valueOf(jsonNode.get("enabled")));
	            c_mdr_off = Double.parseDouble(String.valueOf(jsonNode.get("disabled")));
	            } catch (Exception e){
	                System.out.println("Error parsing data from power consumption component "+c_id);
	            }
	            m_drs_on.put(c_id, c_mdr_on/BATTERY_SCALE);
	            m_drs_off.put(c_id, c_mdr_off/BATTERY_SCALE);	            
	            System.out.println("Added component " + c_id +" - Discharge rate (on): " + String.valueOf(c_mdr_on/BATTERY_SCALE) +" - Discharge rate (off): " + String.valueOf(c_mdr_off/BATTERY_SCALE));
	        }
	    }

	public DetailedConfigurationBatteryModel(Properties props) {
		loadConfig(props.getProperty(PropertiesConfigurationSynthesizer.BATTERYMODEL_PROPKEY));
	}
	
	public Double computeEnergyDischargeRate(LinkedList<String> items){
		Double res = 0.0;
		for (Map.Entry<String, Double> e: m_drs_on.entrySet()){
			if (items.contains(e.getKey())){
				res += m_drs_on.get(e.getKey());
			} else {
				res += m_drs_off.get(e.getKey());
			}
		}
		return res;
	}
	 
}
