package org.sa.rainbow.testing.prepare.stub.ports;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.gauges.IGaugeIdentifier;
import org.sa.rainbow.core.ports.IGaugeLifecycleBusPort;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import java.util.List;

/**
 * This is the stubbed IGaugeLifecycleBusPort
 */
public class LoggerGaugeLifecycleBusPort implements IGaugeLifecycleBusPort {
    private static final Logger LOGGER = Logger.getLogger(LoggerGaugeLifecycleBusPort.class);

    private static String displayGauge(IGaugeIdentifier gauge) {
        return gauge.gaugeDesc().getName() + " (" + gauge.gaugeDesc().getType() + ")";
    }

    /**
     * Reports that a gauge has been created, giving it's id, type, and associated model
     *
     * @param gauge
     */
    @Override
    public void reportCreated(IGaugeIdentifier gauge) {
        LOGGER.info("Gauge created: " + displayGauge(gauge));
    }

    /**
     * Reports that a gauge has been deleted, giving it's id, type, and associated model
     *
     * @param gauge
     */
    @Override
    public void reportDeleted(IGaugeIdentifier gauge) {
        LOGGER.info("Gauge deleted: " + displayGauge(gauge));
    }

    /**
     * Reports that a gauge has been configured, along with the configuration parameters
     *
     * @param gauge        The gauge configured
     * @param configParams
     */
    @Override
    public void reportConfigured(IGaugeIdentifier gauge, List<TypedAttributeWithValue> configParams) {
        LOGGER.info("Gauge configured: " + displayGauge(gauge));
    }

    /**
     * The method through which a gauge sends its heartbeat, or beacon, so that listeners can be sure that it is still
     * alive
     *
     * @param gauge The gauge sending the beacon
     */
    @Override
    public void sendBeacon(IGaugeIdentifier gauge) {
        LOGGER.info("Beacon sent: " + displayGauge(gauge));
    }

    /**
     * Should be called when this port is no longer required. Implementors should dispose of all resources.
     */
    @Override
    public void dispose() {

    }
}
