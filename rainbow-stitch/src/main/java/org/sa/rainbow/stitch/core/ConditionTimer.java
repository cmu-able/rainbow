/**
 * 
 */
package org.sa.rainbow.stitch.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

/**
 * A singleton class with a running thread to facilitate timed conditions.
 * Any Strategy node that has duration in it condition can register the
 * condition expression + duration with this timer, and the timer will notify
 * the node EITHER when the expression evaluates to true or time is up!
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public class ConditionTimer implements Endable {

	public static final int SLEEP_TIME_SHORT = 10;  // ms
	public static final int SLEEP_TIME_LONG = 100;  // ms

	private static ConditionTimer m_singleton = null;

	private Thread m_thread = null;
	private List<TimedCondition> m_conditions = null;

	/**
	 * Private constructor.
	 */
	private ConditionTimer() {
		m_thread = new Thread(this, "Rainbow Strategy-Condition Timer");
		m_conditions = new ArrayList<TimedCondition>();

		m_thread.start();
	}

	/**
	 * Lazy initializes the condition timer, at which point it'll be
	 * continuously running.
	 */
	public static ConditionTimer instance () {
		if (m_singleton == null) {
			m_singleton = new ConditionTimer();
		}
		return m_singleton;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		Thread currentThread = Thread.currentThread();
		int idx = 0;  // index into the registered conditions array, -1 causes long sleep
		/*
		 * Algorithm:  We keep looping while we ARE the current thread. <ol>
		 * <li> Index into the condition array starts at 0 (first item).
		 * <li> At every cycle, take the current condition item and evaluate it.
		 * Evaluating a timed condition entails the following.
		 *   <ol>
		 *   <li> If time is not up, evaluate the condition expression and
		 *   update the timedCondition's result; if the condition expression
		 *   evaluates to <code>true</code>, then the timedCondition is
		 *   satisfied, we then deregister the observable
		 *   <li> If time IS up, then notify observers of the result, in the
		 *   process deregistering the observable from the list of conditions
		 *   </ol>
		 * <li> Sleep the short sleep if we have more items; long sleep if we've
		 * gone through all items already
		 * <li> Repeat the cycle either with the next item or starting from the
		 * first item in array, depending on how index was updated
		 * </ol> 
		 */
		while (m_thread == currentThread) {
			if (idx >= m_conditions.size()) {  // reset idx
				idx = 0;
				try {  // sleep long
					Thread.sleep(SLEEP_TIME_LONG);
				} catch (InterruptedException e) {
					// intentional ignore
				}
			} else {  // process a timed condition
				TimedCondition timedCond = m_conditions.get(idx++);
				if (timedCond.isTimeUp()) {
					timedCond.updateResult();
					// notify observers, whether true or false
					timedCond.notifyObservers();
					// remove timed condition from queue
					m_conditions.remove(timedCond);
					--idx;  // move index backward by one
				} else {
					timedCond.updateResult();
					if (timedCond.result()) {  // evaluated to true
						timedCond.notifyObservers();
						m_conditions.remove(timedCond);
						--idx;
					}
				}
				try {  // sleep short
					Thread.sleep(SLEEP_TIME_SHORT);
				} catch (InterruptedException e) {
					// intentional ignore
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.sa.rainbow.stitch.core.Endable#end()
	 */
	public void end() {
		m_thread = null;  // stop running
		m_conditions.clear();
		m_conditions = null;
		m_singleton = null;
	}

	public void registerCondition (List<Expression> exprList, long dur, Observer o) {
		// instantiate an observable with the supplied expression and duration
		TimedCondition timedCond = new TimedCondition(exprList, dur);
		// initiate timer!
		timedCond.resetTimer();
		// add to list of observables
		timedCond.addObserver(o);
		m_conditions.add(timedCond);
	}

	public void deregisterCondition (TimedCondition timedCond) {
		m_conditions.remove(timedCond);
	}

}
