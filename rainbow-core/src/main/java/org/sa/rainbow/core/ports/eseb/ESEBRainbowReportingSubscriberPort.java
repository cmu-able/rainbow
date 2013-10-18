package org.sa.rainbow.core.ports.eseb;

import java.io.IOException;
import java.util.EnumSet;

import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.IESEBListener;

public class ESEBRainbowReportingSubscriberPort implements IRainbowReportingSubscriberPort {

    private ESEBConnector m_connector;
    private IRainbowReportingSubscriberCallback m_reportTo;
    private EnumSet<RainbowComponentT>          m_components = EnumSet.noneOf (RainbowComponentT.class);
    private EnumSet<ReportType>                 m_reports    = EnumSet.noneOf (ReportType.class);

    public ESEBRainbowReportingSubscriberPort (IRainbowReportingSubscriberCallback reportTo) throws IOException {
        m_reportTo = reportTo;
        m_connector = new ESEBConnector (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (),
                ChannelT.UIREPORT);
        m_connector.addListener (new IESEBListener () {

            @Override
            public void receive (RainbowESEBMessage msg) {
                if (msg.getProperty (ESEBConstants.MSG_CHANNEL_KEY).equals (ChannelT.UIREPORT.name ())) {
                    String componentStr = (String )msg.getProperty (ESEBConstants.COMPONENT_TYPE_KEY);
                    String reportTypeStr = (String )msg.getProperty (ESEBConstants.REPORT_TYPE_KEY);
                    RainbowComponentT component = RainbowComponentT.DELEGATE;
                    try {
                        component = RainbowComponentT.valueOf (componentStr);
                    }
                    catch (Exception e) {
                    }
                    ReportType reportType = ReportType.INFO;
                    try {
                        reportType = ReportType.valueOf (reportTypeStr);
                    }
                    catch (Exception e) {
                    }
                    if (m_reports.contains (reportType) && m_components.contains (component)) {
                        m_reportTo.report (component, reportType,
                                (String )msg.getProperty (ESEBConstants.REPORT_MSG_KEY));
                    }

                }
            }
        });
    }

    @Override
    public void subscribe (EnumSet<RainbowComponentT> components, EnumSet<ReportType> reports) {
        if (components != null) {
            m_components.addAll (components);
        }
        if (reports != null) {
            m_reports.addAll (reports);
        }
    }

    @Override
    public void unsubscribe (EnumSet<RainbowComponentT> components, EnumSet<ReportType> reports) {
        if (components != null) {
            m_components.removeAll (components);
        }
        if (reports != null) {
            m_reports.removeAll (reports);
        }
    }

}
