package org.sa.rainbow.core.util;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.ports.IRainbowReportingPort;

public class RainbowLogger {

    public static void error (RainbowComponentT compT,
            String msg,
            Throwable e,
            IRainbowReportingPort port,
            Logger logger) {
        if (logger != null) {
            logger.error (msg, e);
        }
        if (port != null) {
            port.error (compT, msg, e);
        }
    }

    public static void error (RainbowComponentT compT, String msg, IRainbowReportingPort port, Logger logger) {
        if (logger != null) {
            logger.error (msg);
        }
        if (port != null) {
            port.error (compT, msg);
        }
    }

    public static void
            warn (RainbowComponentT compT, String msg, Throwable e, IRainbowReportingPort port, Logger logger) {
        if (logger != null) {
            logger.warn (msg, e);
        }
        if (port != null) {
            port.warn (compT, msg, e);
        }
    }

    public static void warn (RainbowComponentT compT, String msg, IRainbowReportingPort port, Logger logger) {
        if (logger != null) {
            logger.warn (msg);
        }
        if (port != null) {
            port.warn (compT, msg);
        }
    }

    public static void fatal (RainbowComponentT compT,
            String msg,
            Throwable e,
            IRainbowReportingPort port,
            Logger logger) {
        if (logger != null) {
            logger.fatal (msg, e);
        }
        if (port != null) {
            port.fatal (compT, msg, e);
        }
    }

    public static void info (RainbowComponentT compT, String msg, IRainbowReportingPort port, Logger logger) {
        if (logger != null) {
            logger.info (msg);
        }
        if (port != null) {
            port.info (compT, msg);
        }
    }

}
