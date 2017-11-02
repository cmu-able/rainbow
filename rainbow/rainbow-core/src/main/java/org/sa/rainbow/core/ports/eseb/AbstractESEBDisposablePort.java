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
package org.sa.rainbow.core.ports.eseb;

import org.sa.rainbow.core.ports.IDisposablePort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;

import java.io.IOException;

/**
 * The parent class of all ESEB publishing ports. This class manages the connection to ESEB, as well as disposing
 * resources.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public abstract class AbstractESEBDisposablePort implements IDisposablePort {


    private ESEBConnector m_connectionRole;

    protected AbstractESEBDisposablePort (String host, short port, ChannelT channel) throws IOException {
        if (host != null) {
            m_connectionRole = new ESEBConnector (host, port, channel);
        }
    }

    protected AbstractESEBDisposablePort (short port, ChannelT channel) throws IOException {
        m_connectionRole = new ESEBConnector (port, channel);
    }

    /**
     * Closes the connection for this port.
     */
    @Override
    public void dispose () {
        if (getConnectionRole () == null) return;
        getConnectionRole ().close ();
        m_connectionRole = null;
    }


    public ESEBConnector getConnectionRole () {
        return m_connectionRole;
    }


}
