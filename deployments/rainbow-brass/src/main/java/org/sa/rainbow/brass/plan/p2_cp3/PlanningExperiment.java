package org.sa.rainbow.brass.plan.p2_cp3;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;

import org.acmestudio.acme.element.IAcmeSystem;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.sa.rainbow.brass.PropertiesConnector;
import org.sa.rainbow.brass.adaptation.PrismPolicy;
import org.sa.rainbow.brass.confsynthesis.ConfigurationSynthesizer;
import org.sa.rainbow.brass.confsynthesis.ReconfSynthReal;
import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.ICP3ModelAccessor;
import org.sa.rainbow.brass.model.p2_cp3.acme.TurtlebotModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionStateModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowStateModelInstance;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState.Sensors;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotStateModelInstance;
import org.sa.rainbow.brass.plan.p2.MapTranslator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.impl.action.StoreTrueArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;

public class PlanningExperiment {

	static enum Perturbations {
		NODE, SENSOR, NODEANDSENSOR, NONE
	};

	class PERS extends CP3RobotState {
		private boolean m_dark;

		public PERS(String config) {
			super(null);
			String sens = getSensorFromConfig(config);
			switch (sens) {
			case "kinect":
				setSensor(Sensors.KINECT, true);
				break;
			case "lidar":
				setSensor(Sensors.LIDAR, true);
				break;
			case "camera":
				setSensor(Sensors.CAMERA, true);
			}

		}

	}

	class PETBMI extends TurtlebotModelInstance {
		Set<String> components = new HashSet<>();
		Set<String> failedComponents = new HashSet<>();
		Set<String> allComponents = new HashSet<>(Arrays.asList(new String[] { "amcl", "map_server", "mrpt",
				"marker_pose_publisher", "map_server_obs", "laserScan_nodelet", "aruco_marker_publisher_front" }));

		public PETBMI(String config) {
			super(null, "local");
			String loc = getLocalizationComponent(config);
			switch (loc) {
			case "amcl":
				components.add("amcl");
				components.add("map_server");
				break;
			case "mrpt":
				components.add("mrpt");
				components.add("map_server");
				break;
			case "aruco":
				components.add("marker_pose_publisher");
				components.add("map_server_obs");
			}
			String sens = getSensorFromConfig(config);
			switch (sens) {
			case "kinect":
				components.add("laserScan_nodelet");
				break;
			case "camera":
				components.add("aruco_marker_publisher_front");
				break;
			}
		}
		
		@Override
		public void setModelInstance(IAcmeSystem model) {
		//	super.setModelInstance(model);
			// Do nothing because we don't have a real system
		}

		public void failComponent(String comp) {
			failedComponents.add(comp);
			components.remove(comp);
		}

		public java.util.Collection<String> getFailedComponents() {
			return failedComponents;
		}

		@Override
		public Collection<String> getActiveComponents() {
			return components;
		}

		@Override
		public Collection<String> getInactiveComponents() {
			Set<String> ia = new HashSet<>(allComponents);
			ia.removeAll(components);
			ia.removeAll(failedComponents);
			return ia;
		}
	};

	static class PEModelAccessor implements ICP3ModelAccessor {

		EnvMapModelInstance m_envMap;
		private TurtlebotModelInstance m_tb;
		private CP3RobotStateModelInstance m_rs;

		public PEModelAccessor(EnvMap m, TurtlebotModelInstance tb, CP3RobotStateModelInstance rs) {
			m_envMap = new EnvMapModelInstance(m, null);
			m_tb = tb;
			m_rs = rs;

		}

		@Override
		public EnvMapModelInstance getEnvMapModel() {
			return m_envMap;
		}

		@Override
		public InstructionGraphModelInstance getInstructionGraphModel() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public MissionStateModelInstance getMissionStateModel() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public RainbowStateModelInstance getRainbowStateModel() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CP3RobotStateModelInstance getRobotStateModel() {
			return m_rs;
		}

		@Override
		public TurtlebotModelInstance getTurtlebotModel() {
			return m_tb;
		}

	}

	private String m_target;
	private String m_source;
	private EnvMap m_envMap;
	private ArrayList<String> m_path;
	private String m_perturbLocation;
	private Perturbations m_perturbAction;
	private String m_startConfig;
	private Properties m_props;
	private boolean m_dark;
	private boolean m_singleModel;

	public PlanningExperiment() {

	}

	private static class Arguments {

		@Arg(dest = "properties")
		public String properties;

		@Arg(dest = "start_config")
		public String startConfiguration;

		@Arg(dest = "perturb_config")
		public String perturbConfiguration;

		@Arg(dest = "map")
		public String map;
		
		@Arg(dest = "single_model")
		public boolean singleModel;
	}

	public static void main(String[] args) throws Exception {
		ArgumentParser parser = ArgumentParsers.newFor("prog").build()
				.description("Run the planner on the specified testcase");
		parser.addArgument("-p", "--properties").metavar("FILE").type(String.class)
				.help("The file containing properties/environment variables for PRISM etc.");
		parser.addArgument("-s", "--single_model").action(new StoreTrueArgumentAction()).help("Use a single model for model checking. (Be careful of state explosion.)");
		parser.addArgument("map").metavar("FILE").type(String.class).help("The map containing the waypoints");
		parser.addArgument("start_config").type(String.class)
				.help("The JSON file specifying the start configuration of the robot");
		parser.addArgument("perturb_config").type(String.class)
				.help("The JSON file specifying experiment configuration");

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
		} else {
			props = PropertiesConnector.DEFAULT;
		}
		props.setProperty(PropertiesConnector.MAP_PROPKEY, theArgs.map);
		JsonParser p = new JsonParser();
		JsonObject startConfig = (JsonObject) p.parse(new FileReader(new File(theArgs.startConfiguration)));
		JsonObject perturbConfig = (JsonObject) p.parse(new FileReader(new File(theArgs.perturbConfiguration)));

		PlanningExperiment exp = new PlanningExperiment();
		exp.m_target = startConfig.get("target-loc").getAsString();
		exp.m_source = startConfig.get("start-loc").getAsString();
		exp.m_envMap = new EnvMap(null, props);
		exp.m_startConfig = startConfig.get("start-configuration").getAsString();
		exp.m_props = props;
		exp.m_singleModel = theArgs.singleModel;

		/*
		 * { "path" : ["l1", "l2", "l3", "l4"] // the path starting out on "perturb-loc"
		 * : "l2" // the location to perturb "perturb" : "node" // what to perturb, one
		 * of {"node", "sensor", "nodeandsensor"} "dark" : true // is it dark at the
		 * location "fix-paths" : bool // whether to fix paths "max-reconfs" : int //
		 * how many reconfigs to allow in a plan "cost" : bool // whether to consider
		 * cost} "balanced" : boolean // whether to use a more balanced utilityfunction
		 */

		exp.m_path = new ArrayList<String>();
		perturbConfig.get("path").getAsJsonArray().forEach(new Consumer<JsonElement>() {
			@Override
			public void accept(JsonElement t) {
				exp.m_path.add(t.getAsString());
			}
		});
		exp.m_perturbLocation = perturbConfig.get("perturb-loc").getAsString();
		exp.m_perturbAction = Perturbations.valueOf(perturbConfig.get("perturb").getAsString().toUpperCase());
		exp.m_dark = perturbConfig.get("dark").getAsBoolean();

		exp.m_props.setProperty("rainbow.fix_path", Boolean.toString(perturbConfig.get("fix-paths").getAsBoolean()));
		exp.m_props.setProperty("rainbow.consider_cost", Boolean.toString(perturbConfig.get("cost").getAsBoolean()));
		exp.m_props.setProperty("rainbow.balanced_utility",
				Boolean.toString(perturbConfig.get("balanced").getAsBoolean()));
		exp.m_props.setProperty("rainbow.max_reconfs", Integer.toString(perturbConfig.get("max-reconfs").getAsInt()));
		if (perturbConfig.has("success-threshold")) {
			
			double st = perturbConfig.get("success-threshold").getAsDouble();
			System.out.println("Setting success threshold to " + st);
			exp.m_props.setProperty("rainbow.success_threshold",Double.toString(st));
		}

		DecisionEngineCP3.init(exp.m_props);
		System.out.println("Success threshold is " + org.sa.rainbow.brass.plan.p2.MapTranslator.TRAVERSAL_SUCCESS_THRESHOLD);
		String func = startConfig.get("utility-function").getAsString();
		switch (func) {
		case "favor-timeliness":
			DecisionEngineCP3.setTimelinessPreference();
			break;
		case "favor-efficiency":
			DecisionEngineCP3.setEnergyPreference();
			break;
		case "favor-safety":
			DecisionEngineCP3.setSafetyPreference();
			break;
		}
		exp.run();

	}

	private void run() throws Exception {
		final Set<String> unusableLocalization = new HashSet<>();
		final Set<Sensors> unusableSensors = new HashSet<>();
		switch (m_perturbAction) {
		case NODE:
			unusableLocalization.add(getLocalizationComponent(m_startConfig));
			break;
		case SENSOR:
			unusableSensors.add(Sensors.valueOfIgnoreCase(getSensorFromConfig(m_startConfig)));
			break;
		case NODEANDSENSOR:
			unusableLocalization.add(getLocalizationComponent(m_startConfig));
			unusableSensors.add(Sensors.valueOfIgnoreCase(getSensorFromConfig(m_startConfig)));
		}

//		DecisionEngineCP3.init(m_props);
		DecisionEngineCP3.setMap(m_envMap);
		ConfigurationSynthesizer configurationSynthesizer = new ConfigurationSynthesizer(m_props);
		configurationSynthesizer.populate();
		DecisionEngineCP3.setConfigurationProvider(configurationSynthesizer);
		CP3RobotStateModelInstance rsmi = new CP3RobotStateModelInstance(new PERS(m_startConfig), null);
		rsmi.getModelInstance().setIllumination(m_dark ? 10 : 255);
		PEModelAccessor models = new PEModelAccessor(m_envMap, new PETBMI(m_startConfig), rsmi);
		long startTime = new Date().getTime();
		
		ReconfSynthReal reconfSynth = new ReconfSynthReal(models);

		// Find valid configurations
		String confInitString = reconfSynth.getCurrentConfigurationInitConstants(unusableLocalization, unusableSensors);
		configurationSynthesizer.generateReconfigurationsFrom(confInitString);
		double timeForReconfigurations = (new Date().getTime() - startTime)/1000.0;
		if (DecisionEngineCP3.do_not_change_paths) {
			Stack<String> currentPath = new Stack<>();
			int pi = m_path.indexOf(m_perturbLocation);
			for (int i = pi + 1; i < m_path.size(); i++) {
				currentPath.push(m_path.get(i));
			}
			DecisionEngineCP3.generateCandidates(currentPath, !m_singleModel);
		} else {
			DecisionEngineCP3.generateCandidates(m_perturbLocation, m_target, !m_singleModel);
		}
		double timeForCandidates = (new Date().getTime() - startTime)/1000.0 - timeForReconfigurations;
		DecisionEngineCP3.scoreCandidates(m_envMap, 180000, 1);
		double timeForScore = (new Date().getTime() - startTime)/1000.0 - timeForReconfigurations - timeForCandidates;
		long endTime = new Date().getTime();
		JSONObject results = new JSONObject();
		if (DecisionEngineCP3.m_scoreboard.isEmpty()) {
			results.put("error", "Failed to find a plan");
			System.out.println("Failed to find a plan");
		} else {
			PrismPolicy pp = new PrismPolicy(DecisionEngineCP3.selectPolicy());
			pp.readPolicy();
			ArrayList<String> plan = pp.getPlan(configurationSynthesizer, confInitString);
			PolicyToIGCP3 translator = new PolicyToIGCP3(pp,m_envMap);
			String translate = translator.translate(configurationSynthesizer, confInitString);
			
			JSONArray pA = new JSONArray();
			for (String s : plan) {
				pA.add(s);
			}
			results.put("plan", pA);
			JSONObject time = new JSONObject();
			time.put("total", (endTime - startTime)/1000.0);
			time.put("generate-reconfigs", timeForReconfigurations);
			time.put("generated-candidates", timeForCandidates);
			time.put("generate-plan", timeForScore);
			results.put("time", time);
			results.put("considered-cost", MapTranslator.CONSIDER_RECONFIGURATION_COST);
			results.put("used-fixed-plan", DecisionEngineCP3.do_not_change_paths);
			String table = DecisionEngineCP3.export(models.getEnvMapModel().getModelInstance());
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(
					System.getProperty("user.home") + "/logs/selected_policy.tex"))) {
				writer.write(table);
			} catch (Exception e) {
			}
			
		}
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(m_props.getProperty("rainbow.plan_loc")))) {
			writer.write(results.toJSONString());
		} catch (Exception e) {
		}
		System.exit(0);

	}

	private String getSensorFromConfig(String startConfig) {
		return startConfig.split("-")[1];

	}

	private String getLocalizationComponent(String startConfig) {
		return startConfig.split("-")[0];
	}

}
