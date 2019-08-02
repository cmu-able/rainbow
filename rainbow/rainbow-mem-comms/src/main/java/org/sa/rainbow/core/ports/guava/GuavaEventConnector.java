package org.sa.rainbow.core.ports.guava;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class GuavaEventConnector {

	static final Logger LOGGER = Logger.getLogger(GuavaEventConnector.class);

	public enum ChannelT {
		HEALTH, UIREPORT, MODEL_US, MODEL_CHANGE, SYSTEM_US, MODEL_DS
	}

	private static final Map<ChannelT, EventBus> EVENT_BUSSES = new HashMap<>();
	private static final LinkedBlockingQueue<GuavaRainbowMessage> REPLY_Q = new LinkedBlockingQueue<>();
	private static final Map<String, IGuavaMessageListener> m_replyListeners = new HashMap<>();
	private static final ExecutorService POOL = 		Executors.newFixedThreadPool(20);


	private ChannelT m_channel;
	private static Thread REPLY_THREAD = null;

	interface IGuavaMessageListener {
		@Subscribe
		public void receive(GuavaRainbowMessage m);
	}

	public GuavaEventConnector(ChannelT channel) {
		m_channel = channel;
		synchronized (EVENT_BUSSES) {
			if (!EVENT_BUSSES.containsKey(channel)) {
				EventBus b = new EventBus();
//				EventBus b = new AsyncEventBus(Executors.newCachedThreadPool());
//				EventBus b = new AsyncEventBus(Executors.newSingleThreadExecutor());
				EVENT_BUSSES.put(m_channel, b);

			}
		}
		if (REPLY_THREAD == null) {
			REPLY_THREAD  = 	new Thread(new Runnable() {

				@Override
				public void run() {
					GuavaRainbowMessage msg;
					try {
						while ((msg = REPLY_Q.take()) != null) {
							String repKey = (String) msg.getProperty(ESEBConstants.MSG_REPLY_KEY);

							IGuavaMessageListener l;
							synchronized (m_replyListeners) {
								l = m_replyListeners.remove(repKey);
							}
							if (l != null) {
								GuavaRainbowMessage gmsg = new GuavaRainbowMessage(msg);
								sanitizeMessage(gmsg);
								l.receive(gmsg);
							} else {
								LOGGER.error(MessageFormat.format(
										"Received a reply on ESEB for which there is no listener. For reply key: {0}",

										repKey));
								LOGGER.info(msg.toString());
								synchronized (m_replyListeners) {
									for (String rk : m_replyListeners.keySet()) {
										LOGGER.info(rk + " -> " + m_replyListeners.get(rk));
									}
								}
							}
						}
					} catch (InterruptedException e) {
					}
				}

				private void sanitizeMessage(GuavaRainbowMessage gmsg) {
					gmsg.removeProperty(ESEBConstants.MSG_REPLY_KEY);
				}

			}, "REPLIES");
			REPLY_THREAD.start();
		}
	}

	public void publish(GuavaRainbowMessage msg) {
		POOL.execute(() -> EVENT_BUSSES.get(m_channel).post(msg));
	}

	public void sendAndReceive(GuavaRainbowMessage msg, final IGuavaMessageListener receiveListener) {
		final String replyKey = UUID.randomUUID().toString();
		msg.setProperty(ESEBConstants.MSG_REPLY_KEY, replyKey);

		synchronized (m_replyListeners) {
			m_replyListeners.put(replyKey, receiveListener);
		}



		EVENT_BUSSES.get(m_channel).register(new IGuavaMessageListener() {

			@Override
			@Subscribe
			public void receive(GuavaRainbowMessage msg) {
				Object msgType = msg.getProperty(ESEBConstants.MSG_TYPE_KEY);

				if (ESEBConstants.MSG_TYPE_REPLY.equals(msgType)) {
					REPLY_Q.add(msg);
				}
			}

		});
		publish(msg);
	}

	public class BlockingListener implements IGuavaMessageListener {

		private final IGuavaMessageListener m_l;
		boolean ret = false;

		public BlockingListener(IGuavaMessageListener l) {
			m_l = l;
		}

		@Override
		@Subscribe
		public void receive(GuavaRainbowMessage m) {
			m_l.receive(m);
			ret = true;
			synchronized (this) {
				this.notifyAll();
			}
		}
	}

	public void blockingSendAndReceive(GuavaRainbowMessage msg, final IGuavaMessageListener l, long timeout)
			throws RainbowConnectionException {
		BlockingListener bl = new BlockingListener(l);
		synchronized (bl) {
			sendAndReceive(msg, bl);
			try {
				bl.wait(timeout);
			} catch (InterruptedException e) {
			}
		}
		if (!bl.ret) {
			throw new RainbowConnectionException(
					MessageFormat.format("Blocking send and receive did not return in specified time {0}", timeout));

		}
	}

	public void addListener(final IGuavaMessageListener l) {
		EVENT_BUSSES.get(m_channel).register(new IGuavaMessageListener() {

			@Override
			@Subscribe
			public void receive(GuavaRainbowMessage m) {
				if (!ESEBConstants.MSG_TYPE_REPLY.equals(m.getProperty(ESEBConstants.MSG_TYPE_KEY))) {
					GuavaRainbowMessage msg = new GuavaRainbowMessage(m);
					l.receive(msg);
				}
			}
		});
	}

	public void close() {
		EVENT_BUSSES.remove(m_channel);
	}

	public void replyToMessage(GuavaRainbowMessage msg, Object result) {
		String repKey = (String) msg.getProperty(ESEBConstants.MSG_REPLY_KEY);
		if (repKey != null) {
			GuavaRainbowMessage reply = new GuavaRainbowMessage();
			reply.setProperty(ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_REPLY);
			reply.setProperty(ESEBConstants.MSG_REPLY_KEY, repKey);
			reply.setProperty(ESEBConstants.MSG_REPLY_VALUE, result);
			publish(reply);
		}
	}

}
