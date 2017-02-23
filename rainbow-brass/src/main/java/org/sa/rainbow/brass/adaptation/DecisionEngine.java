package org.sa.rainbow.brass.adaptation;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.sa.rainbow.brass.model.map.MapTranslator;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.core.Rainbow;



public class DecisionEngine {

	public static String m_export_path = "/Users/jcamara/Dropbox/Documents/Work/Projects/BRASS/rainbow-prototype/trunk/rainbow-brass/prismtmp/";
	public static MapTranslator m_mt;
	public static PrismConnector m_pc;
	public static String m_origin;
	public static String m_destination;
	public static Map<List, String> m_candidates;
	public static Map<List, Double > m_scoreboard;
	
	
	public static void init(){
		 m_mt = new MapTranslator ();
         m_pc = new PrismConnector (null); // Does this work with hard-wired props in the constructor?
         m_origin="";
         m_destination="";
         m_scoreboard= new HashMap<List, Double>();
	}
	
	public static void setMap(EnvMap map){
		m_mt.setMap(map);
	}
	
	public static void generateCandidates(String origin, String destination){
		m_candidates = m_mt.exportConstrainedTranslationsBetween(m_export_path, origin, destination);	
	}

	public static void scoreCandidates(EnvMap map){
		m_scoreboard.clear();
		String result;
		synchronized (map){
			for (List candidate_key : m_candidates.keySet() ){
				result = m_pc.invokeGenPolicy(m_candidates.get(candidate_key), map.getNodeId(String.valueOf(candidate_key.get(0))), map.getNodeId(String.valueOf(candidate_key.get(candidate_key.size()-1))));
				m_scoreboard.put(candidate_key, Double.valueOf(result));
			}
		}
	}
	
	public static String selectPolicy(){
		Map.Entry<List, Double> maxEntry = null;
		for (Map.Entry<List, Double> entry : m_scoreboard.entrySet())
		{
		    if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
		    {
		        maxEntry = entry;
		    }
		}
		return m_candidates.get(maxEntry.getKey());
	}
	
	
	public static void main(String[] args){
		init();
		EnvMap dummyMap = new EnvMap(null);
		setMap(dummyMap);
		generateCandidates("ls", "l1");
		scoreCandidates(dummyMap);
		System.out.println(String.valueOf(m_scoreboard));
		System.out.println(selectPolicy());
		
	}
	
}
