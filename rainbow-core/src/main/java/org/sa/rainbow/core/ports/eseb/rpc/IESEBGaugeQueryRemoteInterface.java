package org.sa.rainbow.core.ports.eseb.rpc;

import java.util.Collection;

import org.sa.rainbow.core.gauges.IGaugeState;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IGaugeQueryPort;

import edu.cmu.cs.able.eseb.rpc.ParametersTypeMapping;
import edu.cmu.cs.able.eseb.rpc.ReturnTypeMapping;

public interface IESEBGaugeQueryRemoteInterface extends IGaugeQueryPort {

    public static final int ID = 1;

    @Override
    @ReturnTypeMapping ("set<operation_representation>")
    public Collection<IRainbowOperation> queryAllCommands ();

    @Override
    @ParametersTypeMapping ({ "string" })
    @ReturnTypeMapping ("operation_representation")
    public IRainbowOperation queryCommand (String commandName);

    @Override
    @ReturnTypeMapping ("gauge_state")
    public IGaugeState queryGaugeState ();

}
