package org.sa.rainbow.testing.prepare.stub.ports;	

 import org.apache.log4j.Logger;	
import org.sa.rainbow.core.RainbowComponentT;	
import org.sa.rainbow.core.ports.IRainbowReportingPort;	

 /**	
 * This is a stubbed IRainbowReportingPort instance that logs all events with a logger.	
 */	
public class LoggerRainbowReportingPort implements IRainbowReportingPort {	
    private static Logger LOGGER = Logger.getLogger(LoggerRainbowReportingPort.class);	

     @Override	
    public void fatal(RainbowComponentT type, String msg, Throwable e, Logger logger) {	
        logger.fatal(msg, e);	
    }	

     @Override	
    public void fatal(RainbowComponentT type, String msg, Logger logger) {	
        logger.fatal(msg);	
    }	

     @Override	
    public void fatal(RainbowComponentT type, String msg, Throwable e) {	
        fatal(type, msg, e, LOGGER);	
    }	

     @Override	
    public void fatal(RainbowComponentT type, String msg) {	
        fatal(type, msg, LOGGER);	
    }	

     @Override	
    public void error(RainbowComponentT type, String msg, Throwable e, Logger logger) {	
        logger.error(msg, e);	
    }	

     @Override	
    public void error(RainbowComponentT type, String msg, Logger logger) {	
        logger.error(msg);	
    }	

     @Override	
    public void error(RainbowComponentT type, String msg, Throwable e) {	
        error(type, msg, e, LOGGER);	
    }	

     @Override	
    public void error(RainbowComponentT type, String msg) {	
        error(type, msg, LOGGER);	
    }	

     @Override	
    public void warn(RainbowComponentT type, String msg, Throwable e, Logger logger) {	
        logger.warn(msg, e);	
    }	

     @Override	
    public void warn(RainbowComponentT type, String msg, Logger logger) {	
        logger.warn(msg);	
    }	

     @Override	
    public void warn(RainbowComponentT type, String msg, Throwable e) {	
        warn(type, msg, e, LOGGER);	
    }	

     @Override	
    public void warn(RainbowComponentT type, String msg) {	
        warn(type, msg, LOGGER);	
    }	

     @Override	
    public void info(RainbowComponentT type, String msg, Logger logger) {	
        logger.info(msg);	
    }	

     @Override	
    public void info(RainbowComponentT type, String msg) {	
        info(type, msg, LOGGER);	
    }	

     @Override	
    public void trace(RainbowComponentT type, String msg) {	
        LOGGER.trace(msg);	
    }	

     /**	
     * Should be called when this port is no longer required. Implementors should dispose of all resources.	
     */	
    @Override	
    public void dispose() {	

     }	
}