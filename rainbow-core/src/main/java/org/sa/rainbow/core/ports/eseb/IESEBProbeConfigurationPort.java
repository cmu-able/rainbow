package org.sa.rainbow.core.ports.eseb;

import java.util.Map;

import org.sa.rainbow.core.ports.IProbeConfigurationPort;

import edu.cmu.cs.able.eseb.rpc.ParametersTypeMapping;

public interface IESEBProbeConfigurationPort extends IProbeConfigurationPort {
    @Override
    @ParametersTypeMapping({"map<string,any>"})
    public void configure (Map<String, Object> configParams);
}
