package incubator.polling;

import java.util.List;
import java.util.Random;

import junit.framework.AssertionFailedError;

/**
 * Test class with a data source used for testing purposes. This data source
 * allows setting the data to be returned. The class is multithread safe. It
 * also provides a way to artificially delay data gathering.
 */
class TestDataSource implements PollingDataSource<Object> {
	/**
	 * Minimum amount of delay to gather data (0 disables).
	 */
	private int minDelay;
	
	/**
	 * Maximum amount of delay to gather data (0 disables).
	 */
	private int maxDelay;
	
	/**
	 * Current data to be returned.
	 */
	private List<Object> data;
	
	/**
	 * Random generator.
	 */
	private final Random random;
	
	/**
	 * Creates a test data source.
	 */
	TestDataSource() {
		minDelay = 0;
		maxDelay = 0;
		data = null;
		random = new Random();
	}

	@Override
	public List<Object> getPollingData() {
		List<Object> mydata;
		synchronized(this) {
			mydata = data;
		}
		
		if (minDelay > 0 && maxDelay > 0 && maxDelay >= minDelay) {
			try {
				int stime;
				if (maxDelay != minDelay) {
					stime = random.nextInt(maxDelay - minDelay) + minDelay;
				} else {
					stime = maxDelay;
				}
				
				Thread.sleep(stime);
			} catch (InterruptedException e) {
				throw new AssertionFailedError("Thread interrupted but not "
						+ "expected.");
			}
		}
		
		return mydata;
	}
	
	/**
	 * Sets the list with data to return.
	 * 
	 * @param data the data to return
	 */
	synchronized void setData(List<Object> data) {
		this.data = data;
	}
	
	/**
	 * Sets the interval of values which specify the minimum and maximum time
	 * to wait when polling data.
	 * 
	 * @param min minimum amount of time (in milliseconds)
	 * @param max maximum amount of time( in milliseconds)
	 */
	synchronized void setDelay(int min, int max) {
		minDelay = min;
		maxDelay = max;
	}
}
