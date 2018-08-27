/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
/**
 * Created April 8, 2006
 */
package org.sa.rainbow.core;

import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.DisconnectedRainbowDelegateConnectionPort;
import org.sa.rainbow.core.ports.IRainbowReportingPort;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;

/**
 * Convenience abstract class that handles the usual thread start/stop/terminate
 * steps, so subclass need not worry about those unless specific actions
 * at those junctions are needed
 * 
 * @author Shang-Wen Cheng (zensoul@cs.cmu.edu)
 */
public abstract class AbstractRainbowRunnable implements IRainbowRunnable, Identifiable {


    private Thread m_thread = null;
    private String m_name = null;
    private long m_sleepTime = SLEEP_TIME;

    private State m_threadState = State.RAW;
    private State m_nextState = State.RAW;
    private boolean m_restarting = false;

    protected IRainbowReportingPort m_reportingPort;
    protected static final int DELAY_TOLERANCE = 500;

    protected IRainbowEnvironment m_rainbowEnvironment = Rainbow.instance ();

    /**
     * Default Constructor with name for the thread.
     * @param name  Name of the Thread
     */
    public AbstractRainbowRunnable (String name) {
        m_name = name;

        m_thread = new Thread(m_rainbowEnvironment.getThreadGroup(), this, m_name);
        try {
            m_reportingPort = new DisconnectedRainbowDelegateConnectionPort ();
        }
        catch (IOException e) {
            // Should never happen
            e.printStackTrace ();
        }
    }

    public void initialize (IRainbowReportingPort port) throws RainbowConnectionException {
        m_reportingPort = port;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.core.IDisposable#isDisposed()
     */
    @Override
    public boolean isDisposed() {
        return isTerminated();
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.core.IRainbowRunnable#name()
     */
    @Override
    public String id() {
        return m_name;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.core.IRainbowRunnable#start()
     */
    @Override
    public void start () {
        if (m_thread == null || m_threadState == State.TERMINATED) return;

        switch (m_threadState) {
        case RAW:  // note yet started
            m_threadState = State.STARTED;
            m_thread.start();
            log(m_name + " started.");
            break;
        case STOPPED:
            m_threadState = State.STARTED;
            log(m_name + " started.");
            break;
        default:
            break;
        }
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.core.IRainbowRunnable#stop()
     */
    @Override
    public void stop() {
        transition(State.STOPPED);
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.core.IRainbowRunnable#restart()
     */
    @Override
    public void restart() {
        m_restarting = true;
        transition(State.STOPPED);
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.core.IRainbowRunnable#terminate()
     */
    @Override
    public void terminate() {
        transition(State.TERMINATED);
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.core.IRainbowRunnable#state()
     */

    @Override
    public State state () {
        return m_threadState;
    }

    /* (non-Javadoc)
     * @see org.sa.rainbow.core.IRainbowRunnable#isTerminated()
     */
    @Override
    public boolean isTerminated() {
        return m_threadState == State.TERMINATED;
    }

    /**
     * Pauses the execution of the runnable, preserving any states until it is
     * resumed again by call to start.
     */
    private void doStop () {
        if (m_thread == null || m_threadState == State.RAW || m_threadState == State.TERMINATED) return;

        m_threadState = State.STOPPED;
        log(m_name + " stopped.");
    }

    /**
     * Stops the runnable thread and causes exit from fun.
     */
    protected void doTerminate () {
        log (m_name + " terminated.");
        dispose ();
        finalTerminate();
    }

    /**
     * This method is only to be invoked if overriding the doTerminate method().
     */
    private void finalTerminate () {
        m_threadState = State.TERMINATED;
        m_name = null;
        m_thread = null;
    }

    protected long sleepTime () {
        return m_sleepTime;
    }
    protected void setSleepTime (long time) {
        if (time < SLEEP_TIME) {
            time = SLEEP_TIME;
        }
        m_sleepTime = time;
    }

    /**
     * Returns whether this runnable should terminate.  If <code>true</code>,
     * causes the doTerminate to be called.
     * <p>
     * The default is query Rainbow to determine if we should terminate.
     * Override to define special conditions.
     * @return boolean  <code>true</code> if runnable should terminate, <code>false</code> otherwise
     */
    private boolean shouldTerminate () {
        return m_threadState == State.TERMINATED;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run () {
        Thread currentThread = Thread.currentThread();
        int errorCount = 0;
        long nextRelease = new Date ().getTime () + m_sleepTime;
        
        while (m_thread == currentThread) {
            long sleepTime = nextRelease -  new Date ().getTime ();

            /*
             * a release is skipped if the task is behind by more
             * than DELAY_TOLERANCE
             */
            boolean isTaskBehind = sleepTime < -DELAY_TOLERANCE;
            boolean interrupted = false;
            if (!isTaskBehind) {
                try {
                    Thread.sleep (Math.max (0,sleepTime));
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            if (!interrupted) {
            	nextRelease += m_sleepTime;
            }
            if (m_threadState == State.STARTED) {  // only process if started
                if (shouldTerminate()) {
                    // time to stop RainbowRunnable as well
                    doTerminate();
                } else if (!isTaskBehind && !interrupted) {
                    try {
                        runAction();
                    } catch (Throwable t) {
                        if (errorCount < 3) {
                            // Change this so that the error is reported, but the thread doesn't terminate
                            String errMsg = MessageFormat
                                    .format("Runtime error in {0}! ... Continuing for {1} more attempts.",
                                            m_name, (3 - ++errorCount));
                            m_reportingPort.error (getComponentType (), errMsg, t);
                            t.printStackTrace ();
                        }
                        else {
                            // make sure Rainbow can dispose despite error... by terminating
                            m_reportingPort.fatal (getComponentType (), "Runtime , terminating runnable " + m_name
                                    + "!", t);
                            terminate();
                            // TODO: BRS work out restart strategy
//                            // cause a Rainbow restart if we're the event service, or slave
//                            if (this instanceof IEventService
//                                    || !Rainbow.isMaster()) {
//                                Rainbow.signalTerminate(Rainbow.ExitState.RESTART);
//                            }
                        }
                    }
                }
            }
            // check whether to advance state, not if terminated
            if (m_threadState != m_nextState && m_threadState != State.TERMINATED) {
                switch (m_nextState) {
                case STARTED:
                    start();
                    break;
                case STOPPED:
                    doStop();
                    break;
                case TERMINATED:
                    doTerminate();
                    break;
                default:
                    break;
                }
                if (m_restarting && m_nextState == State.STOPPED) {
                    m_nextState = State.STARTED;  // cause restart next
                    m_restarting = false;
                } else {
                    m_nextState = m_threadState;
                }
            }
        }
        log ("Terminated " + m_name);
    }

    protected Thread activeThread () {
        return m_thread;
    }

    protected abstract void log (String txt);
    protected abstract void runAction (); 

    public abstract RainbowComponentT getComponentType ();

    /**
     * Sets the next State to transition this Runnable to; with appropriate guard.
     * @param nextState  new runnable state
     */
    private void transition(State nextState) {
        // rule out prohibited transitions, basically TERMINATED->* and *->RAW
        if (m_threadState == State.TERMINATED || nextState == State.RAW) return;
        if (m_restarting && m_threadState != State.STOPPED) {
            m_restarting = false;
            return;
        }

        m_nextState = nextState;
    }

    public IRainbowReportingPort reportingPort () {
        return m_reportingPort;
    }

}
