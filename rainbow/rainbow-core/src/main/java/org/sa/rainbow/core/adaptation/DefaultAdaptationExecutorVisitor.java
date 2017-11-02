package org.sa.rainbow.core.adaptation;

import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.ports.IRainbowReportingPort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * An abstract class that defines the semantics for executing adaptation trees. Subclasses will define how to evaluate a
 * particular adapatation leaf, and how to return a new visitor in the case of requiring separate threads. This visitor
 * runs as its own thread.
 * 
 * @author Bradley Schmerl: schmerl
 *
 * @param <S>
 */
public abstract class DefaultAdaptationExecutorVisitor<S extends IEvaluable> extends Thread implements IAdaptationVisitor<S> {

    /** The tree to visit **/
    private final AdaptationTree<S> m_adtToVisit;
    /** The latch used to indicate when this thread is finished (i.e., the tree has been visited) **/
    private final CountDownLatch m_done;
    /** The result of executing this tree **/
    private boolean m_result = true;
    private final IRainbowReportingPort m_reporter;

    public DefaultAdaptationExecutorVisitor (AdaptationTree<S> adt, ThreadGroup tg, String threadName,
            CountDownLatch done, IRainbowReportingPort reporter) {
        super (tg, threadName);
        m_adtToVisit = adt;
        m_done = done;
        m_reporter = reporter;
    }

    @Override
    public void run () {
        //Start the visitor
        m_result = m_adtToVisit.visit (this);
        // Indicate that the execution of this tree has finished
        if (m_done != null) {
            m_done.countDown ();
        }

    }

    @Override
    public final boolean visitLeaf (AdaptationTree<S> tree) {
        S s = tree.getHead ();
        m_reporter.info (RainbowComponentT.EXECUTOR, "Visiting execution leaf");
        Object evaluate = this.evaluate (s);
        if (evaluate instanceof Boolean) return (Boolean )evaluate;
        return false;
    }

    /**
     * Evaluates an adaptation
     * 
     * @param adaptation
     *            The adaptation to evaluate
     * @return true if the adaptation successfully completes
     */
    protected abstract boolean evaluate (S adaptation);

    @Override
    public final boolean visitSequence (AdaptationTree<S> tree) {
        Collection<AdaptationTree<S>> subTrees = tree.getSubTrees ();
        // Will return true of all branches are successful
        boolean ret = true;
        for (AdaptationTree<S> adt : subTrees) {
            ret &= adt.visit (this);
        }
        return ret;
    }

    @Override
    public final boolean visitParallel (AdaptationTree<S> tree) {
        Collection<AdaptationTree<S>> subTrees = tree.getSubTrees ();
        // Create a new thread group to manage the execution of these branches
        ThreadGroup g = new ThreadGroup (this.getThreadGroup (), "");
        // Create a latch to keep track of which branches have finished
        CountDownLatch doneSignal = new CountDownLatch (subTrees.size ());
        // Create a list to store the executor thread for each branch
        List<DefaultAdaptationExecutorVisitor<S>> nodes = new ArrayList<> (subTrees.size ());
        // Spawn a new executor for each branch
        for (AdaptationTree<S> adt : subTrees) {
            DefaultAdaptationExecutorVisitor<S> aexec = spawnNewExecutorForTree (adt, g, doneSignal);
            nodes.add (aexec);
            aexec.start ();
        }
        // Wait until all the subthreads have finished
        try {
            doneSignal.await ();
        }
        catch (InterruptedException e) {
        }
        // Calculate the results, which will be false
        // if at least one of the branch's results was fault
        for (DefaultAdaptationExecutorVisitor<S> e : nodes) {
            if (!e.m_result) {
                m_result = false;
            }
        }
//        for (int i = 0; i < subTrees.size (); i++) {
//            m_done.countDown ();
//        }
        return m_result;
    }

    protected abstract DefaultAdaptationExecutorVisitor<S> spawnNewExecutorForTree (AdaptationTree<S> adt,
            ThreadGroup g,
            CountDownLatch doneSignal);

    @Override
    public final boolean visitSequenceStopSuccess (AdaptationTree<S> tree) {
        Collection<AdaptationTree<S>> subTrees = tree.getSubTrees ();
        for (AdaptationTree<S> adt : subTrees) {
            boolean ret = adt.visit (this);
            if (ret) return true;
        }
        return false;
    }

    @Override
    public final boolean visitSequenceStopFailure (AdaptationTree<S> tree) {
        Collection<AdaptationTree<S>> subTrees = tree.getSubTrees ();
        for (AdaptationTree<S> adt : subTrees) {
            boolean ret = adt.visit (this);
            if (!ret) return false;
        }
        return true;
    }

}
