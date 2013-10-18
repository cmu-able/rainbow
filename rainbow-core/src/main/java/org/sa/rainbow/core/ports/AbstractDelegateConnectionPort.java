package org.sa.rainbow.core.ports;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowDelegate;

public abstract class AbstractDelegateConnectionPort implements IMasterConnectionPort, IRainbowReportingPort {

    protected RainbowDelegate m_delegate;

    public AbstractDelegateConnectionPort (RainbowDelegate delegate) {
        m_delegate = delegate;
    }

    public void report (ReportType type, RainbowComponentT compType, String msg) {
        report (m_delegate.getId (), type, compType, msg);
    }

    public void report (ReportType type, RainbowComponentT compType, String msg, Throwable t) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        PrintStream ps = new PrintStream (baos);
        t.printStackTrace (ps);
        ps.close ();
        report (m_delegate.getId (), type, compType,
                MessageFormat.format ("{0}.\nException: {1}\n{2}", msg, t.getMessage (), baos.toString ()));
    }

    @Override
    public void info (RainbowComponentT type, String msg) {
        report (ReportType.INFO, type, msg);
    }

    @Override
    public void info (RainbowComponentT type, String msg, Logger logger) {
        logger.info (msg);
        report (ReportType.INFO, type, msg);
    }

    @Override
    public void warn (RainbowComponentT type, String msg) {
        report (ReportType.WARNING, type, msg);
    }

    @Override
    public void warn (RainbowComponentT type, String msg, Throwable e) {
        report (ReportType.WARNING, type, msg, e);
    }

    @Override
    public void warn (RainbowComponentT type, String msg, Logger logger) {
        logger.warn (msg);
        report (ReportType.WARNING, type, msg);
    }

    @Override
    public void warn (RainbowComponentT type, String msg, Throwable e, Logger logger) {
        logger.warn (msg, e);
        report (ReportType.WARNING, type, msg, e);
    }

    @Override
    public void error (RainbowComponentT type, String msg) {
        report (ReportType.ERROR, type, msg);
    }

    @Override
    public void error (RainbowComponentT type, String msg, Throwable e) {
        report (ReportType.ERROR, type, msg, e);
    }

    @Override
    public void error (RainbowComponentT type, String msg, Logger logger) {
        logger.error (msg);
        report (ReportType.ERROR, type, msg);
    }

    @Override
    public void error (RainbowComponentT type, String msg, Throwable e, Logger logger) {
        logger.error (msg, e);
        report (ReportType.ERROR, type, msg, e);
    }

    @Override
    public void fatal (RainbowComponentT type, String msg) {
        report (ReportType.FATAL, type, msg);
    }

    @Override
    public void fatal (RainbowComponentT type, String msg, Throwable e) {
        report (ReportType.FATAL, type, msg, e);
    }

    @Override
    public void fatal (RainbowComponentT type, String msg, Logger logger) {
        logger.fatal (msg);
        report (ReportType.FATAL, type, msg);
    }

    @Override
    public void fatal (RainbowComponentT type, String msg, Throwable e, Logger logger) {
        logger.fatal (msg, e);
        report (ReportType.FATAL, type, msg, e);
    }

}
