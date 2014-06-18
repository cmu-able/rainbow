package org.sa.rainbow.core.ports.eseb;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.ports.IProbeReportPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.translator.probes.IProbeIdentifier;

public class ESEBProbeReportingPortSender extends AbstractESEBDisposablePort implements IProbeReportPort {
    static Logger         LOGGER = Logger.getLogger (ESEBProbeReportingPortSender.class);
    private Identifiable  m_sender;

    public ESEBProbeReportingPortSender (Identifiable probe) throws IOException {
        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (), ChannelT.SYSTEM_US);
        m_sender = probe;

    }

    @Override
    public void reportData (IProbeIdentifier probe, String data) {
        if (probe.id ().equals (m_sender.id ())) {
            RainbowESEBMessage msg = getConnectionRole().createMessage ();
            msg.setProperty (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_PROBE_REPORT);
            msg.setProperty (ESEBConstants.MSG_PROBE_ID_KEY, m_sender.id ());
            msg.setProperty (ESEBConstants.MSG_PROBE_LOCATION_KEY, probe.location ());
            msg.setProperty (ESEBConstants.MSG_PROBE_TYPE_KEY, probe.type ());
            msg.setProperty (ESEBConstants.MSG_DATA_KEY, data);
            getConnectionRole().publish (msg);
        }
        else {
            LOGGER.error (MessageFormat.format ("Attempt to send a report on {0}''s reporting port by {1}", m_sender.id (), probe.id ()));
        }
    }

}
