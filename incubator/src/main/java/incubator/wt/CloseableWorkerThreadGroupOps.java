package incubator.wt;

import incubator.pval.Ensure;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Class with utility operations on closeable workers which are part of
 * worker thread groups.
 */
public class CloseableWorkerThreadGroupOps {
	/**
	 * Utility class: no constructor.
	 */
	private CloseableWorkerThreadGroupOps() {
	}
	
	/**
	 * Closes all closeables in the given worker thread group.
	 * @param g the group
	 * @throws IOException failed to close at least one thread; some threads
	 * may not have been closed
	 */
	public static void close_all(WorkerThreadGroupCI g) throws IOException {
		Ensure.not_null(g, "g == null");
		
		Set<WorkerThreadGroupCI> all = g.all_subgroups();
		all.add(g);
		
		Set<WorkerThreadCI> threads = new HashSet<>();
		for (WorkerThreadGroupCI c : all) {
			threads.addAll(c.threads());
		}
		
		for (WorkerThreadCI t : threads) {
			if (t instanceof CloseableWorkerThread<?>) {
				((CloseableWorkerThread<?>) t).close();
			}
		}
	}
}
