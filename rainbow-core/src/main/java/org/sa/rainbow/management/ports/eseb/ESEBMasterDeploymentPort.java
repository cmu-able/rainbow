package org.sa.rainbow.management.ports.eseb;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.sa.rainbow.RainbowMaster;
import org.sa.rainbow.core.Rainbow;
import org.sa.rainbow.management.ports.AbstractMasterDeploymentPort;
import org.sa.rainbow.management.ports.eseb.ESEBConnector.IESEBListener;

public class ESEBMasterDeploymentPort extends AbstractMasterDeploymentPort implements ESEBDeploymentPortConstants {
    static Logger         LOGGER = Logger.getLogger (ESEBMasterDeploymentPort.class);

    private ESEBConnector m_role;


    public ESEBMasterDeploymentPort (RainbowMaster master, String delegateID, Properties connectionProperties) throws IOException {
        super (master, delegateID);
        String delegateHost = connectionProperties.getProperty (ESEBConstants.PROPKEY_ESEB_DELEGATE_DEPLOYMENT_HOST,
                "localhost");
        String delegatePort = connectionProperties.getProperty (ESEBConstants.PROPKEY_ESEB_DELEGATE_DEPLOYMENT_PORT,
                Rainbow.properties ().getProperty (Rainbow.PROPKEY_DEPLOYMENT_LOCATION, "1234"));
        Short port = Short.valueOf (delegatePort);
        m_role = new ESEBConnector(delegateHost, port);
        m_role.addListener (new IESEBListener () {

            @Override
            public void receive (Map<String, String> msg) {
                String msgType = msg.get (ESEBConstants.MSG_TYPE_KEY);
                switch (msgType) {
                case REQUEST_CONFIG_INFORMATION: {
                    if (msg.get (ESEBConstants.MSG_DELEGATE_ID_KEY).equals (getDelegateId())) {
                        requestConfigurationInformation ();
                    }

                }
                break;
                case RECEIVE_HEARTBEAT: {
                    if (msg.get (ESEBConstants.MSG_DELEGATE_ID_KEY).equals (getDelegateId ())) {
                        receiveHeartbeat ();
                    }
                }
                }
            }
        });
    }

    @Override
    public void sendConfigurationInformation (Properties configuration) {
        Map<String, String> msg = m_role.encodePropertiesAsMap (configuration);
        // No response is expected from the client, so don't do any waiting, just send
        msg.put (ESEBConstants.MSG_TYPE_KEY, SEND_CONFIGURATION_INFORMATION);
        msg.put (ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId ());
        m_role.publish (msg);
    }


    class BooleanReply implements IESEBListener {
        boolean m_reply = false;

        @Override
        public void receive (Map<String, String> msg) {
            m_reply = Boolean.valueOf (msg.get (ESEBConstants.MSG_REPLY_VALUE));
            synchronized (this) {
                this.notifyAll ();
            }
        }
    }

    @Override
    public boolean startDelegate () throws IllegalStateException {
        Map<String, String> msg = new HashMap<String, String> ();
        msg.put (ESEBConstants.MSG_TYPE_KEY, START_DELEGATE);
        msg.put (ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId ());

        BooleanReply reply = new BooleanReply ();
        m_role.sendAndReceive (msg, reply);

        synchronized (reply) {
            try {
                reply.wait (10000);
            }
            catch (InterruptedException e) {
            }
        }

        return reply.m_reply;
    }

    @Override
    public boolean pauseDelegate () throws IllegalStateException {
        Map<String, String> msg = new HashMap<String, String> ();
        msg.put (ESEBConstants.MSG_TYPE_KEY, PAUSE_DELEGATE);
        msg.put (ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId ());

        BooleanReply reply = new BooleanReply ();
        m_role.sendAndReceive (msg, reply);

        synchronized (reply) {
            try {
                reply.wait (10000);
            }
            catch (InterruptedException e) {
            }
        }

        return reply.m_reply;
    }

    @Override
    public boolean terminateDelegate () throws IllegalStateException {
        Map<String, String> msg = new HashMap<String, String> ();
        msg.put (ESEBConstants.MSG_TYPE_KEY, TERMINATE_DELEGATE);
        msg.put (ESEBConstants.MSG_DELEGATE_ID_KEY, getDelegateId ());

        BooleanReply reply = new BooleanReply ();
        m_role.sendAndReceive (msg, reply);

        synchronized (reply) {
            try {
                reply.wait (10000);
            }
            catch (InterruptedException e) {
            }
        }

        return reply.m_reply;
    }

    @Override
    public void dispose () {
        try {
            m_role.close ();
        }
        catch (IOException e) {
            LOGGER.warn (MessageFormat.format ("Could not close the deployment port on the master for {0}",
                    getDelegateId ()));
        }
    }

}
