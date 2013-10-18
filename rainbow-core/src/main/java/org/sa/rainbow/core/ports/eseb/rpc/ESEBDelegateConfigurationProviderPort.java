package org.sa.rainbow.core.ports.eseb.rpc;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.gauges.GaugeInstanceDescription;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;
import org.sa.rainbow.core.ports.eseb.ESEBProvider;
import org.sa.rainbow.core.ports.eseb.ESEBRPCConnector;

import edu.cmu.cs.able.eseb.participant.ParticipantException;

public class ESEBDelegateConfigurationProviderPort implements IESEBDelegateConfigurationPort {

    private RainbowDelegate m_delegate;
    private ESEBRPCConnector m_connector;

    public ESEBDelegateConfigurationProviderPort (RainbowDelegate delegate) throws IOException, ParticipantException {
        m_delegate = delegate;

        // Port runs on the master
        m_connector = new ESEBRPCConnector (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (),
                delegate.getId ());

        m_connector.createRegistryWrapper (IESEBDelegateConfigurationPort.class, this,
                m_delegate.getId () + IESEBDelegateConfigurationPort.class.getSimpleName ());

    }

    @Override
    public void sendConfigurationInformation (Properties props,
            List<ProbeAttributes> probes,
            List<EffectorAttributes> effectors, List<GaugeInstanceDescription> gauges) {
        m_delegate.receiveConfigurationInformation (props, probes, effectors, gauges);
    }

}
