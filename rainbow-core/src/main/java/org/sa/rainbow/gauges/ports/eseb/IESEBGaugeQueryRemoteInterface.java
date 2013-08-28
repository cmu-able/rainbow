package org.sa.rainbow.gauges.ports.eseb;

import java.util.Collection;

import org.sa.rainbow.gauges.IGaugeQueryInterface;
import org.sa.rainbow.gauges.IGaugeState;
import org.sa.rainbow.models.commands.IRainbowModelCommandRepresentation;

import edu.cmu.cs.able.eseb.rpc.ParametersTypeMapping;
import edu.cmu.cs.able.eseb.rpc.ReturnTypeMapping;

public interface IESEBGaugeQueryRemoteInterface extends IGaugeQueryInterface {

    @Override
    @ReturnTypeMapping ("list<command_representation>")
    public Collection<IRainbowModelCommandRepresentation> queryAllCommands ();

    @Override
    @ParametersTypeMapping ({ "string" })
    @ReturnTypeMapping ("command_representation")
    public IRainbowModelCommandRepresentation queryCommand (String commandName);

    @Override
    @ReturnTypeMapping ("gauge_state")
    public IGaugeState queryGaugeState ();

}
