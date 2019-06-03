package org.sa.rainbow.core.ports.guava;

import java.util.List;

import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.eseb.RainbowESEBMessage;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.ChannelT;

public class GuagaChangeBusAnnouncePort implements IModelChangeBusPort {

	private GuavaEventConnector m_eventBus;

	public GuagaChangeBusAnnouncePort() {
		m_eventBus = new GuavaEventConnector(ChannelT.MODEL_CHANGE);
	}

	@Override
	public IRainbowMessage createMessage() {
		return new GuavaRainbowMessage();
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void announce(IRainbowMessage event) {
		if (event instanceof GuavaRainbowMessage) {
			m_eventBus.publish((GuavaRainbowMessage) event);
		} else
			throw new IllegalArgumentException(
					"Attempt to pass a non Guava message to an ESEB connector, or on the wrong channel.");

	}

	@Override
	public void announce(List<? extends IRainbowMessage> events) {
		for (IRainbowMessage msg : events) {
			announce(msg);
		}
	}

}
