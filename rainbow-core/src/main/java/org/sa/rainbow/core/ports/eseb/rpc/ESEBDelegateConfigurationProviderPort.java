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
package org.sa.rainbow.core.ports.eseb.rpc;

import edu.cmu.cs.able.eseb.participant.ParticipantException;
import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;
import org.sa.rainbow.core.ports.eseb.ESEBProvider;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class ESEBDelegateConfigurationProviderPort extends AbstractESEBDisposableRPCPort implements
IESEBDelegateConfigurationPort {

    private RainbowDelegate m_delegate;

    public ESEBDelegateConfigurationProviderPort (RainbowDelegate delegate) throws IOException, ParticipantException {
        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (), delegate.getId ());
        m_delegate = delegate;

        // Port runs on the master

        getConnectionRole().createRegistryWrapper (IESEBDelegateConfigurationPort.class, this,
                m_delegate.getId () + IESEBDelegateConfigurationPort.class.getSimpleName ());

    }

    @Override
    public void sendConfigurationInformation (Properties props,
            List<ProbeAttributes> probes,
            List<EffectorAttributes> effectors, List<GaugeInstanceDescription> gauges) {
        m_delegate.receiveConfigurationInformation (props, probes, effectors, gauges);
    }

}
