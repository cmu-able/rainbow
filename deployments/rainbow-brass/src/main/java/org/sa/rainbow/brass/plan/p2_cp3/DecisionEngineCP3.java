package org.sa.rainbow.brass.plan.p2_cp3;

import java.awt.geom.Point2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.sa.rainbow.brass.PropertiesConnector;
import org.sa.rainbow.brass.adaptation.PrismPolicy;
import org.sa.rainbow.brass.confsynthesis.ConfigurationSynthesizer;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapPath;
import org.sa.rainbow.brass.plan.p2.DecisionEngine;
import org.sa.rainbow.brass.plan.p2.MapTranslator;
import org.sa.rainbow.core.Rainbow;

public class DecisionEngineCP3 extends DecisionEngine {

	public static double m_selected_candidate_time = 0.0;
	public static double m_selected_candidate_safety = 0.0;
	public static double m_selected_candidate_energy = 0.0;
	public static double m_safetyWeight;
	public static double m_energyWeight;
	public static double m_timelinessWeight;
	public static boolean choose_balanced_utilty;
	public static boolean do_not_change_paths;
	public static String m_selected_candidate;

	public static void init(Properties props) throws Exception {
		DecisionEngine.init(props);
		MapTranslator.ROBOT_BATTERY_RANGE_MAX = 180000;
		MapTranslator.CONSIDER_RECONFIGURATION_COST = Boolean
				.parseBoolean(props.getProperty("rainbow.consider_cost", "false"));
		MapTranslator.ROBOT_MAX_RECONF_VAL = props.getProperty("rainbow.max_reconfs", "1");
		DecisionEngineCP3.choose_balanced_utilty = Boolean
				.parseBoolean(props.getProperty("rainbow.balanced_utility", "true"));
		DecisionEngineCP3.do_not_change_paths = Boolean.parseBoolean(props.getProperty("rainbow.fix_path", "false"));
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
	}

	public static void setEnergyPreference() {
		m_safetyWeight = 0.2;
		m_energyWeight = 0.6;
		m_timelinessWeight = 0.2;
	}

	public static void setTimelinessPreference() {
		m_safetyWeight = 0.2;
		m_energyWeight = 0.2;
		m_timelinessWeight = 0.6;
	}

	/**
	 * Returns the maximum estimated remaining energy for a candidate policy in the
	 * scoreboard
	 * 
	 * @return
	 */
	public static Double getMaxEnergy() {
		return getMaxItem(ENERGY_INDEX);
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

		String balancedCandidate = m_candidates.get(maxEntry.getKey());
		log("Balanced candidate policy: " + balancedCandidate);
		log("Score: " + String.valueOf(m_selected_candidate_score) + " Safety: "
				+ String.valueOf(m_selected_candidate_safety) + " Time: " + String.valueOf(m_selected_candidate_time)
				+ " Energy: " + String.valueOf(m_selected_candidate_energy));
		String singleCriterionCandidate = m_candidates.get(maxSingleEntry.getKey());
		log("Single Criterion selected, based on really preferring one quality: " + singleCriterionCandidate);
		if (!Objects.equals(maxSingleEntry.getKey(), maxEntry.getKey())) {
			log("THESE PLANS ARE DIFFERENT");
		}
		log("Choosing the plan: " + (choose_balanced_utilty ? "Balanced" : "Prioritized"));
		m_selected_candidate = DecisionEngineCP3.choose_balanced_utilty ? (balancedCandidate)
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
            Double entryPsuccess = entry.getValue().get(SUCCESS_INDEX);

            PrismPolicy pp = new PrismPolicy(m_candidates.get(entry.getKey())+".adv");
            pp.readPolicy();
            String entryConfig = pp.getAllowedReconfigurations().toString();
            ConfigurationSynthesizer cs = new ConfigurationSynthesizer(Rainbow.instance().allProperties());
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

	public static void generateCandidates(List<String> path) {
		generateCandidates(path, false);
	}

	public static void generateCandidates(List<String> path, boolean inhibitTactics) {
		m_origin = path.get(0);
		m_destination = path.get(path.size() - 1);
		Map<List, String> specifications = new HashMap<List, String>();
		int c = 0;
		String filename = m_export_path + "/" + String.valueOf(c);
		EnvMapPath epath = new EnvMapPath(path, m_mt.getMap());
		m_mt.exportMapTranslation(filename, epath.getPath(), new ArrayList<String>(), inhibitTactics);
		System.out.println("Exported map translation " + String.valueOf(c));
		specifications.put(epath.getPath(), filename);
		System.out.println(
				"Candidate Path distance : " + String.valueOf(epath.getDistance()) + " " + String.valueOf(path));
		m_candidates = specifications;

	}

	/**
	 * Class test
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		init(null);

		List<Point2D> coordinates = new ArrayList<Point2D>();
		PrismPolicy pp = null;

		EnvMap dummyMap = new EnvMap(null, null);
		System.out.println("Loading Map: " + PropertiesConnector.DEFAULT.getProperty(PropertiesConnector.MAP_PROPKEY));
		dummyMap.loadFromFile(PropertiesConnector.DEFAULT.getProperty(PropertiesConnector.MAP_PROPKEY));
		System.out.println("Setting map...");
		setMap(dummyMap);

		ConfigurationSynthesizer cs = new ConfigurationSynthesizer();
		System.out.println("Populating configuration list..");
		cs.populate();
		System.out.println("Setting configuration provider...");
		setConfigurationProvider(cs);

		// String
		// currentConfStr="mapServerStd0_INIT=0,mapServerObs0_INIT=0,safeSpeedSetting0_INIT=0,markerLocalization0_INIT=0,markerRecognizer0_INIT=0,amcl0_INIT=1,laserscanNodelet0_INIT=1,mrpt0_INIT=2,camera0_INIT=1,lidar0_INIT=1,headlamp0_INIT=0,kinect0_INIT=2,fullSpeedSetting0_INIT=0,halfSpeedSetting0_INIT=1";
		String currentConfStr = "laserscanNodelet0_INIT=0,amcl0_INIT=0,markerLocalization0_INIT=0,mapServerObs0_INIT=0,markerRecognizer0_INIT=0,mapServerStd0_INIT=1,mrpt0_INIT=2,kinect0_INIT=0,camera0_INIT=0,headlamp0_INIT=0,lidar0_INIT=2,fullSpeedSetting0_INIT=0,halfSpeedSetting0_INIT=1,safeSpeedSetting0_INIT=0";
		cs.generateReconfigurationsFrom(currentConfStr);

		setTimelinessPreference();
		for (int i = 180000; i < 180500; i += 500) {
			System.out.println("Generating candidates for l1-l4...");
			generateCandidates("l32", "l36");
			System.out.println("Scoring candidates...");
			scoreCandidates(dummyMap, i, 1, "-1");
			System.out.println(String.valueOf(m_scoreboard));
			pp = new PrismPolicy(selectPolicy());
			pp.readPolicy();
			String plan = pp.getPlan(cs, currentConfStr).toString();
			System.out.println("Selected Plan: " + plan);
			PolicyToIGCP3 translator = new PolicyToIGCP3(pp, dummyMap);
			System.out.println(translator.translate(cs, currentConfStr));
		}
		System.out.println(export(dummyMap));

	}

}
