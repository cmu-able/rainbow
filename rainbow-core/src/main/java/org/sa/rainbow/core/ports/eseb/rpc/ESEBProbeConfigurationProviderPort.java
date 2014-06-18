package org.sa.rainbow.core.ports.eseb.rpc;

import java.io.IOException;
import java.util.Map;

import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.ports.IProbeConfigurationPort;
import org.sa.rainbow.core.ports.eseb.ESEBProvider;

import edu.cmu.cs.able.eseb.participant.ParticipantException;

public class ESEBProbeConfigurationProviderPort extends AbstractESEBDisposableRPCPort implements
        IESEBProbeConfigurationPort {

    private IProbeConfigurationPort m_callback;

    public ESEBProbeConfigurationProviderPort (Identifiable probe, IProbeConfigurationPort callback)
            throws IOException, ParticipantException {
        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (), probe.id ());
        m_callback = callback;

        getConnectionRole().createRegistryWrapper (IESEBProbeConfigurationPort.class, this, probe.id ()
                + IESEBGaugeQueryRemoteInterface.class.getSimpleName ());
    }

    @Override
    public void configure (Map<String, Object> configParams) {
        m_callback.configure (configParams);
    }



}
