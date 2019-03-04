package org.sa.rainbow.core.ports.guava;

import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;
import org.sa.rainbow.core.ports.IDelegateConfigurationPort;

public class LocalDelegateConfigurationProviderPort implements IDelegateConfigurationPort {
	private static final Logger LOGGER = Logger.getLogger(LocalDelegateConfigurationProviderPort.class);
	private RainbowDelegate m_rainbowDelegate;


	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendConfigurationInformation(Properties props, List<ProbeAttributes> probes,
			List<EffectorAttributes> effectors, List<GaugeInstanceDescription> gauges) {
		if (m_rainbowDelegate != null) m_rainbowDelegate.receiveConfigurationInformation(props, probes, effectors, gauges);
		else {
			String message = "Call made to sendConfigurationInformation before provider end created";
			LOGGER.error(message);
			throw new NullPointerException(message);
		}
		
	}

	public void setDelegate(RainbowDelegate rainbowDelegate) {
		m_rainbowDelegate = rainbowDelegate;
		
	}

}
