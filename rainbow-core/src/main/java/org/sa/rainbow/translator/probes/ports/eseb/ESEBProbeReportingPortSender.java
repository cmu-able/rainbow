package org.sa.rainbow.translator.probes.ports.eseb;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.management.ports.eseb.ESEBConnector;
import org.sa.rainbow.core.management.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.management.ports.eseb.ESEBProvider;
import org.sa.rainbow.core.management.ports.eseb.RainbowESEBMessage;
import org.sa.rainbow.core.management.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.translator.probes.IProbe;
import org.sa.rainbow.translator.probes.ports.IProbeReportPort;

public class ESEBProbeReportingPortSender implements IProbeReportPort {
    static Logger         LOGGER = Logger.getLogger (ESEBProbeReportingPortSender.class);
    private ESEBConnector m_role;
    private Identifiable  m_sender;

    public ESEBProbeReportingPortSender (Identifiable probe) throws IOException {
        m_sender = probe;
        m_role = new ESEBConnector (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (),
                ChannelT.SYSTEM_US);

    }

    @Override
    public void reportData (IProbe probe, String data) {
        if (probe.id ().equals (m_sender.id ())) {
            RainbowESEBMessage msg = m_role.createMessage ();
            msg.setProperty (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_PROBE_REPORT);
            msg.setProperty (ESEBConstants.MSG_PROBE_ID_KEY, m_sender.id ());
            msg.setProperty (ESEBConstants.MSG_DATA_KEY, data);
            m_role.publish (msg);
        }
        else {
            LOGGER.error (MessageFormat.format ("Attempt to send a report on {0}''s reporting port by {1}", m_sender.id (), probe.id ()));
        }
    }

}
