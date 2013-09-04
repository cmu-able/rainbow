package org.sa.rainbow.core.ports.eseb;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.sa.rainbow.core.RainbowDelegate;
import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;

import edu.cmu.cs.able.eseb.participant.ParticipantException;
import edu.cmu.cs.able.eseb.rpc.JavaRpcFactory;

public class ESEBDelegateConfigurationPort implements IESEBDelegateConfigurationPort {

    private RainbowDelegate m_delegate;
    private ESEBRPCConnector m_connector;

    public ESEBDelegateConfigurationPort (RainbowDelegate delegate) throws IOException, ParticipantException {
        m_delegate = delegate;

        // Port runs on the master
        m_connector = new ESEBRPCConnector (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort ());

        /*m_service_closeable = */JavaRpcFactory.create_registry_wrapper (IESEBDelegateConfigurationPort.class, this,
                m_connector.getRPCEnvironment (),
                m_delegate.getId () + IESEBDelegateConfigurationPort.class.getSimpleName ());
    }

    @Override
    public void sendConfigurationInformation (Properties props,
            List<ProbeAttributes> probes,
            List<EffectorAttributes> effectors) {
        m_delegate.receiveConfigurationInformation (props, probes, effectors);
    }

}
