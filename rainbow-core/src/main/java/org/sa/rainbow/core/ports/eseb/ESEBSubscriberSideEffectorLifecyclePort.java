package org.sa.rainbow.core.ports.eseb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.ports.IEffectorLifecycleBusPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.IESEBListener;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;
import org.sa.rainbow.translator.effectors.IEffectorProtocol;

public class ESEBSubscriberSideEffectorLifecyclePort extends AbstractESEBDisposablePort implements
        IEffectorLifecycleBusPort {

    class MessageEffectorIdentifier implements IEffectorIdentifier {

        private IRainbowMessage m_msg;

        public MessageEffectorIdentifier (IRainbowMessage msg) {
            m_msg = msg;
        }

        @Override
        public String id () {
            return (String )m_msg.getProperty (IEffectorProtocol.ID);
        }

        @Override
        public String service () {
            return (String )m_msg.getProperty (IEffectorProtocol.SERVICE);

        }

        @Override
        public Kind kind () {
            try {
                return Kind.valueOf ((String )m_msg.getProperty (IEffectorProtocol.KIND));
            }
            catch (Exception e) {
                return Kind.NULL;
            }
        }

    }

    private IEffectorLifecycleBusPort m_delegate;

    public ESEBSubscriberSideEffectorLifecyclePort (IEffectorLifecycleBusPort client) throws IOException {
        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (), ChannelT.HEALTH);
        m_delegate = client;
        getConnectionRole().addListener (new IESEBListener () {

            @Override
            public void receive (RainbowESEBMessage msg) {
                String type = (String )msg.getProperty (ESEBConstants.MSG_TYPE_KEY);
                MessageEffectorIdentifier mei;
                switch (type) {
                case IEffectorProtocol.EFFECTOR_CREATED:
                    mei = new MessageEffectorIdentifier (msg);
                    reportCreated (mei);
                    break;
                case IEffectorProtocol.EFFECTOR_DELETED:
                    mei = new MessageEffectorIdentifier (msg);
                    reportDeleted (mei);
                    break;
                case IEffectorProtocol.EFFECTOR_EXECUTED:
                    mei = new MessageEffectorIdentifier (msg);
                    int size = (int )msg.getProperty (IEffectorProtocol.ARGUMENT + IEffectorProtocol.SIZE);
                    List<String> args = new ArrayList<> (size);
                    for (int i = 0; i < size; i++) {
                        args.add ((String )msg.getProperty (IEffectorProtocol.ARGUMENT + i));
                    }

                    Outcome outcome = Outcome.UNKNOWN;
                    try {
                        outcome = Outcome.valueOf ((String )msg.getProperty (IEffectorProtocol.OUTCOME));
                    }
                    catch (Exception e) {
                    }
                    reportExecuted (mei, outcome, args);
                }
            }
        });
    }

    @Override
    public void reportCreated (IEffectorIdentifier effector) {
        m_delegate.reportCreated (effector);
    }

    @Override
    public void reportDeleted (IEffectorIdentifier effector) {
        m_delegate.reportDeleted (effector);
    }

    @Override
    public void reportExecuted (IEffectorIdentifier effector, Outcome outcome, List<String> args) {
        m_delegate.reportExecuted (effector, outcome, args);
    }

}
