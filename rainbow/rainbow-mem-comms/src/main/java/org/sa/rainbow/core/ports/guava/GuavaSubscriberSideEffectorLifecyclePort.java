package org.sa.rainbow.core.ports.guava;

import java.util.ArrayList;
import java.util.List;

import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.ports.IEffectorLifecycleBusPort;
import org.sa.rainbow.core.ports.eseb.ESEBConstants;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.ChannelT;
import org.sa.rainbow.core.ports.guava.GuavaEventConnector.IGuavaMessageListener;
import org.sa.rainbow.translator.effectors.IEffectorExecutionPort.Outcome;
import org.sa.rainbow.translator.effectors.IEffectorIdentifier;
import org.sa.rainbow.translator.effectors.IEffectorProtocol;

public class GuavaSubscriberSideEffectorLifecyclePort implements IEffectorLifecycleBusPort {
	
    class MessageEffectorIdentifier implements IEffectorIdentifier {

        private final IRainbowMessage m_msg;

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
	private GuavaEventConnector m_eventBus;

	public GuavaSubscriberSideEffectorLifecyclePort(IEffectorLifecycleBusPort delegate) {
		m_delegate = delegate;
		m_eventBus = new GuavaEventConnector(ChannelT.HEALTH);
		m_eventBus.addListener(new IGuavaMessageListener() {
			
			@Override
			public void receive(GuavaRainbowMessage msg) {
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
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reportCreated(IEffectorIdentifier effector) {
		m_delegate.reportCreated(effector);
	}

	@Override
	public void reportDeleted(IEffectorIdentifier effector) {
		m_delegate.reportDeleted(effector);
	}

	@Override
	public void reportExecuted(IEffectorIdentifier effector, Outcome outcome, List<String> args) {
		m_delegate.reportExecuted(effector, outcome, args);
	}

}
