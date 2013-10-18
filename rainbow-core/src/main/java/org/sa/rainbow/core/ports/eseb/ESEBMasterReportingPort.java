package org.sa.rainbow.core.ports.eseb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.util.Util;

public class ESEBMasterReportingPort implements IRainbowReportingPort {
    Logger                LOGGER = Logger.getLogger (this.getClass ());

    private ESEBConnector m_connectionRole;

    public ESEBMasterReportingPort () throws IOException {
        m_connectionRole = new ESEBConnector (
                ESEBProvider.getESEBClientPort (RainbowConstants.PROPKEY_MASTER_CONNECTION_PORT), ChannelT.HEALTH);
    }

    @Override
    public void fatal (RainbowComponentT type, String msg, Throwable e, Logger logger) {
        logger.fatal (msg, e);
        report (ReportType.FATAL, type, msg, e);
    }

    @Override
    public void fatal (RainbowComponentT type, String msg, Logger logger) {
        logger.fatal (msg);
        report (ReportType.FATAL, type, msg);
    }

    @Override
    public void fatal (RainbowComponentT type, String msg, Throwable e) {
        report (ReportType.FATAL, type, msg, e);

    }

    @Override
    public void fatal (RainbowComponentT type, String msg) {
        report (ReportType.FATAL, type, msg);

    }

    @Override
    public void error (RainbowComponentT type, String msg, Throwable e, Logger logger) {
        logger.error (msg, e);
        report (ReportType.ERROR, type, msg, e);
    }

    @Override
    public void error (RainbowComponentT type, String msg, Logger logger) {
        logger.error (msg);
        report (ReportType.ERROR, type, msg);
    }

    @Override
    public void error (RainbowComponentT type, String msg, Throwable e) {
        report (ReportType.ERROR, type, msg, e);

    }

    @Override
    public void error (RainbowComponentT type, String msg) {
        report (ReportType.ERROR, type, msg);

    }

    @Override
    public void warn (RainbowComponentT type, String msg, Throwable e, Logger logger) {
        logger.warn (msg, e);
        report (ReportType.WARNING, type, msg, e);
    }

    @Override
    public void warn (RainbowComponentT type, String msg, Logger logger) {
        logger.warn (msg);
        report (ReportType.WARNING, type, msg);
    }

    @Override
    public void warn (RainbowComponentT type, String msg, Throwable e) {
        report (ReportType.WARNING, type, msg, e);

    }

    @Override
    public void warn (RainbowComponentT type, String msg) {
        report (ReportType.WARNING, type, msg);

    }

    @Override
    public void info (RainbowComponentT type, String msg, Logger logger) {
        logger.info (msg);
        report (ReportType.INFO, type, msg);
    }

    @Override
    public void info (RainbowComponentT type, String msg) {
        report (ReportType.INFO, type, msg);

    }

    public void report (ReportType type, RainbowComponentT compT, String msg) {
        report (type, compT, msg, (String )null);
    }

    public void report (ReportType type, RainbowComponentT compT, String msg, String additionalInfo) {
        LOGGER.log (Util.reportTypeToPriority (type), compT.name () + ": " + msg);
        LOGGER.info (additionalInfo);
        RainbowESEBMessage esebMsg = m_connectionRole.createMessage ();
        esebMsg.setProperty (ESEBConstants.MSG_CHANNEL_KEY, ChannelT.UIREPORT.name ());
        esebMsg.setProperty (ESEBConstants.COMPONENT_TYPE_KEY, compT.name ());
        esebMsg.setProperty (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_UI_REPORT);
        esebMsg.setProperty (ESEBConstants.REPORT_TYPE_KEY, type.name ());
        esebMsg.setProperty (ESEBConstants.REPORT_MSG_KEY, msg);
        esebMsg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, "master");
        if (additionalInfo != null) {
            esebMsg.setProperty (ESEBConstants.REPORT_MSG_ADDITIONAL_INFO, additionalInfo);
        }
        m_connectionRole.publish (esebMsg);

    }

    public void report (ReportType type, RainbowComponentT compType, String msg, Throwable t) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        PrintStream ps = new PrintStream (baos);
        t.printStackTrace (ps);
        ps.close ();
        report (type, compType, MessageFormat.format ("{0}. Exception: {1}.", msg, t.getMessage ()), baos.toString ());
    }

    @Override
    public void trace (RainbowComponentT type, String msg) {
        if (LOGGER.isTraceEnabled ()) {
            LOGGER.trace (msg);
        }
    }

}
