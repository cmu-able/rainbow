package org.sa.rainbow.core.ports.guava;

import java.util.List;

import org.sa.rainbow.core.IRainbowEnvironment;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.RainbowEnvironmentDelegate;
import org.sa.rainbow.core.ports.IEffectorLifecycleBusPort;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.eseb.RainbowESEBMessage;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.ChannelT;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;
import org.sa.rainbow.translator.effectors.IEffectorProtocol;

public class GuavaEffectorSideLifecyclePort implements IEffectorLifecycleBusPort {

	private GuavaEventConnector m_eventBus;
	
	protected static IRainbowEnvironment m_rainbowEnvironment = new RainbowEnvironmentDelegate();


	public GuavaEffectorSideLifecyclePort() {
		m_eventBus = new GuavaEventConnector(ChannelT.HEALTH);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reportCreated(IEffectorIdentifier effector) {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, IEffectorProtocol.EFFECTOR_CREATED);
		setCommonEffectorProperties(effector, msg);
		m_eventBus.publish(msg);
	}

	@Override
	public void reportDeleted(IEffectorIdentifier effector) {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, IEffectorProtocol.EFFECTOR_DELETED);
		setCommonEffectorProperties(effector, msg);
		m_eventBus.publish(msg);
	}

	@Override
	public void reportExecuted(IEffectorIdentifier effector, Outcome outcome, List<String> args) {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, IEffectorProtocol.EFFECTOR_EXECUTED);
		setCommonEffectorProperties(effector, msg);
		msg.setProperty(IEffectorProtocol.OUTCOME, outcome.name());
		msg.setProperty(IEffectorProtocol.ARGUMENT + IEffectorProtocol.SIZE, args.size());
		for (int i = 0; i < args.size(); i++) {
			msg.setProperty(IEffectorProtocol.ARGUMENT + i, args.get(i));
		}
		m_eventBus.publish(msg);
	}

	private void setCommonEffectorProperties(IEffectorIdentifier effector, GuavaRainbowMessage msg) {
		msg.setProperty(IEffectorProtocol.ID, effector.id());
		msg.setProperty(IEffectorProtocol.SERVICE, effector.service());
		msg.setProperty(IEffectorProtocol.KIND, effector.kind().name());
		msg.setProperty(IEffectorProtocol.LOCATION,
				m_rainbowEnvironment.getProperty(RainbowConstants.PROPKEY_DEPLOYMENT_LOCATION));
	}

	@Override
	public void reportExecuting(IEffectorIdentifier effector, List<String> args) {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, IEffectorProtocol.EFFECTOR_EXECUTING);
		setCommonEffectorProperties(effector, msg);
		msg.setProperty(IEffectorProtocol.ARGUMENT + IEffectorProtocol.SIZE, args.size());
		for (int i = 0; i < args.size(); i++) {
			msg.setProperty(IEffectorProtocol.ARGUMENT + i, args.get(i));
		}
		m_eventBus.publish(msg);
	}
}
