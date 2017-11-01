package org.sa.rainbow.core.ports.eseb.rpc;

import java.io.IOException;
import java.util.List;

import org.sa.rainbow.core.globals.ExitState;
import org.sa.rainbow.core.ports.IMasterCommandPort;
import org.sa.rainbow.core.ports.eseb.ESEBProvider;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;

import edu.cmu.cs.able.eseb.participant.ParticipantException;

public class ESEBMasterCommandProviderPort extends AbstractESEBDisposableRPCPort
implements IESEBMasterCommandPortRemoteInterface {

    private IMasterCommandPort m_delegate;

    public ESEBMasterCommandProviderPort (IMasterCommandPort delegate) throws IOException, ParticipantException {
        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (),
                IESEBMasterCommandPortRemoteInterface.class.getSimpleName ());
        m_delegate = delegate;
        getConnectionRole ().createRegistryWrapper (IESEBMasterCommandPortRemoteInterface.class, this,
                "gui" + IESEBMasterCommandPortRemoteInterface.class.getSimpleName ());
    }

    @Override
    public void startProbes () {
        m_delegate.startProbes ();
    }

    @Override
    public void killProbes () {
        m_delegate.killProbes ();
    }

    @Override
    public void enableAdaptation (boolean enabled) {
        m_delegate.enableAdaptation (enabled);
    }

    @Override
    public Outcome testEffector (String target, String effName, List<String> args) {
        return m_delegate.testEffector (target, effName, args);
    }

    @Override
    public void sleep () {
        m_delegate.sleep ();
    }

    @Override
    public void terminate (ExitState exitState) {
        m_delegate.terminate (exitState);
    }

    @Override
    public void restartDelegates () {
        m_delegate.restartDelegates ();
    }

    @Override
    public void sleepDelegates () {
        m_delegate.sleepDelegates ();
    }

    @Override
    public void destroyDelegates () {
        m_delegate.destroyDelegates ();
    }

    @Override
    public void killDelegate (String ipOfDelegate) {
        m_delegate.killDelegate (ipOfDelegate);
    }

    @Override
    public List<String> getExpectedDelegateLocations () {
        return m_delegate.getExpectedDelegateLocations ();
    }

    @Override
    public boolean allDelegatesOK () {
        return m_delegate.allDelegatesOK ();
    }
}
