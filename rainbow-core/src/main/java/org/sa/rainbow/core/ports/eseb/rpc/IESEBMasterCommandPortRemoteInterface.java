package org.sa.rainbow.core.ports.eseb.rpc;

import edu.cmu.cs.able.eseb.rpc.ParametersTypeMapping;
import edu.cmu.cs.able.eseb.rpc.ReturnTypeMapping;
import org.sa.rainbow.core.Rainbow.ExitState;
import org.sa.rainbow.core.ports.IMasterCommandPort;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;

import java.util.List;

public interface IESEBMasterCommandPortRemoteInterface extends IMasterCommandPort {

    @Override
    void startProbes ();

    @Override
    void killProbes ();

    @Override
    @ParametersTypeMapping ({ "bool" })
    void enableAdaptation (boolean enabled);

    @Override
    @ParametersTypeMapping ({ "string", "string", "list<string>" })
    @ReturnTypeMapping ("outcome")
    Outcome testEffector (String target, String effName, List<String> args);

    @Override
    void sleep ();

    @Override
    @ParametersTypeMapping ({ "exit_state" })
    void terminate (ExitState exitState);

    @Override
    void restartDelegates ();

    @Override
    void sleepDelegates ();

    @Override
    void destroyDelegates ();

    @Override
    @ParametersTypeMapping ({ "string" })
    void killDelegate (String ipOfDelegate);

    @Override
    @ReturnTypeMapping ("list<string>")
    List<String> getExpectedDelegateLocations ();

}
