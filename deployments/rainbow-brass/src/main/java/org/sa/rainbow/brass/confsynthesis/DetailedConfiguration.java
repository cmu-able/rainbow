package org.sa.rainbow.brass.confsynthesis;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import org.sa.rainbow.brass.confsynthesis.AlloySolution;


public class DetailedConfiguration implements Configuration {

	private String m_id;
	private HashMap<String, Double> m_cdrs = new HashMap<String, Double>();

	private Double m_cdr;
	private Double m_speed;
	
	private static final HashMap<String, Double> m_drs;
	static{
		m_drs = new HashMap<String, Double>();
		m_drs.put("lidar0", 100.0);
		m_drs.put("kinect0", 70.0);
		m_drs.put("laserscanNodelet0", 60.0);
		m_drs.put("headlamp0", 0.0);
		m_drs.put("amcl0", 50.0);
		m_drs.put("mrpt0", 50.0);
		m_drs.put("halfSpeedSetting0", 50.0);		
		m_drs.put("fullSpeedSetting0", 100.0);	
		m_drs.put("camera0", 100.0);	
		m_drs.put("markerRecognizer0", 100.0);		
		m_drs.put("markerLocalization0", 100.0);
	}

	private static final HashMap<String, Double> m_speedsettings;
	static{
		m_speedsettings = new HashMap<String, Double>();
		m_speedsettings.put("halfSpeedSetting0", 0.35);
		m_speedsettings.put("fullSpeedSetting0", 0.7);
	}
	
	public boolean isSpeedSetting(String id){
		return m_speedsettings.containsKey(id);
	}
	
	public DetailedConfiguration (String id, String solStr){
		m_id = id;
		AlloySolution sol = new AlloySolution();
		sol.loadFromString(solStr);
//		System.out.println(String.valueOf(sol));
		for (Map.Entry<String, LinkedList<String>> e: sol.getAllInstances().entrySet()){
//			System.out.println(String.valueOf(e.getValue()));
			for (int i=0; i<e.getValue().size(); i++){
				String cid = e.getValue().get(i).replace("$", "");
				if (!Objects.equals(cid,"") && !m_cdrs.containsKey(cid))
					m_cdrs.put(cid, m_drs.get(cid));
				if (isSpeedSetting(cid))
					m_speed = m_speedsettings.get(cid);
			}
		}
//		System.out.println(String.valueOf(m_cdrs));
		computeEnergyDischargeRate();
		System.out.println("Speed: " + String.valueOf(m_speed));
	}

	public String getId(){
		return m_id;
	}
		
	public void computeEnergyDischargeRate(){
		Double res=0.0;
		for (Map.Entry<String, Double> e: m_cdrs.entrySet()){
			System.out.println(e.getKey());
			res += e.getValue();
		}
		m_cdr=res;
		System.out.println(m_id+" Energy discharge rate: " + String.valueOf(m_cdr));
	}
	
	public Double getEnergyDischargeRate(){
		return m_cdr;
	}
	
	public Double getSpeed(){
		return m_speed;
	}
	
}
