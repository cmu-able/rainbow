package org.sa.rainbow.translator.probes;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.IRainbowRunnable;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.error.ErrorsReachedThresholdException;


/**
 * Common superclass for probes that require their own running thread.
 *
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public abstract class AbstractRunnableProbe extends AbstractProbe implements Runnable {

    private Thread m_thread = null;
    private int m_errorCnt = 0;
    private long m_sleepTime = IRainbowRunnable.LONG_SLEEP_TIME;

    /**
     * Main Constuctor that initializes the ID of this Probe.
     * @param id    the unique identifier of the Probe
     * @param type  the type name of the Probe
     * @param kind  one of the enumerated {@link IProbe.Kind}s designating how
     *     this Probe would be handled by the ProbeBusRelay
     * @param long  sleep period in milliseconds
     */
    public AbstractRunnableProbe (String id, String type, Kind kind, long sleepTime) {
        super(id, type, kind);
        m_sleepTime = sleepTime;
        LOGGER = Logger.getLogger (getClass ());
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.probes.AbstractProbe#create()
     */
    @Override
    public synchronized void create () {
        super.create();
        m_thread = new Thread(this, id());
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.probes.AbstractProbe#activate()
     */
    @Override
    public synchronized void activate () {
        super.activate();
        m_thread.start();
    }

    @Override
    public synchronized void deactivate() {
        super.deactivate();
        m_thread = new Thread(this, id());
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.probes.AbstractProbe#destroy()
     */
    @Override
    public synchronized void destroy () {
        super.destroy();
        m_thread = null;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.translator.probes.AbstractProbe#isAlive()
     */
    @Override
    public boolean isAlive () {
        boolean alive = true;
        if (lcState().compareTo(State.NULL) <= 0) {
            alive = false;
        }
        return alive && m_thread != null;
    }

    /**
     * Returns the current Thread instance.
     * @return Thread  current thread instance for this Runnable.
     */
    protected Thread thread () {
        return m_thread;
    }

    /**
     * Returns the amount of time in milliseconds to sleep.  Default to
     * {@linkplain IRainbowRunnable.LONG_SLEEP_TIME}.
     * @return long  the sleep time in milliseconds
     */
    protected long sleepTime () {
        return m_sleepTime;
    }

    /**
     * Increments the error counter, and deactivates and destroys this IProbe
     * if error count threshold reached.
     */
    protected void tallyError () {
        ++m_errorCnt;
        if (m_errorCnt > RainbowConstants.MAX_ERROR_CNT) {
            deactivate();
            destroy();
            throw new ErrorsReachedThresholdException("Probe error count exceeded " + RainbowConstants.MAX_ERROR_CNT + ", self-destroyed!");
        }
    }

}
