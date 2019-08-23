package org.sa.rainbow.core.ports.guava;

import java.util.Map;

import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.ports.IProbeConfigurationPort;

public class GuavaProbeConfigurationPort implements IProbeConfigurationPort {

	private Identifiable m_probe;
	private IProbeConfigurationPort m_callback;

	public GuavaProbeConfigurationPort(Identifiable probe, IProbeConfigurationPort callback) {
		m_probe = probe;
		m_callback = callback;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void configure(Map<String, Object> configParams) {
		m_callback.configure(configParams);
	}

}
