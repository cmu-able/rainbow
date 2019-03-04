package org.sa.rainbow.core.ports.guava;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowChangeBusSubscription;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort.IRainbowModelChangeCallback;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.ChannelT;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.IGuavaMessageListener;
import org.sa.rainbow.core.util.Pair;

public class GuavaModelChangeBusSubscriptionPort implements IModelChangeBusSubscriberPort {

	private GuavaEventConnector m_eventBus;
	private final Collection<Pair<IRainbowChangeBusSubscription, IRainbowModelChangeCallback>> m_subscribers = new LinkedList<>();

	public GuavaModelChangeBusSubscriptionPort() {
		m_eventBus = new GuavaEventConnector(ChannelT.MODEL_CHANGE);
		m_eventBus.addListener(new IGuavaMessageListener() {

			@Override
			public void receive(GuavaRainbowMessage msg) {
				synchronized (m_subscribers) {
					for (Pair<IRainbowChangeBusSubscription, IRainbowModelChangeCallback> pair : m_subscribers) {
						if (pair.firstValue().matches(msg)) {
							ModelReference mr = new ModelReference(
									(String) msg.getProperty(IModelChangeBusPort.MODEL_NAME_PROP),
									(String) msg.getProperty(IModelChangeBusPort.MODEL_TYPE_PROP));
							pair.secondValue().onEvent(mr, msg);
						}
					}
				}
			}
		});
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void subscribe(IRainbowChangeBusSubscription subscriber, IRainbowModelChangeCallback callback) {
		Pair<IRainbowChangeBusSubscription, IRainbowModelChangeCallback> subscription = new Pair<>(subscriber,
				callback);
		synchronized (m_subscribers) {
			m_subscribers.add(subscription);
		}

	}

	@Override
	public void unsubscribe(IRainbowModelChangeCallback callback) {
		synchronized (m_subscribers) {
			for (Iterator i = m_subscribers.iterator(); i.hasNext();) {
				Pair<IRainbowChangeBusSubscription, IRainbowModelChangeCallback> subscription = (Pair<IRainbowChangeBusSubscription, IRainbowModelChangeCallback>) i
						.next();
				if (subscription.secondValue() == callback) {
					i.remove();
				}
			}
		}
	}

}
