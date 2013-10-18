package org.sa.rainbow.core.ports.eseb.rpc;

import java.io.IOException;
import java.util.List;

import org.sa.rainbow.core.ports.eseb.ESEBProvider;
import org.sa.rainbow.core.ports.eseb.ESEBRPCConnector;
import org.sa.rainbow.translator.effectors.IEffector;

import edu.cmu.cs.able.eseb.participant.ParticipantException;

public class ESEBEffectorExecutionProviderPort implements IESEBEffectorExecutionRemoteInterface {

    private ESEBRPCConnector m_connector;
    private IEffector        m_effector;

    public ESEBEffectorExecutionProviderPort (IEffector effector) throws IOException, ParticipantException {
        m_connector = new ESEBRPCConnector (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (),
                effector.id ());
        m_effector = effector;
        try {
            m_connector.createRegistryWrapper (IESEBEffectorExecutionRemoteInterface.class, this, effector.id ()
                    + IESEBEffectorExecutionRemoteInterface.class.getSimpleName ());
        }
        catch (Exception e) {
        }

    }

    @Override
    public Outcome execute (List<String> args) {
        return m_effector.execute (args);
    }

}
