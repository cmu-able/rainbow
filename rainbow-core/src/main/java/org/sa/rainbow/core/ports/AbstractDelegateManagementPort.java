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

import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;
import org.sa.rainbow.core.ports.eseb.AbstractESEBDisposablePort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

/**
 * This class represents the common methods for delegate deployment ports. These methods correspond to those that are
 * sent to the delegate from the master.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public abstract class AbstractDelegateManagementPort extends AbstractESEBDisposablePort implements
IDelegateManagementPort {

    private RainbowDelegate m_delegate;

    /**
     * Create a new DeploymentPort for the delegate
     * 
     * @param delegate
     *            The delegate for this port
     * @param channel
     * @param port
     * @param host
     * @throws IOException
     */
    protected AbstractDelegateManagementPort (RainbowDelegate delegate, String host, short port, ChannelT channel)
            throws IOException {
        super (host, port, channel);
        m_delegate = delegate;
    }

    @Override
    public String getDelegateId () {
        return m_delegate.getId ();
    }

    @Override
    //TODO: Delete this interfaces
    public void sendConfigurationInformation (Properties configuration) {
        m_delegate.receiveConfigurationInformation (configuration, Collections.<ProbeAttributes> emptyList (),
                Collections.<EffectorAttributes> emptyList (), Collections.<GaugeInstanceDescription> emptyList ());

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
    public void startProbes () throws IllegalStateException {
        m_delegate.startProbes ();
    }

    @Override
    public void killProbes () throws IllegalStateException {
        m_delegate.killProbes ();
    }

}
