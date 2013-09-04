package org.sa.rainbow.core.management.ports.eseb;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.sa.rainbow.core.RainbowConstants;
import org.sa.rainbow.core.RainbowMaster;
import org.sa.rainbow.core.management.ports.AbstractMasterConnectionPort;
import org.sa.rainbow.core.management.ports.IRainbowManagementPort;
import org.sa.rainbow.core.management.ports.eseb.ESEBConnector.ChannelT;

public class ESEBMasterConnectionPort extends AbstractMasterConnectionPort {
    static Logger         LOGGER = Logger.getLogger (ESEBMasterConnectionPort.class);

    private ESEBConnector m_connectionRole;

    public ESEBMasterConnectionPort (RainbowMaster master) throws IOException {
        super (master);
        m_connectionRole = new ESEBConnector (
                ESEBProvider.getESEBClientPort (RainbowConstants.PROPKEY_MASTER_CONNECTION_PORT), ChannelT.HEALTH);
        m_connectionRole.addListener (new ESEBConnector.IESEBListener () {

            @Override
            public void receive (RainbowESEBMessage msg) {
                String type = (String )msg.getProperty (ESEBConstants.MSG_TYPE_KEY);
                switch (type) {
                case ESEBConstants.MSG_TYPE_CONNECT_DELEGATE: {
//                    if (msg.hasProperty (ESEBConstants.TARGET)) {
                    String delegateId = (String )msg.getProperty (ESEBConstants.MSG_DELEGATE_ID_KEY);
                    Properties connectionProperties = msg.pulloutProperties ();
                    String replyMsg = ESEBConstants.MSG_REPLY_OK;
                    try {
                        IRainbowManagementPort port = connectDelegate (delegateId, connectionProperties);
                        if (port == null) {
                            replyMsg = "Could not create a deployment port on the master.";
                        }
                    }
                    catch (Throwable t) {
                        replyMsg = MessageFormat.format ("Failed to connect with the following exception: {0}",
                                t.getMessage ());
                    }
                    RainbowESEBMessage reply = m_connectionRole.createMessage ();
                    reply.setProperty (ESEBConstants.MSG_REPLY_KEY,
                            (String )msg.getProperty (ESEBConstants.MSG_REPLY_KEY));
                    reply.setProperty (ESEBConstants.MSG_CONNECT_REPLY, replyMsg);
                    reply.setProperty (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_REPLY);
                    m_connectionRole.publish (reply);
//                    }
                }
                break;
                case ESEBConstants.MSG_TYPE_DISCONNECT_DELEGATE: {
                    String delegateId = (String )msg.getProperty (ESEBConstants.MSG_DELEGATE_ID_KEY);
                    m_master.disconnectDelegate (delegateId);
                }
                break;
                }
            }
        });
    }

    @Override
    public void disconnectDelegate (String delegateId) {
        RainbowESEBMessage msg = m_connectionRole.createMessage (/*ChannelT.HEALTH*/);
        msg.setProperty (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_DISCONNECT_DELEGATE);
        msg.setProperty (ESEBConstants.MSG_DELEGATE_ID_KEY, delegateId);
        msg.setProperty (ESEBConstants.TARGET, delegateId);
        m_connectionRole.publish (msg);
    }

    @Override
    public void dispose () {
        try {
            m_connectionRole.close ();
        }
        catch (IOException e) {
            LOGGER.warn ("Could not close down the connection port on the master");
        }
    }

}
