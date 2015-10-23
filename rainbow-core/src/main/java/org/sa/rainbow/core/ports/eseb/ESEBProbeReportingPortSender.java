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

import org.apache.log4j.Logger;
import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.ports.IProbeReportPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.translator.probes.IProbeIdentifier;

import java.io.IOException;
import java.text.MessageFormat;

public class ESEBProbeReportingPortSender extends AbstractESEBDisposablePort implements IProbeReportPort {
    private static final Logger LOGGER = Logger.getLogger (ESEBProbeReportingPortSender.class);
    private Identifiable  m_sender;

    public ESEBProbeReportingPortSender (Identifiable probe) throws IOException {
        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (), ChannelT.SYSTEM_US);
        m_sender = probe;

    }

    @Override
    public void reportData (IProbeIdentifier probe, String data) {
        if (probe.id ().equals (m_sender.id ())) {
            RainbowESEBMessage msg = getConnectionRole().createMessage ();
            msg.setProperty (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_PROBE_REPORT);
            msg.setProperty (ESEBConstants.MSG_PROBE_ID_KEY, m_sender.id ());
            msg.setProperty (ESEBConstants.MSG_PROBE_LOCATION_KEY, probe.location ());
            msg.setProperty (ESEBConstants.MSG_PROBE_TYPE_KEY, probe.type ());
            msg.setProperty (ESEBConstants.MSG_DATA_KEY, data);
            getConnectionRole().publish (msg);
        }
        else {
            LOGGER.error (MessageFormat.format ("Attempt to send a report on {0}''s reporting port by {1}", m_sender.id (), probe.id ()));
        }
    }

}
