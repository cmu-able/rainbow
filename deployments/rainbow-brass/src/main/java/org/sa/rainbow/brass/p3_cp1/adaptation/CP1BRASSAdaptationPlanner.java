package org.sa.rainbow.brass.p3_cp1.adaptation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sa.rainbow.brass.PropertiesConnector;
import org.sa.rainbow.brass.adaptation.BrassPlan;
import org.sa.rainbow.brass.adaptation.NewInstructionGraph;
import org.sa.rainbow.brass.adaptation.PrismConnectorAPI;
import org.sa.rainbow.brass.adaptation.PrismPolicy;
import org.sa.rainbow.brass.confsynthesis.SimpleConfigurationStore;
import org.sa.rainbow.brass.das.BRASSHttpConnector;
import org.sa.rainbow.brass.das.IBRASSConnector.DASPhase2StatusT;
import org.sa.rainbow.brass.das.IBRASSConnector.Phases;
import org.sa.rainbow.brass.model.instructions.IInstruction;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress.IGExecutionStateT;
import org.sa.rainbow.brass.model.instructions.MoveAbsHInstruction;
import org.sa.rainbow.brass.model.instructions.SetExecutionFailedCmd;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.p3_cp1.model.CP1ModelAccessor;
import org.sa.rainbow.brass.p3_cp1.model.power.PowerModelCommandFactory;
import org.sa.rainbow.brass.p3_cp1.model.power.UpdatePowerModelCmd;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState.Heading;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState.LocationRecording;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowState.CP3ModelState;
import org.sa.rainbow.brass.plan.p2.MapTranslator;
import org.sa.rainbow.brass.p3_cp1.plan.DecisionEngineCP1;
import org.sa.rainbow.brass.p3_cp1.plan.PolicyToIGCP1;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.adaptation.AdaptationTree;
import org.sa.rainbow.core.adaptation.DefaultAdaptationTreeWalker;
import org.sa.rainbow.core.adaptation.IAdaptationManager;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.IRainbowAdaptationEnqueuePort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;

public class CP1BRASSAdaptationPlanner extends AbstractRainbowRunnable implements IAdaptationManager<BrassPlan> {

	public static final String NAME = "BRASS Adaptation Planner";
	// The thread "sleep" time. runAction will be called every 10 seconds in this
	// case
	public static final int SLEEP_TIME = 10000 /* ms */;
	private IModelsManagerPort m_modelsManagerPort;
	private IModelChangeBusSubscriberPort m_modelChangePort;
	private CP1ModelAccessor m_models;
	private SimpleConfigurationStore m_configurationStore;
	private ModelReference m_modelRef;
	private IRainbowAdaptationEnqueuePort<BrassPlan> m_adaptationEnqueuePort;
	private boolean m_executingPlan = false;
	private boolean m_adaptationEnabled = true;
	private boolean m_errorDetected = false;
	private boolean m_inLastResort = false;
	private boolean m_reportAdapted;
	
	

	/**
	 * Default Constructor with name for the thread.
	 */
	public CP1BRASSAdaptationPlanner() {
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
			DecisionEngineCP1.init(Rainbow.instance().allProperties());
			DecisionEngineCP1.m_real_observed_battery_ratio=0.9;
			DecisionEngineCP1.setMap(m_models.getEnvMapModel().getModelInstance());
			m_configurationStore = new SimpleConfigurationStore(Rainbow.instance().allProperties());
			m_configurationStore.populate();
			DecisionEngineCP1.setConfigurationProvider(m_configurationStore);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RainbowConnectionException("Cannot initialize DecisionEngine", e);
		}
	}

	private void initConnectors() throws RainbowConnectionException {
		// Create port to query models manager
		m_modelsManagerPort = RainbowPortFactory.createModelsManagerRequirerPort();
		m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort();
		m_modelChangePort.subscribe((message) -> {
			String operation = (String )message.getProperty(IModelChangeBusPort.COMMAND_PROP);
			return (PowerModelCommandFactory.UPDATE_POWER_MODEL_CMD.equals(operation));
		}, (model, message) -> {
			synchronized(CP1BRASSAdaptationPlanner.this) {
				this.notifyAll();
			}
		});
		m_models = new CP1ModelAccessor(m_modelsManagerPort);
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
		try {
			plan.visit(v);
		} catch (RainbowException e1) {
			// TODO Auto-generated catch block
			log(e1.getMessage());
		}
		if (m_reportAdapted) {
			if (v.m_allOk) {
				BRASSHttpConnector.instance(Phases.Phase2).reportStatus(DASPhase2StatusT.ADAPTED.name(),
						"Finished adapting the system");
			} else {
				BRASSHttpConnector.instance(Phases.Phase2).reportStatus(DASPhase2StatusT.ADAPTED_FAILED.name(),
						"Something in the adaptation plan failed to execute.");
			}
			m_reportAdapted = false;
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
		m_reportingPort.info(RainbowComponentT.ADAPTATION_MANAGER, txt);

	}
	
	protected void waitForPowerModel() {
		// Be careful with this -- stops the thread entirely
		synchronized (this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
			}
		}
	}
	
	protected void triggerOnlineLearning() {
		log("Triggering online learning");
		boolean learningHasStarted = BRASSHttpConnector.instance(Phases.Phase2).requestOnlineLearning();
		if (learningHasStarted) {
			log("Waiting for new power model");
			waitForPowerModel();
			log("Received new power model");
		}
		else {
			log("NOT waiting for new power model");
		}
	}

	@Override
	protected void runAction() {
		InstructionGraphProgress igModel = m_models.getInstructionGraphModel().getModelInstance();
		if (igModel.getInstructions().isEmpty() || m_models.getRainbowStateModel().getModelInstance().waitForIG())
			return;
		if (m_adaptationEnabled && !m_executingPlan) {
			if (reallyHasError()) {
				BRASSHttpConnector.instance(Phases.Phase2).reportStatus(DASPhase2StatusT.ADAPTING.name(),
						"Detected a problem");
				cancelInstructions();
				
				// Trigger online learning and wait for the model to be updated
				triggerOnlineLearning();
				m_errorDetected = false;
				m_reportingPort.info(getComponentType(), "Determining an appropriate adaptation");
				// DecisionEngineCP3.setMap(m_models.getEnvMapModel().getModelInstance());

				EnvMap envMap = m_models.getEnvMapModel().getModelInstance(); // This means that the obstruction will
																				// only persist for this plan
				IInstruction ci = igModel.getCurrentInstruction();
				LocationRecording cp = m_models.getMissionStateModel().getModelInstance().getCurrentPose();
				String srcLabel = null;
				String tgtLabel = null;
				if (ci instanceof MoveAbsHInstruction) {
					MoveAbsHInstruction mi = (MoveAbsHInstruction) ci;
					srcLabel = envMap.getNextNodeId();
					boolean obstructed = m_models.getRainbowStateModel().getModelInstance().getProblems()
							.contains(CP3ModelState.IS_OBSTRUCTED);
					m_reportingPort.info(getComponentType(), MessageFormat.format("Inserting obstruction btw {0} and {0}", mi.getSourceWaypoint(), mi.getTargetWaypoint()));
					srcLabel = envMap.insertNode(srcLabel, mi.getSourceWaypoint(), mi.getTargetWaypoint(), cp.getX(),
							cp.getY(), obstructed);
					tgtLabel = mi.getTargetWaypoint();
				} else {
					List<? extends IInstruction> remainingInstructions = igModel.getRemainingInstructions();
					for (Iterator<? extends IInstruction> iterator = remainingInstructions.iterator(); iterator.hasNext()
							&& !(ci instanceof MoveAbsHInstruction);) {
						ci = iterator.next();
					}
					if (ci != null) {
						MoveAbsHInstruction mi = (MoveAbsHInstruction) ci;
						srcLabel = mi.getSourceWaypoint();
						tgtLabel = mi.getTargetWaypoint();
					} else {
						m_reportingPort.error(getComponentType(),
								"There are no move instructions left -- the last instruction in an instruction graph for BRASS should always be a move");
					}
				}

				DecisionEngineCP1.setMap(envMap);
				String tgt = m_models.getMissionStateModel().getModelInstance().getTargetWaypoint();
				log("Generating candidate paths from " + srcLabel + " to " + tgt);

				DecisionEngineCP1.generateCandidates(srcLabel, tgt, true);
				log("---> found " + DecisionEngineCP1.m_candidates.size());

				try {
					DecisionEngineCP1.scoreCandidates(envMap,
							Math.round(m_models.getRobotStateModel().getModelInstance().getCharge()),
							Heading.convertFromRadians(
									m_models.getMissionStateModel().getModelInstance().getCurrentPose().getRotation())
									.ordinal());
					PrismPolicy pp = null;
					if (!DecisionEngineCP1.m_scoreboard.isEmpty()) {
						pp = new PrismPolicy(DecisionEngineCP1.selectPolicy());
						pp.readPolicy();
					}

					if (DecisionEngineCP1.m_scoreboard.isEmpty() || pp.getPlan() == null || pp.getPlan().isEmpty()) {
						// BRASSHttpConnector.instance ().reportStatus (DASStatusT.MISSION_ABORTED,
						// "Could not find a valid adaptation... trying again.");
						log("Could not find a valid adaptation...");

						MapTranslator.exportMapTranslation(
								Rainbow.instance().getProperty(PropertiesConnector.PRISM_MODEL_PROPKEY));
						PrismConnectorAPI.instance()
								.loadModel(Rainbow.instance().getProperty(PropertiesConnector.PRISM_MODEL_PROPKEY));
						String m_consts = MapTranslator.INITIAL_ROBOT_CONF_CONST + "=-1,"
								+ MapTranslator.INITIAL_ROBOT_LOCATION_CONST + "="
								+ String.valueOf(envMap.getNodeId(srcLabel)) + ","
								+ MapTranslator.TARGET_ROBOT_LOCATION_CONST + "="
								+ String.valueOf(envMap.getNodeId(tgt)) + ","
								+ MapTranslator.INITIAL_ROBOT_BATTERY_CONST + "="
								+ String.valueOf(
										Math.round(m_models.getRobotStateModel().getModelInstance().getCharge()))
								+ "," + MapTranslator.INITIAL_ROBOT_HEADING_CONST + "=2";

						log("Generating last resort plan for " + m_consts);
						String result;
						result = PrismConnectorAPI.instance().modelCheckFromFileS(
								Rainbow.instance().getProperty(PropertiesConnector.PRISM_MODEL_PROPKEY),
								Rainbow.instance().getProperty(PropertiesConnector.PRISM_PROPERTIES_PROPKEY),
								/*
								 * Rainbow.instance ().getProperty
								 * (PropertiesConnector.PRISM_ADV_EXPORT_PROPKEY)
								 */"lastResortPolicy", 0, m_consts);
						pp = new PrismPolicy("lastResortPolicy.adv");
						pp.readPolicy();
						if (pp.getPlan() == null || pp.getPlan().isEmpty()) {
							log("Could not find last resort plan -- marking task as FAILED");
							BrassPlan ct = new CompletedTask(m_models, false);
							AdaptationTree<BrassPlan> at = new AdaptationTree<>(ct);
							m_executingPlan = true;
							BRASSHttpConnector.instance(Phases.Phase2).reportStatus(
									DASPhase2StatusT.ADAPTED_FAILED.name(),
									"Could not find an alternate (even last resort) plan to complete task: " + tgt);
							m_adaptationEnqueuePort.offerAdaptation(at, new Object[] {});
							return;
						}
						m_inLastResort = true;
						log("Found last resort plan: " + pp.getPlan().toString());
					}

					String plan = pp.getPlan().toString();
					m_reportingPort.info(getComponentType(), "Planner chooses the plan " + plan);
					PolicyToIGCP1 translator = new PolicyToIGCP1(pp, envMap);
					
					ArrayList<String> planArray = pp.getPlan();
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
					
					String translate = translator.translate(m_configurationStore);

					BrassPlan nig = new NewInstructionGraph(m_models, translate);
					AdaptationTree<BrassPlan> at = new AdaptationTree<>(nig);
					m_executingPlan = true;
					m_models.getRainbowStateModel().getModelInstance().m_waitForIG = true;
					m_reportAdapted = true;
					m_adaptationEnqueuePort.offerAdaptation(at, new Object[] {});
				} catch (Exception e) {
					e.printStackTrace();
					m_reportingPort.error(getComponentType(), "Failed to find a plan " + e.getMessage());
				}
			} else if (m_models.getRainbowStateModel().getModelInstance().getProblems()
					.contains(CP3ModelState.OUT_OF_BATTERY)) {
				m_reportingPort.error(getComponentType(),
						"Cannot find a plan that magically gives us more power -- we are out of battery");
				BRASSHttpConnector.instance(Phases.Phase2).reportDone(true, "Ran out of power");
			} else if (!m_models.getRainbowStateModel().getModelInstance().waitForIG()
					&& m_models.getInstructionGraphModel().getModelInstance()
							.getInstructionGraphState() == IGExecutionStateT.FINISHED_SUCCESS) {
				log("Planner detected successful completion of instruction graph");
				log("Reporting that we completed successfully");
				CompletedTask ct = new CompletedTask(m_models, true);
				AdaptationTree<BrassPlan> at = new AdaptationTree<>(ct);
				m_executingPlan = true;
				m_models.getRainbowStateModel().getModelInstance().m_waitForIG = true;
				m_adaptationEnqueuePort.offerAdaptation(at, new Object[] {});
			} else if (m_inLastResort && m_models.getInstructionGraphModel().getModelInstance()
					.getInstructionGraphState() == IGExecutionStateT.FINISHED_FAILED) {
				CompletedTask ct = new CompletedTask(m_models, false);
				AdaptationTree<BrassPlan> at = new AdaptationTree<>(ct);
				m_executingPlan = true;
				m_adaptationEnqueuePort.offerAdaptation(at, new Object[] {});
			}

		}

	}

	protected void cancelInstructions() {
		m_reportingPort.info(getComponentType(), "Canceling the current set of instructions.");
		CancelInstructionsTask cancelTask = new CancelInstructionsTask(m_models);
		AdaptationTree<BrassPlan> at = new AdaptationTree<>(cancelTask);
		m_adaptationEnqueuePort.offerAdaptation(at, new Object[0]);
	}

	private boolean reallyHasError() {
		EnumSet<CP3ModelState> realErrors = EnumSet.of(CP3ModelState.LOW_ON_BATTERY, CP3ModelState.IS_OBSTRUCTED,
				CP3ModelState.INSTRUCTION_GRAPH_FAILED);
		for (CP3ModelState state : realErrors) {
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

	@Override
	public ModelReference getManagedModel() {
		return m_modelRef;
	}

}
