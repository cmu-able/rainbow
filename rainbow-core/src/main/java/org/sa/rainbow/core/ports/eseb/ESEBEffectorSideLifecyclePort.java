package org.sa.rainbow.core.ports.eseb;

import java.io.IOException;
import java.util.List;

import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.ports.IEffectorLifecycleBusPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;
import org.sa.rainbow.translator.effectors.IEffectorProtocol;

public class ESEBEffectorSideLifecyclePort extends AbstractESEBDisposablePort implements IEffectorLifecycleBusPort {


    public ESEBEffectorSideLifecyclePort () throws IOException {
        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (),
                ChannelT.HEALTH);
    }

    @Override
    public void reportCreated (IEffectorIdentifier effector) {
        RainbowESEBMessage msg = getConnectionRole().createMessage ();
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IEffectorProtocol.EFFECTOR_CREATED);
        setCommonEffectorProperties (effector, msg);
        getConnectionRole().publish (msg);
    }

    void setCommonEffectorProperties (IEffectorIdentifier effector, RainbowESEBMessage msg) {
        msg.setProperty (IEffectorProtocol.ID, effector.id ());
        msg.setProperty (IEffectorProtocol.SERVICE, effector.service ());
        msg.setProperty (IEffectorProtocol.KIND, effector.kind ().name ());
        msg.setProperty (IEffectorProtocol.LOCATION, Rainbow.getProperty (RainbowConstants.PROPKEY_DEPLOYMENT_LOCATION));
    }

    @Override
    public void reportDeleted (IEffectorIdentifier effector) {
        RainbowESEBMessage msg = getConnectionRole().createMessage ();
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IEffectorProtocol.EFFECTOR_DELETED);
        setCommonEffectorProperties (effector, msg);
        getConnectionRole().publish (msg);
    }

    @Override
    public void reportExecuted (IEffectorIdentifier effector, Outcome outcome, List<String> args) {
        RainbowESEBMessage msg = getConnectionRole().createMessage ();
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, IEffectorProtocol.EFFECTOR_EXECUTED);
        setCommonEffectorProperties (effector, msg);
        msg.setProperty (IEffectorProtocol.OUTCOME, outcome.name ());
        msg.setProperty (IEffectorProtocol.ARGUMENT + IEffectorProtocol.SIZE, args.size ());
        for (int i = 0; i < args.size (); i++) {
            msg.setProperty (IEffectorProtocol.ARGUMENT + i, args.get (i));
        }
        getConnectionRole().publish (msg);
    }


}
