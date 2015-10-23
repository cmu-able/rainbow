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


import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.gauges.IGaugeIdentifier;
import org.sa.rainbow.core.gauges.IGaugeProtocol;
import org.sa.rainbow.core.ports.IGaugeLifecycleBusPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.IESEBListener;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ESEBReceiverSideGaugeLifecyclePort extends AbstractESEBDisposablePort implements IGaugeLifecycleBusPort {

    private IGaugeLifecycleBusPort m_manager;

    class MessageGaugeIdentifier implements IGaugeIdentifier {

        private final IRainbowMessage m_msg;

        public MessageGaugeIdentifier (IRainbowMessage msg) {
            m_msg = msg;
        }


        @Override
        public String id () {
            return (String )m_msg.getProperty (IGaugeProtocol.ID);
        }


        @Override
        public TypedAttribute gaugeDesc () {
            return new TypedAttribute ((String )m_msg.getProperty (IGaugeProtocol.GAUGE_NAME),
                    (String )m_msg.getProperty (IGaugeProtocol.GAUGE_TYPE));
        }


        @Override
        public TypedAttribute modelDesc () {
            return new TypedAttribute ((String )m_msg.getProperty (IGaugeProtocol.MODEL_NAME),
                    (String )m_msg.getProperty (IGaugeProtocol.MODEL_TYPE));
        }

    }

    public ESEBReceiverSideGaugeLifecyclePort (IGaugeLifecycleBusPort manager) throws IOException {
        super (ESEBProvider.getESEBClientPort (RainbowConstants.PROPKEY_MASTER_CONNECTION_PORT), ChannelT.HEALTH);
        m_manager = manager;
        getConnectionRole().addListener (new IESEBListener () {

            @Override
            public void receive (RainbowESEBMessage msg) {
                String type = (String )msg.getProperty (ESEBConstants.MSG_TYPE_KEY);
                MessageGaugeIdentifier mgi;
                switch (type) {
                case IGaugeProtocol.GAUGE_CREATED:
                    mgi = new MessageGaugeIdentifier (msg);
                    reportCreated (mgi);
                    break;
                case IGaugeProtocol.GAUGE_DELETED:
                    mgi = new MessageGaugeIdentifier (msg);
                    reportDeleted (mgi);
                    break;
                case IGaugeProtocol.GAUGE_HEARTBEAT:
                    mgi = new MessageGaugeIdentifier (msg);
                    sendBeacon (mgi);
                    break;
                case IGaugeProtocol.GAUGE_CONFIGURED: {
                    mgi = new MessageGaugeIdentifier (msg);
                    List<TypedAttributeWithValue> params = new LinkedList<> ();

                    int i = 0;
                    do {
                        String name = (String )msg.getProperty (IGaugeProtocol.CONFIG_PARAM_NAME + i);
                        if (name == null) {
                            break;
                        }
                        String t = (String )msg.getProperty (IGaugeProtocol.CONFIG_PARAM_TYPE + i);
                        Object v = msg.getProperty (IGaugeProtocol.CONFIG_PARAM_VALUE + i);
                        i++;
                        params.add (new TypedAttributeWithValue (name, t, v));
                    } while (true);
                    reportConfigured (mgi, params);
                }
                break;
                }

            }
        });
    }

    @Override
    public void reportCreated (IGaugeIdentifier gauge) {
        m_manager.reportCreated (gauge);
    }

    @Override
    public void reportDeleted (IGaugeIdentifier gauge) {
        m_manager.reportDeleted (gauge);
    }

    @Override
    public void reportConfigured (IGaugeIdentifier gauge, List<TypedAttributeWithValue> configParams) {
        m_manager.reportConfigured (gauge, configParams);
    }

    @Override
    public void sendBeacon (IGaugeIdentifier gauge) {
        m_manager.sendBeacon (gauge);
    }

}
