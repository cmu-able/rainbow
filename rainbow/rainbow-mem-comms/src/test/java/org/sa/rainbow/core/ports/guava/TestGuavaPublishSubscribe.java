package org.sa.rainbow.core.ports.guava;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.ChannelT;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.IGuavaMessageListener;

import com.google.common.eventbus.Subscribe;

public class TestGuavaPublishSubscribe {

	protected GuavaRainbowMessage result;

	@Test
	public void testOneMessage() {
		GuavaEventConnector conn = new GuavaEventConnector(ChannelT.HEALTH);

		conn.addListener(new IGuavaMessageListener() {

			@Override
			@Subscribe
			public void receive(GuavaRainbowMessage m) {
				synchronized (TestGuavaPublishSubscribe.this) {
					result = m;
					TestGuavaPublishSubscribe.this.notifyAll();
				}
			}
		});

		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		msg.setProperty("TEST", "test");

		synchronized (this) {
			conn.publish(msg);
			try {
				this.wait();
			} catch (InterruptedException e) {
			}
		}
		assertTrue(result != null);
		assertTrue(result.hasProperty("TEST"));
		assertTrue(result.getProperty("TEST").equals("test"));
	}

	@Test
	public void testTwoMessages() {
		final List<GuavaRainbowMessage> results = new ArrayList<>();
		GuavaEventConnector conn = new GuavaEventConnector(ChannelT.HEALTH);

		conn.addListener(new IGuavaMessageListener() {

			@Override
			@Subscribe
			public void receive(GuavaRainbowMessage m) {
				synchronized (TestGuavaPublishSubscribe.this) {
					results.add(m);
//					TestGuavaPublishSubscribe.this.notifyAll();
				}
			}
		});

		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		msg.setProperty("TEST", "test");

		synchronized (this) {
			conn.publish(msg);
			msg.setProperty("TEST", "test2");
			conn.publish(msg);
			try {
				this.wait(1000);
			} catch (InterruptedException e) {
			}
		}
		assertTrue(!results.isEmpty());
		assertTrue(results.size() == 2);
	}
	
	@Test
	public void testTwoMessagesDifferentBus() {
		final List<GuavaRainbowMessage> results = new ArrayList<>();
		GuavaEventConnector conn = new GuavaEventConnector(ChannelT.HEALTH);
		GuavaEventConnector conn2 = new GuavaEventConnector(ChannelT.MODEL_CHANGE);

		conn.addListener(new IGuavaMessageListener() {

			@Override
			@Subscribe
			public void receive(GuavaRainbowMessage m) {
				synchronized (TestGuavaPublishSubscribe.this) {
					results.add(m);
					TestGuavaPublishSubscribe.this.notifyAll();
				}
			}
		});

		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		msg.setProperty("TEST", "test");

		synchronized (this) {
			conn.publish(msg);
			msg.setProperty("TEST", "test2");
			conn2.publish(msg);
			try {
				this.wait(1000);
			} catch (InterruptedException e) {
			}
		}
		assertTrue(!results.isEmpty());
		assertTrue("results.size() == " + results.size(), results.size() == 1);
	}

	
	@Test
	public void testOneBusTwoConsumers() {
		final List<GuavaRainbowMessage> results = new ArrayList<>();
		GuavaEventConnector conn = new GuavaEventConnector(ChannelT.HEALTH);

		conn.addListener(new IGuavaMessageListener() {

			@Override
			@Subscribe
			public void receive(GuavaRainbowMessage m) {
				synchronized (TestGuavaPublishSubscribe.this) {
					results.add(m);
				}
			}
		});
		
		conn.addListener(new IGuavaMessageListener() {

			@Override
			@Subscribe
			public void receive(GuavaRainbowMessage m) {
				synchronized (TestGuavaPublishSubscribe.this) {
					results.add(m);
				}
			}
		});

		GuavaRainbowMessage msg = new GuavaRainbowMessage();
		msg.setProperty("TEST", "test");

		synchronized (this) {
			conn.publish(msg);
			msg.setProperty("TEST", "test2");
			try {
				this.wait(1000);
			} catch (InterruptedException e) {
			}
		}
		assertTrue(!results.isEmpty());
		assertTrue("results.size() == " + results.size(), results.size() == 2);
	}
}
