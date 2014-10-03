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
package org.sa.rainbow.core.ports.local;

import java.io.IOException;
import java.util.Properties;

import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.AbstractDelegateConnectionPort;
import org.sa.rainbow.core.ports.IDelegateManagementPort;

public class LocalDelegateConnectionPort extends AbstractDelegateConnectionPort {

    private LocalMasterConnectionPort m_masterPort;
    private LocalRainbowPortFactory m_factory;

    public LocalDelegateConnectionPort (RainbowDelegate delegate, LocalRainbowPortFactory factory) throws IOException {
        super (delegate, null, (short )-1, null);
        m_factory = factory;
    }

    @Override
    public IDelegateManagementPort connectDelegate (String delegateID, Properties connectionProperties) throws RainbowConnectionException {
        if (m_masterPort != null) {
            IDelegateManagementPort ddp = m_factory.createDelegateSideManagementPort (m_delegate, m_delegate.getId ());
            m_masterPort.connectDelegate (delegateID, connectionProperties);
            return ddp;
        }
        else
            throw new IllegalStateException ("The delegate port has not been connected to a master!");
    }

    @Override
    public void disconnectDelegate (String delegateId) {
        if (m_masterPort != null) {
            m_masterPort.disconnectDelegate (delegateId);
        }
    }

    void connect (LocalMasterConnectionPort masterPort) {
        m_masterPort = masterPort;
    }

    RainbowDelegate getDelegate () {
        return m_delegate;
    }

    @Override
    public void dispose () {
    }

    @Override
    public void report (String delegateID, ReportType type, RainbowComponentT compT, String msg) {
        if (m_masterPort != null) {
            m_masterPort.report (delegateID, type, compT, msg);
        }
    }

    @Override
    public void trace (RainbowComponentT type, String msg) {

    }

}
