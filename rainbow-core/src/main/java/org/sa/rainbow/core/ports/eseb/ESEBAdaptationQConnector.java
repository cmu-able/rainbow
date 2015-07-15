package org.sa.rainbow.core.ports.eseb;

import java.util.concurrent.LinkedBlockingDeque;

import org.sa.rainbow.core.adaptation.AdaptationTree;
import org.sa.rainbow.core.adaptation.IEvaluable;
import org.sa.rainbow.core.ports.IRainbowAdaptationDequeuePort;
import org.sa.rainbow.core.ports.IRainbowAdaptationEnqueuePort;

public class ESEBAdaptationQConnector<S extends IEvaluable> implements IRainbowAdaptationEnqueuePort<S>, IRainbowAdaptationDequeuePort<S> {

    private LinkedBlockingDeque<AdaptationTree<S>> m_Q;

    public ESEBAdaptationQConnector () {
        m_Q = new LinkedBlockingDeque<AdaptationTree<S>> ();
    }

    @Override
    public void dispose () {
        m_Q.clear ();
        m_Q = null;
    }

    @Override
    public void offerAdaptation (AdaptationTree<S> adaptation, Object[] args) {
        m_Q.offer (adaptation);
    }

    @Override
    public AdaptationTree<S> dequeue () {
        return m_Q.poll ();
    }

    @Override
    public boolean isEmpty () {
        return m_Q.isEmpty ();
    }

}
