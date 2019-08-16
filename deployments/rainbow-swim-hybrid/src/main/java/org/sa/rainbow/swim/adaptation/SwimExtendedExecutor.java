package org.sa.rainbow.swim.adaptation;

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

public class SwimExtendedExecutor extends AbstractRainbowRunnable 
	implements IAdaptationExecutor<SwimExtendedPlan> {

    private final class SWIMEXTExecutionVisitor extends DefaultAdaptationExecutorVisitor<SwimExtendedPlan> {
        private IRainbowReportingPort m_reporter;

		public SWIMEXTExecutionVisitor(AdaptationTree<SwimExtendedPlan> adt, ThreadGroup tg, String threadName,
				CountDownLatch done, IRainbowReportingPort reporter) {
			super(adt, tg, threadName, done, reporter);
            m_reporter = reporter;
		}

		@Override
		protected boolean evaluate(SwimExtendedPlan adaptation) {
            Object evaluate = adaptation.evaluate (new Object[0]);
            if (evaluate instanceof Boolean) return ((Boolean )evaluate);
            return false;
		}

		@Override
		protected DefaultAdaptationExecutorVisitor<SwimExtendedPlan> spawnNewExecutorForTree(
				AdaptationTree<SwimExtendedPlan> adt, ThreadGroup g, CountDownLatch doneSignal) {
			return new SWIMEXTExecutionVisitor (adt, g, "SWIMEXT", doneSignal, m_reporter);
		}      
    }
	
	
	public static final String NAME = "Swim Extended Plan Executor";
    private ModelReference                           m_modelRef;
    private IRainbowAdaptationDequeuePort<SwimExtendedPlan> m_adaptationDQPort;
    private IModelDSBusPublisherPort m_modelDSPort;
    private java.lang.ThreadGroup                    m_executionThreadGroup;
    /**
     * Default Constructor with name for the thread.
     */
    public SwimExtendedExecutor () {
        super (NAME);
    }

    @Override
    public void initialize (IRainbowReportingPort port) throws RainbowConnectionException {
        super.initialize (port);

        // Create the model DS port where operations go (and effectors listen)
        m_modelDSPort = RainbowPortFactory.createModelDSPublishPort (this);
    }


	@Override
	public void dispose() {
		if (m_adaptationDQPort != null) {
            m_adaptationDQPort.dispose ();
        }
        m_modelDSPort.dispose ();
        m_reportingPort.dispose ();
	}

	@Override
	public void setModelToManage(ModelReference modelRef) {
        m_modelRef = modelRef;
        m_adaptationDQPort = RainbowPortFactory.createAdaptationDequeuePort (m_modelRef);
        m_executionThreadGroup = new ThreadGroup (m_modelRef.toString () + " ThreadGroup");
		
	}
	@Override
	public IModelDSBusPublisherPort getOperationPublishingPort() {
		return m_modelDSPort;
	}
	@Override
	public IRainbowReportingPort getReportingPort() {
		return m_reportingPort;
	}
	@Override
	protected void log(String txt) {
		m_reportingPort.info (RainbowComponentT.EXECUTOR, txt);
	}
	@Override
	protected void runAction() {
	   if (m_adaptationDQPort != null && !m_adaptationDQPort.isEmpty ()) {
            AdaptationTree<SwimExtendedPlan> at = m_adaptationDQPort.dequeue ();
            log ("Got a new plan -- executing");
            CountDownLatch latch = new CountDownLatch (1);
            SWIMEXTExecutionVisitor executor = new SWIMEXTExecutionVisitor 
                (at, m_executionThreadGroup, "SWIMEXT Execution", latch, m_reportingPort);
            executor.start();
            try {
                latch.await (20, TimeUnit.SECONDS);
            }
            catch (InterruptedException e) {
                e.printStackTrace ();
            }

            if (!Rainbow.instance ().shouldTerminate ()) {
                final IAdaptationManager<SwimExtendedPlan> adaptationManager = Rainbow.instance ().getRainbowMaster ()
                        .adaptationManagerForModel (this.m_modelRef.toString ());
                if (adaptationManager != null) {
                    adaptationManager.markStrategyExecuted (at);
                } else {
                    log("AM null");
                }
            }
       }
	}
	@Override
	public RainbowComponentT getComponentType() {
		return RainbowComponentT.EXECUTOR;
	}



}
