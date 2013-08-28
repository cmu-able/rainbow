package org.sa.rainbow.gauges.ports.eseb;

import java.io.IOException;
import java.util.Collection;

import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.gauges.IGaugeIdentifier;
import org.sa.rainbow.gauges.IGaugeState;
import org.sa.rainbow.management.ports.eseb.ESEBRPCConnector;
import org.sa.rainbow.models.commands.IRainbowModelCommandRepresentation;

import edu.cmu.cs.able.eseb.participant.ParticipantException;
import edu.cmu.cs.able.eseb.rpc.JavaRpcFactory;

public class ESEBGaugeQueryInterfaceRequirer implements IESEBGaugeQueryRemoteInterface {

    private ESEBRPCConnector               m_connectionRole;
    private IESEBGaugeQueryRemoteInterface m_stub;

    public ESEBGaugeQueryInterfaceRequirer (IGaugeIdentifier gauge) throws IOException, ParticipantException {
        String port = Rainbow.properties ().getProperty (RainbowConstants.PROPKEY_MASTER_CONNECTION_PORT);
        if (port == null) {
            port = Rainbow.properties ().getProperty (RainbowConstants.PROPKEY_MASTER_LOCATION_PORT);
            if (port == null) {
                port = "1234";
            }
        }
        short p = Short.valueOf (port);

        // All these messages go on the HEALTH channel
        m_connectionRole = new ESEBRPCConnector (Rainbow.properties ().getProperty (
                RainbowConstants.PROPKEY_MASTER_LOCATION), p);

        m_stub = JavaRpcFactory.create_remote_stub (IESEBGaugeQueryRemoteInterface.class,
                m_connectionRole.getRPCEnvironment (), m_connectionRole.getParticipantIdentifier ().id (), 1500,
                gauge.id_long ());

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
