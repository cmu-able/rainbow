package org.sa.rainbow.core.ports.eseb.rpc;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.globals.ExitState;
import org.sa.rainbow.core.ports.eseb.ESEBProvider;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;

import edu.cmu.cs.able.eseb.participant.ParticipantException;

public class ESEBMasterCommandRequirerPort extends AbstractESEBDisposableRPCPort
implements IESEBMasterCommandPortRemoteInterface {

    static Logger                                 LOGGER = Logger.getLogger (ESEBMasterCommandRequirerPort.class);
    private IESEBMasterCommandPortRemoteInterface m_stub;

    public ESEBMasterCommandRequirerPort () throws IOException, ParticipantException {
        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (),
                IESEBMasterCommandPortRemoteInterface.class.getSimpleName ());
        m_stub = getConnectionRole ().createRemoteStub (IESEBMasterCommandPortRemoteInterface.class,
                "gui" + IESEBMasterCommandPortRemoteInterface.class.getSimpleName ());
    }

    @Override
    public void startProbes () {
        m_stub.startProbes ();
    }

    @Override
    public void killProbes () {
        m_stub.killProbes ();
    }

    @Override
    public void enableAdaptation (boolean enabled) {
        m_stub.enableAdaptation (enabled);
    }

    @Override
    public Outcome testEffector (String target, String effName, List<String> args) {
        return m_stub.testEffector (target, effName, args);
    }

    @Override
    public void sleep () {
        m_stub.sleep ();
    }

    @Override
    public void terminate (ExitState exitState) {
        m_stub.terminate (exitState);
    }

    @Override
    public void restartDelegates () {
        m_stub.restartDelegates ();
    }

    @Override
    public void sleepDelegates () {
        m_stub.sleepDelegates ();
    }

    @Override
    public void destroyDelegates () {
        m_stub.destroyDelegates ();
    }

    @Override
    public void killDelegate (String ipOfDelegate) {
        m_stub.killDelegate (ipOfDelegate);
    }

    @Override
    public List<String> getExpectedDelegateLocations () {
        return m_stub.getExpectedDelegateLocations ();
    }

    @Override
    public boolean allDelegatesOK () {
        return m_stub.allDelegatesOK ();
    }
}
