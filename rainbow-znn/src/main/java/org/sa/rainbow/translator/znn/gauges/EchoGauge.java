package org.sa.rainbow.translator.gauges;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Pattern;

import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.gauges.AbstractGauge;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.util.Util;

/**
 * # # Gauge Type and Gauge Instance Specifications # - time periods generally
 * in milliseconds # # History: # - [SWC 2007.01.18] Created file, populated
 * ZNews gauge descriptions. # - [SWC 2007.04.09] Changed LatencyGaugeT to use
 * RtLatencyMultiHostGauge. # - [SWC 2007.04.10] Modified descriptions for
 * different target locations. # - [SWC 2007.04.11] Added ResponseTimeGaugeT #
 * gauge-types:
 * 
 * EchoGaugeT: values: echo : double setupParams: targetIP: type: String
 * default: "localhost" beaconPeriod: type: long default: 30000 javaClass: type:
 * String default: "org.sa.rainbow.translator.gauges.EchoGauge" configParams:
 * targetGaugeName: type: String default: ~ targetGaugeType: type: String
 * default: ~ targetGaugeValue: type: String default: ~ comment:
 * "EchoGaugeT echos a value reported by another gauge"
 * 
 * 
 * 
 * gauge-instances:
 * 
 * EG1: type: EchoGaugeT model: "ZNewsSys:Acme" mappings: "echo": foobar
 * setupValues: targetIP: "${rainbow.master.location.host}" configValues:
 * targetGaugeName: EERTG1 targetGaugeType: ResponseTimeGaugeT targetGaugeValue:
 * end2endRespTime
 * 
 * 
 * c1.mightbemalicious, malicious, false c2.mightbemalicious, malicious, true
 * 
 * @author schmerl
 * 
 */
public class EchoGauge extends AbstractGauge implements IGaugeConsumer {

    public static final String NAME = "G - Maliciousness";

    private static final String[] valueNames = { "maliciousness(*)" };
    public static final int AVG_SAMPLE_WINDOW = 5;

    /** The gauge that we are listening to **/
    private GaugeInstanceDescription m_listeningTo;
    private GaugeConsumerEventHandler m_eventHandler;
    private boolean m_configured = false;

    private Map<String, Queue<Double>> m_historyMap = null;
    private Map<String, Double> m_cumulationMap = null;

    private Pattern m_valuePattern;

    public EchoGauge(String id, long beaconPeriod, TypeNamePair gaugeDesc,
            TypeNamePair modelDesc, List<AttributeValueTriple> setupParams,
            List<ValuePropertyMappingPair> mappings) {
        super(NAME, id, beaconPeriod, gaugeDesc, modelDesc, setupParams,
                mappings);
        m_historyMap = new HashMap<String, Queue<Double>>();
        m_cumulationMap = new HashMap<String, Double>();
        m_eventHandler = new GaugeConsumerEventHandler(this);
        m_valuePattern = Pattern.compile("maliciousness(.*)");
        Rainbow.eventService().listen(IEventService.TOPIC_GAUGE_BUS,
                m_eventHandler);

    }

    @Override
    public boolean configureGauge(List<AttributeValueTriple> configParams) {
        if (configParams.size() == 0)
            return false;
        boolean c = super.configureGauge(configParams);
        // There is a potential race condition here where
        // RainbowDelegate.startProbes calls reconfigure gauges before the
        // configuration message is put on the bus
        // If this happens, configParams will be empty
        if (c && configParams.size() > 0) {
            m_configured = true;
        }
        if (m_configured) {
            // The gauge being listened to might already have been configured,
            // in which case m_listeningTo will be null
            // Therefore, we can construct the description out of the config
            // params and use that.
            m_listeningTo = new GaugeInstanceDescription(
                    (String) m_configParams.get("targetGaugeType")
                    .secondValue(), (String) m_configParams.get(
                            "targetGaugeName").secondValue(), "", "");
        }
        return m_configured;
    }

    @Override
    public void onReportValue(String id, TypeNamePair gaugeDesc,
            TypeNamePair modelDesc, AttributeValueTriple value) {
        if (m_listeningTo != null && m_configured) {
            if (gaugeDesc.name().equals(m_listeningTo.gaugeName())
                    && gaugeDesc.type().equals(m_listeningTo.gaugeType())
                    && value.secondValue().equals(
                            m_configParams.get("targetGaugeValue")
                            .secondValue())) {
                String property = m_modelDesc.name() + Util.DOT
                        + m_mappings.get(valueNames[0]);
                eventHandler().reportValue(
                        new AttributeValueTriple(property, valueNames[0], value
                                .value()));
            }
        }
    }

    @Override
    public void onReportMultipleValues(String id, TypeNamePair gaugeDesc,
            TypeNamePair modelDesc, List<AttributeValueTriple> values) {
        if (m_listeningTo != null && m_configured) {
            if (gaugeDesc.name().equals(m_listeningTo.gaugeName())
                    && gaugeDesc.type().equals(m_listeningTo.gaugeType())) {
                for (AttributeValueTriple val : values) {
                    Object vSV = val.firstValue().secondValue();
                    Object cSV = m_configParams.get("targetGaugeValue")
                            .secondValue();
                    if (vSV.equals(cSV)) {

                        String listeningTarget = val.firstValue().firstValue();
                        Double maliciousness = 0.0;
                        if (val.secondValue() instanceof String) {
                            maliciousness = Double.valueOf(val.secondValue()
                                    .toString());
                        }
                        else {
                            maliciousness = (Double) val.secondValue();
                        }
                        String component = listeningTarget.split("\\.")[0];
//						if (!m_historyMap.containsKey(component)) {
//							m_historyMap.put(component,
//									new LinkedList<Double>());
//							m_cumulationMap.put(component, 0.0);
//						}
//						Queue<Double> history = m_historyMap.get(component);
//						double cumulation = m_cumulationMap.get(component);
//						cumulation += (Double) maliciousness;
//						history.offer((Double) maliciousness);
//						if (history.size() > AVG_SAMPLE_WINDOW) {
//							cumulation -= history.poll();
//						}
//						m_cumulationMap.put(component, cumulation);
//						maliciousness = cumulation / history.size();
                        for (String valueName : valueNames) {
                            valueName = valueName.replace("*", component);
                            if (m_mappings.containsKey(valueName)) {
                                String pMaliciousness = m_modelDesc.name()
                                        + Util.DOT + m_mappings.get(valueName);
                                m_logger.info("Echo gauge found value: "
                                        + val.firstValue().firstValue()
                                        + ", "
                                        + val.firstValue().secondValue()
                                        .toString() + ","
                                        + vSV.toString());
                                eventHandler().reportValue(
                                        new AttributeValueTriple(
                                                pMaliciousness, valueName,
                                                maliciousness));
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onReportCreated(String id, TypeNamePair gaugeDesc,
            TypeNamePair modelDesc) {
        if (m_configured) {
            GaugeInstanceDescription instSpec = Rainbow.instance().gaugeDesc().instSpec
                    .get(gaugeDesc.name());
            if (gaugeDesc.name().equals(
                    m_configParams.get("targetGaugeName").secondValue())
                    && gaugeDesc.type()
                    .equals(m_configParams.get("targetGaugeType")
                            .secondValue())) {
                m_listeningTo = instSpec;
            }
        }
    }

    @Override
    public void onReportDeleted(String id, TypeNamePair gaugeDesc,
            TypeNamePair modelDesc) {
        if (m_configured) {
            if (gaugeDesc.name().equals(
                    m_configParams.get("targetGaugeName").secondValue())
                    && gaugeDesc.type()
                    .equals(m_configParams.get("targetGaugeType")
                            .secondValue())) {
                m_listeningTo = null;
            }
        }
    }

    @Override
    public void onReportConfigured(String id, TypeNamePair gaugeDesc,
            TypeNamePair modelDesc, List<AttributeValueTriple> configParams) {
    }

    @Override
    public void gaugeBeacon(String id, long period) {
        if (m_listeningTo != null && m_listeningTo.gaugeName().equals("id")) {
            System.out.println("Received a beacon from " + id);
        }
    }

    @Override
    protected void initProperty(String name, Object value) {

    }

}
