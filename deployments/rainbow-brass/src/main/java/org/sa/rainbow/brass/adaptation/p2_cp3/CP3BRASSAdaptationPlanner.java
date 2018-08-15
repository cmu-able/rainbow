package org.sa.rainbow.brass.adaptation.p2_cp3;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.sa.rainbow.brass.adaptation.BrassPlan;
import org.sa.rainbow.brass.adaptation.NewInstructionGraph;
import org.sa.rainbow.brass.adaptation.PrismPolicy;
import org.sa.rainbow.brass.confsynthesis.ConfigurationSynthesizer;
import org.sa.rainbow.brass.confsynthesis.ReconfSynthReal;
import org.sa.rainbow.brass.das.BRASSHttpConnector;
import org.sa.rainbow.brass.das.IBRASSConnector.DASPhase2StatusT;
import org.sa.rainbow.brass.das.IBRASSConnector.Phases;
import org.sa.rainbow.brass.model.instructions.IInstruction;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress;
import org.sa.rainbow.brass.model.instructions.MoveAbsHInstruction;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.p2_cp3.CP3ModelAccessor;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState.Heading;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState.LocationRecording;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState.UtilityPreference;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowState.CP3ModelState;
import org.sa.rainbow.brass.model.p2_cp3.robot.CP3RobotState.Sensors;
import org.sa.rainbow.brass.plan.p2.MapTranslator;
import org.sa.rainbow.brass.plan.p2_cp3.DecisionEngineCP3;
import org.sa.rainbow.brass.plan.p2_cp3.PolicyToIGCP3;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.adaptation.AdaptationTree;
import org.sa.rainbow.core.adaptation.DefaultAdaptationTreeWalker;
import org.sa.rainbow.core.adaptation.IAdaptationManager;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.IRainbowAdaptationEnqueuePort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;

public class CP3BRASSAdaptationPlanner extends AbstractRainbowRunnable implements IAdaptationManager<BrassPlan> {

	public static final String NAME = "BRASS Adaptation Planner";
	Logger LOGGER = Logger.getLogger("AdaptationManager");
	// The thread "sleep" time. runAction will be called every 10 seconds in this
	// case
	public static final int SLEEP_TIME = 10000 /* ms */;
	private IModelsManagerPort m_modelsManagerPort;
	private IModelChangeBusSubscriberPort m_modelChangePort;
	private CP3ModelAccessor m_models;
	private ConfigurationSynthesizer m_configurationSynthesizer;
	private ModelReference m_modelRef;
	private IRainbowAdaptationEnqueuePort<BrassPlan> m_adaptationEnqueuePort;
	private boolean m_executingPlan = false;
	private boolean m_adaptationEnabled = true;
	private boolean m_errorDetected = false;

	private ReconfSynthReal m_reconfSynth;

	/**
	 * Default Constructor with name for the thread.
	 */
	public CP3BRASSAdaptationPlanner() {
		super(NAME);
		String per = Rainbow.instance().getProperty(RainbowConstants.PROPKEY_MODEL_EVAL_PERIOD);
		if (per != null) {
			setSleepTime(Long.parseLong(per));
		} else {
			setSleepTime(SLEEP_TIME);
		}
	}

	@Override
	public void initialize(IRainbowReportingPort port) throws RainbowConnectionException {
		super.initialize(port);
		initConnectors();
		try {
			DecisionEngineCP3.init(Rainbow.instance().allProperties());
			DecisionEngineCP3.setMap(m_models.getEnvMapModel().getModelInstance());
			m_configurationSynthesizer = new ConfigurationSynthesizer(Rainbow.instance().allProperties());
			m_configurationSynthesizer.populate();
			DecisionEngineCP3.setConfigurationProvider(m_configurationSynthesizer);
			DecisionEngineCP3.LOGGER = LOGGER;
			m_reconfSynth = new ReconfSynthReal(m_models);
			m_reconfSynth.LOGGER = LOGGER;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RainbowConnectionException("Cannot initialize DecisionEngine", e);
		}
	}

	private void initConnectors() throws RainbowConnectionException {
		// Create port to query models manager
		m_modelsManagerPort = RainbowPortFactory.createModelsManagerRequirerPort();
		m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort();
		m_models = new CP3ModelAccessor(m_modelsManagerPort);
		// If you want to listen to changes, then you need to create a modelChangePort
		// and write a subscriber to it.
		// See org.sa.rainbow.stitch.AdaptationManger for an example of this

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setModelToManage(ModelReference modelRef) {
		m_modelRef = modelRef;
		// Create a port to send any plans on (AdaptationTree<BrassPlan>) that will be
		// sent to an executor
		m_adaptationEnqueuePort = RainbowPortFactory.createAdaptationEnqueuePort(modelRef);

	}

	@Override
	public void markStrategyExecuted(AdaptationTree<BrassPlan> plan) {
		// Insert code here to record when a plan has been executed by the execution
		// manager
		// Possible things to do:
		// (a) keep a history of plan success
		// (b) start listening to model events to generate new plans again

		AdaptationResultsVisitor v = new AdaptationResultsVisitor(plan);
		plan.visit(v);
		if (v.m_allOk) {
			log("Adaptation was successfully deployed");
			BRASSHttpConnector.instance(Phases.Phase2).reportStatus(DASPhase2StatusT.ADAPTED.name(),
					"Finished adapting the system");
		} else {
			log("Adaptation was unsuccessfully deployed");
			BRASSHttpConnector.instance(Phases.Phase2).reportStatus(DASPhase2StatusT.ADAPTED_FAILED.name(),
					"Something in the adaptation plan failed to execute.");
		}
		try {
			// Wait for IG to come to Analyzer
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}
		m_executingPlan = false;
	}

	@Override
	public void setEnabled(boolean enabled) {
		m_adaptationEnabled = enabled;

	}

	@Override
	public boolean isEnabled() {
		return m_adaptationEnabled;

	}

	@Override
	protected void log(String txt) {
		m_reportingPort.info(RainbowComponentT.ADAPTATION_MANAGER, txt, LOGGER);

	}

	String reportErrors() {
		StringBuffer sb = new StringBuffer();
		for (CP3ModelState s : m_models.getRainbowStateModel().getModelInstance().getProblems()) {
			sb.append(s);
			sb.append(" ");
		}
		return sb.toString();
	}

	@Override
	protected void runAction() {
		try {
			InstructionGraphProgress igModel = m_models.getInstructionGraphModel().getModelInstance();
			if (igModel.getInstructions().isEmpty() || m_models.getRainbowStateModel().getModelInstance().waitForIG())
				return;
			DecisionEngineCP3.LOGGER = LOGGER;
			if (m_adaptationEnabled && reallyHasError() && !m_executingPlan) {
				String message = "Detected problems: " + reportErrors();
				BRASSHttpConnector.instance(Phases.Phase2).reportStatus(DASPhase2StatusT.ADAPTING.name(), message);
				BRASSHttpConnector.instance(Phases.Phase2).reportStatus(DASPhase2StatusT.ADAPTING.name(), message);
				log(message);
				m_errorDetected = false;
				m_reportingPort.info(getComponentType(), "Determining an appropriate adaptation");
				BRASSHttpConnector.instance(Phases.Phase2).reportStatus(DASPhase2StatusT.ADAPTING.name(), "params: fix_paths =" 
				+ DecisionEngineCP3.do_not_change_paths 
				+ ", consider_cost=" + MapTranslator.CONSIDER_RECONFIGURATION_COST 
				+ ", rc/plan=" + MapTranslator.ROBOT_MAX_RECONF_VAL 
				+ ", balanced_utility=" + DecisionEngineCP3.choose_balanced_utilty);
				// DecisionEngineCP3.setMap(m_models.getEnvMapModel().getModelInstance());

				// 1. Determine string for intialization of the planner
				Set<String> unusableLocalization = new HashSet<>();
				Set<Sensors> unusableSensors = new HashSet<>();
				EnumSet<CP3ModelState> knownConfigErrors = EnumSet.of(CP3ModelState.ARCHITECTURE_ERROR,
						CP3ModelState.CONFIGURATION_ERROR);
				String confInitString;
				if (containsAnyOfTheseErrors(knownConfigErrors)) {
					confInitString = determineValidReconfigurations(unusableLocalization, unusableSensors);
				} else if (m_models.getRainbowStateModel().getModelInstance().getProblems()
						.contains(CP3ModelState.TOO_DARK)
						&& m_models.getTurtlebotModel().getActiveComponents().contains("marker_pose_publisher")) {
					ConfigurationSynthesizer.enableOnlyDarkConfigs();
					confInitString = determineValidReconfigurations(unusableLocalization, unusableSensors);
				} else if (m_models.getRainbowStateModel().getModelInstance().getProblems()
						.contains(CP3ModelState.INSTRUCTION_GRAPH_FAILED)) {
					// The instruction graph failed for some unknown reason, try another
					// configuration
					Collection<String> activeComponents = m_models.getTurtlebotModel().getActiveComponents();
					if (activeComponents.contains("amcl"))
						unusableLocalization.add("amcl");
					else if (activeComponents.contains("mrpt"))
						unusableLocalization.add("mrpt");
					else if (activeComponents.contains("marker_pose_publisher"))
						unusableLocalization.add("marker_pose_publisher");
					EnumSet<Sensors> activeSensors = m_models.getRobotStateModel().getModelInstance().getSensors();
					unusableSensors.addAll(activeSensors);
					confInitString = determineValidReconfigurations(unusableLocalization, unusableSensors);
				} else {
					log("Error: Unknown error and unknown configuration. This will probably cause a loop");
					confInitString = determineValidReconfigurations(unusableLocalization, unusableSensors);
				}

				EnvMap copy = m_models.getEnvMapModel()
						.getModelInstance()/*
											 * .copy() causes potential failure in IGWaypointAnaluzyzer which needs to
											 * know about waypoints
											 */;
				DecisionEngineCP3.setMap(copy);
				UtilityPreference preference = m_models.getMissionStateModel().getModelInstance()
						.getUtilityPreference();
				if (preference != null) {
					switch (preference) {
					case FAVOR_EFFICIENCY:
						DecisionEngineCP3.setEnergyPreference();
						break;
					case FAVOR_TIMELINESS:
						DecisionEngineCP3.setTimelinessPreference();
						break;
					case FAVOR_SAFETY:
						DecisionEngineCP3.setSafetyPreference();
						break;
					}
				}
				// Insert a node where the robot is

				IInstruction ci = igModel.getCurrentInstruction();
				LocationRecording cp = m_models.getMissionStateModel().getModelInstance().getCurrentPose();
				String srcLabel = null;
				if (ci instanceof MoveAbsHInstruction) {
					MoveAbsHInstruction mi = (MoveAbsHInstruction) ci;
					srcLabel = copy.getNextNodeId();
					srcLabel = copy.insertNode(srcLabel, mi.getSourceWaypoint(), mi.getTargetWaypoint(), cp.getX(),
							cp.getY(), false);
				} else {
					List<? extends IInstruction> remainingInstructions = igModel.getRemainingInstructions();
					for (Iterator iterator = remainingInstructions.iterator(); iterator.hasNext()
							&& !(ci instanceof MoveAbsHInstruction);) {
						ci = (IInstruction) iterator.next();
					}
					if (ci instanceof MoveAbsHInstruction) {
						MoveAbsHInstruction mi = (MoveAbsHInstruction) ci;
						srcLabel = mi.getSourceWaypoint();
					} else {
						m_reportingPort.error(getComponentType(),
								"There are no move instructions left -- the last instruction in an instruction graph for BRASS should always be a move",
								LOGGER);
						m_reportingPort.error(getComponentType(),
								"This is the graph: " + Arrays.toString(igModel.getInstructions().toArray()));
					}
				}
				String tgt = m_models.getMissionStateModel().getModelInstance().getTargetWaypoint();
				if (DecisionEngineCP3.do_not_change_paths) {
					Stack<String> currentPath = new Stack<>();
					currentPath.push(srcLabel);
					List<? extends IInstruction> remainingInstructions = igModel.getRemainingInstructions();
					for (IInstruction inst : remainingInstructions) {
						if (inst instanceof MoveAbsHInstruction) {
							MoveAbsHInstruction i = (MoveAbsHInstruction) inst;
							if (!currentPath.contains(i.getSourceWaypoint()))
								currentPath.push(i.getSourceWaypoint());	
						}
					}
					currentPath.push(tgt);
					log("---> using path " + currentPath.toString());
					DecisionEngineCP3.generateCandidates(currentPath);
					BRASSHttpConnector.instance(Phases.Phase2).reportStatus(DASPhase2StatusT.ADAPTING.name(), "Using path " + DecisionEngineCP3.m_candidates.keySet().iterator().next().toString());

				} else {
					log("Generating candidate paths from " + srcLabel + " to " + tgt);

					DecisionEngineCP3.generateCandidates(srcLabel, tgt);
					log("---> found " + DecisionEngineCP3.m_candidates.size());
					BRASSHttpConnector.instance(Phases.Phase2).reportStatus(DASPhase2StatusT.ADAPTING.name(),
							"Found " + DecisionEngineCP3.m_candidates.size() + " valid paths");
				}
				// DecisionEngineCP3.generateCandidates("l1", "l8");
				try {
					AdaptationTree<BrassPlan> at = scoreAndGeneratePlan(confInitString, copy);
					log("Found a plan");
					m_executingPlan = true;
					m_models.getRainbowStateModel().getModelInstance().m_waitForIG = true;

					m_adaptationEnqueuePort.offerAdaptation(at, new Object[] {});
				} catch (Throwable e) {
					e.printStackTrace();
					m_reportingPort.error(getComponentType(), "Failed to find a plan " + e.getMessage(), LOGGER);
					BRASSHttpConnector.instance(Phases.Phase2).reportStatus(DASPhase2StatusT.ADAPTED_FAILED.name(),
							"Did not find a plan");
					BRASSHttpConnector.instance(Phases.Phase2).reportDone(true, "No plan was found");
				}

			}
		} catch (Throwable e) {
			e.printStackTrace();
			m_reportingPort.error(getComponentType(),
					"The BRASS Adaptation Planner threw an exception: " + e.getMessage(), LOGGER);
			BRASSHttpConnector.instance(Phases.Phase2).reportStatus(DASPhase2StatusT.ADAPTED_FAILED.name(),
					"Did not find a plan due to exception: " + e.getMessage());
			BRASSHttpConnector.instance(Phases.Phase2).reportDone(true, "No plan was found");
			m_adaptationEnabled = false;
		} finally {
			ConfigurationSynthesizer.restoreAllConfigs();
		}

	}

	private AdaptationTree<BrassPlan> scoreAndGeneratePlan(String confInitString, EnvMap copy) throws Exception {
		try {
			DecisionEngineCP3.scoreCandidates(copy,
					Math.round(m_models.getRobotStateModel().getModelInstance().getCharge()),
					Heading.convertFromRadians(
							m_models.getMissionStateModel().getModelInstance().getCurrentPose().getRotation())
							.ordinal());
			if (DecisionEngineCP3.m_scoreboard.isEmpty()) {
				throw new RainbowException("Failed to find a plan for " + confInitString);
			}
			String table = DecisionEngineCP3.export(m_models.getEnvMapModel().getModelInstance());
			long curTime = new Date().getTime();
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(
					System.getProperty("user.home") + "/logs/selected_policy" + curTime + ".tex"))) {
				writer.write(table);
			} catch (Exception e) {
			}
			PrismPolicy pp = new PrismPolicy(DecisionEngineCP3.selectPolicy());
			pp.readPolicy();
			ArrayList<String> planArray = pp.getPlan(m_configurationSynthesizer, confInitString);
			if (planArray.isEmpty()) {
				throw new RainbowException("Failed to find a plan for " + confInitString);
			}
			String plan = planArray.toString();
			m_reportingPort.info(getComponentType(), "Planner chooses the plan " + plan, LOGGER);
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(
					System.getProperty("user.home") + "/logs/selected_plan_" + curTime + ""))) {
				writer.write(plan.toString());
			} catch (Exception e) {
			} 
			ArrayList<String> planToTA = new ArrayList<String>(planArray.size());
			ArrayList<String> planToReport = new ArrayList<>(planArray.size());
			for (String cmd : planArray) {
				if (cmd.contains("_to_")) {
					Pattern p = Pattern.compile("([^_]*)_to_(.*)");
					Matcher m = p.matcher(cmd);
					if (m.matches()) {
						planToTA.add(m.group(2));
						planToReport.add(m.group(2));
					}

				} else {
					planToReport.add(cmd);
				}
			}
			BRASSHttpConnector.instance(Phases.Phase2).reportNewPlan(planToTA);
			BRASSHttpConnector.instance(Phases.Phase2).reportStatus(DASPhase2StatusT.ADAPTING.name(),
					"Chose plan: " + planToReport.toString());
			PolicyToIGCP3 translator = new PolicyToIGCP3(pp, copy);
			String translate = translator.translate(m_configurationSynthesizer, confInitString);

			BrassPlan nig = new NewInstructionGraph(m_models, translate);
			AdaptationTree<BrassPlan> at = new AdaptationTree<>(nig);
			return at;
		} catch (Throwable t) {
			throw t;
		}
	}

	private String determineValidReconfigurations(Set<String> unusableLocalization, Set<Sensors> unusableSensors) {
		String confInitString = m_reconfSynth.getCurrentConfigurationInitConstants(unusableLocalization,
				unusableSensors);
		try {
			log("Looking for reconfigurations from: " + confInitString);
			m_configurationSynthesizer.generateReconfigurationsFrom(confInitString);
			log("----> found " + m_configurationSynthesizer.m_reconfigurations.size() + "("
					+ m_configurationSynthesizer.getNumberOfValidReconfigurations() + " not empty)");
			StringBuffer nonempty = new StringBuffer();
			for (Entry<String, List<String>> e : m_configurationSynthesizer.m_reconfigurations.entrySet()) {
				if (!e.getValue().isEmpty())
					nonempty.append(m_configurationSynthesizer.translateId(e.getKey()) + " ");
			}
			BRASSHttpConnector.instance(Phases.Phase2).reportStatus(DASPhase2StatusT.ADAPTING.name(),
					"Found " + m_configurationSynthesizer.getNumberOfValidReconfigurations()
							+ " valid reconfigurations: " + nonempty.toString());
		} catch (RainbowException e1) {
			e1.printStackTrace();
			m_reportingPort.error(getComponentType(), "Could not synthesize configurations " + e1.getMessage(), LOGGER);
			return confInitString;

		}
		return confInitString;
	}

	private boolean reallyHasError() {
		EnumSet<CP3ModelState> realErrors = EnumSet.of(CP3ModelState.ARCHITECTURE_ERROR,
				CP3ModelState.CONFIGURATION_ERROR, CP3ModelState.INSTRUCTION_GRAPH_FAILED);
		if (containsAnyOfTheseErrors(realErrors))
			return true;
		if (m_models.getRainbowStateModel().getModelInstance().getProblems().contains(CP3ModelState.TOO_DARK)
				&& m_models.getTurtlebotModel().getActiveComponents().contains("marker_pose_publisher")) {
			return true;
		}
		return false;
	}

	private boolean containsAnyOfTheseErrors(EnumSet<CP3ModelState> errors) {
		for (CP3ModelState state : errors) {
			if (m_models.getRainbowStateModel().getModelInstance().getProblems().contains(state))
				return true;
		}
		return false;
	}

	@Override
	public RainbowComponentT getComponentType() {
		return RainbowComponentT.ADAPTATION_MANAGER;

	}

	public static String DUMMY_ALTERNATE_IG = "P(V(1, do MoveAbs (19.5,69,1) then 2),V(2, do MoveAbs (19.5,59,1) then 3)::V(3, do Move (42.5, 59, 0) then 4)::V(4, end)::nil)";

	private class AdaptationResultsVisitor extends DefaultAdaptationTreeWalker<BrassPlan> {

		public AdaptationResultsVisitor(AdaptationTree<BrassPlan> adt) {
			super(adt);
		}

		boolean m_allOk = true;

		@Override
		protected void evaluate(BrassPlan adaptation) {
			m_allOk &= adaptation.getOutcome();
		}

	}

}
