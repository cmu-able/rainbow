package org.sa.rainbow.core.ports;

import java.util.Map;

public interface IProbeConfigurationPort {
    public abstract void configure (Map<String, Object> configParams);
}
