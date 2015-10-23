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
import org.sa.rainbow.core.gauges.IGaugeIdentifier;
import org.sa.rainbow.core.gauges.IGaugeProtocol;
import org.sa.rainbow.core.ports.IGaugeLifecycleBusPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import java.io.IOException;
import java.util.List;

public class ESEBGaugeSideLifecyclePort extends AbstractESEBDisposablePort implements IGaugeLifecycleBusPort {


    public ESEBGaugeSideLifecyclePort () throws IOException {

        // All these messages go on the HEALTH channel. Runs on master
        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (),
                ChannelT.HEALTH);

    }

    @Override
    public void reportCreated (IGaugeIdentifier gauge) {
        RainbowESEBMessage msg = getConnectionRole().createMessage (/*ChannelT.HEALTH*/);
        setCommonGaugeProperties (msg, gauge);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IGaugeProtocol.GAUGE_CREATED);
        getConnectionRole().publish (msg);
    }

    private void setCommonGaugeProperties (RainbowESEBMessage msg, IGaugeIdentifier gauge) {
        msg.setProperty (IGaugeProtocol.ID, gauge.id ());
        msg.setProperty (IGaugeProtocol.GAUGE_NAME, gauge.gaugeDesc ().getName ());
        msg.setProperty (IGaugeProtocol.GAUGE_TYPE, gauge.gaugeDesc ().getType ());
        msg.setProperty (IGaugeProtocol.MODEL_TYPE, gauge.modelDesc ().getType ());
        msg.setProperty (IGaugeProtocol.MODEL_NAME, gauge.modelDesc ().getName ());
    }

    @Override
    public void reportDeleted (IGaugeIdentifier gauge) {
        RainbowESEBMessage msg = getConnectionRole().createMessage ();
        setCommonGaugeProperties (msg, gauge);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IGaugeProtocol.GAUGE_DELETED);
        getConnectionRole().publish (msg);
    }

    @Override
    public void reportConfigured (IGaugeIdentifier gauge, List<TypedAttributeWithValue> configParams) {
        RainbowESEBMessage msg = getConnectionRole().createMessage ();
        setCommonGaugeProperties (msg, gauge);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IGaugeProtocol.GAUGE_CONFIGURED);
        int i = 0;
        for (TypedAttributeWithValue tav : configParams) {
            msg.setProperty (IGaugeProtocol.CONFIG_PARAM_NAME + i, tav.getName ());
            msg.setProperty (IGaugeProtocol.CONFIG_PARAM_TYPE + i, tav.getType ());
            try {
                msg.setProperty (IGaugeProtocol.CONFIG_PARAM_VALUE + i, tav.getValue ());
            }
            catch (RainbowException e) {
                msg.setProperty (IGaugeProtocol.CONFIG_PARAM_VALUE + i, "unknown");
            }
            i++;
        }
        getConnectionRole().publish (msg);
    }

    @Override
    public void sendBeacon (IGaugeIdentifier gauge) {
        RainbowESEBMessage msg = getConnectionRole().createMessage (/*ChannelT.HEALTH*/);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IGaugeProtocol.GAUGE_HEARTBEAT);
        setCommonGaugeProperties (msg, gauge);
        getConnectionRole().publish (msg);
    }

}
