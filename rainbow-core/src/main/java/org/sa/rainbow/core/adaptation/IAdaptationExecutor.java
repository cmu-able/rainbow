package org.sa.rainbow.core.adaptation;

import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;

public interface IAdaptationExecutor<S> extends IRainbowRunnable {


    public abstract void setModelToManage (String name, String type);

    public abstract void enqueueStrategy (S selectedStrategy, Object[] args);

    public abstract void initialize (IRainbowReportingPort reportingPort) throws RainbowConnectionException;

    public abstract IModelDSBusPublisherPort getOperationPublishingPort ();
}
