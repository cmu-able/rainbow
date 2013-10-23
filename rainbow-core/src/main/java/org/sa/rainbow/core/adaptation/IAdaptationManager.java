package org.sa.rainbow.core.adaptation;

import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.IRainbowReportingPort;

public interface IAdaptationManager<S> extends IRainbowRunnable {

    public abstract void setModelToManage (String modelName, String modelType);

    public abstract void markStrategyExecuted (S strategy);

    public abstract void initialize (IRainbowReportingPort port) throws RainbowConnectionException;

    public abstract void setEnabled (boolean enabled);

}
