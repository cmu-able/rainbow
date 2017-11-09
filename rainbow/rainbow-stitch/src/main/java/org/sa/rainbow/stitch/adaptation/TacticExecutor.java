package org.sa.rainbow.stitch.adaptation;

import org.acmestudio.acme.element.IAcmeSystem;
import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.adaptation.AdaptationTree;
import org.sa.rainbow.core.adaptation.IAdaptationExecutor;
import org.sa.rainbow.core.adaptation.IAdaptationManager;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowModelException;
import org.sa.rainbow.core.models.IModelInstance;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.models.ModelsManager;
import org.sa.rainbow.core.ports.*;
import org.sa.rainbow.model.acme.AcmeModelInstance;
import org.sa.rainbow.stitch.core.Tactic;
import org.sa.rainbow.stitch.history.ExecutionHistoryModelInstance;
import org.sa.rainbow.stitch.util.ExecutionHistoryData;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by schmerl on 6/16/2016.
 */
public class TacticExecutor extends AbstractRainbowRunnable implements IAdaptationExecutor<Tactic>{

    public static final String NAME = "Rainbow Tactic Executor";
    private ModelReference                        m_modelRef;
    private IRainbowAdaptationDequeuePort<Tactic> m_adaptationDQPort;
    private AcmeModelInstance                     m_model;
    private ThreadGroup                           m_executionThreadGroup;
    private IModelDSBusPublisherPort              m_modelDSPort;
    private IModelUSBusPort                       m_modelUSBusPort;
    private ExecutionHistoryModelInstance m_historyModel;
    private CountDownLatch m_done;
    private AdaptationTree<Tactic> m_adaptationTreeExecuting;

    /**
     * Default Constructor with name for the thread.
     */
    public TacticExecutor () {
        super (NAME);
    }

    @Override
    public void initialize (IRainbowReportingPort port) throws RainbowConnectionException {
        super.initialize (port);
        m_modelDSPort = RainbowPortFactory.createModelDSPublishPort (this);
        m_modelUSBusPort = RainbowPortFactory.createModelsManagerClientUSPort (this);
        ModelsManager mm = Rainbow.instance ().getRainbowMaster ().modelsManager ();
        try {
            if (!mm.getRegisteredModelTypes ().contains (ExecutionHistoryModelInstance.EXECUTION_HISTORY_TYPE)) {
                mm.registerModelType (ExecutionHistoryModelInstance.EXECUTION_HISTORY_TYPE);
            }
            String historyModelName = "tactic-execution-" + this.id ();
            m_historyModel = new ExecutionHistoryModelInstance (new HashMap<String, ExecutionHistoryData> (),
                                                                historyModelName, "memory");
            mm.registerModel (new ModelReference (historyModelName,
                                                  ExecutionHistoryModelInstance.EXECUTION_HISTORY_TYPE), m_historyModel);
        }
        catch (RainbowModelException e) {
            m_reportingPort.warn (getComponentType (), "Could not create a tactic execution history model", e);
        }
        log ("Tactic executor initialized");
    }

    @Override
    protected void log (String txt) {
        m_reportingPort.info (RainbowComponentT.EXECUTOR, txt);
    }

    @Override
    protected void runAction () {
        if (!m_adaptationDQPort.isEmpty ()) {
            // get the next adaptation in the queue and execute it
            // because this just deals with tactics, then use a TacticExecutionVisitor to visit this adaptation
            m_adaptationTreeExecuting = m_adaptationDQPort.dequeue ();
            log ("Dequeued an adaptation");
            m_done = new CountDownLatch (1);
            TacticExecutionVisitor tacticVisitor = new TacticExecutionVisitor (this, m_modelRef, m_historyModel
                    .getCommandFactory (), m_adaptationTreeExecuting, m_executionThreadGroup, m_done);
            tacticVisitor.start ();

        } else if (m_done != null) { // We have a tactic that is executing
            if (m_done.getCount () == 0) {
                if (!Rainbow.instance ().shouldTerminate ()) {
                    final IAdaptationManager<Tactic> adaptationManager = Rainbow.instance ().getRainbowMaster ()
                            .adaptationManagerForModel (this.m_modelRef.toString ());
                    if (adaptationManager != null) {
                        adaptationManager.markStrategyExecuted (m_adaptationTreeExecuting);
                    }
                }
            }
            m_done = null;
        }
    }

    @Override
    public RainbowComponentT getComponentType () {
        return RainbowComponentT.EXECUTOR;
    }

    @Override
    public void setModelToManage (ModelReference model) {
        m_modelRef = model;

        IModelInstance<IAcmeSystem> mi = Rainbow.instance ().getRainbowMaster ().modelsManager ().getModelInstance
                (model);
        m_adaptationDQPort = RainbowPortFactory.createAdaptationDequeuePort (model);
        if (mi == null ) {
            m_reportingPort.error (RainbowComponentT.EXECUTOR, "Referring to unknown model " + model.getModelName ()
                    + ":" + model.getModelType ());
        }
        if (!(mi instanceof AcmeModelInstance)) {
            m_reportingPort.error (RainbowComponentT.EXECUTOR, "Referring to non-Acme model " + model);
            throw new IllegalArgumentException ("Referring to non-Acme model " + model);
        }
        m_model = (AcmeModelInstance )mi;
        m_executionThreadGroup = new ThreadGroup (m_modelRef.toString () + " ThreadGroup");

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
        m_modelDSPort.dispose ();
        m_reportingPort.dispose ();
        if (m_adaptationDQPort != null)
            m_adaptationDQPort.dispose ();
    }
}
