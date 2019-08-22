package org.sa.rainbow.brass.adaptation;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.sa.rainbow.core.AbstractRainbowRunnable;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.adaptation.AdaptationTree;
import org.sa.rainbow.core.adaptation.DefaultAdaptationExecutorVisitor;
import org.sa.rainbow.core.adaptation.IAdaptationExecutor;
import org.sa.rainbow.core.adaptation.IAdaptationManager;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.gauges.IGauge;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort;
import org.sa.rainbow.core.ports.IRainbowAdaptationDequeuePort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;

/**
 * Created by schmerl on 12/13/2016.
 */
public class PlanExecutor extends AbstractRainbowRunnable implements IAdaptationExecutor<BrassPlan> {
    private final class BRASSExecutionVisitor extends DefaultAdaptationExecutorVisitor<BrassPlan> {
        private IRainbowReportingPort m_reporter;

        private BRASSExecutionVisitor (AdaptationTree<BrassPlan> adt, ThreadGroup tg, String threadName,
                CountDownLatch done, IRainbowReportingPort reporter) {
            super (adt, tg, threadName, done, reporter);
            m_reporter = reporter;
        }

        @Override
        protected boolean evaluate (BrassPlan adaptation) {
            Object evaluate = adaptation.evaluate (new Object[0]);
            if (evaluate instanceof Boolean) return ((Boolean )evaluate);
            return false;
        }

        @Override
        protected DefaultAdaptationExecutorVisitor<BrassPlan> spawnNewExecutorForTree (AdaptationTree<BrassPlan> adt,
                java.lang.ThreadGroup g,
                CountDownLatch doneSignal) {
            return new BRASSExecutionVisitor (adt, g, "BRASS", doneSignal, m_reporter);
        }
    }

    public static final String NAME = "BRASS Plan Executor";
    private ModelReference                           m_modelRef;
    private IRainbowAdaptationDequeuePort<BrassPlan> m_adaptationDQPort;
    private IModelDSBusPublisherPort m_modelDSPort;
    private java.lang.ThreadGroup                    m_executionThreadGroup;

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
        if (m_adaptationDQPort != null) {
            m_adaptationDQPort.dispose ();
        }
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
//        	Rainbow.instance().getRainbowMaster().gaugeManager().configureAllGaugews(IGauge.RAINBOW_ADAPTING, "boolean", "true");
            AdaptationTree<BrassPlan> at = m_adaptationDQPort.dequeue ();
            log ("Got a new plan -- executing");
            CountDownLatch latch = new CountDownLatch (1);
            BRASSExecutionVisitor executor = new BRASSExecutionVisitor (at, m_executionThreadGroup, "BRASS Execution",
                    latch, m_reportingPort);

            executor.start ();

            try {
                latch.await (20, TimeUnit.SECONDS);
            }
            catch (InterruptedException e) {
                e.printStackTrace ();
            }

            if (!Rainbow.instance ().shouldTerminate ()) {
                final IAdaptationManager<BrassPlan> adaptationManager = Rainbow.instance ().getRainbowMaster ()
                        .adaptationManagerForModel (this.m_modelRef.toString ());
                if (adaptationManager != null) {
                    adaptationManager.markStrategyExecuted (at);
                }
            }
//            Rainbow.instance().getRainbowMaster().gaugeManager().configureAllGaugews(IGauge.RAINBOW_ADAPTING, "boolean", "false");
        }

    }

    @Override
    public RainbowComponentT getComponentType () {
        return RainbowComponentT.EXECUTOR;
    }

	@Override
	public ModelReference getManagedModel() {
		return m_modelRef;
	}
}
