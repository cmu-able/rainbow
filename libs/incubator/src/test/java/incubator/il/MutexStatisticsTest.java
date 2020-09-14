package incubator.il;

import incubator.il.IMutexStatisticsImpl;

import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Checks mutex statistics.
 */
public class MutexStatisticsTest extends DefaultTCase {
	/**
	 * Performs several acquisitions and checks that the number of acquisitions
	 * obtained by the statistics is correct.
	 * @throws Exception test failed
	 */
	@Test
	public void acquisition_count() throws Exception {
		IMutexStatisticsImpl stats = new IMutexStatisticsImpl();
		
		int rand = RandomUtils.nextInt(1000);
		assertEquals(0, stats.total_acquisition_count());
		for (int i = 0; i < rand; i++) {
			stats.acquired(0);
		}
		
		assertEquals(rand, stats.total_acquisition_count());
	}
	
	/**
	 * Performs several acquisitions, some with wait time and some without
	 * wait time.  Checks that the number of no wait acquisitions is correct.
	 * @throws Exception test failed
	 */
	@Test
	public void testAcquisitionWithNoWait() throws Exception {
		IMutexStatisticsImpl stats = new IMutexStatisticsImpl();
		
		int rand = RandomUtils.nextInt(1000);
		int count = 0;
		for (int i = 0; i < rand; i++) {
			int wt = RandomUtils.nextInt(3);
			if (wt == 0) {
				count++;
			}
			
			stats.acquired(wt);
		}
		
		assertEquals(count, stats.counts_with_no_wait());
	}
	
	/**
	 * Acquires a mutex several times with random times. Some of the
	 * acquisitions have wait and some don't. Checks the average acquisition
	 * time and the average wait time.
	 * @throws Exception test failed
	 */
	@Test
	public void average_wait_time() throws Exception {
		IMutexStatisticsImpl stats = new IMutexStatisticsImpl();
		
		int cycles = RandomUtils.nextInt(10000);
		long totalWait = 0;
		long total = 0;
		int totalWaitCount = 0;
		int totalCount = 0;
		for (int i  = 0; i < cycles || total == 0 || totalCount == 0; i++) {
			int nowait = RandomUtils.nextInt(2);
			if (nowait == 0) {
				stats.acquired(0);
				totalCount++;
			} else {
				int wt = RandomUtils.nextInt(50);
				stats.acquired(wt);
				totalWaitCount++;
				totalCount++;
				totalWait += wt;
				total += wt;
			}
		}
		
		assertTrue(totalWait / totalWaitCount >= stats.average_wait_time() - 1
				&& totalWait / totalWaitCount <= stats.average_wait_time() + 1);
		assertEquals(total / totalCount, stats.average_acquire_time());
	}
	
	/**
	 * Checks that the usage time average is computed correctly.
	 * @throws Exception test failed
	 */
	@Test
	public void average_usage_time() throws Exception {
		IMutexStatisticsImpl stats = new IMutexStatisticsImpl();
		
		int cycles = RandomUtils.nextInt(1000) + 1;
		long total = 0;
		int totalCount = 0;
		
		for (int i = 0; i < cycles; i++) {
			stats.acquired(0);
			int wt = RandomUtils.nextInt(30);
			total += wt;
			totalCount++;
			stats.released(wt);
		}
		
		assertEquals(total / totalCount, stats.average_usage_time());
	}
	
	/**
	 * Checks that we can create an object from another one and the copy
	 * works correctly.
	 * @throws Exception test failed
	 */
	@Test
	public void copy_constructor() throws Exception {
		IMutexStatisticsImpl stats = new IMutexStatisticsImpl();
		
		int cycles = RandomUtils.nextInt(1000);
		for (int i = 0; i < cycles; i++) {
			int wt = RandomUtils.nextInt(50);
			stats.acquired(wt);
		}
		
		IMutexStatisticsImpl copy = new IMutexStatisticsImpl(stats);
		assertEquals(stats.average_acquire_time(),
				copy.average_acquire_time());
		assertEquals(stats.average_usage_time(), copy.average_usage_time());
		assertEquals(stats.average_wait_time(), copy.average_wait_time());
		assertEquals(stats.counts_with_no_wait(), copy.counts_with_no_wait());
		assertEquals(stats.total_acquisition_count(),
				copy.total_acquisition_count());
	}
	
	/**
	 * Places statistics in an object and then does a reset checking that all
	 * values are no zero.
	 * @throws Exception test failed
	 */
	@Test
	public void reset() throws Exception {
		IMutexStatisticsImpl stats = new IMutexStatisticsImpl();
		
		stats.acquired(30);
		stats.released(5);
		stats.acquired(0);
		
		assertEquals(15, stats.average_acquire_time());
		assertEquals(5, stats.average_usage_time());
		assertEquals(30, stats.average_wait_time());
		assertEquals(1, stats.counts_with_no_wait());
		assertEquals(2, stats.total_acquisition_count());
		
		stats.reset();
		
		assertEquals(0, stats.average_acquire_time());
		assertEquals(0, stats.average_usage_time());
		assertEquals(0, stats.average_wait_time());
		assertEquals(0, stats.counts_with_no_wait());
		assertEquals(0, stats.total_acquisition_count());
	}
}
