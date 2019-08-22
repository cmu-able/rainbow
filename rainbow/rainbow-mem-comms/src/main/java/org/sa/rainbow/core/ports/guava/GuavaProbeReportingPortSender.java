package org.sa.rainbow.core.ports.guava;

import java.text.MessageFormat;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.ports.IProbeReportPort;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.ChannelT;
import org.sa.rainbow.translator.probes.IProbe;
import org.sa.rainbow.translator.probes.IProbeIdentifier;

public class GuavaProbeReportingPortSender implements IProbeReportPort {

	private static final Logger LOGGER = Logger.getLogger(GuavaProbeReportingPortSender.class);
	private IProbe m_probe;
	private GuavaEventConnector m_eventBus;

	public GuavaProbeReportingPortSender(IProbe probe) {
		m_eventBus = new GuavaEventConnector(ChannelT.SYSTEM_US);
		m_probe = probe;
	}

	@Override
	public void dispose() {

	}

	@Override
	public void reportData(IProbeIdentifier probe, String data) {
		if (probe.id().equals(m_probe.id())) {
			GuavaRainbowMessage msg = new GuavaRainbowMessage();
            msg.setProperty (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_PROBE_REPORT);
            msg.setProperty (ESEBConstants.MSG_PROBE_ID_KEY, m_probe.id ());
            msg.setProperty (ESEBConstants.MSG_PROBE_LOCATION_KEY, probe.location ());
            msg.setProperty (ESEBConstants.MSG_PROBE_TYPE_KEY, probe.type ());
            msg.setProperty (ESEBConstants.MSG_DATA_KEY, data);
            m_eventBus.publish(msg);
		}
		else {
            LOGGER.error (MessageFormat.format ("Attempt to send a report on {0}''s reporting port by {1}", m_probe.id (), probe.id ()));

		}
	}

}
