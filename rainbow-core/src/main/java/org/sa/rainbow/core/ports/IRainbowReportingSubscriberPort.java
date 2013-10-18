package org.sa.rainbow.core.ports;

import java.util.EnumSet;

import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.ports.IMasterConnectionPort.ReportType;

public interface IRainbowReportingSubscriberPort {

    public interface IRainbowReportingSubscriberCallback {
        public void report (RainbowComponentT component, ReportType type, String message);
    }
    public void subscribe (EnumSet<RainbowComponentT> components, EnumSet<ReportType> reports);
    public void unsubscribe (EnumSet<RainbowComponentT> components, EnumSet<ReportType> reports);

}
