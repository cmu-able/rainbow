package org.sa.rainbow.core.ports;

import java.util.Map;

public interface IProbeConfigurationPort extends IDisposablePort {
    public abstract void configure (Map<String, Object> configParams);
}
