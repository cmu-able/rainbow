package org.sa.rainbow.brass.adaptation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sa.rainbow.brass.PropertiesConnector;
import org.sa.rainbow.brass.das.BRASSHttpConnector;
import org.sa.rainbow.brass.das.IBRASSConnector.DASPhase1StatusT;
import org.sa.rainbow.brass.das.IBRASSConnector.Phases;
import org.sa.rainbow.brass.model.instructions.InstructionGraphModelInstance;
import org.sa.rainbow.brass.model.instructions.InstructionGraphProgress;
import org.sa.rainbow.brass.model.map.EnvMap;
import org.sa.rainbow.brass.model.map.EnvMapModelInstance;
import org.sa.rainbow.brass.model.map.EnvMapNode;
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
    private List<String>                  m_currentPlan     = Collections.<String> emptyList ();

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
        try {
            DecisionEngine.init (Rainbow.instance ().allProperties ());
        }
        catch (Exception e) {
            e.printStackTrace ();
            throw new RainbowConnectionException ("Cannot initialize DecisionEngine", e);
        }

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

        AdaptationResultsVisitor v = new AdaptationResultsVisitor (plan);
        plan.visit (v);
        if (v.m_allOk) {
            BRASSHttpConnector.instance (Phases.Phase1).reportStatus (DASPhase1StatusT.ADAPTATION_COMPLETED.name(),
                    "Finished adapting the system");
        }
        else {
            BRASSHttpConnector.instance (Phases.Phase1).reportStatus (DASPhase1StatusT.TEST_ERROR.name(),
                    "Something in the adaptation plan failed to execute.");
        }
        try {
            // Wait for IG to come to Analyzer
            Thread.sleep (5000);
        }
        catch (InterruptedException e) {
        }
        m_executingPlan = false;

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

    PrismPolicy m_prismPolicy;

    @Override
    protected void runAction () {
        ModelReference missionStateRef = new ModelReference ("RobotAndEnvironmentState",
                MissionStateModelInstance.MISSION_STATE_TYPE);
        MissionStateModelInstance missionStateModel = (MissionStateModelInstance )m_modelsManagerPort
                .<MissionState> getModelInstance (missionStateRef);
        MissionState ms = missionStateModel.getModelInstance ();

        if (m_adaptationEnabled && ms.isAdaptationNeeded () && !m_executingPlan) {
            m_errorDetected = false;
            m_reportingPort.info (getComponentType (), "Determining an appropriate adaptation");
            boolean robotAccurate = ms.isRobotAccurate ();
            boolean robotObstructed = ms.isRobotObstructed ();
            boolean robotOnTime = ms.isRobotOnTime ();
            boolean robotMicalibrated = ms.isBadlyCalibrated ();
            log ("(RobotAccurate: " + robotAccurate + ", RobotObstructed: " + robotObstructed + ", RobotOnTime: "
                    + robotOnTime + ", RobotMiscalibrated: " + robotMicalibrated + ")");
            // The code below retrieves the map, translates it, and invokes the decision engine to generate the policy

            ModelReference emRef = new ModelReference ("Map", EnvMapModelInstance.ENV_MAP_TYPE);
            EnvMapModelInstance envModel = (EnvMapModelInstance )m_modelsManagerPort.<EnvMap> getModelInstance (emRef);
            ModelReference igRef = new ModelReference ("ExecutingInstructionGraph",
                    InstructionGraphModelInstance.INSTRUCTION_GRAPH_TYPE);
            InstructionGraphModelInstance igModel = (InstructionGraphModelInstance )m_modelsManagerPort
                    .<InstructionGraphProgress> getModelInstance (igRef);

            if (envModel != null && igModel != null && missionStateModel != null) {

                if (!ms.isAdaptationNeeded ()) // If there is no need for adaptation, planner does not compute a new plan
                    return;
                // Challenge problem 2
                if (Rainbow.instance ().getProperty ("brass.challenge").equals ("2")) {
                    boolean badlyCalibrated = ms.isBadlyCalibrated ();
                    if (badlyCalibrated) {
                        try {
//                            BrassPlan recalibrate = new Recalibrate (missionStateModel, igModel);
                            //AdaptationTree<BrassPlan> rc = new AdaptationTree<> (recalibrate);

                            // Generate new plan with inhibited tactics
                            EnvMap map = envModel.getModelInstance ();
                            DecisionEngine.setMap (map);

                            LocationRecording pose = missionStateModel.getModelInstance ().getCurrentPose ();
                            String label = envModel.getModelInstance ().getNode (pose.getX (), pose.getY ())
                                    .getLabel ();

                            DecisionEngine.generateCandidates (label, ms.getTargetWaypoint (), true); // Generate candidate solutions to go from current waypoint to target one (inhibited tactics, just move commands)
                            DecisionEngine.scoreCandidates (map, MapTranslator.ROBOT_BATTERY_RANGE_MAX, 2); // Property 1 in file deals with time subject to target reachability (R{"time"}min=? [ F goal ])

                            PrismPolicy prismPolicy = new PrismPolicy (DecisionEngine.selectPolicy ());
                            prismPolicy.readPolicy ();
                            if (!prismPolicy.getPlan ().isEmpty ()) {
                                PolicyToIG translator = new PolicyToIG (prismPolicy, map);
                                BrassPlan nig = new NewInstructionGraph (igModel, translator.translate (-1, true));
                                if (!m_currentPlan.equals (prismPolicy.getPlan ())) {
//                            AdaptationTree<BrassPlan> at = new AdaptationTree<> (AdaptationExecutionOperatorT.SEQUENCE);
                                    // TODO: Need to add a leaf with stop here
//                            at.addLeaf (recalibrate);
                                    m_currentPlan = new ArrayList<> (prismPolicy.getPlan ());
                                    AdaptationTree<BrassPlan> at = new AdaptationTree<> (nig);
//                            at.addLeaf (nig);
                                    BRASSHttpConnector.instance (Phases.Phase1).reportStatus (DASPhase1StatusT.ADAPTATION_INITIATED.name(),
                                            "Enqueuing a new plan");
                                    m_adaptationEnqueuePort.offerAdaptation (at, new Object[0]);

                                    m_executingPlan = true;
                                }
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace ();
                        }
                    }
                }
                else {

                    if (!robotAccurate || robotObstructed || !robotOnTime) {
                        try {
                            EnvMap map = envModel.getModelInstance ();
                            DecisionEngine.setMap (map);

                            // Get the current location of the robot
                            LocationRecording pose = missionStateModel.getModelInstance ().getCurrentPose ();
                            EnvMapNode node = envModel.getModelInstance ().getNode (pose.getX (), pose.getY ());
                            if (node == null) {
                                log ("Node is null, planner getting called too often (RobotAccurate: " + robotAccurate
                                        + ", RobotObstructed: " + robotObstructed + ", RobotOnTime: " + robotOnTime
                                        + ")");
                                return;
                            }
                            String label = node.getLabel ();

                            DecisionEngine.generateCandidates (label, ms.getTargetWaypoint ()); // Generate candidate solutions to go from current waypoint to target one
                            DecisionEngine.scoreCandidates (map, Math.round (ms.getBatteryCharge ()),
                                    1); // Property 1 in file deals with time subject to target reachability (R{"time"}min=? [ F goal ])

                            // Translate model to the IG
                            // Create a NewInstrcutionGraph object and enqueue it on the adaptation port, set new deadline
                            PrismPolicy prismPolicy = new PrismPolicy (DecisionEngine.selectPolicy ());
                            prismPolicy.readPolicy ();
                            m_reportingPort.info (getComponentType (),
                                    "Found new plan: " + prismPolicy.getPlan ().toString ());
                            if (prismPolicy.getPlan () == null || prismPolicy.getPlan ().isEmpty ()) {
                                //  BRASSHttpConnector.instance ().reportStatus (DASStatusT.MISSION_ABORTED, "Could not find a valid adaptation... trying again.");                        
                                m_reportingPort.info (getComponentType (),
                                        "Could not find a valid adaptation... trying again.");

                                MapTranslator.exportMapTranslation (
                                        Rainbow.instance ().getProperty (PropertiesConnector.PRISM_MODEL_PROPKEY));
                                PrismConnectorAPI.instance().loadModel (
                                        Rainbow.instance ().getProperty (PropertiesConnector.PRISM_MODEL_PROPKEY));
                                String m_consts = MapTranslator.INITIAL_ROBOT_LOCATION_CONST + "="
                                        + String.valueOf (map.getNodeId (label)) + ","
                                        + MapTranslator.TARGET_ROBOT_LOCATION_CONST + "="
                                        + String.valueOf (map.getNodeId (ms.getTargetWaypoint ())) + ","
                                        + MapTranslator.INITIAL_ROBOT_BATTERY_CONST + "="
                                        + String.valueOf (Math.round (ms.getBatteryCharge ())) + ","
                                        + MapTranslator.INITIAL_ROBOT_HEADING_CONST + "=2";

                                System.out.println ("Generating last resort plan for " + m_consts);
                                String result;
                                result = PrismConnectorAPI.instance().modelCheckFromFileS (
                                        Rainbow.instance ().getProperty (PropertiesConnector.PRISM_MODEL_PROPKEY),
                                        Rainbow.instance ().getProperty (PropertiesConnector.PRISM_PROPERTIES_PROPKEY),
                                        /*Rainbow.instance ().getProperty (PropertiesConnector.PRISM_ADV_EXPORT_PROPKEY)*/"lastResortPolicy",
                                        0, m_consts);
                                prismPolicy = new PrismPolicy (
                                        "lastResortPolicy.adv");
                                prismPolicy.readPolicy ();
                                if (prismPolicy.getPlan () == null || prismPolicy.getPlan ().isEmpty ()) {
//                                BRASSHttpConnector.instance ().reportStatus (DASStatusT.MISSION_ABORTED,
//                                        "Could not find a valid adaptation");
                                }
                                m_reportingPort.info (getComponentType (),
                                        "Found last resort plan: " + prismPolicy.getPlan ().toString ());
                            }
                            if (prismPolicy.getPlan () != null && !prismPolicy.getPlan ().isEmpty ()
                                    && !prismPolicy.getPlan ().equals (m_currentPlan)) {
                                m_currentPlan = new ArrayList<> (prismPolicy.getPlan ());
                                Long deadline = new Double (
                                        ms.getCurrentTime () + DecisionEngine.getSelectedPolicyTime ()
                                        + prismPolicy.getPlan ().size () * 1.5 + 3).longValue (); // for the sleep in the deadline
//                                    String translate = translator.translate (deadline, null);
//                                    if (translate.indexOf ("P(", 5) != -1) {
//                                        log ("Have a strange instruction graph");
//                                        return;
//                                    }
                                PolicyToIG translator = new PolicyToIG (prismPolicy, map);
                                BrassPlan nig = new NewInstructionGraph (igModel,
                                        translator.translate (deadline, null));

                                AdaptationTree<BrassPlan> at = new AdaptationTree<> (nig);
//                                at.addLeaf (nig);
//                                at.addLeaf (new SetDeadline (missionStateModel, igModel, deadline));

//                    AdaptationTree<BrassPlan> at = new AdaptationTree<BrassPlan> (nig);
                                m_reportingPort.info (getComponentType (), "New adaptation found - enqueuing it");
                                m_executingPlan = true;
                                BRASSHttpConnector.instance (Phases.Phase1).reportStatus (DASPhase1StatusT.ADAPTATION_INITIATED.name(),
                                        "Enqueuing a new plan");

                                m_adaptationEnqueuePort.offerAdaptation (at, new Object[] {});
                            }
                        }
                        catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace ();
                        }
                    }
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
