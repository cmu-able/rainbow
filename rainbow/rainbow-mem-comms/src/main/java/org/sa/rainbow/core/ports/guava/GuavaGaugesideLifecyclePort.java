package org.sa.rainbow.core.ports.guava;

import java.util.List;

import org.sa.rainbow.core.gauges.IGaugeIdentifier;
import org.sa.rainbow.core.gauges.IGaugeProtocol;
import org.sa.rainbow.core.ports.IGaugeLifecycleBusPort;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.ChannelT;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

public class GuavaGaugesideLifecyclePort implements IGaugeLifecycleBusPort {

	private GuavaEventConnector m_eventBus;

	public GuavaGaugesideLifecyclePort() {
		m_eventBus = new GuavaEventConnector(ChannelT.HEALTH);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reportCreated(IGaugeIdentifier gauge) {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		setCommonGaugeProperties(msg, gauge);
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, IGaugeProtocol.GAUGE_CREATED);
		m_eventBus.publish(msg);
	}

	private void setCommonGaugeProperties(GuavaRainbowMessage msg, IGaugeIdentifier gauge) {
		msg.setProperty(IGaugeProtocol.ID, gauge.id());
		msg.setProperty(IGaugeProtocol.GAUGE_NAME, gauge.gaugeDesc().getName());
		msg.setProperty(IGaugeProtocol.GAUGE_TYPE, gauge.gaugeDesc().getType());
		msg.setProperty(IGaugeProtocol.MODEL_TYPE, gauge.modelDesc().getType());
		msg.setProperty(IGaugeProtocol.MODEL_NAME, gauge.modelDesc().getName());
	}

	@Override
	public void reportDeleted(IGaugeIdentifier gauge) {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		setCommonGaugeProperties(msg, gauge);
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, IGaugeProtocol.GAUGE_DELETED);
		m_eventBus.publish(msg);

	}

	@Override
	public void reportConfigured(IGaugeIdentifier gauge, List<TypedAttributeWithValue> configParams) {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		setCommonGaugeProperties(msg, gauge);
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, IGaugeProtocol.GAUGE_CONFIGURED);
		int i = 0;
		for (TypedAttributeWithValue tav : configParams) {
			msg.setProperty(IGaugeProtocol.CONFIG_PARAM_NAME + i, tav.getName());
			msg.setProperty(IGaugeProtocol.CONFIG_PARAM_TYPE + i, tav.getType());
			msg.setProperty(IGaugeProtocol.CONFIG_PARAM_VALUE + i, tav.getValue());
			msg.setProperty(IGaugeProtocol.CONFIG_PARAM_VALUE + i, "unknown");
			i++;
		}
		m_eventBus.publish(msg);

	}

	@Override
	public void sendBeacon(IGaugeIdentifier gauge) {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, IGaugeProtocol.GAUGE_HEARTBEAT);
		setCommonGaugeProperties(msg, gauge);
		m_eventBus.publish(msg);
	}

}
