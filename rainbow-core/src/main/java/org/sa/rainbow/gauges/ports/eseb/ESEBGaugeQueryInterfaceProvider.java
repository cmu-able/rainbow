package org.sa.rainbow.gauges.ports.eseb;

import java.io.IOException;
import java.util.Collection;

import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.gauges.IGauge;
import org.sa.rainbow.gauges.IGaugeState;
import org.sa.rainbow.management.ports.eseb.ESEBRPCConnector;
import org.sa.rainbow.models.commands.IRainbowModelCommandRepresentation;

import edu.cmu.cs.able.eseb.participant.ParticipantException;
import edu.cmu.cs.able.eseb.rpc.JavaRpcFactory;

public class ESEBGaugeQueryInterfaceProvider implements IESEBGaugeQueryRemoteInterface {

    private ESEBRPCConnector m_connector;
    private IGauge           m_gauge;

    public ESEBGaugeQueryInterfaceProvider (IGauge gauge) throws IOException, ParticipantException {
        String port = Rainbow.properties ().getProperty (RainbowConstants.PROPKEY_MASTER_CONNECTION_PORT);
        if (port == null) {
            port = Rainbow.properties ().getProperty (RainbowConstants.PROPKEY_MASTER_LOCATION_PORT);
            if (port == null) {
                port = "1234";
            }
        }
        short p = Short.valueOf (port);
        m_connector = new ESEBRPCConnector (p);
        m_gauge = gauge;
        JavaRpcFactory.create_registry_wrapper (IESEBGaugeQueryRemoteInterface.class, this,
                m_connector.getRPCEnvironment (), gauge.id_long ());
    }

    @Override
    public Collection<IRainbowModelCommandRepresentation> queryAllCommands () {
        return m_gauge.queryAllCommands ();

    }

    @Override
    public IRainbowModelCommandRepresentation queryCommand (String commandName) {
        return m_gauge.queryCommand (commandName);
    }

    @Override
    public IGaugeState queryGaugeState () {
        return m_gauge.queryGaugeState ();
    }

}
