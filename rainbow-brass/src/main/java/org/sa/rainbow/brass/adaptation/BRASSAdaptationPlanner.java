package org.sa.rainbow.brass.adaptation;

import org.sa.rainbow.brass.das.BRASSHttpConnector;
import org.sa.rainbow.brass.das.IBRASSConnector.DASStatusT;
import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapModelInstance;
import org.sa.rainbow.brass.model.map.MapTranslator;
import org.sa.rainbow.brass.model.mission.MissionState;
import org.sa.rainbow.brass.model.mission.MissionState.LocationRecording;
import org.sa.rainbow.brass.model.mission.MissionStateModelInstance;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.adaptation.AdaptationTree;
import org.sa.rainbow.core.adaptation.DefaultAdaptationTreeWalker;
import org.sa.rainbow.core.adaptation.IAdaptationManager;
import org.sa.rainbow.core.error.RainbowConnectionException;
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

/**
 * Created by schmerl on 12/13/2016. This class implements the BRASS planner. This should either (a) run periodically
 * and check a model that it cares about, or (b) implements IRainbowModelChangeCallback to listen to model events that
 * will invoke planning. (See org.sa.rainbow.stitch.adaptation.AdaptationManager for an example for Stitch)
 * <p>
 * BrassPlan (the type parameter) is the evaluable plan to execute
 */
public class BRASSAdaptationPlanner extends AbstractRainbowRunnable
implements IAdaptationManager<BrassPlan>, IRainbowModelChangeCallback {

    public static final String NAME = "BRASS Adaptation Planner";
    // The thread "sleep" time. runAction will be called every 10 seconds in this case
    public static final int SLEEP_TIME = 10000 /*ms*/;

    // Port to query with any models in models manager
    private IModelsManagerPort                       m_modelsManagerPort;
    private IModelChangeBusSubscriberPort            m_modelChangePort;
    private ModelReference                           m_modelRef;
    private IRainbowAdaptationEnqueuePort<BrassPlan> m_adaptationEnqueuePort;
    private boolean                                  m_adaptationEnabled = true;
    private boolean                                  m_errorDetected     = false;

    private IRainbowChangeBusSubscription m_robotObstructed = new IRainbowChangeBusSubscription () {

        @Override
        public boolean matches (IRainbowMessage message) {
            String modelName = (String )message.getProperty (
                    IModelChangeBusPort.MODEL_NAME_PROP);
            String modelType = (String )message.getProperty (
                    IModelChangeBusPort.MODEL_TYPE_PROP);
            String commandName = (String )message.getProperty (
                    IModelChangeBusPort.COMMAND_PROP);

            return MissionStateModelInstance.MISSION_STATE_TYPE
                    .equals (modelType)
                    && "RobotAndEnvironmentState"
                    .equals (modelName)
                    && "setRobotObstructed"
                    .equals (commandName);
        }
    };
    private boolean                       m_executingPlan   = false;

    /**
     * Default Constructor with name for the thread.
     */
    public BRASSAdaptationPlanner () {
        super (NAME);
        String per = Rainbow.instance ().getProperty (RainbowConstants.PROPKEY_MODEL_EVAL_PERIOD);
        if (per != null) {
            setSleepTime (Long.parseLong (per));
        }
        else {
            setSleepTime (SLEEP_TIME);
        }
    }

    @Override
    public void initialize (IRainbowReportingPort port) throws RainbowConnectionException {
        super.initialize (port);
        initConnectors ();
    }

    private void initConnectors () throws RainbowConnectionException {
        // Create port to query models manager
        m_modelsManagerPort = RainbowPortFactory.createModelsManagerRequirerPort ();
        m_modelChangePort = RainbowPortFactory.createModelChangeBusSubscriptionPort ();
        m_modelChangePort.subscribe (m_robotObstructed, this);
        // If you want to listen to changes, then you need to create a modelChangePort and write a subscriber to it.
        // See org.sa.rainbow.stitch.AdaptationManger for an example of this

    }

    // This is the "main" model that the adaptation will listen to
    @Override
    public void setModelToManage (ModelReference modelRef) {
        m_modelRef = modelRef;
        // Create a port to send any plans on (AdaptationTree<BrassPlan>) that will be sent to an executor
        m_adaptationEnqueuePort = RainbowPortFactory.createAdaptationEnqueuePort (modelRef);
    }

    @Override
    public void onEvent (ModelReference mr, IRainbowMessage message) {
        synchronized (this) {
            Boolean obstructed = Boolean
                    .parseBoolean ((String )message.getProperty (IModelChangeBusPort.PARAMETER_PROP + "0"));
            m_errorDetected = obstructed;
        }
    }

    @Override
    public void markStrategyExecuted (AdaptationTree<BrassPlan> plan) {
        // Insert code here to record when a plan has been executed by the execution manager
        // Possible things to do:
        // (a) keep a history of plan success
        // (b) start listening to model events to generate new plans again

        m_executingPlan = false;
        AdaptationResultsVisitor v = new AdaptationResultsVisitor (plan);
        plan.visit (v);
        if (v.m_allOk) {
            BRASSHttpConnector.instance ().reportStatus (DASStatusT.ADAPTATION_COMPLETED,
                    "Finished adapting the system");
        }
        else {
            BRASSHttpConnector.instance ().reportStatus (DASStatusT.ERROR,
                    "Something in the adaptation plan failed to execute.");
        }

    }

    @Override
    public void setEnabled (boolean enabled) {
        m_adaptationEnabled = enabled;
    }

    @Override
    public boolean isEnabled () {
        return m_adaptationEnabled;
    }

    @Override
    public void dispose () {

    }

    @Override
    protected void log (String txt) {
        m_reportingPort.info (RainbowComponentT.ADAPTATION_MANAGER, txt);
    }

    @Override
    protected void runAction () {
        if (m_adaptationEnabled && m_errorDetected && !m_executingPlan) {
            BRASSHttpConnector.instance ().reportStatus (DASStatusT.ADAPTING, "Finding a new plan");
            m_errorDetected = false;
            m_reportingPort.info (getComponentType (), "Determining an appropriate adaptation");
            // Work out if a plan needs to be generated
            // Generate the plan
            // Enqueue the plan
            // (see AdaptationManager for how to enqueue the plan)
            // Javier : The code below retrieves the map, translates it, and invokes prism to generate the policy
            // TODO: Planning parameters are now hard-wired. We need to retrieve: label of source and target locations
            // to invoke PRISM.
            ModelReference emRef = new ModelReference ("Map", EnvMapModelInstance.ENV_MAP_TYPE);
            EnvMapModelInstance envModel = (EnvMapModelInstance )m_modelsManagerPort.<EnvMap> getModelInstance (emRef);
            ModelReference igRef = new ModelReference ("ExecutingInstructionGraph",
                    InstructionGraphModelInstance.INSTRUCTION_GRAPH_TYPE);
            InstructionGraphModelInstance igModel = (InstructionGraphModelInstance )m_modelsManagerPort
                    .<InstructionGraphProgress> getModelInstance (igRef);
            ModelReference missionStateRef = new ModelReference ("RobotAndEnvironmentState",
                    MissionStateModelInstance.MISSION_STATE_TYPE);
            MissionStateModelInstance missionStateModel = (MissionStateModelInstance )m_modelsManagerPort
                    .<MissionState> getModelInstance (missionStateRef);

            if (envModel != null && igModel != null && missionStateModel != null) {
                EnvMap map = envModel.getModelInstance ();
                MapTranslator mt = new MapTranslator ();
                PrismConnector pc = new PrismConnector (Rainbow.instance ().allProperties ()); // Does this work with hard-wired props in the constructor?

                mt.setMap (map);
                mt.exportMapTranslation (pc.getPrismModelLocation ());

                // Get the current locatino of the robot
                LocationRecording pose = missionStateModel.getModelInstance ().getCurrentPose ();
                String label = envModel.getModelInstance ().getNode (pose.getX (), pose.getY ()).m_label;
                pc.invoke (map.getNodeId (label), map.getNodeId ("l6"));
//                pc.invoke (map.getNodeId ("ls"), map.getNodeId ("l1")); // Change from hard-wired to values read from model properties

                // TODO: Ashutosh
                // Translate model to the IG
                // Create a NewInstrcutionGraph object and enqueue it on the adaptation port
                PrismPolicy prismPolicy = new PrismPolicy (pc.getPrismPolicyLocation ());
                prismPolicy.readPolicy ();
                m_reportingPort.info (getComponentType (), "Found new plan: " + prismPolicy.getPlan ().toString ());
                if (prismPolicy.getPlan () == null || prismPolicy.getPlan ().isEmpty ()) {
                    BRASSHttpConnector.instance ().reportStatus (DASStatusT.MISSION_ABORTED,
                            "Could not find a valid adaptation");
                }
                else {
                    PolicyToIG translator = new PolicyToIG (prismPolicy, map);
                    NewInstructionGraph nig = new NewInstructionGraph (igModel, translator.translate ()); // Ashutosh: do this
                    AdaptationTree<BrassPlan> at = new AdaptationTree<BrassPlan> (nig);
                    m_reportingPort.info (getComponentType (), "New adaptation found - enqueuing it");
                    m_executingPlan = true;
                    m_adaptationEnqueuePort.offerAdaptation (at, new Object[] {});
                }
            }

        }

    }

    @Override
    public RainbowComponentT getComponentType () {
        return RainbowComponentT.ADAPTATION_MANAGER;
    }

    public static String DUMMY_ALTERNATE_IG = "P(V(1, do MoveAbs (19.5,69,1) then 2),V(2, do MoveAbs (19.5,59,1) then 3)::V(3, do Move (42.5, 59, 0) then 4)::V(4, end)::nil)";

    private class AdaptationResultsVisitor extends DefaultAdaptationTreeWalker<BrassPlan> {

        public AdaptationResultsVisitor (AdaptationTree<BrassPlan> adt) {
            super (adt);
        }

        boolean m_allOk = true;

        @Override
        protected void evaluate (BrassPlan adaptation) {
            m_allOk &= adaptation.getOutcome ();
        }

    }

}
