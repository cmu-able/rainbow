package org.sa.rainbow.core.ports.eseb.rpc;

import java.io.IOException;
import java.util.Collection;

import org.sa.rainbow.core.gauges.IGaugeIdentifier;
import org.sa.rainbow.core.gauges.IGaugeState;
import org.sa.rainbow.core.models.commands.IRainbowModelCommandRepresentation;
import org.sa.rainbow.core.ports.eseb.ESEBProvider;
import org.sa.rainbow.core.ports.eseb.ESEBRPCConnector;

import edu.cmu.cs.able.eseb.participant.ParticipantException;

public class ESEBGaugeQueryRequirerPort implements IESEBGaugeQueryRemoteInterface {

    private ESEBRPCConnector               m_connectionRole;
    private IESEBGaugeQueryRemoteInterface m_stub;

    public ESEBGaugeQueryRequirerPort (IGaugeIdentifier gauge) throws IOException, ParticipantException {
        m_connectionRole = new ESEBRPCConnector (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (),
                gauge.id ());

        m_stub = m_connectionRole.createRemoteStub (IESEBGaugeQueryRemoteInterface.class,
                gauge.id () + IESEBGaugeQueryRemoteInterface.class.getSimpleName ());

    }

    @Override
    public Collection<IRainbowModelCommandRepresentation> queryAllCommands () {
        return m_stub.queryAllCommands ();
    }

    @Override
    public IRainbowModelCommandRepresentation queryCommand (String commandName) {
        return m_stub.queryCommand (commandName);
    }

    @Override
    public IGaugeState queryGaugeState () {
        return m_stub.queryGaugeState ();
    }

}
