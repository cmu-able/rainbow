package incubator.polling;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Random tests for the polling API.
 */
public class PollingRdTest extends Assert {
	/**
	 * Creates a poller with a data source whose contents change randomly. Time
	 * to obtain data also varies randomly. Besides a listener which should be
	 * informed (correctly) of the changes, there are several threads which
	 * randomly force a poll. 
	 * 
	 * @throws Exception failed
	 */
	@Test
	public void randomThreadedAccess() throws Exception {
		final long testTime = 5000;
		final boolean[] quit = new boolean[1];
		final TestDataSource tds = new TestDataSource();
		tds.setData(new ArrayList<>());
		final TestPollerListener tl = new TestPollerListener();
		final Poller<Object> poller = new Poller<>(100, tl, tds);
		final List<Object> finalData = new ArrayList<>();
		
		Thread t = new Thread() {
			@Override
			public void run() {
				List<Object> data = null;
				while (!quit[0]) {
					long randomSleepTime = 50 + RandomUtils.nextInt(100);
					try {
						Thread.sleep(randomSleepTime);
					} catch (InterruptedException e) {
						// We'll ignore this...
					}
					
					int cnt = RandomUtils.nextInt(10);
					data = new ArrayList<>();
					for (int i = 0; i < cnt; i++) {
						data.add(RandomStringUtils.randomAlphabetic(5));
					}
					
					tds.setData(data);
				}
				
				finalData.addAll(data);
			}
		};
		
		Thread fp[] = new Thread[5];
		for (int i = 0; i< fp.length; i++) {
			fp[i] = new Thread() {
				@Override
				public void run() {
					while (!quit[0]) {
						long randomSleepTime = 50 + RandomUtils.nextInt(100);
						try {
							Thread.sleep(randomSleepTime);
						} catch (InterruptedException e) {
							// We'll ignore this...
						}
						
						poller.forcePoll();
					}
				}
			};
			
			fp[i].start();
		}
		
		t.start();
		
		Thread.sleep(testTime);
		quit[0] = true;
		while (t.isAlive()) {
			Thread.sleep(10);
		}
		
		for (int i = 0; i < fp.length; i++) {
			while (fp[i].isAlive()) {
				Thread.sleep(10);
			}
		}
		
		/*
		 * Give some time to do a last poll.
		 */
		Thread.sleep(250);
		
		TestPollingUtils.checkListenerChanged(new ArrayList<>(),
				finalData, tl);
	}
}
