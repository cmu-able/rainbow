package org.sa.rainbow.core.ports;


public interface IRainbowAdaptationEnqueuePort<S> extends IDisposablePort {

    public void offer (S selectedStrategy, Object[] args);

}
