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

    enum State {
        RAW, STARTED, STOPPED, TERMINATED
    }

    long SLEEP_TIME = 100;  // ms
    long LONG_SLEEP_TIME = 1000;  // ms
    long SHORT_SLEEP_TIME = 10;  // ms
    /** Threshold value useful for preventing a runnable from hogging CPU */
    int MAX_CYCLES_PER_SLEEP = 100;

    /**
     * Returns the name of this runnable.
     * @return String  the IRainbowRunnable's name
     */
    String id ();

    /**
     * Starts the execution of the runnable, usually via thisThread.start().
     */
    void start ();

    /**
     * Pauses the execution of the runnable, preserving any states until it is
     * resumed again by call to start.  This method actually calls an
     * implementation method, transition().
     */
    void stop ();

    /**
     * Stops, then starts, the execution of the runnable.
     * This method actually calls an implementation method, transition().
     */
    void restart ();

    /**
     * Stops the runnable thread and (potentially) causes exit from fun.
     * This method actually calls an implementation method, transition().
     */
    void terminate ();

    /**
     * Returns the running state of this runnable.
     * @return State  one of the states, <code>RAW</code>, <code>STARTED</code>, <code>STOPPED</code>, <code>TERMINATED</code> 
     */
    State state ();

    /**
     * A convenience method for checking whether this IRainbowRunnable is in a
     * TERMINATED state.
     * @return boolean  <code>true</code> if TERMINATED, <code>false</code> otherwise.
     */
    boolean isTerminated ();

}
