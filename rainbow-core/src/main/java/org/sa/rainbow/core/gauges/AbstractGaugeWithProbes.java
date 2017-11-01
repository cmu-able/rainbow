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
package org.sa.rainbow.core.gauges;


import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IProbeReportPort;
import org.sa.rainbow.core.ports.IProbeReportSubscriberPort;
import org.sa.rainbow.core.ports.RainbowPortFactory;
import org.sa.rainbow.core.util.Pair;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.translator.probes.IProbeIdentifier;
import org.sa.rainbow.util.Beacon;
import org.sa.rainbow.util.Util;

public abstract class AbstractGaugeWithProbes extends AbstractGauge {

    private Beacon m_probeBeacon;

    private IProbeReportSubscriberPort m_probeReportingPort;
    private boolean                      m_subscribedToProbePort;

    protected AbstractGaugeWithProbes (String threadName, String id, long beaconPeriod, TypedAttribute gaugeDesc,
            TypedAttribute modelDesc, List<TypedAttributeWithValue> setupParams,
            Map<String, IRainbowOperation> mappings)
                    throws RainbowException {
        super (threadName, id, beaconPeriod, gaugeDesc, modelDesc, setupParams, mappings);

        m_probeBeacon = new Beacon ();
        m_subscribedToProbePort = false;

    }

    public void setBeaconPeriod (long period) {
        m_probeBeacon.setPeriod (period);
    }

    public void reportFromProbe (IProbeIdentifier probe, String data) {
        m_probeBeacon.mark ();
    }

    @Override
    protected void handleConfigParam (TypedAttributeWithValue tav) {
        super.handleConfigParam (tav);
        if (tav.getName ().equals (CONFIG_PROBE_MAPPING)) {
            pubProbeGaugeMapping ((String )tav.getValue ());
        }
        if (tav.getName ().equals (CONFIG_PROBE_MAPPING_LIST)) {
            StringTokenizer tokens = new StringTokenizer ((String )tav.getValue (), ",");
            while (tokens.hasMoreTokens ()) {
                pubProbeGaugeMapping (tokens.nextToken ().trim ());
            }
        }
    }

    /**
     * Given a probe type, subscribe to interest in information from that probe type. Probe Type may have location info,
     * so that gets parsed and passed along.
     * 
     * @param probeType
     *            the probe type identifier
     */
    private void pubProbeGaugeMapping (String probeType) {
        Pair<String, String> nameLocPair = Util.decomposeID (probeType);
        if (nameLocPair.secondValue () == null) {
            // default: no location specified, so only interested in probes on the same host as the gauge
            nameLocPair.setSecondValue (deploymentLocation ());
        }
        else if (nameLocPair.secondValue ().equals (IGauge.ALL_LOCATIONS)) {
            nameLocPair.setSecondValue (null); // null, notify all locations
        }
        if (!m_subscribedToProbePort) {
            try {
                m_probeReportingPort = RainbowPortFactory.createProbeReportingPortSubscriber (new IProbeReportPort () {

                    @Override
                    public void reportData (IProbeIdentifier probe, String data) {
                        AbstractGaugeWithProbes.this.reportFromProbe (probe, data);
                    }

                    @Override
                    public void dispose () {
                    }
                });
                m_subscribedToProbePort = true;
            }
            catch (RainbowConnectionException e) {
                m_reportingPort.error (RainbowComponentT.GAUGE,
                        MessageFormat.format ("Gauge ''{0}'' could not subscribe to probe bus", this.id ()));
            }
        }
        m_probeReportingPort.subscribeToProbe (nameLocPair.firstValue (), nameLocPair.secondValue ());
    }

    @Override
    public void dispose () {
        super.dispose ();
        m_probeReportingPort.dispose ();
        m_probeReportingPort = null;
    }

}
