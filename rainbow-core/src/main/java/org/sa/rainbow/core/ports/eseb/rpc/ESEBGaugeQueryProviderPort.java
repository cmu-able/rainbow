package org.sa.rainbow.core.ports.eseb.rpc;

import java.io.IOException;
import java.util.Collection;

import org.sa.rainbow.core.gauges.IGauge;
import org.sa.rainbow.core.gauges.IGaugeState;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.eseb.ESEBProvider;

import edu.cmu.cs.able.eseb.participant.ParticipantException;

public class ESEBGaugeQueryProviderPort extends AbstractESEBDisposableRPCPort implements IESEBGaugeQueryRemoteInterface {

    private IGauge           m_gauge;

    public ESEBGaugeQueryProviderPort (IGauge gauge) throws IOException, ParticipantException {

        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (), gauge.id ());
        m_gauge = gauge;
        getConnectionRole().createRegistryWrapper (IESEBGaugeQueryRemoteInterface.class, this, gauge.id ()
                + IESEBGaugeQueryRemoteInterface.class.getSimpleName ());
    }

    @Override
    public Collection<IRainbowOperation> queryAllCommands () {
        return m_gauge.queryAllCommands ();

    }

    @Override
    public IRainbowOperation queryCommand (String commandName) {
        return m_gauge.queryCommand (commandName);
    }

    @Override
    public IGaugeState queryGaugeState () {
        return m_gauge.queryGaugeState ();
    }

}
