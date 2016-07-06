package org.sa.rainbow.stitch.adaptation;

import org.sa.rainbow.core.adaptation.AdaptationTree;
import org.sa.rainbow.core.adaptation.DefaultAdaptationExecutorVisitor;
import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.stitch.core.Tactic;
import org.sa.rainbow.stitch.tactic.history.ExecutionHistoryCommandFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Created by schmerl on 6/16/2016.
 */
public class TacticExecutionVisitor extends DefaultAdaptationExecutorVisitor<Tactic>{
    private final TacticExecutor m_executor;
    private final ModelReference m_modelRef;
    private final ExecutionHistoryCommandFactory m_historyFactory;

    public TacticExecutionVisitor (TacticExecutor executor, ModelReference modelRef,
                                   ExecutionHistoryCommandFactory factory, AdaptationTree<Tactic> adt, ThreadGroup tg,
                                   CountDownLatch done) {
        super (adt, tg, modelRef + " Visitor", done, executor.getReportingPort ());
        m_executor = executor;
        m_modelRef = modelRef;
        m_historyFactory = factory;
    }

    @Override
    protected boolean evaluate (Tactic adaptation) {
        m_executor.log ("Executing Strategey " + adaptation.getName () + "...");
        adaptation.evaluate (null);
        return adaptation.checkEffect ();
    }

    @Override
    protected DefaultAdaptationExecutorVisitor<Tactic> spawnNewExecutorForTree (AdaptationTree<Tactic> adt, ThreadGroup g, CountDownLatch doneSignal) {
        return new TacticExecutionVisitor (m_executor, m_modelRef, m_historyFactory, adt, g, doneSignal);
    }
}
