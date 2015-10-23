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


import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.ports.IProbeLifecyclePort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.translator.probes.IProbe;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

public class ESEBProbeLifecyclePort extends AbstractESEBDisposablePort implements IProbeLifecyclePort {

    private IProbe m_probe;

    public ESEBProbeLifecyclePort (IProbe probe) throws IOException {
        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (), ChannelT.HEALTH);
        m_probe = probe;
        // All these messages go on the HEALTH channel. Runs on master
    }

    @Override
    public void reportCreated () {
        RainbowESEBMessage msg = getConnectionRole().createMessage (/*ChannelT.HEALTH*/);
        setCommonGaugeProperties (msg);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IProbeLifecyclePort.PROBE_CREATED);
        getConnectionRole().publish (msg);
    }

    private void setCommonGaugeProperties (RainbowESEBMessage msg) {
        msg.setProperty (IProbeLifecyclePort.PROBE_ID, m_probe.id ());
        msg.setProperty (IProbeLifecyclePort.PROBE_LOCATION, m_probe.location ());
        msg.setProperty (IProbeLifecyclePort.PROBE_NAME, m_probe.name ());
    }

    @Override
    public void reportDeleted () {
        RainbowESEBMessage msg = getConnectionRole().createMessage ();
        setCommonGaugeProperties (msg);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IProbeLifecyclePort.PROBE_DELETED);
        getConnectionRole().publish (msg);
    }

    @Override
    public void reportConfigured (Map<String, Object> configParams) {
        RainbowESEBMessage msg = getConnectionRole().createMessage ();
        setCommonGaugeProperties (msg);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IProbeLifecyclePort.PROBE_CONFIGURED);
        int i = 0;
        for (Entry<String, Object> e : configParams.entrySet ()) {
            try {
                msg.setProperty (IProbeLifecyclePort.CONFIG_PARAM_NAME + i, e.getKey ());
                msg.setProperty (IProbeLifecyclePort.CONFIG_PARAM_VALUE + i, e.getValue ());
                i++;
            }
            catch (RainbowException e1) {
                e1.printStackTrace ();
            }
        }
        getConnectionRole().publish (msg);
    }

    @Override
    public void reportDeactivated () {
        RainbowESEBMessage msg = getConnectionRole().createMessage ();
        setCommonGaugeProperties (msg);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IProbeLifecyclePort.PROBE_DEACTIVATED);
        getConnectionRole().publish (msg);

    }

    @Override
    public void reportActivated () {
        RainbowESEBMessage msg = getConnectionRole().createMessage ();
        setCommonGaugeProperties (msg);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IProbeLifecyclePort.PROBE_ACTIVATED);
        getConnectionRole().publish (msg);

    }


}
