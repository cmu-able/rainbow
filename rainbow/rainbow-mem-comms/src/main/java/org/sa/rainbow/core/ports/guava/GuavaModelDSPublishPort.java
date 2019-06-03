package org.sa.rainbow.core.ports.guava;

import java.util.HashSet;
import java.util.Set;

import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort;
import org.sa.rainbow.core.ports.IModelDSBusSubscriberPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.IESEBListener;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.eseb.RainbowESEBMessage;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.ChannelT;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.IGuavaMessageListener;

public class GuavaModelDSPublishPort implements IModelDSBusPublisherPort, IModelDSBusSubscriberPort {

	private Identifiable m_publisher;
	private final Set<IModelDSBusPublisherPort> m_callbacks = new HashSet<>();
	private GuavaEventConnector m_eventBus;

	public GuavaModelDSPublishPort(Identifiable client) {
		m_publisher = client;
		m_eventBus = new GuavaEventConnector(ChannelT.MODEL_DS);
		m_eventBus.addListener(new IGuavaMessageListener() {

			@Override
			public void receive(GuavaRainbowMessage msg) {
				if (m_callbacks == null || m_callbacks.isEmpty())
					return; // no one interested
				String msgType = (String) msg.getProperty(ESEBConstants.MSG_TYPE_KEY);
				String channel = (String) msg.getProperty(ESEBConstants.MSG_CHANNEL_KEY);
				if (ESEBConstants.MSG_TYPE_UPDATE_MODEL.equals(msgType)) {
					IRainbowOperation cmd = GuavaCommandHelper.msgToCommand(msg);
					for (IModelDSBusPublisherPort callback : m_callbacks) {
						OperationResult result = callback.publishOperation(cmd);
						if (result != null) {
							GuavaRainbowMessage reply = new GuavaRainbowMessage();
							reply.setProperty(ESEBConstants.MSG_REPLY_KEY,
									msg.getProperty(ESEBConstants.MSG_REPLY_KEY));
							reply.setProperty(ESEBConstants.MSG_UPDATE_MODEL_REPLY, result);
							reply.setProperty(ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_REPLY);
							reply.setProperty(ESEBConstants.MSG_DELEGATE_ID_KEY, m_publisher.id());
//	                                    System.out.println ("DS Publishing " + reply.toString ());
							m_eventBus.publish(reply);
						}
					}
//	                    else {
//	                        for (IModelDSBusPublisherPort callback : m_callbacks) {
//	                            callback.publishMessage (msg);
//	                        }
//	                    }
				}
			}
		});
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
	public OperationResult publishOperation(IRainbowOperation cmd) {
		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		msg.setProperty(ESEBConstants.MSG_DELEGATE_ID_KEY, m_publisher.id());
		msg.setProperty(ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_UPDATE_MODEL);
		GuavaCommandHelper.command2Message(cmd, msg);

		final OperationResult result = new OperationResult();
		result.result = Result.FAILURE;
		result.reply = "Operation timed out";
		try {
			m_eventBus.blockingSendAndReceive(msg, new IGuavaMessageListener() {

				@Override
				public void receive(GuavaRainbowMessage m) {
					OperationResult reply = (OperationResult) m.getProperty(ESEBConstants.MSG_UPDATE_MODEL_REPLY);
					result.result = reply.result;
					result.reply = reply.reply;
				}
			}, 1000000);
		} catch (RainbowConnectionException e) {
			result.reply = e.getMessage();
		}
//        System.out.println ("======> publishOperation[RECEIVE]: " + cmd.toString () + " = " + result.result);
		return result;
//    }
	}

	@Override
	public void unsubscribeToOperations(IModelDSBusPublisherPort callback) {
		m_callbacks.remove(callback);

	}

	@Override
	public void subscribeToOperations(IModelDSBusPublisherPort callback) {
		m_callbacks.add(callback);

	}

}
