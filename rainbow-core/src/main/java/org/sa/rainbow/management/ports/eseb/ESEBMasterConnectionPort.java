package org.sa.rainbow.management.ports.eseb;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.sa.rainbow.RainbowConstants;
import org.sa.rainbow.RainbowMaster;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.management.ports.AbstractMasterConnectionPort;
import org.sa.rainbow.management.ports.IRainbowManagementPort;

public class ESEBMasterConnectionPort extends AbstractMasterConnectionPort {
    static Logger         LOGGER = Logger.getLogger (ESEBMasterConnectionPort.class);

    private ESEBConnector m_connectionRole;

    public ESEBMasterConnectionPort (RainbowMaster master) throws IOException {
        super (master);
        String port = Rainbow.properties ().getProperty (RainbowConstants.PROPKEY_MASTER_CONNECTION_PORT);
        if (port == null) {
            port = Rainbow.properties ().getProperty (RainbowConstants.PROPKEY_MASTER_LOCATION_PORT);
            if (port == null) {
                port = "1234";
            }
        }
        short p = Short.valueOf (port);
        m_connectionRole = new ESEBConnector (p);
        m_connectionRole.addListener (new ESEBConnector.IESEBListener () {

            @Override
            public void receive (Map<String, String> msg) {
                String type = msg.get (ESEBConstants.MSG_TYPE_KEY);
                switch (type) {
                case ESEBConstants.MSG_TYPE_CONNECT_DELEGATE: {
                    if (msg.get (ESEBConstants.TARGET) == null) {
                        String delegateId = msg.get (ESEBConstants.MSG_DELEGATE_ID_KEY);
                        Properties connectionProperties = m_connectionRole.decodeProperties (msg);
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
                        Map<String, String> reply = new HashMap<> ();
                        reply.put (ESEBConstants.MSG_REPLY_KEY, msg.get (ESEBConstants.MSG_REPLY_KEY));
                        reply.put (ESEBConstants.MSG_CONNECT_REPLY, replyMsg);
                        reply.put (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_REPLY);
                        m_connectionRole.publish (reply);
                    }
                }
                break;
                case ESEBConstants.MSG_TYPE_DISCONNECT_DELEGATE: {
                    String delegateId = msg.get (ESEBConstants.MSG_DELEGATE_ID_KEY);
                    m_master.disconnectDelegate (delegateId);
                }
                break;
                }
            }
        });
    }

    @Override
    public void disconnectDelegate (String delegateId) {
        Map<String, String> msg = new HashMap<> ();
        msg.put (ESEBConstants.MSG_TYPE_KEY, ESEBConstants.MSG_TYPE_DISCONNECT_DELEGATE);
        msg.put (ESEBConstants.MSG_DELEGATE_ID_KEY, delegateId);
        msg.put (ESEBConstants.TARGET, delegateId);
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
