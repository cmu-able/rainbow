package org.sa.rainbow.gauges.ports.eseb;

import java.io.IOException;
import java.util.List;

import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.util.TypedAttributeWithValue;
import org.sa.rainbow.gauges.IGaugeIdentifier;
import org.sa.rainbow.management.ports.eseb.ESEBRPCConnector;

import edu.cmu.cs.able.eseb.participant.ParticipantException;
import edu.cmu.cs.able.eseb.rpc.JavaRpcFactory;

public class ESEBGaugeConfigurationInterfaceRequirer implements IESEBGaugeConfigurationRemoteInterface {

    private ESEBRPCConnector                       m_connectionRole;
    private IESEBGaugeConfigurationRemoteInterface m_stub;

    public ESEBGaugeConfigurationInterfaceRequirer (IGaugeIdentifier gauge) throws IOException, ParticipantException {
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


        m_stub = JavaRpcFactory.create_remote_stub (IESEBGaugeConfigurationRemoteInterface.class,
                m_connectionRole.getRPCEnvironment (), m_connectionRole.getParticipantIdentifier ().id (), 1500,
                gauge.id_long ());

    }

    @Override
    public boolean configureGauge (List<TypedAttributeWithValue> configParams) {
        return m_stub.configureGauge (configParams);
    }

    @Override
    public boolean reconfigureGauge () {
        return m_stub.reconfigureGauge ();
    }


}
