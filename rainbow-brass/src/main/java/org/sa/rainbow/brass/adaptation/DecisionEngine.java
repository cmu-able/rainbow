package org.sa.rainbow.brass.adaptation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.sa.rainbow.brass.PropertiesConnector;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.MapTranslator;
import org.sa.rainbow.brass.adaptation.PrismPolicy;




public class DecisionEngine {

    public static String m_export_path;
    public static MapTranslator m_mt;
//    public static PrismConnector m_pc;
    public static PrismConnectorAPI m_pc;
    public static String m_origin;
    public static String m_destination;
    public static Map<List, String> m_candidates;
    public static Map<List, Double > m_scoreboard;
    public static PrismPolicy m_plan;


    public static void init (Properties props) {
        if (props == null) {
            props = PropertiesConnector.DEFAULT;
        }
        m_export_path = props.getProperty (PropertiesConnector.PRISM_OUTPUT_DIR);
        m_mt = new MapTranslator ();
//        m_pc = new PrismConnector (null); // Old version invokes an installation of prism
        m_pc = new PrismConnectorAPI (); // PRISM invoked via API
        m_origin="";
        m_destination="";
        m_scoreboard= new HashMap<List, Double>();
    }

    public static void setMap(EnvMap map){
        m_mt.setMap(map);
    }

    public static void generateCandidates(String origin, String destination){
    	m_origin = origin;
    	m_destination = destination;
        m_candidates = m_mt.exportConstrainedTranslationsBetween(m_export_path, origin, destination);	
    }

    public static void scoreCandidates(EnvMap map, String batteryLevel, String robotHeading){
        m_scoreboard.clear();
        synchronized (map){
        String m_consts = MapTranslator.INITIAL_ROBOT_LOCATION_CONST+"="+String.valueOf(map.getNodeId(m_origin)) +","+ MapTranslator.TARGET_ROBOT_LOCATION_CONST 
        		+ "="+String.valueOf(map.getNodeId(m_destination))+ "," + MapTranslator.INITIAL_ROBOT_BATTERY_CONST+"="+batteryLevel+","+MapTranslator.INITIAL_ROBOT_HEADING_CONST+"="+robotHeading;

        System.out.println(m_consts);
        String result;
            for (List candidate_key : m_candidates.keySet() ){                           	
            result = m_pc.modelCheckFromFileS(m_candidates.get(candidate_key), m_export_path+"mapbot.props", m_candidates.get(candidate_key), 0, m_consts);
            m_scoreboard.put(candidate_key, Double.valueOf(result));
            }
        }
    }

    public static String selectPolicy(){
        Map.Entry<List, Double> maxEntry = null;
        for (Map.Entry<List, Double> entry : m_scoreboard.entrySet())
        {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) < 0)
            {
                maxEntry = entry;
            }
        }
        return m_candidates.get(maxEntry.getKey());
    }


    public static void main(String[] args){
        init (null);
        EnvMap dummyMap = new EnvMap (null, null);
        setMap(dummyMap);
        generateCandidates("l5", "l1");
        scoreCandidates(dummyMap, "1000", "1");
        System.out.println(String.valueOf(m_scoreboard));
        System.out.println();
        
        PrismPolicy pp = new PrismPolicy(selectPolicy()+".adv");
  	  	pp.readPolicy();  
  	    System.out.println(pp.getPlan().toString());

    }

}
