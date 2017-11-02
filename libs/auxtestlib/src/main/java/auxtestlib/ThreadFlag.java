package auxtestlib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Class used to control running threads. A flag keeps a marker whether threads
 * should or not continue and keeps record of which threads have reached the
 * flag. It is essentially an implementation of a barrier.
 * </p>
 * <p>
 * Using this class several threads may keep a synchronization point. Lets
 * suppose we have a process that divides into three phases, A, B and C and we
 * want several threads in parallel to execute phase A. We want all threads to
 * move to phase B when all have finished A. When all finish phase B we want all
 * to start phase C. In order to do this we create two flags, one representing
 * the end of phase A and the other the end of phase B. Each thread is now:
 * </p>
 * 
 * <pre>
 * public void process(ThreadFlag flagA, ThreadFla flagB) {
 * 	doSomethingInPhazeA();
 * 	flagA.reach();
 * 	doSomethingInPhazeB();
 * 	flagB.reach();
 * 	doSomethingInPhazeC();
 * }
 * </pre>
 * <p>
 * The method <code>reach</code> will block the current thread until someone
 * invokes the <code>allowContinue</code> method. So, if there were 20 threads
 * running, we could use a subclass of <code>ThreadFlag</code> to start all
 * threads into the next phase:
 * 
 * <pre>
 * public class MyStartAllAtOnceFlag extends ThreadFlag {
 * 	public int count = 0;
 * 
 * 	public void threadReached(List l) {
 * 		if (l.size() == 20) {
 * 			// All threads have arrived.
 * 			allowContinue();
 * 		}
 * 	}
 * }
 * </pre>
 */
public class ThreadFlag {
	/**
	 * Can the threads continue?
	 */
	private boolean canContinue;

	/**
	 * Which threads have already reached the flag.
	 */
	private final List<Thread> reachedThreadList;

	/**
	 * Creates a new flag.
	 */
	public ThreadFlag() {
		canContinue = false;
		reachedThreadList = Collections
				.synchronizedList(new ArrayList<Thread>());
	}

	/**
	 * Invoked by a thread to mark that it has reached the flag. This method
	 * will block the thread until it receives an order to continue. If the flag
	 * has been marked to continue (the {@link #allowContinue()} method has been
	 * called) this method returns immediately.
	 */
	public synchronized void reach() {
		Thread t = Thread.currentThread();
		reachedThreadList.add(t);

		threadReached(Collections.unmodifiableList(reachedThreadList));

		while (!canContinue) {
			try {
				wait();
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	/**
	 * Starts all threads currently stopped at the flag.
	 */
	public synchronized void allowContinue() {
		canContinue = true;
		notifyAll();
	}

	/**
	 * A thread has reached the flag. This method does nothing and may be
	 * overridden by subclasses.
	 * 
	 * @param l the list of threads that have reached the flag
	 */
	public void threadReached(List<Thread> l) {
		/*
		 * Nothing to do.
		 */
	}

	/**
	 * Obtains the list of all threads that have reached the flag.
	 * 
	 * @return the list of threads
	 */
	public synchronized List<Thread> reached() {
		return Collections.unmodifiableList(new ArrayList<>(
				reachedThreadList));
	}
}
