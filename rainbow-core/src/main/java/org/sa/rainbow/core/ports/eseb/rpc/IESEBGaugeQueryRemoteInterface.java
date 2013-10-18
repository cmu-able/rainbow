package org.sa.rainbow.core.ports.eseb.rpc;

import java.util.Collection;

import org.sa.rainbow.core.gauges.IGaugeState;
import org.sa.rainbow.core.models.commands.IRainbowModelCommandRepresentation;
import org.sa.rainbow.core.ports.IGaugeQueryPort;

import edu.cmu.cs.able.eseb.rpc.ParametersTypeMapping;
import edu.cmu.cs.able.eseb.rpc.ReturnTypeMapping;

public interface IESEBGaugeQueryRemoteInterface extends IGaugeQueryPort {

    public static final int ID = 1;

    @Override
    @ReturnTypeMapping ("set<command_representation>")
    public Collection<IRainbowModelCommandRepresentation> queryAllCommands ();

    @Override
    @ParametersTypeMapping ({ "string" })
    @ReturnTypeMapping ("command_representation")
    public IRainbowModelCommandRepresentation queryCommand (String commandName);

    @Override
    @ReturnTypeMapping ("gauge_state")
    public IGaugeState queryGaugeState ();

}
