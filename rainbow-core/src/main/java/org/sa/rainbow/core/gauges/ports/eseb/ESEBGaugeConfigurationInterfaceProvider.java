package org.sa.rainbow.core.gauges.ports.eseb;

import java.io.IOException;
import java.util.List;

import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.gauges.IGauge;
import org.sa.rainbow.core.management.ports.eseb.ESEBRPCConnector;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import edu.cmu.cs.able.eseb.participant.ParticipantException;
import edu.cmu.cs.able.eseb.rpc.JavaRpcFactory;

public class ESEBGaugeConfigurationInterfaceProvider implements IESEBGaugeConfigurationRemoteInterface {

    private IGauge m_gauge;
    private ESEBRPCConnector      m_connector;

    public ESEBGaugeConfigurationInterfaceProvider (IGauge gauge) throws IOException, ParticipantException {
        String port = Rainbow.getProperty (RainbowConstants.PROPKEY_MASTER_CONNECTION_PORT);
        if (port == null) {
            port = Rainbow.getProperty (RainbowConstants.PROPKEY_MASTER_LOCATION_PORT);
            if (port == null) {
                port = "1234";
            }
        }
        short p = Short.valueOf (port);
        m_connector = new ESEBRPCConnector (p);


        m_gauge = gauge;
        /*m_service_closeable = */JavaRpcFactory.create_registry_wrapper (
                IESEBGaugeConfigurationRemoteInterface.class, this, m_connector.getRPCEnvironment (), gauge.id ()
                + IESEBGaugeConfigurationRemoteInterface.class.getSimpleName ());
    }

    @Override
    public boolean configureGauge (List<TypedAttributeWithValue> configParams) {
        return m_gauge.configureGauge (configParams);
    }

    @Override
    public boolean reconfigureGauge () {
        return m_gauge.reconfigureGauge ();
    }


}
