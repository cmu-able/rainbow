package org.sa.rainbow.core.management.ports.eseb;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.sa.rainbow.core.models.EffectorDescription.EffectorAttributes;
import org.sa.rainbow.core.models.ProbeDescription.ProbeAttributes;

import edu.cmu.cs.able.eseb.participant.ParticipantException;
import edu.cmu.cs.able.eseb.rpc.JavaRpcFactory;

public class ESEBDelegateConfigurationPortClient implements IESEBDelegateConfigurationPort {

    private ESEBRPCConnector               m_connectionRole;
    private IESEBDelegateConfigurationPort m_stub;

    public ESEBDelegateConfigurationPortClient (String delegateID) throws IOException, ParticipantException {
        // All these messages go on the HEALTH channel. Port is on the master
        m_connectionRole = new ESEBRPCConnector (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort ());

        m_stub = JavaRpcFactory.create_remote_stub (IESEBDelegateConfigurationPort.class,
                m_connectionRole.getRPCEnvironment (), m_connectionRole.getParticipantIdentifier ().id (), 1500,
                delegateID + IESEBDelegateConfigurationPort.class.getSimpleName ());
    }

    @Override
    public void sendConfigurationInformation (Properties props,
            List<ProbeAttributes> probes,
            List<EffectorAttributes> effectors) {
        m_stub.sendConfigurationInformation (props, probes, effectors);
    }

}
