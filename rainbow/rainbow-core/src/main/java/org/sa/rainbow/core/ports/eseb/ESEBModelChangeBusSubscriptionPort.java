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

import org.sa.rainbow.core.models.ModelReference;
import org.sa.rainbow.core.ports.IModelChangeBusPort;
import org.sa.rainbow.core.ports.IModelChangeBusSubscriberPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.IESEBListener;
import org.sa.rainbow.core.util.Pair;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class ESEBModelChangeBusSubscriptionPort extends AbstractESEBDisposablePort implements
IModelChangeBusSubscriberPort {

    private final Collection<Pair<IRainbowChangeBusSubscription, IRainbowModelChangeCallback>> m_subscribers = new LinkedList<> ();

    public ESEBModelChangeBusSubscriptionPort () throws IOException {
        this (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort ());

    }

    private ESEBModelChangeBusSubscriptionPort (String esebClientHost, short esebClientPort) throws IOException {
        super (esebClientHost, esebClientPort, ChannelT.MODEL_CHANGE);
        getConnectionRole ().addListener (new IESEBListener () {

            @Override
            public void receive (RainbowESEBMessage msg) {
                if (msg.getProperty (ESEBConstants.MSG_CHANNEL_KEY).equals (ChannelT.MODEL_CHANGE.name ())) {
                    synchronized (m_subscribers) {
                        for (Pair<IRainbowChangeBusSubscription, IRainbowModelChangeCallback> pair : m_subscribers) {
                            if (pair.firstValue ().matches (msg)) {
                                ModelReference mr = new ModelReference ((String )msg
                                        .getProperty (IModelChangeBusPort.MODEL_NAME_PROP), (String )msg
                                        .getProperty (IModelChangeBusPort.MODEL_TYPE_PROP));
                                pair.secondValue ().onEvent (mr, msg);
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void subscribe (IRainbowChangeBusSubscription subscriber, IRainbowModelChangeCallback callback) {
        Pair<IRainbowChangeBusSubscription, IRainbowModelChangeCallback> subscription = new Pair<> (subscriber,
                callback);
        synchronized (m_subscribers) {
            m_subscribers.add (subscription);
        }
    }

    @Override
    public void unsubscribe (IRainbowModelChangeCallback callback) {
        synchronized (m_subscribers) {
            for (Iterator i = m_subscribers.iterator (); i.hasNext ();) {
                Pair<IRainbowChangeBusSubscription, IRainbowModelChangeCallback> subscription = (Pair<IRainbowChangeBusSubscription, IRainbowModelChangeCallback> )i
                        .next ();
                if (subscription.secondValue () == callback) {
                    i.remove ();
                }
            }
        }
    }

}
