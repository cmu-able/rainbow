package org.sa.rainbow.brass.plan.p2_cp3;

import java.awt.geom.Point2D;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.sa.rainbow.brass.model.map.MapTranslatorHAIQ;
import org.jcm.haiq.analyzer.*;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import org.sa.rainbow.brass.PropertiesConnector;
import org.sa.rainbow.brass.adaptation.PrismPolicy;
import org.sa.rainbow.brass.confsynthesis.ConfigurationSynthesizer;
import org.sa.rainbow.brass.confsynthesis.DetailedConfigurationBatteryModel;
import org.sa.rainbow.brass.confsynthesis.PropertiesConfigurationSynthesizer;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapPath;
import org.sa.rainbow.brass.plan.p2.DecisionEngine;
import org.sa.rainbow.brass.plan.p2.MapTranslator;
import org.sa.rainbow.core.Rainbow;


public class DecisionEngineCP3HAIQ extends DecisionEngine {

	public static double m_selected_candidate_time = 0.0;
	public static double m_selected_candidate_safety = 0.0;
	public static double m_selected_candidate_energy = 0.0;
	public static double m_safetyWeight;
	public static double m_energyWeight;
	public static double m_timelinessWeight;
	public static boolean choose_balanced_utilty;
	public static boolean do_not_change_paths;
	public static String m_selected_candidate;
	private static Properties s_properties;

	public static void init(Properties props) throws Exception {
		s_properties = props;
		DecisionEngine.init(props);
		MapTranslatorHAIQ.ROBOT_BATTERY_RANGE_MAX = 180000.0;
		MapTranslator.CONSIDER_RECONFIGURATION_COST = Boolean
				.parseBoolean(props.getProperty("rainbow.consider_cost", "false"));
		MapTranslator.ROBOT_MAX_RECONF_VAL = props.getProperty("rainbow.max_reconfs", "1");
		MapTranslator.TRAVERSAL_SUCCESS_THRESHOLD = Double.parseDouble(props.getProperty("rainbow.success_threshold", "0.9").trim());
		DecisionEngineCP3HAIQ.choose_balanced_utilty = Boolean
				.parseBoolean(props.getProperty("rainbow.balanced_utility", "true"));
		DecisionEngineCP3HAIQ.do_not_change_paths = Boolean.parseBoolean(props.getProperty("rainbow.fix_path", "false"));
		setSafetyPreference();
	}

	public static final int ENERGY_INDEX = 0;
	public static final int TIME_INDEX = 1;
	public static final int SAFETY_INDEX = 2;
	public static final int SUCCESS_INDEX = 3; 

	public static void setSafetyPreference() {
		m_safetyWeight = 0.6;
		m_energyWeight = 0.2;
		m_timelinessWeight = 0.2;
		m_priority_index = SAFETY_INDEX;
	}

	public static void setEnergyPreference() {
		m_safetyWeight = 0.2;
		m_energyWeight = 0.6;
		m_timelinessWeight = 0.2;
		m_priority_index = ENERGY_INDEX;
	}

	public static void setTimelinessPreference() {
		m_safetyWeight = 0.2;
		m_energyWeight = 0.2;
		m_timelinessWeight = 0.6;
		m_priority_index = TIME_INDEX;
	}

	/**
	 * Returns the maximum estimated remaining energy for a candidate policy in the
	 * scoreboard
	 * 
	 * @return
	 */
	public static Double getMaxEnergy() {
	  	Double res = 0.0;
    	for (Map.Entry<List, ArrayList<Double>> entry : m_scoreboard.entrySet()){
    		Double e = entry.getValue().get(ENERGY_INDEX);
			if (e>res && e != INFINITY){
    			res = e;
    		}
    	}
    	return res;
	}

	/**
	 * Returns the maximum estimated time for a candidate policy in the scoreboard
	 * 
	 * @return
	 */
	public static Double getMaxTime() {
		return getMaxItem(TIME_INDEX);
	}

	/**
	 * Returns the maximum estimated safety index for a candidate policy in the
	 * scoreboard
	 * 
	 * @return
	 */
	public static Double getMaxSafety() {
		return getMaxItem(SAFETY_INDEX);
	}

	/**
	 * Selects the policy with the best score (CP3)
	 * 
	 * @return String filename of the selected policy
	 */
	public static String selectPolicy() {
		Double maxTime = getMaxTime();
		Double maxSafety = getMaxSafety();
		Double maxEnergy = getMaxEnergy();
		Double maxScore = 0.0;
		log("Considering cost: " + MapTranslator.CONSIDER_RECONFIGURATION_COST);
		log("Max reconfgs: " + MapTranslator.ROBOT_MAX_RECONF_VAL);
		log(String.format("|%3s|%5s|%5s|%5s|%5s|", "", "Time", "Safty", "NRG", "Score", "AllOrNothingScore"));
		log("---+-----+-----+-----+----+");
		log(String.format("|%3s|%5.2f|%5.2f|%5.2f|%5s|", "Max", maxTime, maxSafety, maxEnergy, "1"));

		Map.Entry<List, ArrayList<Double>> maxEntry = m_scoreboard.entrySet().iterator().next();
		double maxSingeScore = 0.0;
		Map.Entry<List, ArrayList<Double>> maxSingleEntry = m_scoreboard.entrySet().iterator().next();
		for (Map.Entry<List, ArrayList<Double>> entry : m_scoreboard.entrySet()) {
			Double entryTime = entry.getValue().get(TIME_INDEX);
			Double entryTimeliness = 0.0;
			if (maxTime > 0.0) {
				entryTimeliness = 1.0 - (entryTime / maxTime);
			}
			Double entryProbSafety = entry.getValue().get(SAFETY_INDEX);
			Double entrySafety = 0.0;
			if (maxSafety > 0.0) {
				entrySafety = (entryProbSafety / maxSafety);
			}

			Double entryEnergy = entry.getValue().get(ENERGY_INDEX) / maxEnergy;

			Double entryScore = m_safetyWeight * entrySafety + m_timelinessWeight * entryTimeliness
					+ m_energyWeight * entryEnergy;
			log(String.format("|%3s|%5.2f|%5.2f|%5.2f|%5.2f|", "", entryTimeliness, entrySafety, entryEnergy,
					entryScore));
			log(String.format("|%40s|", String.valueOf(entry.getKey())));
			double singleScore = 0.0;
			if (m_safetyWeight == 0.6)
				singleScore = entrySafety;
			if (m_timelinessWeight == 0.6)
				singleScore = entryTimeliness;
			if (m_energyWeight == 0.6)
				singleScore = entryEnergy;
			if (entryScore > maxScore) {
				maxEntry = entry;
				maxScore = entryScore;
			}
			if (singleScore > maxSingeScore) {
				maxSingeScore = singleScore;
				maxSingleEntry = entry;
			}
		}
		m_selected_candidate_time = maxEntry.getValue().get(TIME_INDEX);
		m_selected_candidate_safety = maxEntry.getValue().get(SAFETY_INDEX);
		m_selected_candidate_energy = maxEntry.getValue().get(ENERGY_INDEX);
		m_selected_candidate_score = maxScore;

//		String balancedCandidate = m_candidates.get(maxEntry.getKey());
		String balancedCandidate = maxEntry.getKey().toString();
		log("Balanced candidate policy: " + balancedCandidate);
		log("Score: " + String.valueOf(m_selected_candidate_score) + " Safety: "
				+ String.valueOf(m_selected_candidate_safety) + " Time: " + String.valueOf(m_selected_candidate_time)
				+ " Energy: " + String.valueOf(m_selected_candidate_energy));
//		String singleCriterionCandidate = m_candidates.get(maxSingleEntry.getKey());
		String singleCriterionCandidate = maxSingleEntry.getKey().toString();
		log("Single Criterion selected, based on really preferring one quality: " + singleCriterionCandidate);
		if (!Objects.equals(maxSingleEntry.getKey(), maxEntry.getKey())) {
			log("THESE PLANS ARE DIFFERENT");
		}
		log("Choosing the plan: " + (choose_balanced_utilty ? "Balanced" : "Prioritized") + ", score=" + (choose_balanced_utilty?m_selected_candidate_score:maxSingeScore));
		m_selected_candidate = DecisionEngineCP3HAIQ.choose_balanced_utilty ? (balancedCandidate)
				: (singleCriterionCandidate);
		return m_selected_candidate + ".adv";
	}

	public static double getSelectedPolicyTime() {
		return m_selected_candidate_time;
	}

	public static String generateLaTeXCandidateTable(){

    	String code="";
    	code +="\n {\\scriptsize \n \\setlength\\tabcolsep{1pt} \n \\centering \n \\begin{tabular}{| p{2.5cm} | l || l | l | l | l | || l | l | l || l | } \n \\hline \n"
    			+ "{\\bf Candidate} & {\\bf Config} & {\\bf PSuccess} & {\\bf Time} & {\\bf Safety} & {\\bf Energy} & {\\bf \\sf $U_{Time}$}  & {\\bf \\sf $U_{Safety}$}  & {\\bf \\sf $U_{Energy}$}  & {\\bf Score} \\\\ \n"
    			+ "\\hline \n";
    	
    	Double maxTime = getMaxTime();
    	Double maxSafety = getMaxSafety();
    	Double maxEnergy = getMaxEnergy();
    	Double maxScore=0.0;

        Map.Entry<List, ArrayList<Double>> maxEntry = m_scoreboard.entrySet().iterator().next();
        double maxSingeScore = 0.0;
        Map.Entry<List, ArrayList<Double>> maxSingleEntry = m_scoreboard.entrySet().iterator().next();
        for (Map.Entry<List, ArrayList<Double>> entry : m_scoreboard.entrySet())
        {
            Double entryTime = entry.getValue().get(TIME_INDEX);
            Double entryTimeliness = 0.0;
            if (maxTime>0.0){
            	entryTimeliness = 1.0-(entryTime / maxTime);
            }
            Double entryProbSafety = entry.getValue().get(SAFETY_INDEX);
            Double entrySafety=0.0;
            if (maxSafety>0.0){
            	entrySafety = (entryProbSafety/maxSafety);
            }

            Double entryEnergy = entry.getValue().get(ENERGY_INDEX)/maxEnergy;
            Double entryPsuccess =  0.0;// entry.getValue().get(SUCCESS_INDEX);

            PrismPolicy pp = new PrismPolicy(m_candidates.get(entry.getKey())+".adv");
            pp.readPolicy();
            String entryConfig = pp.getAllowedReconfigurations().toString();
            ConfigurationSynthesizer cs = new ConfigurationSynthesizer(DecisionEngineCP3HAIQ.s_properties);
            String entryConfigName = cs.translateId(entryConfig.replace("[", "").replace("]", "").replace("t_set_",""));
            
            Double entryScore = m_safetyWeight * entrySafety + m_timelinessWeight * entryTimeliness + m_energyWeight * entryEnergy;
        	
            code += String.format("\n %40s & ", String.valueOf(entry.getKey()).replace("[", "").replace("]", ""));
            code += String.format(" %30s & ", entryConfigName);
            code += String.format("%5.2f & ", entryPsuccess);
            code += String.format("%5.2f & %5.2f & %5.2f ", entryTime, entrySafety, entry.getValue().get(0));
            code += String.format("%3s & %5.2f & %5.2f & %5.2f & %5.2f \\\\", "", entryTimeliness, entrySafety, entryEnergy, entryScore);

        	double singleScore = 0.0;
        	if (m_safetyWeight == 0.6) singleScore = entrySafety;
        	if (m_timelinessWeight == 0.6) singleScore = entryTimeliness;
        	if (m_energyWeight == 0.6) singleScore = entryEnergy;
        	if ( entryScore > maxScore)
            {
                maxEntry = entry;
                maxScore = entryScore;
            }

        	if (singleScore > maxSingeScore) {
        		maxSingeScore =singleScore;
        		maxSingleEntry = entry;
        	}
        }

        m_selected_candidate_time = maxEntry.getValue().get(TIME_INDEX);
        m_selected_candidate_safety = maxEntry.getValue().get(SAFETY_INDEX);
        m_selected_candidate_energy = maxEntry.getValue().get(ENERGY_INDEX);
        m_selected_candidate_score = maxScore;

    	code +="\n \\hline \n"
    			+ "\\end{tabular} } \n";

    	return code;
    }

	public static String exportPathToTikz(ArrayList<String> path, String mode, String color) {
		String code = "\\begin{scope}[> = stealth,  ," + color + "," + mode
				+ ",thick, every node/.style = {black,right,align=center}]\n";
		for (int i = 0; i < path.size() - 1; i++) {
			code += "\\draw (" + path.get(i) + ") edge [left]   node     {}     (" + path.get(i + 1) + ");\n";

		}
		code += "\\end{scope}\n\n";
		return code;
	}

	public static String openLaTeXDocument() {
		String sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		String code = "%------------\n% Experiment export generated on " + sdf
				+ "\n%-----------\n\n \\documentclass{article} \n" + "\\usepackage{graphicx} \n"
				+ "\\usepackage{tikz} \n" + "\\usepackage{pgfplots} \n" + "\\usepackage{pgfplotstable} \n"
				+ "\\usepgfplotslibrary{colorbrewer} \n" + "\\usetikzlibrary{shadows} \n"
				+ "\\usetikzlibrary{arrows} \n" + "\\usetikzlibrary{arrows.meta} \n"
				+ "\\usetikzlibrary{shadows.blur} \n" + "\\pgfplotsset{compat=1.14}\n\n" + "\\begin{document}\n\n";
		return code;
	}

	public static String closeLaTeXDocument() {
		return "\n\\end{document}\n%---- End of exported data -----\n";
	}

	public static String openTikzPicture() {
		return "\n \\begin{tikzpicture}[mynode/.style =  {draw,inner sep=0.6mm, circle,fill=white, blur shadow={shadow blur steps=3}},  scale=0.15]\n\n";
	}

	public static String closeTikzPicture() {
		return "\\end{tikzpicture}\n\n";
	}

	public static String export(EnvMap m) {
		String[] colors = { "red", "gray", "darkgray", "cyan", "violet" };
		String code = "";
		code += openLaTeXDocument();
		code += generateLaTeXCandidateTable();

		code += "\n \\vspace{0.5cm} \n Selected: " + m_selected_candidate + "\n \\vspace{0.5cm}\n\n";

		code += openTikzPicture();
		code += m.exportToTikz();
		int i = 0;
		for (Map.Entry<List, ArrayList<Double>> entry : m_scoreboard.entrySet()) {
			ArrayList<String> p = new ArrayList<String>(
					Arrays.asList(String.valueOf(entry.getKey()).replace("[", "").replace("]", "").split(",")));
			String color = colors[i];
			String mode = "dashed";
			if (Objects.equals(String.valueOf(entry.getKey()), m_selected_candidate)) {
				mode = "solid";
				color = "green";
			}
			code += exportPathToTikz(p, mode, color);
			i++;
		}

		code += closeTikzPicture();
		code += closeLaTeXDocument();
		return code;
	}

	public static void generateCandidates(List<String> path, boolean generateSeparatePaths) {
		generateCandidates(path, false, generateSeparatePaths);
	}

	public static void generateCandidates(List<String> path, boolean inhibitTactics, boolean generateSeparatePaths) {
		m_origin = path.get(0);
		m_destination = path.get(path.size() - 1);
		Map<List, String> specifications = new HashMap<List, String>();
		int c = 0;
		String filename = m_export_path + "/" + String.valueOf(c);
		EnvMapPath epath = new EnvMapPath(path, m_mt.getMap());
		if (generateSeparatePaths)
			m_mt.exportMapTranslation(filename, epath.getPath(), new ArrayList<String>(), inhibitTactics);
		else
			m_mt.exportMapTranslationWithReconfs(filename, new ArrayList<>(), inhibitTactics);
		System.out.println("Exported map translation " + String.valueOf(c));
		specifications.put(epath.getPath(), filename);
		System.out.println(
				"Candidate Path distance : " + String.valueOf(epath.getDistance()) + " " + String.valueOf(path));
		m_candidates = specifications;
	}
	
	
	public static void readScoreBoard(String filename) {
    	
        
        Properties props = PropertiesConnector.DEFAULT;

        JSONParser parser = new JSONParser();

        try (Reader reader = new FileReader(filename)) {

            JSONArray jsonArray = (JSONArray) parser.parse(reader);            
            
            for (int i=0; i<jsonArray.size(); i++) {
            	JSONObject o = (JSONObject) jsonArray.get(i);
            	ArrayList<Double> resItem = new ArrayList<Double>();
            	String policyContent="";
           	 		
            	Double energy=0.0, time=0.0, safety=0.0; 
            	
            	for (Object key: o.keySet()) {
                	String sk = (String) key;
                	
                	try (BufferedReader br = new BufferedReader(new FileReader(props.getProperty(PropertiesConnector.PRISM_OUTPUT_DIR_PROPKEY)+"/haiqadv/"+key+"-adv.txt"))){
           	 			policyContent = br.readLine();
           	 		} catch (IOException e) {
           	 			e.printStackTrace();
           	 		}
                	
            
                	JSONArray itemlist = (JSONArray) o.get(sk);
                	
                	for (int j=0;j<itemlist.size();j++) {
	                	JSONObject item = (JSONObject) itemlist.get(j);
	                	if (item.containsKey("energy"))
	                		energy = MapTranslatorHAIQ.ROBOT_BATTERY_RANGE_MAX - Double.parseDouble((String)item.get("energy"));
	
	                	if (item.containsKey("time"))
	                		time = Double.parseDouble((String)item.get("time"));
	
	                	if (item.containsKey("safety"))
	                		safety = Double.parseDouble((String)item.get("safety"));
	                	
                	}

                }
            	resItem.add(energy);
            	resItem.add(time);
            	resItem.add(safety);
                 	           
            	List<String> policy = Arrays.asList(policyContent.replace("[","").replace("]","").split(","));       	
            	m_scoreboard.put(policy, resItem);

            }
           
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
	}
	

	/**
	 * Class test
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		 init(PropertiesConnector.DEFAULT);

		 ConfigurationSynthesizer cs = new ConfigurationSynthesizer();
		 MapTranslatorHAIQ.m_battery_model = new DetailedConfigurationBatteryModel(PropertiesConfigurationSynthesizer.DEFAULT);
		 cs.generateConfigurations();
		 cs.populate();
    	 EnvMap dummyMap = new EnvMap (null, null); 
         MapTranslatorHAIQ.setMap(dummyMap);
         MapTranslatorHAIQ.setConfigurationProvider(cs);
         Properties props = PropertiesConnector.DEFAULT;
         MapTranslatorHAIQ.exportTranslation(props.getProperty(PropertiesConnector.CP3_HAIQ_MODEL_PROPKEY), MapTranslatorHAIQ.getJHAIQTranslation("l32","l36"));
 //         -properties[0,1,2] -exportModels  -setMaxConfigs[100] -exportPolicies[./adv/] -showScoreboard -exportScoreboardJSON[./scores.json]

         String[] HQArgs =  {"-model["+props.getProperty(PropertiesConnector.CP3_HAIQ_MODEL_PROPKEY)+"]",
        		 			 "-properties[0,1,2]", "-setMaxConfigs[10]", "-showScoreboard", 
        		 			 "-exportPolicies["+props.getProperty(PropertiesConnector.PRISM_OUTPUT_DIR_PROPKEY)+"/haiqadv/]",
        		 			 "-exportScoreboardJSON["+props.getProperty(PropertiesConnector.PRISM_OUTPUT_DIR_PROPKEY)+"/haiqadv/scores]",
        		 			 "-exportModels"};
         HaiQ.main(HQArgs);
         readScoreBoard(props.getProperty(PropertiesConnector.PRISM_OUTPUT_DIR_PROPKEY)+"/haiqadv/scores.json");
         System.out.println(m_scoreboard);
        String policyContent = selectPolicy();
        System.out.println(policyContent);
     	ArrayList<String> policy = new ArrayList<String>();
     	policy.addAll(Arrays.asList(policyContent.replace("[","").replace("].adv","").split(",")));
     	System.out.println (policy.toString());
     	BRASSRobotPolicyFilter f = new BRASSRobotPolicyFilter(policy);
     	ArrayList<String> myPlan = f.getPlan("l32", "l36");
     	System.out.println("PLAN TRANSLATED: "+myPlan); 
		PolicyToIGCP3 translator = new PolicyToIGCP3(null, dummyMap);
		System.out.println(translator.translatePlan(myPlan));

		
	}

}
