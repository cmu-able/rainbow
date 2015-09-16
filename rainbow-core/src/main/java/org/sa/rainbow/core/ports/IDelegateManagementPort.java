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
package org.sa.rainbow.core.ports;

import java.util.Properties;

/**
 * This interface specifies the port interface through which the Rainbow master interacts with a delegate. It is
 * intended that there be one port per delegate in the master.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public interface IDelegateManagementPort extends IDisposablePort {
    // identification information

    /**
     * 
     * @return an ID of the delegate
     */
    String getDelegateId ();

    // Master --> Delegate

    /**
     * Sends configuration information to the delegate attached to this port. The configuration information is a set of
     * properties intended to specify how the delegate should connect to various busses and communication channels.
     * Other information (such as what gauges, probes, etc to start) may be passed through other methods in this
     * interface.
     * 
     * @param configuration
     */
    void sendConfigurationInformation (Properties configuration);

    // Delegate --> Master
    /**
     * This is called by the delegate periodically to indicate that it is still alive. The intent is that this method
     * forward information to the Master
     */
    void heartbeat ();

    /**
     * This is called by a delegate when it wants configuration information to be resent.
     */
    void requestConfigurationInformation ();

    // Lifecycle commands
    // Master --> Delegate

    /**
     * Tells the delegate to start. This method will block until the delegate has successfully started (in which case it
     * will return true) or fails to start (in which case it will return false).
     * 
     * @throws IllegalStateException
     *             Thrown if the delegate is not in a state where it can be started. (e.g., it was terminated)
     */
    boolean startDelegate () throws IllegalStateException;

    /**
     * Tells the delegate to pause, blocking until the delegate has successfully paused (in which case it will return
     * true) or fails to pause (returning false).
     * 
     * @return
     * @throws IllegalStateException
     *             Thrown if the delegate is not in a state where it can be paused.
     */
    boolean pauseDelegate () throws IllegalStateException;

    /**
     * Tells the delegate to terminate, effectively shutting it down. Ideally, before terminating, the delegate will
     * disconnect from the master.
     * 
     * @return
     * @throws IllegalStateException
     */
    boolean terminateDelegate () throws IllegalStateException;

    /**
     * Sends a signal to the delegates to start probes
     * 
     * @throws IllegalStateException
     */
    void startProbes () throws IllegalStateException;

    /**
     * Sends a signal to the delegates to kill probes
     * 
     * @throws IllegalStateException
     */
    void killProbes () throws IllegalStateException;

}
