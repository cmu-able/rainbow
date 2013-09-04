package org.sa.rainbow.translator.probes.ports;

import java.util.Map;

public interface IProbeConfigurationPort {
    public abstract void configure (Map<String, Object> configParams);
}
