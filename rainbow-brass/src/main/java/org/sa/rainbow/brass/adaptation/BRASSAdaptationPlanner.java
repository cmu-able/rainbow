package org.sa.rainbow.brass.adaptation;

import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.adaptation.AdaptationTree;
import org.sa.rainbow.core.adaptation.IAdaptationManager;
import org.sa.rainbow.core.adaptation.IEvaluable;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelsManagerPort;
import org.sa.rainbow.core.ports.IRainbowAdaptationEnqueuePort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.core.event.IRainbowMessage;

/**
 * Created by schmerl on 12/13/2016.
 * This class implements the BRASS planner. This should either (a) run periodically and check a model that
 * it cares about, or (b) implements IRainbowModelChangeCallback to listen to model events that will invoke
 * planning. (See org.sa.rainbow.stitch.adaptation.AdaptationManager for an example for Stitch)
 * <p>
 * BrassPlan (the type parameter) is the evaluable plan to execute
 */
public class BRASSAdaptationPlanner extends AbstractRainbowRunnable implements IAdaptationManager<BrassPlan>
																			 , IRainbowModelChangeCallback {

    public static final String NAME       = "BRASS Adaptation Planner";
    // The thread "sleep" time. runAction will be called every 10 seconds in this case
    public static final int    SLEEP_TIME = 10000 /*ms*/;

    // Port to query with any models in models manager
    private IModelsManagerPort                        m_modelsManagerPort;
    private ModelReference                            m_modelRef;
    private IRainbowAdaptationEnqueuePort<IEvaluable> m_adaptationEnqueuePort;
    private boolean m_adaptationEnabled;

    /**
     * Default Constructor with name for the thread.
     */
    public BRASSAdaptationPlanner () {
        super (NAME);
        setSleepTime (SLEEP_TIME);
    }

    @Override
    public void initialize (IRainbowReportingPort port) throws RainbowConnectionException {
        super.initialize (port);
        initConnectors ();
    }

    private void initConnectors () throws RainbowConnectionException {
        // Create port to query models manager
        m_modelsManagerPort = RainbowPortFactory.createModelsManagerRequirerPort ();

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
    public void onEvent (ModelReference mr, IRainbowMessage message){
    	// TODO: Complete code here
    }
    
    
    @Override
    public void markStrategyExecuted (AdaptationTree<BrassPlan> plan) {
        // Insert code here to record when a plan has been executed by the execution manager
        // Possible things to do:
        // (a) keep a history of plan success
        // (b) start listening to model events to generate new plans again

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
        if (m_adaptationEnabled) {
            // Work out if a plan needs to be generated
            // Generate the plan
            // Enqueue the plan
            // (see AdaptationManager for how to enqueue the plan)
        }

    }

    @Override
    public RainbowComponentT getComponentType () {
        return RainbowComponentT.ADAPTATION_MANAGER;
    }
}
