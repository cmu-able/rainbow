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


import org.sa.rainbow.core.ports.IProbeReportPort;
import org.sa.rainbow.core.ports.IProbeReportSubscriberPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.IESEBListener;
import org.sa.rainbow.translator.probes.IProbeIdentifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ESEBProbeReportSubscriberPort extends AbstractESEBDisposablePort implements IProbeReportSubscriberPort {

    private IProbeReportPort m_callback;
    private final Map<String, Set<String>> m_subscriptions = new HashMap<> ();

    public ESEBProbeReportSubscriberPort (IProbeReportPort callback) throws IOException {
        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (), ChannelT.SYSTEM_US);
        m_callback = callback;
        getConnectionRole().addListener (new IESEBListener () {

            @Override
            public void receive (RainbowESEBMessage msg) {
                processMessage (msg);
            }
        });
    }

    // Should only be used for testing
    ESEBProbeReportSubscriberPort () throws IOException {
        super (null, (short )-1, null);
    }

    @Override
    public void subscribeToProbe (String probeType, String location) {
        synchronized (m_subscriptions) {
            Set<String> locations = m_subscriptions.get (probeType);
            if (locations == null) {
                locations = new HashSet<> ();
                locations.add (location == null ? "*" : location);
                m_subscriptions.put (probeType, locations);
            }
            else {
                if (locations.contains ("*") && location != null) {
                    locations.remove (location);
                }
                else if (!locations.contains ("*") && location == null) {
                    locations.clear ();
                    locations.add ("*");
                }
                else {
                    locations.add (location == null ? "*" : location);
                }
            }
        }
    }

    @Override
    public void unsubscribeToProbe (String probeType, String location) {
        synchronized (m_subscriptions) {
            if (location == null) {
                m_subscriptions.remove (probeType);
            }
            else {
                Set<String> locations = m_subscriptions.get (probeType);
                if (locations.contains ("*")) {
                    locations.add (location);
                }
                else {
                    locations.remove (location);
                }
            }
        }
    }

    private void processMessage (final RainbowESEBMessage msg) {
        String type = (String )msg.getProperty (ESEBConstants.MSG_TYPE_KEY);
        if (ESEBConstants.MSG_TYPE_PROBE_REPORT.equals (type)) {
            final String probeType = (String )msg.getProperty (ESEBConstants.MSG_PROBE_TYPE_KEY);
            final String probeLocation = (String )msg.getProperty (ESEBConstants.MSG_PROBE_LOCATION_KEY);
            boolean subscribedToMessage = subscribedToProbe (probeType, probeLocation);
            if (subscribedToMessage) {
                m_callback.reportData (new IProbeIdentifier () {


                    @Override
                    public String id () {
                        return (String )msg.getProperty (ESEBConstants.MSG_PROBE_ID_KEY);
                    }


                    @Override
                    public String type () {
                        return probeType;
                    }


                    @Override
                    public String name () {
                        return id ();
                    }


                    @Override
                    public String location () {
                        return probeLocation;
                    }

                }, (String )msg.getProperty (ESEBConstants.MSG_DATA_KEY));
            }
        }
    }

    public boolean subscribedToProbe (String probeType, String probeLocation) {
        Set<String> locationsInterestedIn;
        synchronized (m_subscriptions) {
            locationsInterestedIn = m_subscriptions.get (probeType);
        }
        if (locationsInterestedIn == null) return false;
        boolean subscribed = false;
        if (locationsInterestedIn.contains ("*") && !locationsInterestedIn.contains (probeLocation)) {
            subscribed = true;
        }
        else if (locationsInterestedIn.contains (probeLocation)) {
            subscribed = true;
        }
        return subscribed;
    }

}
