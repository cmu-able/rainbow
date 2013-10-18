package org.sa.rainbow.core.ports;


public interface IRainbowAdaptationEnqueuePort<S> {

    void offer (S selectedStrategy, Object[] args);

}
