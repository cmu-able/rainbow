/**
 * Created April 6, 2006.
 */
package org.sa.rainbow.core;

/**
 * Represents a Rainbow Runnable interface, defining pause and terminate methods
 * in addition to java.lang.Runnable.run().  Expected state trace is:
 *   raw start ((stop start)* | stop)? terminate
 * <p>
 * The IDisposable methods of dispose() will be called when terminating, while
 * isDisposed() will be synonymous with isTerminated().  This seeming redundancy
 * is to allow methods that deal with isDisposed to work with IRainbowRunnables
 * as well.
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public interface IRainbowRunnable extends IDisposable, Runnable {

	public static enum State {
		RAW, STARTED, STOPPED, TERMINATED
	}

	public static final long SLEEP_TIME = 100;  // ms
	public static final long LONG_SLEEP_TIME = 1000;  // ms
	public static final long SHORT_SLEEP_TIME = 10;  // ms
	/** Threshold value useful for preventing a runnable from hogging CPU */
	public static final int MAX_CYCLES_PER_SLEEP = 100;

	/**
	 * Returns the name of this runnable.
	 * @return String  the IRainbowRunnable's name
	 */
	public String name ();

	/**
	 * Starts the execution of the runnable, usually via thisThread.start().
	 */
	public void start ();

	/**
	 * Pauses the execution of the runnable, preserving any states until it is
	 * resumed again by call to start.  This method actually calls an
	 * implementation method, transition().
	 */
	public void stop ();

	/**
	 * Stops, then starts, the execution of the runnable.
	 * This method actually calls an implementation method, transition().
	 */
	public void restart ();

	/**
	 * Stops the runnable thread and (potentially) causes exit from fun.
	 * This method actually calls an implementation method, transition().
	 */
	public void terminate ();

	/**
	 * Returns the running state of this runnable.
	 * @return State  one of the states, <code>RAW</code>, <code>STARTED</code>, <code>STOPPED</code>, <code>TERMINATED</code> 
	 */
	public State state ();

	/**
	 * A convenience method for checking whether this IRainbowRunnable is in a
	 * TERMINATED state.
	 * @return boolean  <code>true</code> if TERMINATED, <code>false</code> otherwise.
	 */
	public boolean isTerminated ();

}
