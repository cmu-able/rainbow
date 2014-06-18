package org.sa.rainbow.core.ports.eseb.rpc;

import java.io.IOException;
import java.util.List;

import org.sa.rainbow.core.ports.eseb.ESEBProvider;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;

import edu.cmu.cs.able.eseb.participant.ParticipantException;

public class ESEBEffectorExecutionRequirerPort extends AbstractESEBDisposableRPCPort implements
IESEBEffectorExecutionRemoteInterface {

    private IESEBEffectorExecutionRemoteInterface m_stub;

    public ESEBEffectorExecutionRequirerPort (IEffectorIdentifier effector) throws IOException, ParticipantException {
        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (),
                effector.id ());
        m_stub = getConnectionRole().createRemoteStub (IESEBEffectorExecutionRemoteInterface.class,
                effector.id () + IESEBEffectorExecutionRemoteInterface.class.getSimpleName ());
    }

    @Override
    public Outcome execute (List<String> args) {
        return m_stub.execute (args);
    }

}
