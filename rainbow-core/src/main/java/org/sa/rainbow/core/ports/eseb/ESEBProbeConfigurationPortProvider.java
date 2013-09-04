package org.sa.rainbow.core.ports.eseb;

import java.io.IOException;
import java.util.Map;

import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.ports.IProbeConfigurationPort;

import edu.cmu.cs.able.eseb.participant.ParticipantException;
import edu.cmu.cs.able.eseb.rpc.JavaRpcFactory;

public class ESEBProbeConfigurationPortProvider implements IESEBProbeConfigurationPort {

    private IProbeConfigurationPort m_callback;
    private ESEBRPCConnector        m_connector;

    public ESEBProbeConfigurationPortProvider (Identifiable probe, IProbeConfigurationPort callback)
            throws IOException, ParticipantException {
        m_callback = callback;

        m_connector = new ESEBRPCConnector (ESEBProvider.getESEBClientPort ());
        JavaRpcFactory.create_registry_wrapper (IESEBProbeConfigurationPort.class, this,
                m_connector.getRPCEnvironment (), probe.id () + IESEBGaugeQueryRemoteInterface.class.getSimpleName ());
    }

    @Override
    public void configure (Map<String, Object> configParams) {
        m_callback.configure (configParams);
    }

}
