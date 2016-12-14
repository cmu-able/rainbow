package org.sa.rainbow.brass.adaptation;

import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.adaptation.AdaptationTree;
import org.sa.rainbow.core.adaptation.IAdaptationExecutor;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort;
import org.sa.rainbow.core.ports.IRainbowAdaptationDequeuePort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;

/**
 * Created by schmerl on 12/13/2016.
 */
public class PlanExecutor extends AbstractRainbowRunnable implements IAdaptationExecutor<BrassPlan> {
    public static final String NAME = "BRASS Plan Executor";
    private ModelReference                           m_modelRef;
    private IRainbowAdaptationDequeuePort<BrassPlan> m_adaptationDQPort;
    private IModelDSBusPublisherPort m_modelDSPort;

    /**
     * Default Constructor with name for the thread.
     */
    public PlanExecutor () {
        super (NAME);
    }

    @Override
    public void initialize (IRainbowReportingPort port) throws RainbowConnectionException {
        super.initialize (port);

        // Create the model DS port where operations go (and effectors listen)
        m_modelDSPort = RainbowPortFactory.createModelDSPublishPort (this);
    }

    // Sets the model that the executor primarily (updates), and listens for plans that affect this model
    @Override
    public void setModelToManage (ModelReference modelRef) {
        m_modelRef = modelRef;
        // Adaptation plans will be found on this port
        m_adaptationDQPort = RainbowPortFactory.createAdaptationDequeuePort (m_modelRef);
    }

    @Override
    public IModelDSBusPublisherPort getOperationPublishingPort () {
        return m_modelDSPort;
    }

    @Override
    public IRainbowReportingPort getReportingPort () {
        return m_reportingPort;
    }

    @Override
    public void dispose () {
        if (m_adaptationDQPort != null)
            m_adaptationDQPort.dispose ();
        m_modelDSPort.dispose ();
        m_reportingPort.dispose ();
    }

    @Override
    protected void log (String txt) {
        m_reportingPort.info (RainbowComponentT.EXECUTOR, txt);
    }

    @Override
    protected void runAction () {
        if (m_adaptationDQPort != null && !m_adaptationDQPort.isEmpty ()) {
            AdaptationTree<BrassPlan> at = m_adaptationDQPort.dequeue ();
            // TODO: Execute the plan
        }
    }

    @Override
    public RainbowComponentT getComponentType () {
        return RainbowComponentT.EXECUTOR;
    }
}
