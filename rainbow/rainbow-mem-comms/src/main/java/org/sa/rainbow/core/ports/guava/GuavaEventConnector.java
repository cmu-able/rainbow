package org.sa.rainbow.core.ports.guava;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class GuavaEventConnector {
	
	static final Logger LOGGER = Logger.getLogger(GuavaEventConnector.class);
	
	public enum ChannelT {
        HEALTH, UIREPORT, MODEL_US, MODEL_CHANGE, SYSTEM_US, MODEL_DS
    }
	
	private static final Map<ChannelT,EventBus> EVENT_BUSSES = new HashMap<>();
	private static final Map<String,IGuavaMessageListener> m_replyListeners = new HashMap<> ();
	
	
	private ChannelT m_channel;
	
	interface IGuavaMessageListener {
		@Subscribe public void receive(GuavaRainbowMessage m);
	}
	
	
	
	public GuavaEventConnector (ChannelT channel) {
		m_channel = channel;
		synchronized (EVENT_BUSSES) {
			if (!EVENT_BUSSES.containsKey(channel)) {
				EventBus b = new EventBus();
//				EventBus b = new AsyncEventBus(Executors.newCachedThreadPool());
//				EventBus b = new AsyncEventBus(Executors.newSingleThreadExecutor());
				EVENT_BUSSES.put(m_channel, b);
			}
		}
	}
	
	public void publish(GuavaRainbowMessage msg) {
		EVENT_BUSSES.get(m_channel).post(msg);
	}
	
	public void sendAndReceive (GuavaRainbowMessage msg, final IGuavaMessageListener receiveListener) {
		final String replyKey = UUID.randomUUID().toString();
		msg.setProperty(ESEBConstants.MSG_REPLY_KEY, replyKey);
		
		synchronized (m_replyListeners) {
			m_replyListeners.put(replyKey, receiveListener);
		}
		
		EVENT_BUSSES.get(m_channel).register(new IGuavaMessageListener() {

			@Override
			@Subscribe public void receive(GuavaRainbowMessage msg) {
				 String repKey = (String) msg.getProperty (ESEBConstants.MSG_REPLY_KEY);
                 Object msgType = msg.getProperty (ESEBConstants.MSG_TYPE_KEY);

                 if (ESEBConstants.MSG_TYPE_REPLY.equals(msgType)) {
                	 IGuavaMessageListener l;
                	 synchronized (m_replyListeners) {
                		 l = m_replyListeners.remove(repKey);
                	 }
                	 if (l != null) {
                		 GuavaRainbowMessage gmsg = new GuavaRainbowMessage(msg);
                		 sanitizeMessage(gmsg);
                		 l.receive(gmsg);
                	 }
                	 else {
                		 LOGGER.error (MessageFormat.format (
                                 "Received a reply on ESEB for which there is no listener. For reply key: {0}",

                                 repKey));
                         LOGGER.info (msg.toString ());
                         synchronized (m_replyListeners) {
                             for (String rk : m_replyListeners.keySet ()) {
                                 LOGGER.info (rk + " -> " + m_replyListeners.get (rk));
                             }
                         }
                	 }
                 }
			}

			private void sanitizeMessage(GuavaRainbowMessage gmsg) {
				gmsg.removeProperty(ESEBConstants.MSG_REPLY_KEY);
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
		@Subscribe public void receive(GuavaRainbowMessage m) {
			m_l.receive(m);
			ret = true;
			synchronized (this) {
				this.notifyAll();
			}
		}
	}
	
	public void blockingSendAndReceive(GuavaRainbowMessage msg, final IGuavaMessageListener l, long timeout) throws RainbowConnectionException {
		BlockingListener bl = new BlockingListener(l);
		synchronized (bl) {
			sendAndReceive(msg, bl);
			try {
				bl.wait(timeout);
			} catch (InterruptedException e) {}
		}
		if (!bl.ret) {
	         throw new RainbowConnectionException (MessageFormat.format (
	                    "Blocking send and receive did not return in specified time {0}", timeout));

		}
	}
	
	public void addListener (final IGuavaMessageListener l) {
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
			reply.setProperty (ESEBConstants.MSG_REPLY_VALUE, result);
			publish(reply);
		}
	}
	
}
