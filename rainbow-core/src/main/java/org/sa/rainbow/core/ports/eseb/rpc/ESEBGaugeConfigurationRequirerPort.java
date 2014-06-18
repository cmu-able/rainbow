package org.sa.rainbow.core.ports.eseb.rpc;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.gauges.IGaugeIdentifier;
import org.sa.rainbow.core.ports.eseb.ESEBProvider;
import org.sa.rainbow.core.util.TypedAttributeWithValue;

import edu.cmu.cs.able.eseb.participant.ParticipantException;

public class ESEBGaugeConfigurationRequirerPort extends AbstractESEBDisposableRPCPort implements
        IESEBGaugeConfigurationRemoteInterface {
    static Logger                                  LOGGER = Logger.getLogger (ESEBGaugeConfigurationRequirerPort.class);
    private IESEBGaugeConfigurationRemoteInterface m_stub;

    public ESEBGaugeConfigurationRequirerPort (IGaugeIdentifier gauge) throws IOException, ParticipantException {

        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (),
                gauge.id ());
        m_stub = getConnectionRole().createRemoteStub (IESEBGaugeConfigurationRemoteInterface.class,
                gauge.id () + IESEBGaugeConfigurationRemoteInterface.class.getSimpleName ());

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
