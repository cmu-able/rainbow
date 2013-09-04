package org.sa.rainbow.core.gauges.ports.eseb;

import java.util.Collection;

import org.sa.rainbow.core.gauges.IGaugeQueryInterface;
import org.sa.rainbow.core.gauges.IGaugeState;
import org.sa.rainbow.core.models.commands.IRainbowModelCommandRepresentation;

import edu.cmu.cs.able.eseb.rpc.ParametersTypeMapping;
import edu.cmu.cs.able.eseb.rpc.ReturnTypeMapping;

public interface IESEBGaugeQueryRemoteInterface extends IGaugeQueryInterface {

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
