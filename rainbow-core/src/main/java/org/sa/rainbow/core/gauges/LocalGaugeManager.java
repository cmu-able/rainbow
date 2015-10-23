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


import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class for managing the creation and starting of gauges local to a delegate.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public class LocalGaugeManager {

    final IRainbowReportingPort m_reportingPort;

    /** tThe list of gauges started by this gauge manager **/

    private Map<String, IGauge> m_id2Gauge = new HashMap<> ();

    /** The list of guage descriptions started by this gauge manager **/
    private final GaugeDescription m_gauges = new GaugeDescription ();

    public LocalGaugeManager (String id, IRainbowReportingPort masterConnectionPort) {
        m_reportingPort = masterConnectionPort;
    }

    public void initGauges (List<GaugeInstanceDescription> gauges) {
        for (GaugeInstanceDescription instDesc : gauges) {
            m_gauges.instSpec.put (instDesc.gaugeName (), instDesc);
            m_gauges.typeSpec.put (instDesc.gaugeType (), instDesc);
            String gaugeType = instDesc.gaugeType ();
            TypedAttributeWithValue beaconPeriod = instDesc.findSetupParam (IGauge.SETUP_BEACON_PERIOD);
            if (beaconPeriod == null) {
                m_reportingPort.error (RainbowComponentT.GAUGE_MANAGER, MessageFormat.format (
                        "Could not create Gauge Mananager for {0}. Expected setup param beaconPeriod not found.",
                        instDesc.gaugeType ()));
                continue;
            }
            long gaugeBeaconPeriod = (Long) beaconPeriod.getValue ();
            TypedAttributeWithValue javaClassAttr = instDesc.findSetupParam (IGauge.SETUP_JAVA_CLASS);
            if (javaClassAttr == null) {
                m_reportingPort.error (RainbowComponentT.GAUGE_MANAGER, MessageFormat.format (
                        "Could not create a gauge of the type {0}: Expected setup parameter {1} not found!",
                        gaugeType, IGauge.SETUP_JAVA_CLASS));
                continue;
            }
            String gaugeClassName = (String) javaClassAttr.getValue ();
            try {
                Class<?> gaugeClass = Class.forName (gaugeClassName);
                doCreateGauge (gaugeClass, gaugeBeaconPeriod, instDesc);
            } catch (ClassNotFoundException e) {
                m_reportingPort.error (RainbowComponentT.GAUGE_MANAGER, MessageFormat.format (
                        "Could not create gauge of type {0}: Class ''{1}'' not found!", gaugeType, gaugeClassName), e);
            }
        }

    }


    private IGauge doCreateGauge (Class<?> gaugeClass, long beaconPeriod, GaugeInstanceDescription instDesc) {
        AbstractGauge gauge = null;
        String id = GaugeInstanceDescription.genID (instDesc);
        Class<?>[] paramTypes = new Class[6];
        paramTypes[0] = String.class;
        paramTypes[1] = long.class;
        paramTypes[2] = TypedAttribute.class;
        paramTypes[3] = TypedAttribute.class;
        paramTypes[4] = List.class;
        paramTypes[5] = Map.class;
        try {
            Constructor<?> constructor = gaugeClass.getConstructor (paramTypes);
            Object[] args = new Object[6];
            args[0] = id;
            args[1] = beaconPeriod;
            args[2] = new TypedAttribute (instDesc.gaugeName (), instDesc.gaugeType ());
            args[3] = instDesc.modelDesc ();
            args[4] = instDesc.setupParams ();
            args[5] = instDesc.mappings ();
            gauge = (AbstractGauge )constructor.newInstance (args);
            gauge.initialize (m_reportingPort);
            gauge.start ();
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException | RainbowConnectionException e) {
            m_reportingPort.error (RainbowComponentT.GAUGE_MANAGER,
                    MessageFormat.format ("Gauge construction for {0} failed!", id), e);
        }

        return gauge;

    }

}
