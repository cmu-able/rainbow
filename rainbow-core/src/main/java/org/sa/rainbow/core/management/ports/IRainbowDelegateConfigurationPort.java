package org.sa.rainbow.core.management.ports;

import java.util.List;
import java.util.Properties;

import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;

public interface IRainbowDelegateConfigurationPort {
    public abstract void sendConfigurationInformation (Properties props,
            List<ProbeAttributes> probes,
            List<EffectorAttributes> effectors);
}
