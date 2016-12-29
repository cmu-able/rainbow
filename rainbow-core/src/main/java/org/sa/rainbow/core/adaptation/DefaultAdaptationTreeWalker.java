package org.sa.rainbow.core.adaptation;

import org.sa.rainbow.core.ports.IRainbowReportingPort;

import java.util.Collection;

/**
 * Created by schmerl on 8/10/2016.
 */
public abstract class DefaultAdaptationTreeWalker<S extends IEvaluable> implements IAdaptationVisitor<S> {

    private final AdaptationTree<S> m_adtToVisit;

    public DefaultAdaptationTreeWalker (AdaptationTree<S> adt) {
        m_adtToVisit = adt;
    }

    @Override
    public boolean visitLeaf (AdaptationTree<S> tree) {
        S s = tree.getHead ();
        this.evaluate (s);
        return true;
    }

    @Override
    public boolean visitSequence (AdaptationTree<S> tree) {
        Collection<AdaptationTree<S>> subTrees = tree.getSubTrees ();
        for (AdaptationTree<S> adt : subTrees) {
            adt.visit (this);
        }
        return true;
    }

    @Override
    public boolean visitSequenceStopSuccess (AdaptationTree<S> tree) {
        return visitSequence (tree);
    }

    @Override
    public boolean visitSequenceStopFailure (AdaptationTree<S> tree) {
        return visitSequence (tree);
    }

    @Override
    public boolean visitParallel (AdaptationTree<S> tree) {
       return visitSequence (tree);
    }

    protected abstract void evaluate (S adaptation);
}
