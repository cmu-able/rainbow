package org.sa.rainbow.brass.plan.p2;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.sa.rainbow.brass.PropertiesConnector;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.plan.p2.MapTranslator;
import org.sa.rainbow.brass.adaptation.PrismPolicy;
import org.sa.rainbow.brass.adaptation.PrismConnectorAPI;
import org.sa.rainbow.brass.adaptation.PolicyToIG;

import org.sa.rainbow.brass.confsynthesis.ConfigurationProvider;
import org.sa.rainbow.brass.confsynthesis.ConfigurationSynthesizer;
import org.sa.rainbow.brass.confsynthesis.SimpleConfigurationStore;


import com.google.common.base.Objects;

import prism.PrismException;


/**
 * @author jcamara
 *
 */
public class DecisionEngine {

    public static String m_export_path;
    public static String m_properties_file;
    public static MapTranslator m_mt;
    public static String m_origin;
    public static String m_destination;
    public static Map<List, String> m_candidates;
    public static Map<List, ArrayList<Double>> m_scoreboard;
    public static double m_selected_candidate_score;
    public static PrismPolicy m_plan;

    public static final double INFINITY = 999999.0;

    /**
     * Initializes decision engine
     * @param props
     */
    public static void init (Properties props) throws Exception {
        if (props == null) {
            props = PropertiesConnector.DEFAULT;
        }
        m_export_path = props.getProperty (PropertiesConnector.PRISM_OUTPUT_DIR_PROPKEY);
        m_export_path = m_export_path.replaceAll ("\\\"", "");
        m_properties_file = props.getProperty(PropertiesConnector.PRISM_PROPERTIES_PROPKEY);
        m_mt = new MapTranslator ();
        PrismConnectorAPI.instance(); // PRISM invoked via API
        m_origin="";
        m_destination="";
        m_selected_candidate_score=0.0;
        m_scoreboard= new HashMap<List, ArrayList<Double>>();
    }

    /**
     * Sets the map to extract data 
     * @param map
     */
    public static void setMap(EnvMap map){
        m_mt.setMap(map);
    }
    
    /**
     * Sets the configuration provider to extract data
     * @param cp
     */
    public static void setConfigurationProvider(ConfigurationProvider cp){
        m_mt.setConfigurationProvider(cp);
    }
    
    /**
     * Generates all PRISM specifications corresponding to the different non-cyclic paths between
     * origin and destination locations
     * @param origin String label of origin map location
     * @param destination String label of destination map location
     */

    public static void generateCandidates (String origin, String destination){
        generateCandidates(origin, destination, false);
    }

    public static void generateCandidates(String origin, String destination, boolean inhibitTactics){
        m_origin = origin;
        m_destination = destination;
        m_candidates = m_mt.exportConstrainedTranslationsBetweenCutOff(m_export_path, origin, destination, inhibitTactics);	
//        m_candidates = m_mt.exportConstrainedTranslationsBetween(m_export_path, origin, destination, inhibitTactics);	
//        m_candidates = m_mt.exportSingleTranslationBetween(m_export_path, origin, destination, inhibitTactics);	
    }

    /**
     * Assigns a score to each one of the candidate policies synthesized based on the specifications generated by
     * generateCandidates
     * 
     * @param map
     * @param batteryLevel
     *            String amount of remaining battery
     * @param robotHeading
     *            String robot Heading (needs to be converted to an String encoding an int from MissionState.Heading)
     * @throws Exception
     */
    public static void scoreCandidates (EnvMap map, long batteryLevel, int robotHeading) throws Exception {
    	try{
            m_scoreboard.clear();
    	}
    	catch(NullPointerException e){
    	    m_scoreboard = new HashMap<List, ArrayList<Double>>();
    	}
        synchronized (map){
            int originID = map.getNodeId(m_origin);
			int destinationID = map.getNodeId(m_destination);
			if (originID == -1) throw new IllegalArgumentException(m_origin + " does not appear to be in the map");
			if (destinationID == -1) throw new IllegalArgumentException(m_destination + " does not appear in the map");
			String m_consts = MapTranslator.INITIAL_ROBOT_CONF_CONST+"=-1,"+MapTranslator.INITIAL_ROBOT_LOCATION_CONST+"="+String.valueOf(originID) +","+ MapTranslator.TARGET_ROBOT_LOCATION_CONST 
                    + "="+String.valueOf(destinationID)+ "," + MapTranslator.INITIAL_ROBOT_BATTERY_CONST+"="+batteryLevel+","+MapTranslator.INITIAL_ROBOT_HEADING_CONST+"="+robotHeading;

            log(m_consts);
            String result;
            for (List candidate_key : m_candidates.keySet() ){                           	
                result = PrismConnectorAPI.instance().modelCheckFromFileS (m_candidates.get(candidate_key), m_properties_file, m_candidates.get (candidate_key), -1, m_consts);
                
                String[] results = result.split(",");
                ArrayList<Double> resultItems = new ArrayList<Double>();
                for (int i=0; i<results.length; i++){
                	if (!Objects.equal(results[i], "Infinity")) {
                		resultItems.add(Double.valueOf(results[i]));
                	}
                	else {
                		resultItems.add(INFINITY);
                	}
                }
        		m_scoreboard.put(candidate_key, resultItems);
            }
        }
    }

    /**
     * Gets the maximum value of an attribute in the column number index of the scoreboard 
     * (the order of what the column is storing is set by the position of the corresponding 
     * PCTL formula in the properties file)
     * @param index
     * @return
     */
    public static Double getMaxItem(int index){
    	Double res = 0.0;
    	for (Map.Entry<List, ArrayList<Double>> entry : m_scoreboard.entrySet()){
    		if (entry.getValue().get(index)>res){
    			res = entry.getValue().get(index);
    		}
    	}
    	return res;
    }
    
    /**
     * Returns a string with an identifier of the selected candidate policy
     * (To be implemented by inheriting classes)
     * @return
     */
    public static String selectPolicy(){
    	return "None";
    }

	public static Logger LOGGER = null;

	public static Logger getLOGGER() {
		return LOGGER;
	}

	public static void setLOGGER(Logger lOGGER) {
		LOGGER = lOGGER;
		m_mt.setLogger(LOGGER);
		try {
			PrismConnectorAPI.instance().setLogger(lOGGER);
		} catch (PrismException e) {
			LOGGER.fatal(e);
		}
	}

	public static void log(String msg) {
		if (LOGGER != null) {
			LOGGER.info(msg);
		}
		else {
			System.out.println(msg);
		}
	}

}
