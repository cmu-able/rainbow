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

import java.util.Properties;

import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.ports.IDelegateManagementPort;

public class LocalMasterSideManagementPort implements IDelegateManagementPort {

    private String m_delegateID;
    private RainbowMaster m_master;
    private IDelegateManagementPort m_connectedPort;

    public LocalMasterSideManagementPort (RainbowMaster rainbowMaster, String delegateID) {
        m_delegateID = delegateID;
        m_master = rainbowMaster;
    }

    @Override
    public String getDelegateId () {
        return m_delegateID;
    }


    @Override
    public void sendConfigurationInformation (Properties configuration) {
        m_connectedPort.sendConfigurationInformation (configuration);
    }

    @Override
    public void heartbeat () {
        m_master.processHeartbeat (m_delegateID);
    }

    @Override
    public void requestConfigurationInformation () {
        m_master.requestDelegateConfiguration (m_delegateID);
    }

    public void connect (LocalDelegateManagementPort port) {
        m_connectedPort = port;
    }

    @Override
    public boolean startDelegate () throws IllegalStateException {
        return m_connectedPort.startDelegate ();
    }

    @Override
    public boolean pauseDelegate () throws IllegalStateException {
        return m_connectedPort.pauseDelegate ();
    }

    @Override
    public boolean terminateDelegate () throws IllegalStateException {
        return m_connectedPort.terminateDelegate ();
    }

    @Override
    public void dispose () {
    }

    @Override
    public void startProbes () throws IllegalStateException {
        m_connectedPort.startProbes ();
    }

    @Override
    public void killProbes () throws IllegalStateException {
        m_connectedPort.killProbes ();
    }

}
