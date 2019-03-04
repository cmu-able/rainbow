package org.sa.rainbow.core.ports.guava;

import java.util.Map;
import java.util.Map.Entry;

import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.ports.IProbeLifecyclePort;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.ChannelT;
import org.sa.rainbow.translator.probes.IProbe;

public class GuavaProbeLifecyclePort implements IProbeLifecyclePort {

	private IProbe m_probe;
	private GuavaEventConnector m_eventBus;

	public GuavaProbeLifecyclePort(IProbe probe) {
		m_eventBus = new GuavaEventConnector(ChannelT.HEALTH);
		m_probe = probe;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reportCreated() {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		setCommonGaugeProperties(msg);
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, IProbeLifecyclePort.PROBE_CREATED);
		m_eventBus.publish(msg);
	}

	private void setCommonGaugeProperties(GuavaRainbowMessage msg) {
		msg.setProperty(IProbeLifecyclePort.PROBE_ID, m_probe.id());
		msg.setProperty(IProbeLifecyclePort.PROBE_LOCATION, m_probe.location());
		msg.setProperty(IProbeLifecyclePort.PROBE_NAME, m_probe.name());
	}

	@Override
	public void reportDeleted() {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		setCommonGaugeProperties(msg);
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, IProbeLifecyclePort.PROBE_DELETED);
		m_eventBus.publish(msg);

	}

	@Override
	public void reportConfigured(Map<String, Object> configParams) {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		setCommonGaugeProperties(msg);
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, IProbeLifecyclePort.PROBE_CONFIGURED);
		int i = 0;
		for (Entry<String, Object> e : configParams.entrySet()) {
			msg.setProperty(IProbeLifecyclePort.CONFIG_PARAM_NAME + i, e.getKey());
			msg.setProperty(IProbeLifecyclePort.CONFIG_PARAM_VALUE + i, e.getValue());
			i++;
		}
		m_eventBus.publish(msg);

	}

	@Override
	public void reportDeactivated() {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		setCommonGaugeProperties(msg);
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, IProbeLifecyclePort.PROBE_DEACTIVATED);
		m_eventBus.publish(msg);

	}

	@Override
	public void reportActivated() {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		setCommonGaugeProperties(msg);
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, IProbeLifecyclePort.PROBE_ACTIVATED);
		m_eventBus.publish(msg);
	}

}
