/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.sa.rainbow.core.ports.eseb;

import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.IESEBListener;

import java.io.IOException;
import java.util.EnumSet;

public class ESEBRainbowReportingSubscriberPort extends AbstractESEBDisposablePort implements
IRainbowReportingSubscriberPort {

    private IRainbowReportingSubscriberCallback m_reportTo;
    private final EnumSet<RainbowComponentT> m_components = EnumSet.noneOf (RainbowComponentT.class);
    private final EnumSet<ReportType> m_reports = EnumSet.noneOf (ReportType.class);

    public ESEBRainbowReportingSubscriberPort (IRainbowReportingSubscriberCallback reportTo) throws IOException {
        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (), ChannelT.UIREPORT);
        m_reportTo = reportTo;
        getConnectionRole().addListener (new IESEBListener () {

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
