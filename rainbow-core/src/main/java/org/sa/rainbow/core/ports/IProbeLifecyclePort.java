package org.sa.rainbow.core.ports;

import java.util.Map;

/**
 * The API through which probes report their lifeculce (from creation, configuration, to deletion)
 * 
 * @author Bradley Schmerl: schmerl
 * 
 */
public interface IProbeLifecyclePort extends IDisposablePort {

    public String PROBE_CREATED      = "probeCreated";
    public String PROBE_ID           = "probeId";
    public String PROBE_LOCATION     = "probeLocation";
    public String PROBE_NAME         = "probeName";
    public String PROBE_DELETED      = "probeDeleted";
    public String PROBE_CONFIGURED   = "probeConfigured";
    public String CONFIG_PARAM_NAME  = "probeConfigParamName";
    public String CONFIG_PARAM_VALUE = "probeConfigParamValue";
    public String PROBE_DEACTIVATED  = "probeDeactivated";
    public String PROBE_ACTIVATED    = "probeActivated";

    /**
     * Reports that a probes has been created, giving it's id, type, and associated model
     * 
     */
    public void reportCreated ();

    /**
     * Reports that a probes has been deleted, giving it's id, type, and associated model
     * 
     */
    public void reportDeleted ();

    /**
     * Reports that a gaugprobese has been configured, along with the configuration parameters
     * 
     * @param configParams
     *            The parameters with which it was configured
     */
    public void reportConfigured (Map<String, Object> configParams);

    public void reportDeactivated ();

    public void reportActivated ();


}
