package org.sa.rainbow.core.ports;

import org.sa.rainbow.core.adaptation.AdaptationTree;
import org.sa.rainbow.core.adaptation.IEvaluable;

public interface IRainbowAdaptationDequeuePort<S extends IEvaluable> extends IDisposablePort {
    AdaptationTree<S> dequeue ();

    boolean isEmpty ();
}
