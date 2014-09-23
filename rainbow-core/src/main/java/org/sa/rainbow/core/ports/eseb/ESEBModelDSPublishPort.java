package org.sa.rainbow.core.ports.eseb;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.sa.rainbow.core.Identifiable;
import org.sa.rainbow.core.error.RainbowConnectionException;
import org.sa.rainbow.core.error.RainbowException;
import org.sa.rainbow.core.event.IRainbowMessage;
import org.sa.rainbow.core.models.commands.IRainbowOperation;
import org.sa.rainbow.core.ports.IModelDSBusPublisherPort;
import org.sa.rainbow.core.ports.IModelDSBusSubscriberPort;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.ChannelT;
import org.sa.rainbow.core.ports.eseb.ESEBConnector.IESEBListener;

public class ESEBModelDSPublishPort extends AbstractESEBDisposablePort implements IModelDSBusPublisherPort,
IModelDSBusSubscriberPort {

    private Identifiable  m_publisher;
    private Set<IModelDSBusPublisherPort> m_callbacks = new HashSet<> ();

    public ESEBModelDSPublishPort (Identifiable publisher) throws IOException {
        super (ESEBProvider.getESEBClientHost (), ESEBProvider.getESEBClientPort (), ChannelT.MODEL_DS);
        m_publisher = publisher;
        getConnectionRole().addListener (new IESEBListener () {

            @Override
            public void receive (RainbowESEBMessage msg) {
                if (m_callbacks == null || m_callbacks.isEmpty ()) return; // no one interested
                String msgType = (String )msg.getProperty (ESEBConstants.MSG_TYPE_KEY);
                String channel = (String )msg.getProperty (ESEBConstants.MSG_CHANNEL_KEY);
                if (ChannelT.MODEL_DS.name ().equals (channel)) {
                    if (ESEBConstants.MSG_TYPE_UPDATE_MODEL.equals (msgType)) {
                        IRainbowOperation cmd = ESEBCommandHelper.msgToCommand (msg);
                        for (IModelDSBusPublisherPort callback : m_callbacks) {
                            OperationResult result = callback.publishOperation (cmd);
                            if (result != null) {
                                try {
                                    RainbowESEBMessage reply = getConnectionRole ().createMessage ();
                                    reply.setProperty (ESEBConstants.MSG_REPLY_KEY,
                                            msg.getProperty (ESEBConstants.MSG_REPLY_KEY));
                                    reply.setProperty (ESEBConstants.MSG_UPDATE_MODEL_REPLY, result);
                                    reply.setProperty (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_REPLY);
                                    msg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, m_publisher.id ());
                                    getConnectionRole ().publish (reply);
                                }
                                catch (RainbowException e) {
                                    // TODO: What to do?
                                    e.printStackTrace ();
                                }
                            }
                        }
                    }
                    else {
                        for (IModelDSBusPublisherPort callback : m_callbacks) {
                            callback.publishMessage (msg);
                        }
                    }
                }
            }
        });
    }

    @Override
    public OperationResult publishOperation (IRainbowOperation cmd) {
        // Doing this the old way because more than one listener may be interested in this message. The first one to reply, wins.
        RainbowESEBMessage msg = getConnectionRole().createMessage ();
        msg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, m_publisher.id ());
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_UPDATE_MODEL);
        ESEBCommandHelper.command2Message (cmd, msg);

        final OperationResult result = new OperationResult ();
        result.result = Result.FAILURE;
        result.reply = "Operation timed out";

        try {
            getConnectionRole().blockingSendAndReceive (msg, new IESEBListener () {

                @Override
                public void receive (RainbowESEBMessage msg) {
                    OperationResult reply = (OperationResult )msg.getProperty (ESEBConstants.MSG_UPDATE_MODEL_REPLY);
                    result.result = reply.result;
                    result.reply = reply.reply;
                }
            }, 10000);
        }
        catch (RainbowConnectionException e) {
            result.reply = e.getMessage ();
        }
        return result;
    }

    @Override
    public void subscribeToOperations (IModelDSBusPublisherPort callback) {
        m_callbacks.add (callback);
    }

    @Override
    public void unsubscribeToOperations (IModelDSBusPublisherPort callback) {
        m_callbacks.remove (callback);
    }

    @Override
    public void publishMessage (IRainbowMessage msg) {
        if (msg.getProperty (ESEBConstants.MSG_CHANNEL_KEY).equals (ChannelT.MODEL_DS.name ())
                && msg instanceof RainbowESEBMessage) {
            getConnectionRole ().publish ((RainbowESEBMessage )msg);
        }
    }

    @Override
    public IRainbowMessage createMessage () {
        return getConnectionRole ().createMessage ();
    }

}
