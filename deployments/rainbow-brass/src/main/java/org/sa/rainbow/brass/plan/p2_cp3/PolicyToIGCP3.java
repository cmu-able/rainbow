package org.sa.rainbow.brass.plan.p2_cp3;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapArc;
import org.sa.rainbow.brass.model.map.EnvMapNode;
import org.sa.rainbow.brass.model.map.Phase2MapPropertyKeys;
import org.sa.rainbow.brass.plan.p2.MapTranslator;
import org.sa.rainbow.brass.PropertiesConnector;
import org.sa.rainbow.brass.adaptation.PolicyToIG;
import org.sa.rainbow.brass.adaptation.PrismConnector;
import org.sa.rainbow.brass.adaptation.PrismPolicy;
//import org.sa.rainbow.brass.adaptation.PolicyToIG.Arguments;
import org.sa.rainbow.brass.confsynthesis.ConfigurationProvider;
import org.sa.rainbow.brass.confsynthesis.ConfigurationSynthesizer;
import org.sa.rainbow.brass.plan.p2_cp3.DecisionEngineCP3;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.impl.action.StoreTrueArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;


/**
 * @author jcamara
 *
 */

public class PolicyToIGCP3 {
	public PrismPolicy m_prismPolicy;
	public EnvMap m_map;
	public float m_current_speed = MapTranslator.ROBOT_HALF_SPEED_VALUE;
	public boolean m_insert_additional_command = false; // When translation of commands is not 1-to-1 from tactics,
														// additional command insertion flag for translation
	public String m_command_insert = ""; // (see build_cmd_tactic)
	public double m_location_x;
	public double m_location_y;
	public double m_theta;

	public PolicyToIGCP3(PrismPolicy policy, EnvMap map) {
		m_prismPolicy = policy;
		m_map = map;
	}

	/**
	 * Generates a movement instruction for the IG
	 * 
	 * @param cmdId
	 *            int id for the instruction (IG Vertex)
	 * @param tgt_x
	 *            double x coordinate for target location
	 * @param tgt_y
	 *            double y coordinate for target location
	 * @param speed
	 *            double linear speed
	 * @param theta
	 *            orientation angle (in radians) that the robot should face after
	 *            moving
	 * @return
	 */
	private String build_cmd_move(double tgt_x, double tgt_y, double speed, double theta) {
		NumberFormat f = new DecimalFormat("#0.0000");
		NumberFormat f2 = new DecimalFormat("#0.00");
		String cmd = "";

			cmd = "MoveAbsH(" + f2.format(tgt_x) + ", " + f2.format(tgt_y) + ", " + f2.format(speed) + ", "
					+ f.format(theta) + ")";

		return cmd;
	}

	private String build_reconfig_cmd(boolean mode) {
		return "SetReconfiguring(" + (mode?"1":"0") + ")";
	}

	/**
	 * Generates a tactic instruction for the IG
	 * 
	 * @param cmdId
	 *            int id for the instruction (IG Vertex)
	 * @param name
	 *            String literal containing the tactic's identifier
	 * @return
	 */
	private String build_cmd_tactic(String name) {
		String cmd = "";

//		System.out.println("Building command: " + String.valueOf(cmdId)+" - "+name);
		if (name.endsWith("SpeedSetting0_enable")){
			if (name.startsWith("full")){
				m_current_speed = MapTranslator.ROBOT_FULL_SPEED_VALUE;
				return ""; // Just set speed parameter, not explicit command in IG
			}
			if (name.startsWith("half")){
				m_current_speed = MapTranslator.ROBOT_HALF_SPEED_VALUE;
				return ""; // Just set speed parameter, not explicit command in IG
			}
			if (name.startsWith("safe")) {
				m_current_speed = MapTranslator.ROBOT_SAFE_SPEED_VALUE;
				return "";
			}
		} else if (name.endsWith("SpeedSetting0_disable")){
			return ""; // Ignore disable speed setting
		} else if (name.endsWith("0_enable")){
			String toBeEnabled = name.replace("0_enable","");
			if ("kinect".equals(toBeEnabled) || "camera".equals(toBeEnabled) || "lidar".equals(toBeEnabled) || "headlamp".equals(toBeEnabled)) {
				cmd = "SetSensor (%" + toBeEnabled.toUpperCase() + "%, %on%)";
			}
			else if ("amcl".equals(toBeEnabled) || "mrpt".equals(toBeEnabled)) {
				cmd = "StartNodes(%" + toBeEnabled + "%)";
			}
			else if ("markerLocalization".equals(toBeEnabled) || ("markerRecognizer".equals(toBeEnabled))) {
				cmd = "StartNodes(%aruco%)";
			}
			else if ("laserscanNodelet".equals(toBeEnabled)) {
				cmd = "StartNodes(%laserscanNodelet%)";
			}
			else if ("mapServerStd".equals(toBeEnabled)) {
				cmd = "StartNodes(%mapServer%)";
			}
			else if ("mapServerObs".equals(toBeEnabled)) {
				cmd = "StartNodes(%mapServerObs%)";
			}
			else cmd = "Enable ("+toBeEnabled+")";
		} else if (name.endsWith("0_disable")){
			String toBeEnabled = name.replace("0_disable","");
			if ("kinect".equals(toBeEnabled) || "camera".equals(toBeEnabled) || "lidar".equals(toBeEnabled) || "headlamp".equals(toBeEnabled)) {
				cmd = "SetSensor (%" + toBeEnabled.toUpperCase() + "%, %off%)";
			}
			else if ("amcl".equals(toBeEnabled) || "mrpt".equals(toBeEnabled)) {
				cmd = "KillNodes(%" + toBeEnabled + "%)";
			}
			else if ("markerLocalization".equals(toBeEnabled) || ("markerRecognizer".equals(toBeEnabled))) {
				cmd = "KillNodes(%aruco%)";
			}
			else if ("laserscanNodelet".equals(toBeEnabled)) {
				cmd = "KillNodes(%laserscanNodelet%)";
			}
			else if ("mapServerStd".equals(toBeEnabled)) {
				cmd = "KillNodes(%mapServer%)";
			}
			else if ("mapServerObs".equals(toBeEnabled)) {
				cmd = "KillNodes(%mapServerObs%)";
			}
			else cmd = "Disable ("+name.replace("0_disable","")+")";
		}
		
		return cmd;
	}

	/**
	 * Coats instructions with additional syntactic sugar
	 * 
	 * @param cmdId
	 * @param commandLiteral
	 * @return
	 */
	private String build_cmd(int cmdId, String commandLiteral) {
		String cmd = "V(" + cmdId + ", do " + commandLiteral + " then " + ++cmdId + ")";
		return cmd;
	}
	
	private boolean isReconfigurationTactic (String a){
		return (a.endsWith("0_enable") || a.endsWith("0_disable"));
	}
	
	private boolean isReconfig(String c) {
		return c.contains("KillNodes") || c.contains("SetSensor") || c.contains("StartNodes");
	}

	/**
	 * Builds the instruction graph
	 * 
	 * @param cmds
	 *            ArrayList<String> plan actions
	 * @return
	 */
	private String build_ig(ArrayList<String> cmds) {
		ArrayList<String> newCmds = new ArrayList<>(cmds.size());
		String prev = "";
		boolean reconfig = false;
		for (String c : cmds) {
			if (c.equals(prev)) continue;
			if (!reconfig && isReconfig(c)) {
				String r = build_reconfig_cmd(true);
				reconfig = true;
				newCmds.add(r);
			}
			if (reconfig && !isReconfig(c)) {
				String r = build_reconfig_cmd(false);
				reconfig = false;
				newCmds.add(r);
			}
			newCmds.add(c);
//			if ("StartNodes(%amcl%)".equals(c)
			prev = c;

		}
		
		cmds = newCmds;
		
////		Pattern p = Pattern.compile(".*do ([^)]*).*");
//		// remove duplicates
//		String prev = "";
//		for (Iterator iterator = cmds.iterator(); iterator.hasNext();) {
//			String string = (String) iterator.next();
////			Matcher m = p.matcher(string);
////			if (m.matches()) {
//				if (/*m.group(1)*/string.equals(prev)) {
//					iterator.remove();
//				}
//				prev = /*m.group(1)*/string;
////			}
//			
//		}
		String ins_graph = "P(";
		int i = 0;
		for (i = 0; i < cmds.size(); i++) {
			if (i == 0) {
				ins_graph += build_cmd(i+1,cmds.get(i)) + ",\n";
			} else {
				ins_graph += build_cmd(i+1, cmds.get(i)) + "::\n";
			}
		}
		// add the end
		ins_graph += "V(" + (i + 1) + ", end)::\nnil)";

		return ins_graph;
	}

	/**
	 * Finds the orientation of the next movement in plan (to indicate orientation
	 * of robot at the end of current MoveAbsH movement)
	 * 
	 * @param plan
	 * @param index
	 * @return
	 */
	public double findNextOrientation(ArrayList<String> plan, int index, double theta) {

		if (index + 1 >= plan.size())
			return theta;

		synchronized (m_map) {
			String[] e = plan.get(index).split("_"); // Break current move action plan name into chunks
			Double src_x = m_map.getNodeX(e[2]);
			Double src_y = m_map.getNodeY(e[2]);
			if (src_x == Double.NEGATIVE_INFINITY || src_y == Double.NEGATIVE_INFINITY)
				return theta;
			for (int i = index + 1; i < plan.size(); i++) {
				String action = plan.get(i);
				String[] e2 = action.split("_"); // Break action plan name into chunks
				if (!isReconfigurationTactic(action)) { // If action is not a reconfiguration tactic (i.e., it is a move command)																														
					Double tgt_x = m_map.getNodeX(e2[2]);
					Double tgt_y = m_map.getNodeY(e2[2]);
					theta = MapTranslator.findArcOrientation(src_x, src_y, tgt_x, tgt_y);
					return (theta);
				}
			}
		}
		return theta;
	}

	/**
	 * Expands the list of actions in the plan to IG instructions
	 * 
	 * @return
	 */


	public String translate(ConfigurationProvider cp, String currentConfStr) {
		ArrayList<String> plan = m_prismPolicy.getPlan(cp, currentConfStr);
		return translatePlan(plan);
	}

	public String translate(ConfigurationProvider cp, String currentConfStr, boolean includeTactics) {
		ArrayList<String> plan = m_prismPolicy.getPlan(cp, currentConfStr);
		return translatePlan(plan, includeTactics);
	}
	
	private String translatePlan(ArrayList<String> plan){
		return translatePlan(plan, true);
	}
	
	protected String translatePlan(ArrayList<String> plan, boolean includeTactics) {
		ArrayList<String> cmds = new ArrayList<String>();
		String cmd = "";

		int cmd_id = 1;

		for (int i = 0; i < plan.size(); i++) {
			String action = plan.get(i);

			String[] elements = action.split("_"); // Break action plan name into chunks
			if (action.endsWith("0_enable") || action.endsWith("0_disable")) { // If action is a tactic
				if (includeTactics)
					cmd = build_cmd_tactic(action);
				else 
					cmd = "";
			} else { // Other actions (robot movement for the time being)
				synchronized (m_map) {
					String destination = elements[2];
					String origin = elements[0];
					// cmd = build_cmd_move(cmd_id, m_map.getNodeX(destination),
					// m_map.getNodeY(destination), m_current_speed);
					double destX = m_map.getNodeX(destination);
					double destY = m_map.getNodeY(destination);
					if (destX != Double.NEGATIVE_INFINITY && destY != Double.NEGATIVE_INFINITY) {
						m_theta = MapTranslator.findArcOrientation(m_map.getNodeX(origin), m_map.getNodeY(origin),
								destX, destY);
						cmd = build_cmd_move(destX, destY, m_current_speed, findNextOrientation(plan, i, m_theta));
						m_location_x = destX;
						m_location_y = destY;
					} else {
						cmd = "";
					}
				}
			}
			if (!Objects.equals(cmd, "") && !cmds.contains(cmd)) {
				cmds.add(cmd);
				++cmd_id;
				if (m_insert_additional_command) {
					cmds.add(build_cmd(cmd_id, m_command_insert));
					++cmd_id;
					m_insert_additional_command = false;
					m_command_insert = "";
				}
			}
		}
		String ins_graph = build_ig(cmds);
		// System.out.println(ins_graph);
		return ins_graph;
	}

	/**
	 * Generates a JSON description of a waypoint list
	 * 
	 * @param p
	 * @param r
	 * @param w
	 * @return
	 */
	public static String generateJSONWayPointList(PrismPolicy p, String r, double w) {
		Pattern pattern = Pattern.compile("(l.*)_to_(l.*)");
		String out = "{\"path\": [";
		boolean first = true;
		for (int i = 0; i < p.getPlan().size(); i++) {
			if (pattern.matcher(p.getPlan().get(i)).matches()) {
				String[] e = p.getPlan().get(i).split("_");
				if (first) {
					out = out + "\"" + e[0] + "\"";
					first = false;
				}
				out = out + ",\"" + e[2] + "\"";
			}
		}
		out = out + "], \"time\": " + r;
		out = out + ", \"start-dir\": " + w;
		out = out + "}";
		return out;
	}

	/**
	 * Exports IG translation to a file
	 * 
	 * @param f
	 *            String filename
	 * @param s
	 *            String code for the IG
	 */
	public static void exportIGTranslation(String f, String s) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(f));
			out.write(s);
			out.close();
		} catch (IOException e) {
			System.out.println("Error exporting Instruction Graph translation");
		}
	}

	public static double getInitialRotation(EnvMapNode src, PrismPolicy prismPolicy, EnvMap map) {

		ArrayList<String> plan = prismPolicy.getPlan();
		Pattern pattern = Pattern.compile("(l.*)_to_(l.*)");
		for (String step : plan) {
			Matcher matcher = pattern.matcher(step);
			if (matcher.matches()) {
				// System.out.println("First move step is " + step);
				src = map.getNodes().get(matcher.group(1));
				EnvMapNode tgt = map.getNodes().get(matcher.group(2));
				// System.out.println(src.m_label + " to " + tgt.getLabel());
				double w = Math.atan2(tgt.m_y - src.m_y, tgt.m_x - src.m_x);
				return w;
			}
		}

		// EnvMapNode next = map.getNodes().get(prismPolicy.getPlan().get(0));
		// double w = Math.atan2(next.m_x - src.m_x, next.m_y - src.m_y);
		return -10.0;
	}

	private static class Arguments {
		@Arg(dest = "output")
		public String output;

		@Arg(dest = "properties")
		public String properties;
		
		@Arg(dest="waypoint")
		public String waypoint;

		@Arg(dest="configuration")
		public String configuration;

		@Arg(dest="priority")
		public String priority;
		
		@Arg(dest="target")
		public String target;

		@Arg(dest = "map")
		public String map;
		
		@Arg(dest = "append")
		public boolean append;
	}

	
	/**
	 * Class test
	 * Generates paths/IGs for an arbitrary pair of locations, initial configuration, and QA preference
	 * (QA preference keys: "safety", "energy", "timeliness")
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception { // Class test

		ArgumentParser parser = ArgumentParsers.newFor("prog").build()
				.description("Generate instruction graphs and paths for a map");
		parser.addArgument("-o", "--output").metavar("DIR").type(String.class)
				.help("The directory to output paths and instruction graphs");
		parser.addArgument("-p", "--properties").metavar("FILE").type(String.class)
				.help("The file containing properties/environment variables for PRISM etc");
		parser.addArgument("-w", "--waypoint").type(String.class).help("The waypoint to generate to and from");
		parser.addArgument("-c", "--configuration").type(String.class).help("The configuration to generate from");
		parser.addArgument("-pr", "--priority").type(String.class).help("The main priority QA");
		parser.addArgument("-t", "--target").type(String.class).help("The target waypoint, coupled with -w");
		parser.addArgument("map").type(String.class).help("The map file containing the waypoints");
		parser.addArgument("--append").action(new StoreTrueArgumentAction()).help("DO not overwrite exsiting instructions");

		Arguments theArgs = new Arguments();
		parser.parseArgs(args, theArgs);
		Properties props = new Properties();
		if (theArgs.properties != null) {
			try {
				props.load(new FileInputStream(new File(theArgs.properties)));
			} catch (Exception e) {
				System.err.println("Error loading properties file: " + theArgs.properties);
				System.exit(1);
			}
		}
		else {
			props =PropertiesConnector.DEFAULT;
		}
		props.setProperty(PropertiesConnector.MAP_PROPKEY, theArgs.map);

		// Set preference QA
		if (Objects.equals(theArgs.priority, "safety")){
			DecisionEngineCP3.setSafetyPreference();
		}

		if (Objects.equals(theArgs.priority, "energy")){
			DecisionEngineCP3.setEnergyPreference();
		}
		
		if (Objects.equals(theArgs.priority, "timeliness")){
			DecisionEngineCP3.setTimelinessPreference();
		}
		
		// Output dirs
		String out_dir_ig = PrismConnector.convertToAbsolute(theArgs.output == null ? "." : theArgs.output);
		String out_dir_wp = out_dir_ig;

		// Set up decision engine
		DecisionEngineCP3.init(theArgs.properties != null ? props : PropertiesConnector.DEFAULT);
		props.setProperty(PropertiesConnector.MAP_PROPKEY, theArgs.map);
		EnvMap map = new EnvMap(null, props);
		DecisionEngineCP3.setMap(map);
		
		ConfigurationSynthesizer cs = new ConfigurationSynthesizer(props);
        System.out.println("Populating configuration list..");
        cs.populate();
        System.out.println("Setting configuration provider...");
        DecisionEngineCP3.setConfigurationProvider(cs);
        
		String currentConfStr=cs.configurationToPrismConstants(theArgs.configuration);	
		System.out.println("Configuration: "+currentConfStr);
     	cs.generateReconfigurationsFrom(currentConfStr);
		
		PrismPolicy pp;
		
		if (theArgs.target != null && theArgs.waypoint != null) {
			EnvMapNode nodeSrc = map.getNodes().get(theArgs.waypoint);
			EnvMapNode nodeTgt = map.getNodes().get(theArgs.target);
			generatePath(theArgs, out_dir_ig, out_dir_wp, map, cs, currentConfStr, nodeSrc, nodeTgt);
		}
		else {			
			// Compute IGs/Paths
			for (EnvMapNode node_src : map.getNodes().values()) {
				for (EnvMapNode node_tgt : map.getNodes().values()) {
					if (node_src.getId() != node_tgt.getId()) {
						if (theArgs.waypoint != null && !Objects.equals(node_tgt.getLabel(),theArgs.waypoint) && !Objects.equals(node_src.getLabel(),theArgs.waypoint))
							continue;
						if (theArgs.append && new File(out_dir_ig, node_src.getLabel() + "_to_" + node_tgt.getLabel() + ".ig").exists()) { 
							System.out.print("Ignoring " + node_src.getLabel() + "->" + node_tgt.getLabel());
							continue;
						}
						generatePath(theArgs, out_dir_ig, out_dir_wp, map, cs, currentConfStr, node_src, node_tgt);
					}
				}
			}
		}
	}

	private static void generatePath(Arguments theArgs, String out_dir_ig, String out_dir_wp, EnvMap map,
			ConfigurationSynthesizer cs, String currentConfStr, EnvMapNode node_src, EnvMapNode node_tgt) {
		PrismPolicy pp;
		try {
		System.out.println(
				"Src:" + String.valueOf(node_src.getId()) + " Tgt:" + String.valueOf(node_tgt.getId()));
							
		DecisionEngineCP3.generateCandidates(node_src.getLabel(), node_tgt.getLabel(), true); // The last param inhibits reconfiguration tactics
		System.out.println("Scoring candidates (from configuration: "+theArgs.configuration+", index: "+cs.getConfigurationIndex(theArgs.configuration)+")...");
		DecisionEngineCP3.scoreCandidates(map, MapTranslator.ROBOT_BATTERY_RANGE_MAX, 1, cs.getConfigurationIndex(theArgs.configuration));
		System.out.println(String.valueOf(DecisionEngineCP3.m_scoreboard));	
		pp = new PrismPolicy(DecisionEngineCP3.selectPolicy());
		pp.readPolicy();  
		ArrayList<String> planArray = pp.getPlan(cs, currentConfStr);
		String plan = planArray.toString();
		System.out.println("Selected Plan: "+plan);
		PolicyToIGCP3 translator = new PolicyToIGCP3(pp, map);
		System.out.println (translator.translate (cs, currentConfStr, false));
//		            long ttc = esitmateTime(map, planArray, cs.translateId(theArgs.configuration));
		Long ttc = new Double(DecisionEngineCP3.getSelectedPolicyTime()).longValue(); 
		double w = getInitialRotation(node_src, pp, map);
		
		exportIGTranslation(out_dir_ig + "/" + node_src.getLabel() + "_to_" + node_tgt.getLabel() + ".ig",
				translator.translate(cs, currentConfStr, false));
		String wJson = generateJSONWayPointList(pp, String.valueOf(ttc), w);
		System.out.println(wJson);
		exportIGTranslation(out_dir_wp + "/" + node_src.getLabel() + "_to_" + node_tgt.getLabel() + ".json",
				wJson);
		}
		catch (Throwable t) {
			System.out.println("Failed to generate path from " + node_src.getId() + "->" + node_tgt.getId());
		}
	}

	private static long esitmateTime(EnvMap map, ArrayList<String> planArray, String translateId) {
		Pattern pattern = Pattern.compile("(l.*)_to_(l.*)");
		Double time = 0.0;
		for (String step : planArray) {
			Matcher matcher = pattern.matcher(step);
			if (matcher.matches()) {
				EnvMapArc arc = map.getArc(matcher.group(1), matcher.group(2));
				if (arc == null) {
					throw new NullPointerException("Null arc shouldn't happen");
				}
				time += arc.getTime(translateId);
			}
		}
		return Math.round(time);
	}
	
	
}
