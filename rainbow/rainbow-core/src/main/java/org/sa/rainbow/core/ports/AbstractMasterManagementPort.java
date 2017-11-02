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

import java.io.IOException;

import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.ports.eseb.AbstractESEBDisposablePort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;

/**
 * * This class represents the common methods for master deployment port. These methods correspond to those that are
 * sent to the master from a delegate.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public abstract class AbstractMasterManagementPort extends AbstractESEBDisposablePort implements IDelegateManagementPort {

    private RainbowMaster m_master;
    private String        m_delegateID;

    /**
     * Construct a deployment port for the Rainbow Master for processing communication with the identified delegate
     * 
     * @param master
     *            The master that will contain this port
     * @param delegateID
     *            The id of the delegate that is being communicated with
     * @param channel
     * @param port
     * @param host
     * @throws IOException
     */
    protected AbstractMasterManagementPort (RainbowMaster master, String delegateID, String host, Short port,
            ChannelT channel) throws IOException {
        super (host, port, channel);
        m_master = master;
        m_delegateID = delegateID;
    }

    @Override
    public String getDelegateId () {
        return m_delegateID;
    }

    @Override
    public void heartbeat () {
        m_master.processHeartbeat (m_delegateID);
    }

    @Override
    public void requestConfigurationInformation () {
        m_master.requestDelegateConfiguration (m_delegateID);
    }

}
