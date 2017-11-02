package incubator.polling;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

/**
 * Class with utility methods for the polling unit tests.
 */
class TestPollingUtils extends Assert {
	/**
	 * Ensures that the listener contains the changes required to transform the
	 * list in <code>oldData</code> to <code>newData</code>.
	 * 
	 * @param oldData the old data
	 * @param newData the new data
	 * @param listener the listener
	 */
	static void checkListenerChanged(List<Object> oldData,
			List<Object> newData, TestPollerListener listener) {
		assertNotNull(oldData);
		assertNotNull(newData);
		
		List<Object> trf = new ArrayList<>(oldData);
		int ap = 0;
		int rp = 0;
		for (int i = 0; i < listener.added.size(); i++) {
			if (listener.added.get(i)) {
				trf.add(listener.positionsAdded.get(ap),
						listener.objectsAdded.get(ap));
				ap++;
			} else {
				int idx = listener.positionsRemoved.get(rp);
				Object obj = listener.objectsRemoved.get(rp);
				assertEquals(trf.get(idx), obj);
				trf.remove(idx);
				rp++;
			}
		}
		
		assertEquals(newData, trf);
		assertEquals(ap, listener.positionsAdded.size());
		assertEquals(rp, listener.positionsRemoved.size());
	}
}
