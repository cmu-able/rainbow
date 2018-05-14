package org.sa.rainbow.brass.plan.p2_cp3;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.sa.rainbow.brass.PropertiesConnector;
import org.sa.rainbow.brass.adaptation.PrismPolicy;
import org.sa.rainbow.brass.confsynthesis.ConfigurationSynthesizer;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.plan.p2.DecisionEngine;
import org.sa.rainbow.brass.plan.p2.MapTranslator;

public class DecisionEngineCP3 extends DecisionEngine {
	
    public static double m_selected_candidate_time=0.0;
    public static double m_selected_candidate_safety=0.0;
    public static double m_selected_candidate_energy=0.0;
    public static double m_safetyWeight;
	public static double m_energyWeight;
	public static double m_timelinessWeight;
	public static void init (Properties props) throws Exception {
		DecisionEngine.init(props);
        MapTranslator.ROBOT_BATTERY_RANGE_MAX = 180000;
        setSafetyPreference();
	}
	
	public static void setSafetyPreference(){
		m_safetyWeight = 0.6;
		m_energyWeight = 0.2;
		m_timelinessWeight = 0.2;
	}

	public static void setEnergyPreference(){
		m_safetyWeight = 0.2;
		m_energyWeight = 0.6;
		m_timelinessWeight = 0.2;
	}
	
	public static void setTimelinessPreference(){
		m_safetyWeight = 0.2;
		m_energyWeight = 0.2;
		m_timelinessWeight = 0.6;
	}

	/**
    * Returns the maximum estimated remaining energy for a candidate policy in the scoreboard
    * @return
    */
    public static Double getMaxEnergy(){
    	return getMaxItem(0);
    }
	
    /**
     * Returns the maximum estimated time for a candidate policy in the scoreboard
     * @return
     */
    public static Double getMaxTime(){
	    	return getMaxItem(1);
	    }    

   /**
    * Returns the maximum estimated safety index for a candidate policy in the scoreboard
    * @return
    */
   public static Double getMaxSafety(){
   	return getMaxItem(2);
   }    

    /**
     * Selects the policy with the best score (CP3)
     * @return String filename of the selected policy
     */
    public static String selectPolicy(){     	
    	Double maxTime = getMaxTime();
    	Double maxSafety = getMaxSafety();
    	Double maxEnergy = getMaxEnergy();
    	Double maxScore=0.0;
    	
        Map.Entry<List, ArrayList<Double>> maxEntry = m_scoreboard.entrySet().iterator().next();
        for (Map.Entry<List, ArrayList<Double>> entry : m_scoreboard.entrySet())
        {
            Double entryTime = entry.getValue().get(1);
            Double entryTimeliness = 0.0;
            if (maxTime>0.0){
            	entryTimeliness = 1.0-(entryTime / maxTime);
            }
            Double entryProbSafety = entry.getValue().get(2);
            Double entrySafety=0.0;
            if (maxSafety>0.0){
            	entrySafety = (entryProbSafety/maxSafety);
            }
            
            Double entryEnergy = entry.getValue().get(0)/maxEnergy;
            
            Double entryScore = m_safetyWeight * entrySafety + m_timelinessWeight * entryTimeliness + m_energyWeight * entryEnergy;
            
        	if ( entryScore > maxScore)
            {
                maxEntry = entry;
                maxScore = entryScore;
            }
        }
        m_selected_candidate_time = maxEntry.getValue().get(1);
        m_selected_candidate_safety = maxEntry.getValue().get(2);
        m_selected_candidate_energy = maxEntry.getValue().get(0);
        m_selected_candidate_score = maxScore;
        
        log("Selected candidate policy: "+m_candidates.get(maxEntry.getKey()));
        log("Score: "+String.valueOf(m_selected_candidate_score)+" Safety: "+String.valueOf(m_selected_candidate_safety)+" Time: "+String.valueOf(m_selected_candidate_time)+" Energy: "+String.valueOf(m_selected_candidate_energy));
        
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
            scoreCandidates(dummyMap, i, 1);
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
