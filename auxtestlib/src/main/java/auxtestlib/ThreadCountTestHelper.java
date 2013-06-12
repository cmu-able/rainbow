package auxtestlib;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * The thread count test helper ensures that all threads started during the
 * test case are finished by the end of the test case.
 */
public class ThreadCountTestHelper extends AbstractTestHelper {
	/**
	 * Creates a new helper.
	 * @throws Exception initialization failed
	 */
	public ThreadCountTestHelper() throws Exception {
		super();
	}

	/**
	 * Known threads when the test case started.
	 */
	private static Set<Thread> knownThreads;

	@Override
	protected void mySetUp() throws Exception {
		/*
		 * Nothing to do.
		 */
	}

	@Override
	protected void myTearDown() throws Exception {
		/*
		 * Nothing to do.
		 */
	}

	@Override
	protected void myCleanUp() throws Exception {
		/*
		 * If there is a list of known threads, ensure that it matches the
		 * list of known threads.
		 */
		Set<Thread> known = knownThreads;
		knownThreads = null;
		if (known != null) {
			Set<Thread> current = allThreads();
			Set<Thread> remaining = new HashSet<>(current);
			remaining.removeAll(known);
			
			if (remaining.size() > 0) {
				StringBuffer names = new StringBuffer();
				for (Thread t : remaining) {
					if (names.length() != 0) {
						names.append(", ");
					}
					
					names.append("'" + t.getName() + "' (" + t.getState()
							+ ")");
				}
				
				throw new Exception("" + remaining.size() + " threads "
						+ "remaing after execution of test case: " + names
						+ ".");
			}
		}
	}

	@Override
	protected void myPrepareFixture() throws Exception {
		knownThreads = allThreads();
	}
	
	/**
	 * Obtains the set of all running threads.
	 * @return all running threads
	 */
	private Set<Thread> allThreads() {
		ThreadGroup g = Thread.currentThread().getThreadGroup();
		
		/*
		 * Get the top thread group.
		 */
		for( ; g.getParent() != null; g = g.getParent()) ;
		
		/*
		 * Enumerate all threads. Keep recursing until we get what may look
		 * like a consistent snapshot. Of course, it is not really a very
		 * consistent one but...
		 */
		int tcount = g.activeCount();
		int tcountOld;
		Thread t[];
		do {
			t = new Thread[tcount + 1];
			tcountOld = tcount;
			tcount = g.enumerate(t);
		} while (tcountOld != tcount);
		
		Set<Thread> all = new HashSet<>(Arrays.asList(t));
		Iterator<Thread> it = all.iterator();
		while (it.hasNext()) {
			Thread ct = it.next();
			if (ct == null) {
				/*
				 * We can end up with nulls here which is somewhat weird but
				 * this stuff has a high degree of natural weirdness. 
				 */
				it.remove();
				continue;
			}
			
			Thread.State st = ct.getState();
			assert st != null;
			if (!ct.isAlive() && st != Thread.State.NEW) {
				it.remove();
			}
		}
		
		return all;
	}
}
