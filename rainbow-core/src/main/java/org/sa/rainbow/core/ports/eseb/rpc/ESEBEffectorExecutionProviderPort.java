package org.sa.rainbow.core.ports.eseb.rpc;

import java.io.IOException;
import java.util.List;

import org.sa.rainbow.core.ports.eseb.ESEBProvider;
import org.sa.rainbow.translator.effectors.IEffector;

import edu.cmu.cs.able.eseb.participant.ParticipantException;

public class ESEBEffectorExecutionProviderPort extends AbstractESEBDisposableRPCPort implements
        IESEBEffectorExecutionRemoteInterface {

    private IEffector        m_effector;

    public ESEBEffectorExecutionProviderPort (IEffector effector) throws IOException, ParticipantException {
        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (),
                effector.id ());
        m_effector = effector;
        try {
            getConnectionRole().createRegistryWrapper (IESEBEffectorExecutionRemoteInterface.class, this, effector.id ()
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
