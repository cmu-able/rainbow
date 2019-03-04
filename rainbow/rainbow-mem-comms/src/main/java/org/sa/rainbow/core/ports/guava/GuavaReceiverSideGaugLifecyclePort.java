package org.sa.rainbow.core.ports.guava;

import java.util.LinkedList;
import java.util.List;

import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.gauges.IGaugeIdentifier;
import org.sa.rainbow.core.gauges.IGaugeProtocol;
import org.sa.rainbow.core.ports.IGaugeLifecycleBusPort;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.ChannelT;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.IGuavaMessageListener;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

public class GuavaReceiverSideGaugLifecyclePort implements IGaugeLifecycleBusPort {
	class MessageGaugeIdentifier implements IGaugeIdentifier {

		private final IRainbowMessage m_msg;

		public MessageGaugeIdentifier(IRainbowMessage msg) {
			m_msg = msg;
		}

		@Override
		public String id() {
			return (String) m_msg.getProperty(IGaugeProtocol.ID);
		}

		@Override
		public TypedAttribute gaugeDesc() {
			return new TypedAttribute((String) m_msg.getProperty(IGaugeProtocol.GAUGE_NAME),
					(String) m_msg.getProperty(IGaugeProtocol.GAUGE_TYPE));
		}

		@Override
		public TypedAttribute modelDesc() {
			return new TypedAttribute((String) m_msg.getProperty(IGaugeProtocol.MODEL_NAME),
					(String) m_msg.getProperty(IGaugeProtocol.MODEL_TYPE));
		}

	}

	private GuavaEventConnector m_eventBust;
	private IGaugeLifecycleBusPort m_manager;

	public GuavaReceiverSideGaugLifecyclePort(IGaugeLifecycleBusPort manager) {
		m_manager = manager;
		m_eventBust = new GuavaEventConnector(ChannelT.HEALTH);
		m_eventBust.addListener(new IGuavaMessageListener() {

			@Override
			public void receive(GuavaRainbowMessage msg) {
				String type = (String) msg.getProperty(ESEBConstants.MSG_TYPE_KEY);
				MessageGaugeIdentifier mgi;
				switch (type) {
				case IGaugeProtocol.GAUGE_CREATED:
					mgi = new MessageGaugeIdentifier(msg);
					reportCreated(mgi);
					break;
				case IGaugeProtocol.GAUGE_DELETED:
					mgi = new MessageGaugeIdentifier(msg);
					reportDeleted(mgi);
					break;
				case IGaugeProtocol.GAUGE_HEARTBEAT:
					mgi = new MessageGaugeIdentifier(msg);
					sendBeacon(mgi);
					break;
				case IGaugeProtocol.GAUGE_CONFIGURED: {
					mgi = new MessageGaugeIdentifier(msg);
					List<TypedAttributeWithValue> params = new LinkedList<>();

					int i = 0;
					do {
						String name = (String) msg.getProperty(IGaugeProtocol.CONFIG_PARAM_NAME + i);
						if (name == null) {
							break;
						}
						String t = (String) msg.getProperty(IGaugeProtocol.CONFIG_PARAM_TYPE + i);
						Object v = msg.getProperty(IGaugeProtocol.CONFIG_PARAM_VALUE + i);
						i++;
						params.add(new TypedAttributeWithValue(name, t, v));
					} while (true);
					reportConfigured(mgi, params);
				}
					break;
				}
			}
		});

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reportCreated(IGaugeIdentifier gauge) {
		m_manager.reportCreated(gauge);
	}

	@Override
	public void reportDeleted(IGaugeIdentifier gauge) {
		m_manager.reportDeleted(gauge);
	}

	@Override
	public void reportConfigured(IGaugeIdentifier gauge, List<TypedAttributeWithValue> configParams) {
		m_manager.reportConfigured(gauge, configParams);
	}

	@Override
	public void sendBeacon(IGaugeIdentifier gauge) {
		m_manager.sendBeacon(gauge);
	}

}
