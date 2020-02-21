package incubator.polling;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;

/**
 * <p>
 * A poller is a class responsible for gluing the polling mechanism with the
 * user interface. The poller connects two associations: one interface
 * responsible to obtain data and an interface responsible to inform all
 * interested entities through the {@link PollerListener} when a modification is
 * detected through the data polled.
 * </p>
 * 
 * @param <T> the polling object type
 */
public final class Poller<T> {
	/**
	 * Default polling time between polling (60 seconds).
	 */
	private static final long POLLING_TIME = 60000;

	/**
	 * Polling thread.
	 */
	private PollThread<T> pollingThread;
	/**
	 * The {@link PollingDataSource} associated with the {@link Poller}.
	 */
	private PollingDataSource<T> pDataSource;

	/**
	 * The {@link PollerListener} associated with the {@link Poller}.
	 */
	private PollerListener<T> pListener;

	/**
	 * Internal time in milliseconds to refresh the polling data.
	 */
	private long pTime;

	/**
	 * Creates a new Poller entity.
	 * 
	 * @param pollerListener object responsible to be informed when changes are
	 * detected in the data retrieved when comparing with the saved polled data
	 * @param pollingDataSource object responsible to execute and obtain the
	 * most up to date data
	 */
	public Poller(PollerListener<T> pollerListener,
			PollingDataSource<T> pollingDataSource) {
		this(POLLING_TIME, pollerListener, pollingDataSource);
	}

	/**
	 * Creates a new Poller entity.
	 * 
	 * @param pollingTime time between polling to the {@link PollingDataSource}
	 * @param pollerListener object responsible to be informed when changes are
	 * detected in the data retrieved when comparing with the saved polled data
	 * @param pollingDataSource object responsible to execute and obtain the
	 * most up to date data
	 */
	public Poller(long pollingTime, PollerListener<T> pollerListener,
			PollingDataSource<T> pollingDataSource) {

		pTime = pollingTime;

		// Check if is null
		if (pollerListener == null) {
			throw new IllegalArgumentException();
		}

		pListener = pollerListener;

		// Check if is null
		if (pollingDataSource == null) {
			throw new IllegalArgumentException();
		}

		pDataSource = pollingDataSource;

		if (pollingTime <= 0) {
			throw new IllegalArgumentException("pollingTime <= 0");
		}

		// Instance a new thread and start running it.
		pollingThread = new PollThread<>(pTime, pListener, pDataSource);
		pollingThread.start();
	}

	/**
	 * This method stops the poller.
	 */
	public synchronized void destroy() {
		if (pollingThread == null) {
			throw new IllegalStateException("Poller already destroyed");
		}

		pollingThread.quit();
		pollingThread.syncInterrupt();
		pollingThread = null;
	}

	/**
	 * This method obtains data through the {@link PollingDataSource}.
	 */
	public synchronized void forcePoll() {
		if (pollingThread == null) {
			throw new IllegalStateException("Poller already destroyed");
		}

		pollingThread.syncInterrupt();
	}

	/**
	 * Defines the name of the polling thread.
	 * 
	 * @param name the name of the polling thread
	 */
	public synchronized void setPollingThreadName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("name == null");
		}

		if (pollingThread == null) {
			throw new IllegalStateException("Poller already destroyed");
		}

		pollingThread.setName(name);
	}
}

/**
 * This class is the running class that will perform the polling process.
 * 
 * @param <T> the polling object type
 */
final class PollThread<T> extends Thread {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(PollThread.class);

	/**
	 * The {@link PollingDataSource} associated with the {@link Poller}.
	 */
	private final PollingDataSource<T> pDataSource;

	/**
	 * The {@link PollerListener} associated with the {@link Poller}.
	 */
	private final PollerListener<T> pListener;

	/**
	 * Internal time in milliseconds to refresh the polling data.
	 */
	private final long pTime;

	/**
	 * List with the last data polled through the {@link PollingDataSource}.
	 */
	private List<T> pDataPolledOld;

	/**
	 * Used to compare new data with old.
	 */
	private List<T> pDataPolledNew;

	/**
	 * Used to signal the thread that it has to quit.
	 */
	private boolean quit;

	/**
	 * Constructor of the PollTread that receives multiple parameters.
	 * 
	 * @param pollingTime is the time used by the thread to sleep.
	 * @param pollerListener is the listener to be invoked.
	 * @param pollingDataSource is the object to be asked for data.
	 */
	public PollThread(long pollingTime, PollerListener<T> pollerListener,
			PollingDataSource<T> pollingDataSource) {
		pTime = pollingTime;
		pListener = pollerListener;
		pDataSource = pollingDataSource;
		quit = false;
	}

	/**
	 * Signals the thread that it has to quit.
	 */
	synchronized void quit() {
		quit = true;
	}

	/**
	 * Performs a synchronous interrupt: only interrupts the thread if it is
	 * sleeping. If not, it'll wait for the thread to go to sleep before it is
	 * interrupted.
	 */
	synchronized void syncInterrupt() {
		interrupt();

		boolean interrupted = false;
		do {
			try {
				/*
				 * Wait for signaling that a cycle has been done.
				 */
				wait();
			} catch (InterruptedException e) {
				/*
				 * Thread has been interrupted, ignore it.
				 */
				interrupted = true;
			}
		} while (interrupted);
	}

	/**
	 * Run method of the thread when start is invoked.
	 */
	@Override
	public synchronized void run() {

		pDataPolledOld = new ArrayList<>();

		while (!quit) {
			// Gets new data from the data source.
			pDataPolledNew = getDataFromDataSource();
			compareAndNotify(pDataPolledOld, pDataPolledNew);

			pDataPolledOld.clear();
			pDataPolledOld.addAll(pDataPolledNew);

			/*
			 * Sleeps the configured time.
			 */
			try {
				wait(pTime);
			} catch (InterruptedException ex) {
				/*
				 * We'll ignore the exception. We'll use interrupts to break
				 * sleeping time.
				 */
				LOGGER.debug("Waiting interrupted.", ex);
			}

			/*
			 * Informs everyone waiting that we're working.
			 */
			notifyAll();
		}
	}

	/**
	 * Compares and notifies changes on two lists. This method will notify the
	 * listener of all changes needed to perform to <code>l0</code> to transform
	 * it into <code>l1</code>.
	 * 
	 * @param l0 the original list
	 * @param l1 the destination list
	 */
	private void compareAndNotify(List<T> l0, List<T> l1) {
		/*
		 * Please note that in this algorithm, l0 and l1 will always be equal up
		 * to position "i". Note that we will modify l0 so we'll copy it first.
		 */
		l0 = new ArrayList<>(l0);
		for (int i = 0; i < l0.size(); i++) {
			/*
			 * Fetch object in position i of l0.
			 */
			T obj0 = l0.get(i);

			/*
			 * If l1 has less elements than l0, then it must have exactly "i"
			 * elements and we'll remove the current one from l0 (that is why it
			 * has exactly "i" elements),
			 */
			if (l1.size() <= i) {
				assert l1.size() == i;
				pListener.objectRemoved(obj0, i);
				l0.remove(i);
				i--;
				continue;
			}

			/*
			 * Fetch the corresponding object from l1. If they are both equal
			 * than we're ok to move forward.
			 */
			T obj1 = l1.get(i);
			if (ObjectUtils.equals(obj0, obj1)) {
				continue;
			}

			/*
			 * If they are not equal then two things can happen: either obj0
			 * doesn't exist at all in l1 or it exists further along. We'll
			 * check the first hypothesis first since it's simpler.
			 */
			if (!l1.contains(obj0)) {
				pListener.objectRemoved(obj0, i);
				l0.remove(i);
				i--;
				continue;
			}

			/*
			 * At this point we know that obj0 is different from obj1 but it
			 * exists further along in l1. We'll assume this is an insert
			 * operation. There are some quirks tho because if the arrays have
			 * simply been reordered then obj1 may still exist further along in
			 * l0 and we'll inform a listener with the list having duplicate
			 * objects. We can live with that since we're not forbidden by the
			 * contract to do that I think.
			 */
			pListener.objectAdded(obj1, i);
			l0.add(i, obj1);
		}

		/*
		 * When we reach the end it might be the case that l0 is still smaller
		 * than l1 (if there are extra items in the end of l1). These are all
		 * adds.
		 */
		for (int i = l0.size(); i < l1.size(); i++) {
			T obj = l1.get(i);
			l0.add(obj);
			pListener.objectAdded(obj, i);
		}

		/*
		 * At the very end we know both lists must be equal.
		 */
		assert l0.equals(l1);
	}

	/**
	 * Obtains the new data from the data source.
	 * 
	 * @return list of objects with the new data.
	 */
	private List<T> getDataFromDataSource() {
		return pDataSource.getPollingData();
	}
}
