package org.sa.rainbow.stitch.adaptation;

import java.util.concurrent.CountDownLatch;

import org.sa.rainbow.core.adaptation.AdaptationTree;
import org.sa.rainbow.core.adaptation.DefaultAdaptationExecutorVisitor;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.stitch.core.Strategy;
import org.sa.rainbow.stitch.core.Var;
import org.sa.rainbow.stitch.tactic.history.ExecutionHistoryCommandFactory;
import org.sa.rainbow.stitch.tactic.history.ExecutionHistoryModelInstance;
import org.sa.rainbow.stitch.util.ExecutionHistoryData.ExecutionStateT;

/**
 * This class specializes an adaptation visitor to evaluate Stitch-based adaptations. This enables the parallel,
 * sequential, until_fail, and until_success execution semantics to work with strategies.
 * 
 * @author Bradley Schmerl: schmerl
 *
 */
public class StitchExecutionVisitor extends DefaultAdaptationExecutorVisitor<Strategy> {

    private StitchExecutor m_executor;
    private ModelReference m_modelRef;
    private ExecutionHistoryCommandFactory m_historyFactory;

    public StitchExecutionVisitor (StitchExecutor executor, ModelReference modelRef,
            ExecutionHistoryCommandFactory factory, AdaptationTree<Strategy> adt,
            ThreadGroup tg,
            CountDownLatch done) {
        super (adt, tg, modelRef + " Visitor", done, executor.getReportingPort ());
        m_executor = executor;
        m_modelRef = modelRef;
        m_historyFactory = factory;
    }

    /**
     * Evaluate the stitch strSttategy. This evaluation should store the success or otherwise in a model of the
     * strategy, and should mark it executed on the strategy model.
     */
    @Override
    protected boolean evaluate (Strategy adaptation) {
        adaptation.setExecutor (m_executor);
        m_executor.log ("Executing Strategy " + adaptation.getName () + "...");
        Strategy.Outcome o = null;
        // provide var fo _dur_
        Var v = new Var ();
        v.scope = adaptation.stitch ().scope;
        v.setType ("long");
        v.name = "_dur_";
        v.setValue (0L);
        adaptation.stitch ().script.addVar (v.name, v);
        m_executor.getHistoryModelUSPort ().updateModel (
                m_historyFactory.strategyExecutionStateCommand (adaptation.getQualifiedName (),
                        ExecutionHistoryModelInstance.STRATEGY,
                        ExecutionStateT.STARTED, null));
        o = (Strategy.Outcome )adaptation.evaluate (null);
        adaptation.stitch ().script.vars ().remove (v.name);
        m_executor.log (" - Outcome: " + o);
        m_executor.getHistoryModelUSPort ().updateModel (
                m_historyFactory.strategyExecutionStateCommand (adaptation.getQualifiedName (),
                        ExecutionHistoryModelInstance.STRATEGY,
                        ExecutionStateT.FINISHED, o.toString ()));
        return o == Strategy.Outcome.SUCCESS;

    }

    /**
     * Return a new instance to manage the execution of an adaptation in a new thread.
     */
    @Override
    protected DefaultAdaptationExecutorVisitor spawnNewExecutorForTree (AdaptationTree adt,
            ThreadGroup g,
            CountDownLatch doneSignal) {
        return new StitchExecutionVisitor (m_executor, m_modelRef, m_historyFactory, adt, g, doneSignal);
    }

}
