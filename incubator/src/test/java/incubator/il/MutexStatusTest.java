package incubator.il;

import incubator.il.IMutexRequest;
import incubator.il.IMutexRequestImpl;
import incubator.il.IMutexStatisticsImpl;
import incubator.il.IMutexStatusImpl;

import java.util.ArrayList;

import org.junit.Test;

import auxtestlib.DefaultTCase;

/**
 * Test suite that checks mutex status.
 */
public class MutexStatusTest extends DefaultTCase {
	/**
	 * Mutex state can be created without data.
	 * @throws Exception test failed
	 */
	@Test
	public void can_be_created_without_data() throws Exception {
		IMutexStatisticsImpl stats = new IMutexStatisticsImpl();
		IMutexStatusImpl status = new IMutexStatusImpl(null,
				new ArrayList<IMutexRequest>(), stats);
		
		assertNull(status.lock_request());
		assertEquals(0, status.wait_list().size());
		assertEquals(stats, status.statistics());
	}
	
	/**
	 * After being created, data is kept by the mutex state.
	 * @throws Exception test failed
	 */
	@Test
	public void data_is_kept() throws Exception {
		IMutexStatisticsImpl stats = new IMutexStatisticsImpl();
		IMutexRequestImpl r1 = new IMutexRequestImpl();
		IMutexRequestImpl r2 = new IMutexRequestImpl();
		IMutexRequestImpl r3 = new IMutexRequestImpl();
		
		ArrayList<IMutexRequest> l = new ArrayList<>();
		l.add(r1);
		l.add(r2);
		
		IMutexStatusImpl status = new IMutexStatusImpl(r3, l, stats);
		assertEquals(r3, status.lock_request());
		assertEquals(2, status.wait_list().size());
		assertEquals(r1, status.wait_list().get(0));
		assertEquals(r2, status.wait_list().get(1));
		assertEquals(stats, status.statistics());
	}
}
