package org.sa.rainbow.core.ports;

import java.util.List;

import org.sa.rainbow.core.gauges.IGaugeIdentifier;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

/**
 * The API through which gauges report their lifeculce (from creation, configuration, to deletion)
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public interface IGaugeLifecycleBusPort extends IDisposablePort {

    /**
     * Reports that a gauge has been created, giving it's id, type, and associated model
     * 
     * @param gauge
     */
    public void reportCreated (IGaugeIdentifier gauge);

    /**
     * Reports that a gauge has been deleted, giving it's id, type, and associated model
     * 
     * @param gauge
     */
    public void reportDeleted (IGaugeIdentifier gauge);

    /**
     * Reports that a gauge has been configured, along with the configuration parameters
     * 
     * @param gauge
     *            The gauge configured
     * @param configParams
     *            The parameters with which it was configured
     */
    public void reportConfigured (IGaugeIdentifier gauge, List<TypedAttributeWithValue> configParams);

    /**
     * The method through which a gauge sends its heartbeat, or beacon, so that listeners can be sure that it is still
     * alive
     * 
     * @param gauge
     *            The gauge sending the beacon
     */
    public void sendBeacon (IGaugeIdentifier gauge);

}
