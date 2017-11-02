/*
 * The MIT License
 *
 * Copyright 2014 CMU ABLE Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
    private final Set<IModelDSBusPublisherPort> m_callbacks = new HashSet<> ();

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
                                    reply.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, m_publisher.id ());
//                                    System.out.println ("DS Publishing " + reply.toString ());
                                    getConnectionRole ().publish (reply);
                                }
                                catch (RainbowException e) {
                                    // TODO: What to do?
                                    e.printStackTrace ();
                                }
                            }
                        }
                    }
//                    else {
//                        for (IModelDSBusPublisherPort callback : m_callbacks) {
//                            callback.publishMessage (msg);
//                        }
//                    }
                }
            }
        });
    }


    @Override
    public OperationResult publishOperation (IRainbowOperation cmd) {
//        synchronized (this) {
            // Doing this the old way because more than one listener may be interested in this message. The first one to reply, wins.

            RainbowESEBMessage msg = getConnectionRole ().createMessage ();
            msg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, m_publisher.id ());
            msg.setProperty (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_UPDATE_MODEL);
            ESEBCommandHelper.command2Message (cmd, msg);

            final OperationResult result = new OperationResult ();
            result.result = Result.FAILURE;
            result.reply = "Operation timed out";
            try {
                getConnectionRole ().blockingSendAndReceive (msg, new IESEBListener () {

                    @Override
                    public void receive (RainbowESEBMessage msg) {
                        OperationResult reply = (OperationResult) msg.getProperty (ESEBConstants.MSG_UPDATE_MODEL_REPLY);
                        result.result = reply.result;
                        result.reply = reply.reply;
                    }
                }, 1000000);
            } catch (RainbowConnectionException e) {
                result.reply = e.getMessage ();
            }
//            System.out.println ("======> publishOperation[RECEIVE]: " + cmd.toString () + " = " + result.result);
            return result;
//        }
    }

    @Override
    public void subscribeToOperations (IModelDSBusPublisherPort callback) {
        m_callbacks.add (callback);
    }

    @Override
    public void unsubscribeToOperations (IModelDSBusPublisherPort callback) {
        m_callbacks.remove (callback);
    }

//    @Override
//    public void publishMessage (IRainbowMessage msg) {
//        if (msg.getProperty (ESEBConstants.MSG_CHANNEL_KEY).equals (ChannelT.MODEL_DS.name ())
//                && msg instanceof RainbowESEBMessage) {
//            getConnectionRole ().publish ((RainbowESEBMessage )msg);
//        }
//    }


    @Override
    public IRainbowMessage createMessage () {
        return getConnectionRole ().createMessage ();
    }

}
