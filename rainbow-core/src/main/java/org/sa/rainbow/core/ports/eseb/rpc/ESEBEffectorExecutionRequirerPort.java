package org.sa.rainbow.core.ports.eseb.rpc;

import java.io.IOException;
import java.util.List;

import org.sa.rainbow.core.ports.eseb.ESEBProvider;
import org.sa.rainbow.core.ports.eseb.ESEBRPCConnector;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;

import edu.cmu.cs.able.eseb.participant.ParticipantException;

public class ESEBEffectorExecutionRequirerPort implements IESEBEffectorExecutionRemoteInterface {

    private ESEBRPCConnector m_connectionRole;
    private IESEBEffectorExecutionRemoteInterface m_stub;

    public ESEBEffectorExecutionRequirerPort (IEffectorIdentifier effector) throws IOException, ParticipantException {
        m_connectionRole = new ESEBRPCConnector (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (),
                effector.id ());
        m_stub = m_connectionRole.createRemoteStub (IESEBEffectorExecutionRemoteInterface.class,
                effector.id () + IESEBEffectorExecutionRemoteInterface.class.getSimpleName ());
    }

    @Override
    public Outcome execute (List<String> args) {
        return m_stub.execute (args);
    }

}
