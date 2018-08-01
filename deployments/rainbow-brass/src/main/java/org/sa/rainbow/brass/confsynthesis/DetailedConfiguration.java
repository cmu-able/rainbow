package org.sa.rainbow.brass.confsynthesis;

import java.util.HashMap;
import java.awt.geom.Point2D;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.sa.rainbow.brass.PropertiesConnector;
import org.sa.rainbow.brass.adaptation.PrismPolicy;
import org.sa.rainbow.brass.confsynthesis.AlloySolution;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.plan.p2_cp3.PolicyToIGCP3;
import org.sa.rainbow.brass.confsynthesis.DetailedConfigurationBatteryModel;


public class DetailedConfiguration implements Configuration {

	private String m_id;
	private LinkedList m_items = new LinkedList<String>();
	private DetailedConfigurationBatteryModel m_bm;
	
	private Double m_cdr;
	private Double m_speed;
	


	private static final HashMap<String, Double> m_speedsettings;
	static{
		m_speedsettings = new HashMap<String, Double>();
		m_speedsettings.put("halfSpeedSetting0", 0.35);
		m_speedsettings.put("fullSpeedSetting0", 0.68);
		m_speedsettings.put("safeSpeedSetting0", 0.24);		
	}
	
	public boolean isSpeedSetting(String id){
		return m_speedsettings.containsKey(id);
	}
		
	
	public DetailedConfiguration (String id, String solStr, DetailedConfigurationBatteryModel bm){
		m_bm = bm;
		m_id = id;
		AlloySolution sol = new AlloySolution();
		sol.loadFromString(solStr);
//		System.out.println(String.valueOf(sol));
		for (Map.Entry<String, LinkedList<String>> e: sol.getAllInstances().entrySet()){
//			System.out.println(String.valueOf(e.getValue()));
			for (int i=0; i<e.getValue().size(); i++){
				String cid = e.getValue().get(i).replace("$", "");
				if (!Objects.equals(cid,"") && !m_items.contains(cid))
					m_items.add(cid);
				if (isSpeedSetting(cid))
					m_speed = m_speedsettings.get(cid);
			}
		}
//		System.out.println(String.valueOf(m_cdrs));
		computeEnergyDischargeRate();
//		System.out.println("Speed: " + String.valueOf(m_speed));
	}

	public String getId(){
		return m_id;
	}
		
	public void computeEnergyDischargeRate(){
		Double res = m_bm.computeEnergyDischargeRate(m_items);
		m_cdr=res;
		//System.out.println(m_id+" Energy discharge rate: " + String.valueOf(m_cdr));
	}
	
	public Double getEnergyDischargeRate(){
		return m_cdr;
	}
	
	public Double getSpeed(){
		return m_speed;
	}
	
	public ArrayList<String> getComponents(){
		ArrayList<String> res = new ArrayList<String>();
		res.addAll(m_items);
		return res;
	}
	

}
