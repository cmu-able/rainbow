package org.sa.rainbow.core.ports.guava;

import java.util.EnumSet;

import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;
import org.sa.rainbow.core.ports.IRainbowReportingSubscriberPort;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.ChannelT;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.IGuavaMessageListener;

public class GuavaRainbowReportingSubscriberPort implements IRainbowReportingSubscriberPort {
    private IRainbowReportingSubscriberCallback m_reportTo;
    private final EnumSet<RainbowComponentT> m_components = EnumSet.noneOf (RainbowComponentT.class);
    private final EnumSet<ReportType> m_reports = EnumSet.noneOf (ReportType.class);
	private GuavaEventConnector m_eventBus;
    
    public GuavaRainbowReportingSubscriberPort(IRainbowReportingSubscriberCallback reportTo) {
    	m_reportTo = reportTo;
		m_eventBus = new GuavaEventConnector(ChannelT.UIREPORT);
		m_eventBus.addListener(new IGuavaMessageListener() {
			
			@Override
			public void receive(GuavaRainbowMessage msg) {
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
		});
    }
	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void subscribe(EnumSet<RainbowComponentT> components, EnumSet<ReportType> reports) {
	       if (components != null) {
	            m_components.addAll (components);
	        }
	        if (reports != null) {
	            m_reports.addAll (reports);
	        }
	}

	@Override
	public void unsubscribe(EnumSet<RainbowComponentT> components, EnumSet<ReportType> reports) {
        if (components != null) {
            m_components.removeAll (components);
        }
        if (reports != null) {
            m_reports.removeAll (reports);
        }
	}

}
