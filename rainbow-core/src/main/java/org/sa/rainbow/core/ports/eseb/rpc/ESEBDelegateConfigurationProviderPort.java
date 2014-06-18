package org.sa.rainbow.core.ports.eseb.rpc;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;
import org.sa.rainbow.core.ports.eseb.ESEBProvider;

import edu.cmu.cs.able.eseb.participant.ParticipantException;

public class ESEBDelegateConfigurationProviderPort extends AbstractESEBDisposableRPCPort implements
        IESEBDelegateConfigurationPort {

    private RainbowDelegate m_delegate;

    public ESEBDelegateConfigurationProviderPort (RainbowDelegate delegate) throws IOException, ParticipantException {
        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (), delegate.getId ());
        m_delegate = delegate;

        // Port runs on the master

        getConnectionRole().createRegistryWrapper (IESEBDelegateConfigurationPort.class, this,
                m_delegate.getId () + IESEBDelegateConfigurationPort.class.getSimpleName ());

    }

    @Override
    public void sendConfigurationInformation (Properties props,
            List<ProbeAttributes> probes,
            List<EffectorAttributes> effectors, List<GaugeInstanceDescription> gauges) {
        m_delegate.receiveConfigurationInformation (props, probes, effectors, gauges);
    }

}
