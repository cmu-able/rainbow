package org.sa.rainbow.core.ports;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.RainbowComponentT;

public interface IRainbowReportingPort {

    public abstract void fatal (RainbowComponentT type, String msg, Throwable e, Logger logger);

    public abstract void fatal (RainbowComponentT type, String msg, Logger logger);

    public abstract void fatal (RainbowComponentT type, String msg, Throwable e);

    public abstract void fatal (RainbowComponentT type, String msg);

    public abstract void error (RainbowComponentT type, String msg, Throwable e, Logger logger);

    public abstract void error (RainbowComponentT type, String msg, Logger logger);

    public abstract void error (RainbowComponentT type, String msg, Throwable e);

    public abstract void error (RainbowComponentT type, String msg);

    public abstract void warn (RainbowComponentT type, String msg, Throwable e, Logger logger);

    public abstract void warn (RainbowComponentT type, String msg, Logger logger);

    public abstract void warn (RainbowComponentT type, String msg, Throwable e);

    public abstract void warn (RainbowComponentT type, String msg);

    public abstract void info (RainbowComponentT type, String msg, Logger logger);

    public abstract void info (RainbowComponentT type, String msg);

    public abstract void trace (RainbowComponentT type, String msg);

}
