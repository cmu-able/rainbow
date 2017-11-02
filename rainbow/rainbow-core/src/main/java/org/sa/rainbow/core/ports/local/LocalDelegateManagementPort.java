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

import org.apache.log4j.Logger;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.ports.IDelegateManagementPort;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Properties;

public class LocalDelegateManagementPort implements IDelegateManagementPort {

    static final Logger LOGGER = Logger.getLogger (LocalDelegateManagementPort.class);

    private final RainbowDelegate m_delegate;
    private final String m_delegateID;
    LocalMasterSideManagementPort m_connectedTo;

    public LocalDelegateManagementPort (RainbowDelegate delegate, String delegateID) {
        m_delegate = delegate;
        m_delegateID = delegateID;

    }

    @Override
    public String getDelegateId () {
        return m_delegateID;
    }

    @Override
    public void sendConfigurationInformation (Properties configuration) {
        m_delegate.receiveConfigurationInformation (configuration, Collections.EMPTY_LIST, Collections.EMPTY_LIST,
                Collections.EMPTY_LIST);
    }

    @Override
    public void heartbeat () {
        checkConnected ();
        m_connectedTo.heartbeat ();
    }

    private void checkConnected () {
        if (m_connectedTo == null) {
            String errMsg = MessageFormat.format ("Local delegate port not connected to a master: {0}", m_delegateID);
            LOGGER.error (errMsg);
            throw new IllegalStateException (errMsg);
        }
    }

    @Override
    public void requestConfigurationInformation () {
        checkConnected ();
        m_connectedTo.requestConfigurationInformation ();
    }

    public void connect (LocalMasterSideManagementPort port) {
        m_connectedTo = port;
    }

    @Override
    public boolean startDelegate () throws IllegalStateException {
        m_delegate.start ();
        return true;
    }

    @Override
    public boolean pauseDelegate () throws IllegalStateException {
        m_delegate.stop ();
        return true;
    }

    @Override
    public boolean terminateDelegate () throws IllegalStateException {
        m_delegate.terminate ();
        return true;
    }

    @Override
    public void dispose () {
    }

    @Override
    public void startProbes () throws IllegalStateException {
        m_delegate.startProbes ();
    }

    @Override
    public void killProbes () throws IllegalStateException {
        m_delegate.killProbes ();
    }

}
