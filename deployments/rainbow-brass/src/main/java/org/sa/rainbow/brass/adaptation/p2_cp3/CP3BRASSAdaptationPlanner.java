package org.sa.rainbow.brass.adaptation.p2_cp3;

import java.util.EnumSet;

import org.sa.rainbow.brass.adaptation.BrassPlan;
import org.sa.rainbow.brass.adaptation.NewInstructionGraph;
import org.sa.rainbow.brass.adaptation.PrismPolicy;
import org.sa.rainbow.brass.confsynthesis.ConfigurationSynthesizer;
import org.sa.rainbow.brass.confsynthesis.ReconfSynthReal;
import org.sa.rainbow.brass.das.BRASSHttpConnector;
import org.sa.rainbow.brass.das.IBRASSConnector.DASPhase2StatusT;
import org.sa.rainbow.brass.das.IBRASSConnector.Phases;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.p2_cp3.CP3ModelAccessor;
import org.sa.rainbow.brass.model.p2_cp3.mission.MissionState.Heading;
import org.sa.rainbow.brass.model.p2_cp3.rainbowState.RainbowState.CP3ModelState;
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
			m_reconfSynth = new ReconfSynthReal(m_models);
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
			BRASSHttpConnector.instance(Phases.Phase2).reportStatus(DASPhase2StatusT.ADAPTED.name(),
					"Finished adapting the system");
		} else {
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
		m_reportingPort.info(RainbowComponentT.ADAPTATION_MANAGER, txt);

	}

	@Override
	protected void runAction() {
		if (m_models.getInstructionGraphModel().getModelInstance().getInstructions().isEmpty()) 
			return;
		if (m_adaptationEnabled && reallyHasError() && !m_executingPlan ) {
			 BRASSHttpConnector.instance (Phases.Phase2).reportStatus (DASPhase2StatusT.ADAPTING.name(), "Detected an error");

			m_errorDetected = false;
			m_reportingPort.info(getComponentType(), "Determining an appropriate adaptation");
			// DecisionEngineCP3.setMap(m_models.getEnvMapModel().getModelInstance());

			// 1. Determine string for intialization of the planner
			String confInitString = m_reconfSynth.getCurrentConfigurationInitConstants();
			try {
				m_configurationSynthesizer.generateReconfigurationsFrom(confInitString);
			} catch (RainbowException e1) {
				e1.printStackTrace();
				m_reportingPort.error(getComponentType(), "Could not synthesize configurations " + e1.getMessage());

			}

			EnvMap copy = m_models.getEnvMapModel().getModelInstance().copy();
			DecisionEngineCP3.setMap(copy);
			// Insert a node where the robot is
//			copy.insertNode(n, na, nb, x, y, false);

			DecisionEngineCP3.generateCandidates("l1", "l2");
			try {
				DecisionEngineCP3
						.scoreCandidates(copy, String.valueOf(m_models.getRobotStateModel().getModelInstance().getCharge()),
								Integer.toString(Heading.convertFromRadians(
										m_models.getMissionStateModel().getModelInstance().getCurrentPose().getRotation())
										.ordinal()));
				PrismPolicy pp = new PrismPolicy(DecisionEngineCP3.selectPolicy());
				pp.readPolicy();
				String plan = pp.getPlan(m_configurationSynthesizer, confInitString).toString();
				PolicyToIGCP3 translator = new PolicyToIGCP3(pp, copy);
				String translate = translator.translate(m_configurationSynthesizer, confInitString);
				
				BrassPlan nig = new NewInstructionGraph(m_models.getInstructionGraphModel(), translate);
				AdaptationTree<BrassPlan> at = new AdaptationTree<> (nig);
				m_executingPlan = true;
				m_adaptationEnqueuePort.offerAdaptation (at, new Object[] {});
			} catch (Exception e) {
				e.printStackTrace();
				m_reportingPort.error(getComponentType(), "Failed to find a plan " + e.getMessage());
			}

		}

	}

	private boolean reallyHasError() {
		EnumSet<CP3ModelState> realErrors = EnumSet.of(CP3ModelState.ARCHITECTURE_ERROR,
				CP3ModelState.CONFIGURATION_ERROR, CP3ModelState.INSTRUCTION_GRAPH_FAILED);
		for (CP3ModelState state : realErrors) {
			if (m_models.getRainbowStateModel().getModelInstance().getProblems().contains(state))
				return true;
		}
		if (m_models.getRainbowStateModel().getModelInstance().getProblems().contains(CP3ModelState.TOO_DARK)
				&& m_models.getTurtlebotModel().getActiveComponents().contains("marker_pose_publisher")) {
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
