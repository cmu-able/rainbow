package org.sa.rainbow.core.ports;

import org.sa.rainbow.core.Rainbow.ExitState;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;

public interface IMasterCommandPort {
    public void startProbes ();

    public void killProbes ();

    public void enableAdaptation (boolean enabled);

    public Outcome testEffector (String target, String effName, String[] args);

    public void sleep ();

    public void terminate (ExitState exitState);

    public void restartDelegates ();

    public void sleepDelegates ();

    public void destroyDelegates ();

    public void killDelegate (String ipOfDelegate);

}
