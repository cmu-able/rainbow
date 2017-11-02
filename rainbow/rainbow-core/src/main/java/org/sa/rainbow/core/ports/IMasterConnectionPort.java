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

import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.error.RainbowConnectionException;

import java.util.Properties;

/**
 * This interface represents a port through which delegates connect to the master
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public interface IMasterConnectionPort extends IDisposablePort {

    /** Message types for sending to the Rainbow Master, which could be displayed on the UI **/
    enum ReportType {
        INFO, WARNING, ERROR, FATAL
    }

    /**
     * Connects a delegate to the master through the connection port
     * 
     * @param delegateID
     *            The id of the delegate being connected
     * @param connectionProperties
     *            The connection properties, representing information from the delegate that needs to be passed to the
     *            master.
     * 
     * @return A deployment port through which the delegate can be managed
     * @throws RainbowConnectionException TODO
     */
    IDelegateManagementPort connectDelegate (String delegateID, Properties connectionProperties) throws RainbowConnectionException;

    /**
     * Disconnects the delegate from the master. The master will delete the delegate from its records. Any processing
     * that comes from a disconnected delegate will be logged as an error and not processed.
     * 
     * @param delegateId
     *            The delegate being disconnected
     */
    void disconnectDelegate (String delegateId);

    void report (String delegateID, ReportType type, RainbowComponentT compT, String msg);


}
