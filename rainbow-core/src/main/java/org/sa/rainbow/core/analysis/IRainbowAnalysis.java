package org.sa.rainbow.core.analysis;

import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.IRainbowReportingPort;

public interface IRainbowAnalysis extends Identifiable, IRainbowRunnable {

    public abstract void initialize (IRainbowReportingPort port) throws RainbowConnectionException;

    public abstract void setProperty (String key, String value);

    public abstract String getProperty (String key);

}
