package org.sa.rainbow.brass.plan.p2_cp3;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sa.rainbow.brass.PropertiesConnector;
import org.sa.rainbow.brass.adaptation.PrismPolicy;
import org.sa.rainbow.brass.confsynthesis.ConfigurationSynthesizer;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.plan.p2.DecisionEngine;

public class DecisionEngineCP3 extends DecisionEngine {
	
    public static double m_selected_candidate_time=0.0;
    public static double m_selected_candidate_safety=0.0;
    public static double m_safetyWeight=0.5;
	public static double m_timelinessWeight=0.5;
   
   public static Double getMaxTime(){
	    	return getMaxItem(0);
	    }    

   public static Double getMaxSafety(){
   	return getMaxItem(1);
   }    

    /**
     * Selects the policy with the best score (CP3)
     * @return String filename of the selected policy
     */
    public static String selectPolicy(){     	
    	Double maxTime = getMaxTime();
    	Double maxSafety = getMaxSafety();
    	Double maxScore=0.0;
    	
        Map.Entry<List, ArrayList<Double>> maxEntry = m_scoreboard.entrySet().iterator().next();
        for (Map.Entry<List, ArrayList<Double>> entry : m_scoreboard.entrySet())
        {
            Double entryTime = entry.getValue().get(0);
            Double entryTimeliness = 0.0;
            if (maxTime>0.0){
            	entryTimeliness = 1.0-(entryTime / maxTime);
            }
            Double entryProbSafety = entry.getValue().get(1);
            Double entrySafety=0.0;
            if (maxSafety>0.0){
            	entrySafety = (entryProbSafety/maxSafety);
            }
            
            Double entryScore = m_safetyWeight * entrySafety + m_timelinessWeight * entryTimeliness;
            
        	if ( entryScore > maxScore)
            {
                maxEntry = entry;
                maxScore = entryScore;
            }
        }
        m_selected_candidate_time = maxEntry.getValue().get(0);
        m_selected_candidate_safety = maxEntry.getValue().get(1);
        m_selected_candidate_score = maxScore;
        
        System.out.println("Selected candidate policy: "+m_candidates.get(maxEntry.getKey()));
        System.out.println("Score: "+String.valueOf(m_selected_candidate_score)+" Safety: "+String.valueOf(m_selected_candidate_safety)+" Time: "+String.valueOf(m_selected_candidate_time));
        
        return m_candidates.get(maxEntry.getKey())+".adv";
    }

    public static double getSelectedPolicyTime(){
        return m_selected_candidate_time;
    }
    
    
    /**
     * Class test
     * @param args
     */
    public static void main (String[] args) throws Exception {
        init (null);

        List<Point2D> coordinates = new ArrayList<Point2D>();
        PrismPolicy pp=null;

        EnvMap dummyMap = new EnvMap (null, null);
        System.out.println("Loading Map: "+PropertiesConnector.DEFAULT.getProperty(PropertiesConnector.MAP_PROPKEY));
        dummyMap.loadFromFile(PropertiesConnector.DEFAULT.getProperty(PropertiesConnector.MAP_PROPKEY));
        System.out.println("Setting map...");
        setMap(dummyMap);
        
        ConfigurationSynthesizer cs = new ConfigurationSynthesizer();
        System.out.println("Populating configuration list..");
        cs.populate();
        System.out.println("Setting configuration provider...");
        setConfigurationProvider(cs);
        
		String currentConfStr="markerLocalization0_INIT=0,markerRecognizer0_INIT=0,amcl0_INIT=1,laserscanNodelet0_INIT=1,mrpt0_INIT=2,camera0_INIT=1,lidar0_INIT=1,headlamp0_INIT=0,kinect0_INIT=2,fullSpeedSetting0_INIT=0,halfSpeedSetting0_INIT=1";
		
        cs.generateReconfigurationsFrom(currentConfStr);

        for (int i=32000; i< 32500; i+=500){
        	System.out.println("Generating candidates for l1-l4...");
            generateCandidates("l1", "l4");
        	System.out.println("Scoring candidates...");
            scoreCandidates(dummyMap, String.valueOf(i), "1");
            System.out.println(String.valueOf(m_scoreboard));	
            pp = new PrismPolicy(selectPolicy());
            pp.readPolicy();  
            String plan = pp.getPlan(cs, currentConfStr).toString();
            System.out.println("Selected Plan: "+plan);
            PolicyToIGCP3 translator = new PolicyToIGCP3(pp, dummyMap);
            System.out.println (translator.translate (cs, currentConfStr));
        }

    }
    

}
