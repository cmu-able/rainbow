package org.sa.rainbow.core.gauges;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sa.rainbow.core.RainbowComponentT;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.ports.IRainbowReportingPort;
import org.sa.rainbow.core.util.TypedAttribute;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

/**
 * A class for managing the creation and starting of gauges local to a delegate.
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public class LocalGaugeManager {

    IRainbowReportingPort m_reportingPort;

    /** tThe list of gauges started by this gauge manager **/
    private Map<String, IGauge> m_id2Gauge = new HashMap<> ();

    /** The list of guage descriptions started by this gauge manager **/
    private GaugeDescription    m_gauges   = new GaugeDescription ();

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
            long gaugeBeaconPeriod = (Long )beaconPeriod.getValue ();
            TypedAttributeWithValue javaClassAttr = instDesc.findSetupParam (IGauge.SETUP_JAVA_CLASS);
            if (javaClassAttr == null) {
                m_reportingPort.error (RainbowComponentT.GAUGE_MANAGER, MessageFormat.format (
                        "Could not create a gauge of the type {0}: Expected setup parameter {1} not found!",
                        gaugeType, IGauge.SETUP_JAVA_CLASS));
                continue;
            }
            String gaugeClassName = (String )javaClassAttr.getValue ();
            try {
                Class<?> gaugeClass = Class.forName (gaugeClassName);
                doCreateGauge (gaugeClass, gaugeBeaconPeriod, instDesc);
            }
            catch (ClassNotFoundException e) {
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
        }
        catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException | RainbowConnectionException e) {
            m_reportingPort.error (RainbowComponentT.GAUGE_MANAGER,
                    MessageFormat.format ("Gauge construction for {0} failed!", id), e);
        }

        return gauge;

    }

}
