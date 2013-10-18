package org.sa.rainbow.core.ports.eseb.rpc;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.gauges.IGauge;
import org.sa.rainbow.core.ports.eseb.ESEBProvider;
import org.sa.rainbow.core.ports.eseb.ESEBRPCConnector;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import edu.cmu.cs.able.eseb.participant.ParticipantException;

public class ESEBGaugeConfigurationProviderPort implements IESEBGaugeConfigurationRemoteInterface {
    static Logger            LOGGER = Logger.getLogger (ESEBGaugeConfigurationProviderPort.class);
    private IGauge m_gauge;
    private ESEBRPCConnector      m_connector;

    public ESEBGaugeConfigurationProviderPort (IGauge gauge) throws IOException, ParticipantException {

        m_connector = new ESEBRPCConnector (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (),
                gauge.id ());
        m_gauge = gauge;
        m_connector.createRegistryWrapper (IESEBGaugeConfigurationRemoteInterface.class, this, gauge.id ()
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
